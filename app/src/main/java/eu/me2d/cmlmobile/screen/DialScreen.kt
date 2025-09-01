package eu.me2d.cmlmobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.me2d.cmlmobile.R
import eu.me2d.cmlmobile.state.Command
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import timber.log.Timber

@Composable
fun DialScreen(
    globalStateViewModel: GlobalStateViewModel,
    onSecretCode: (() -> Unit)? = null,
) {
    val registrationTimestamp =
        globalStateViewModel.state.collectAsState().value.registrationTimestamp
    if (registrationTimestamp == null) {
        RegistrationRequiredHint()
        return
    }
    val commands = globalStateViewModel.state.collectAsState().value.commands.sortedBy { it.number }

    DialScreenContent(
        commands = commands,
        onSecretCode = onSecretCode,
        onExecuteCommand = { commandNumber ->
            globalStateViewModel.executeCommand(commandNumber)
        }
    )
}

@Composable
fun DialScreenContent(
    commands: List<Command>,
    onSecretCode: (() -> Unit)? = null,
    onExecuteCommand: (Int) -> Unit
) {
    var currentNumber by rememberSaveable { mutableStateOf("") }

    Column {
        Display(currentNumber)
        DialPad { number ->
            if (number == "C") {
                currentNumber = ""
            } else if (number == "\u23CE") {
                // Handle enter press
                if (currentNumber == "1234") {
                    onSecretCode?.invoke()
                    currentNumber = ""
                    Timber.d("Entering log screen")
                } else {
                    val commandNumber = currentNumber.toIntOrNull() ?: -1
                    Timber.i("Going to execute command $currentNumber")
                    currentNumber = ""
                    onExecuteCommand(commandNumber)
                }
            } else {
                currentNumber += number
            }
        }

        // Available Commands List (moved below dial pad)
        if (commands.isNotEmpty()) {
            Text(
                text = stringResource(R.string.available_commands),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                content = {
                    items(commands) { command ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = command.number.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(40.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = command.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            )
        } else {
            Text(
                text = stringResource(R.string.no_commands_available),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun Display(number: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color(0xFF00FF00),
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun DialPad(onPress: (String) -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "\u23CE") // Unicode for Enter symbol
    )
    Column {
        buttons.forEach { row ->
            Row {
                row.forEach { label ->
                    Button(
                        onClick = { onPress(label) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialScreenPreview() {
    // Preview without ViewModel - show the UI components directly
    val mockCommands = listOf(
        Command(1, "Turn on lights"),
        Command(2, "Turn off lights"),
        Command(3, "Lock doors"),
        Command(4, "Unlock doors"),
        Command(5, "Start engine")
    )

    DialScreenContent(
        commands = mockCommands,
        onSecretCode = { /* Handle secret code press */ },
        onExecuteCommand = { commandNumber -> Timber.i("Executing command $commandNumber") }
    )
}