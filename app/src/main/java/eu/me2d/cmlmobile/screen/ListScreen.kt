package eu.me2d.cmlmobile.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.me2d.cmlmobile.state.Command
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import eu.me2d.cmlmobile.screen.RegistrationRequiredHint

@Composable
fun ListScreen(globalStateViewModel: GlobalStateViewModel) {
    val registrationTimestamp =
        globalStateViewModel.state.collectAsState().value.registrationTimestamp
    if (registrationTimestamp == null) {
        RegistrationRequiredHint()
        return
    }
    val sortedCommands = globalStateViewModel.sortedCommands.collectAsState().value
    ListScreenContent(
        commands = sortedCommands,
        onClick = { number -> globalStateViewModel.executeCommand(number) }
    )
}

@Composable
fun ListScreenContent(commands: List<Command>, onClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(commands) { command ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick(command.number) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = command.number.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = command.name,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListScreenContentPreview() {
    val dummyCommands = listOf(
        Command(1, "Power On"),
        Command(2, "Reset Device"),
        Command(42, "Self-Destruct"),
        Command(1001, "Some longer test like Diagnostics Start"),
    )
    ListScreenContent(commands = dummyCommands, onClick = {})
}
