package eu.me2d.cmlmobile.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.me2d.cmlmobile.CmlMobileApp
import eu.me2d.cmlmobile.state.GlobalState
import eu.me2d.cmlmobile.state.StateSettings
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.PatternSyntaxException
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId

@Composable
fun SettingsScreen(viewModel: GlobalStateViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state.collectAsState().value

    fun toGlobalState(settings: StateSettings) = GlobalState(
        settings = settings,
    )

    SettingsScreenContent(
        settings = state.settings,
        isRegistered = state.registrationTimestamp != null,
        registrationTimestamp = state.registrationTimestamp,
        onSaveSettings = { settings ->
            viewModel.saveState(toGlobalState(settings))
            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
        },
        onRegister = { settings ->
            coroutineScope.launch {
                CmlMobileApp.appModule.apiService.register(
                    settings = settings,
                    globalStateViewModel = viewModel
                )
            }
        },
        onGetCommands = {
            // TODO: Implement getCommands method in viewModel
            // viewModel.getCommands()
        }
    )
}

@Composable
fun SettingsScreenContent(
    settings: StateSettings,
    isRegistered: Boolean,
    registrationTimestamp: Instant?,
    onSaveSettings: (StateSettings) -> Unit,
    onRegister: (StateSettings) -> Unit,
    onGetCommands: () -> Unit
) {
    var apiUrl by remember { mutableStateOf(settings.apiUrl) }
    var myId by remember { mutableStateOf(settings.myId) }
    var wifiPattern by remember { mutableStateOf(settings.wifiPattern) }
    var wifiUrl by remember { mutableStateOf(settings.wifiUrl) }

    fun getCurrentSettings() = StateSettings(
        apiUrl = apiUrl,
        myId = myId,
        wifiPattern = wifiPattern,
        wifiUrl = wifiUrl
    )

    // Function to validate if a string is a valid URL
    fun isValidUrl(url: String): Boolean {
        return try {
            if (url.isBlank()) return false
            val urlPattern = Regex("^https?://[^\\s/$.?#].[^\\s]*$", RegexOption.IGNORE_CASE)
            urlPattern.matches(url.trim())
        } catch (e: PatternSyntaxException) {
            Timber.e(e, "URL validation regex pattern error")
            false
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = apiUrl,
            onValueChange = { apiUrl = it },
            label = { Text("API url") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = myId,
            onValueChange = { myId = it },
            label = { Text("My id") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = wifiPattern,
            onValueChange = { wifiPattern = it },
            label = { Text("Wifi name pattern") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        OutlinedTextField(
            value = wifiUrl,
            onValueChange = { wifiUrl = it },
            label = { Text("Url for wifi") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        if (isRegistered) {
            val formattedTimestamp = registrationTimestamp?.let {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(it)
            } ?: "Unknown"
            Text(text = "Registered on $formattedTimestamp")
        } else {
            Text(text = "Not yet registered")
        }

        Spacer(modifier = Modifier.padding(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    onSaveSettings(getCurrentSettings())
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text("Save")
            }
            Button(
                onClick = {
                    val currentSettings = getCurrentSettings()
                    onSaveSettings(currentSettings)
                    onRegister(currentSettings)
                },
                modifier = Modifier.weight(1f),
                enabled = isValidUrl(apiUrl)
            ) {
                Text("Register")
            }
        }
        Button(
            onClick = onGetCommands,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = isRegistered
        ) {
            Text("Get Commands")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreenContent(
        settings = StateSettings(
            apiUrl = "https://api.example.com",
            myId = "user123",
            wifiPattern = "MyWifi.*",
            wifiUrl = "http://192.168.1.100"
        ),
        isRegistered = true,
        registrationTimestamp = Instant.ofEpochSecond(1643723900),
        onSaveSettings = { /* Preview - do nothing */ },
        onRegister = { /* Preview - do nothing */ },
        onGetCommands = { /* Preview - do nothing */ }
    )
}
