package eu.me2d.cmlmobile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import timber.log.Timber
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import eu.me2d.cmlmobile.screen.RegistrationRequiredHint

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
    var currentNumber by remember { mutableStateOf("") }
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
                    globalStateViewModel.executeCommand(commandNumber)
                }
            } else {
                currentNumber += number
            }
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
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
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
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
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
    DialScreen(GlobalStateViewModel())
}