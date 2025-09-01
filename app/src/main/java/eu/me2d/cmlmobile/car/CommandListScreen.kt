package eu.me2d.cmlmobile.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import eu.me2d.cmlmobile.CmlMobileApp
import eu.me2d.cmlmobile.R
import eu.me2d.cmlmobile.state.Command
import eu.me2d.cmlmobile.state.GlobalState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class CommandListScreen(carContext: CarContext) : Screen(carContext) {

    private var pendingCommand: Command? = null
    private var showingConfirmation = false

    init {
        Timber.d("CommandListScreen: Initializing...")
    }

    override fun onGetTemplate(): Template {
        Timber.d("CommandListScreen: onGetTemplate() called")
        return if (showingConfirmation && pendingCommand != null) {
            createConfirmationTemplate(pendingCommand!!)
        } else {
            try {
                createCommandList()
            } catch (e: Exception) {
                Timber.e(e, "CommandListScreen: Error in onGetTemplate, showing fallback")
                createSimpleFallbackTemplate()
            }
        }
    }

    private fun createCommandList(): Template {
        val appModule = CmlMobileApp.appModule

        // Load the current state from storage
        val globalState = appModule.storageService.loadStateWithMigration(
            appModule.migrationService
        )
        Timber.d("CommandListScreen: Loaded state, registered=${globalState.registrationTimestamp != null}")

        // Check if user is registered
        return if (globalState.registrationTimestamp == null) {
            createRegistrationRequiredTemplate()
        } else {
            // Get sorted commands using the same logic as ListScreen
            val sortedCommands = appModule.historyService
                .sortedCommands(globalState.commands, globalState.history)
            Timber.d("CommandListScreen: Found ${sortedCommands.size} commands")
            createCommandListTemplate(sortedCommands)
        }
    }

    private fun createRegistrationRequiredTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle(carContext.getString(R.string.car_registration_required_title))
                            .addText(carContext.getString(R.string.car_registration_required_text))
                            .build()
                    )
                    .build()
            )
            .setHeaderAction(Action.APP_ICON)
            .setTitle(carContext.getString(R.string.car_app_title))
            .build()
    }

    private fun createCommandListTemplate(commands: List<Command>): Template {
        val listBuilder = ItemList.Builder()

        if (commands.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.car_no_commands_title))
                    .addText(carContext.getString(R.string.car_no_commands_text))
                    .build()
            )
        } else {
            commands.forEach { command ->
                listBuilder.addItem(createCommandRow(command))
            }
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .setTitle(carContext.getString(R.string.car_commands_title))
            .build()
    }

    private fun createCommandRow(command: Command): Row {
        val title = "${command.number} - ${command.name}"

        return Row.Builder()
            .setTitle(title)
            .setOnClickListener {
                // Show confirmation dialog
                pendingCommand = command
                showingConfirmation = true
                invalidate()
            }
            .build()
    }

    private fun createConfirmationTemplate(command: Command): Template {
        val message =
            carContext.getString(R.string.car_execute_command_format, command.number, command.name)

        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle(carContext.getString(R.string.car_confirm_command_title))
                            .addText(message)
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle(carContext.getString(R.string.car_execute_button))
                            .setOnClickListener {
                                executeCommand(command.number)
                                pendingCommand = null
                                showingConfirmation = false
                                invalidate()
                            }
                            .build()
                    )
                    .addItem(
                        Row.Builder()
                            .setTitle(carContext.getString(R.string.car_cancel_button))
                            .setOnClickListener {
                                pendingCommand = null
                                showingConfirmation = false
                                invalidate()
                            }
                            .build()
                    )
                    .build()
            )
            .setTitle(carContext.getString(R.string.car_confirm_title))
            .build()
    }

    private fun createSimpleFallbackTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle(carContext.getString(R.string.car_app_title))
                            .addText(carContext.getString(R.string.car_fallback_text))
                            .build()
                    )
                    .build()
            )
            .setHeaderAction(Action.APP_ICON)
            .setTitle(carContext.getString(R.string.car_app_title))
            .build()
    }

    private fun executeCommand(commandNumber: Int) {
        Timber.d("Executing command: $commandNumber")
        try {
            // Execute command in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val currentState = CmlMobileApp.appModule.storageService.loadStateWithMigration(
                        CmlMobileApp.appModule.migrationService
                    )

                    CmlMobileApp.appModule.apiService.executeCommand(
                        settings = currentState.settings,
                        privateKeyEncoded = currentState.privateKeyEncoded,
                        commandNumber = commandNumber
                    )

                    // Update command execution history
                    val updatedState = updateCommandHistory(currentState, commandNumber)
                    CmlMobileApp.appModule.storageService.saveState(updatedState)

                    Timber.d("Command $commandNumber executed successfully")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to execute command: $commandNumber")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute command: $commandNumber")
        }
    }

    private fun updateCommandHistory(state: GlobalState, commandNumber: Int): GlobalState {
        val currentDate = java.time.LocalDate.now().toString()
        val updatedHistory = state.history.toMutableMap()
        val dateHistory = updatedHistory.getOrPut(currentDate) { mutableMapOf() }
        dateHistory[commandNumber] = dateHistory.getOrDefault(commandNumber, 0) + 1

        return state.copy(history = updatedHistory)
    }
}