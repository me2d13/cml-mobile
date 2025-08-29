package eu.me2d.cmlmobile.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.me2d.cmlmobile.LogMemoryStore
import eu.me2d.cmlmobile.util.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogPanel(logs: List<LogEntry>) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val timeWidth: Dp = 110.dp
    val levelWidth: Dp = 60.dp
    Column(modifier = Modifier
        .verticalScroll(rememberScrollState())
        .padding(8.dp)) {
        logs.forEach { log ->
            val timeString = dateFormat.format(Date(log.timestamp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = "$timeString",
                    modifier = Modifier.width(timeWidth),
                    softWrap = false
                )
                Text(
                    text = log.level,
                    modifier = Modifier.width(levelWidth),
                    softWrap = false
                )
                Text(
                    text = log.message,
                    // No explicit width -- take up the rest
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun LogScreen() {
    val logs = LogMemoryStore.instance.getLogs().asReversed() // Most recent first
    LogPanel(logs)
}

@Preview(showBackground = true)
@Composable
fun LogPanelPreview() {
    val baseTime = System.currentTimeMillis()
    val previewLogs = listOf(
        LogEntry(baseTime, "INFO", "Application started"),
        LogEntry(baseTime + 1000, "DEBUG", "Debugging feature X"),
        LogEntry(baseTime + 2000, "WARN", "Something possibly went wrong"),
        LogEntry(baseTime + 3000, "ERROR", "An error occurred! Stacktrace below..."),
    )
    LogPanel(previewLogs)
}
