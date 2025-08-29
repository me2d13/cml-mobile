package eu.me2d.cmlmobile.state

import androidx.lifecycle.ViewModel
import eu.me2d.cmlmobile.CmlMobileApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

class GlobalStateViewModel : ViewModel() {
    private val _state = MutableStateFlow(CmlMobileApp.appModule.storageService.loadState())
    val state: StateFlow<GlobalState> = _state

    fun saveState(newState: GlobalState) {
        CmlMobileApp.appModule.storageService.saveState(newState)
        _state.value = newState
    }

    fun loadState() {
        _state.value = CmlMobileApp.appModule.storageService.loadState()
    }

    fun executeCommand(commandNumber: Int) {
        setApiCallInProgress("executeCommand")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentState = _state.value
                CmlMobileApp.appModule.apiService.executeCommand(
                    settings = currentState.settings,
                    privateKeyEncoded = currentState.privateKeyEncoded,
                    commandNumber = commandNumber
                )

                // Update command execution history
                val currentDate = LocalDate.now().toString()
                val updatedHistory = currentState.history.toMutableMap()
                val dateHistory = updatedHistory.getOrPut(currentDate) { mutableMapOf() }
                dateHistory[commandNumber] = dateHistory.getOrDefault(commandNumber, 0) + 1

                val newState = currentState.copy(history = updatedHistory)
                saveState(newState)
                setApiCallSuccess("executeCommand", "Command $commandNumber executed successfully")
            } catch (e: Exception) {
                setApiCallError("executeCommand", e.message ?: "Unknown error")
            }
        }
    }

    val sortedCommands: StateFlow<List<Command>> = state
        .map { CmlMobileApp.appModule.historyService.sortedCommands(it.commands, it.history) }
        .stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.Eagerly,
            emptyList()
        )

    fun setCurrentPage(pageIdx: Int) {
        val current = _state.value
        val newState = current.copy(currentPage = pageIdx)
        saveState(newState)
    }

    // API State management methods
    fun setApiCallInProgress(callType: String) {
        val current = _state.value
        val newApiState = ApiState(
            status = ApiCallStatus.IN_PROGRESS,
            statusMessage = "Calling $callType...",
            lastCallType = callType
        )
        val newState = current.copy(apiState = newApiState)
        saveState(newState)
    }

    fun setApiCallSuccess(callType: String, message: String = "Success") {
        val current = _state.value
        val newApiState = ApiState(
            status = ApiCallStatus.SUCCESS,
            statusMessage = message,
            lastCallType = callType
        )
        val newState = current.copy(apiState = newApiState)
        saveState(newState)
    }

    fun setApiCallError(callType: String, errorMessage: String) {
        val current = _state.value
        val newApiState = ApiState(
            status = ApiCallStatus.ERROR,
            statusMessage = errorMessage,
            lastCallType = callType
        )
        val newState = current.copy(apiState = newApiState)
        saveState(newState)
    }

    fun clearApiState() {
        val current = _state.value
        val newApiState = ApiState()
        val newState = current.copy(apiState = newApiState)
        saveState(newState)
    }

    fun onRegistrationComplete(privateKeyString: String) {
        val current = _state.value
        val newState = current.copy(
            privateKeyEncoded = privateKeyString,
            registrationTimestamp = Instant.now()
        )
        saveState(newState)
    }

    fun getCommands() {
        setApiCallInProgress("fetchCommands")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentState = _state.value
                val apiCommands = CmlMobileApp.appModule.apiService.fetchCommands(
                    settings = currentState.settings,
                    privateKeyEncoded = currentState.privateKeyEncoded
                )

                // Convert ApiCommand to Command
                val commands = apiCommands.map { apiCommand ->
                    Command(
                        number = apiCommand.number,
                        name = apiCommand.description
                    )
                }

                val newState = currentState.copy(commands = commands)
                saveState(newState)
                setApiCallSuccess("fetchCommands", "Fetched ${commands.size} commands")
            } catch (e: Exception) {
                setApiCallError("fetchCommands", e.message ?: "Unknown error")
            }
        }
    }
}
