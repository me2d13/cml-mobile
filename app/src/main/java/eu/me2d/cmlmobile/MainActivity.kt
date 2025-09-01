package eu.me2d.cmlmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eu.me2d.cmlmobile.screen.DialScreen
import eu.me2d.cmlmobile.screen.ListScreen
import eu.me2d.cmlmobile.screen.LogScreen
import eu.me2d.cmlmobile.screen.SettingsScreen
import eu.me2d.cmlmobile.state.GlobalStateViewModel
import eu.me2d.cmlmobile.ui.ApiStatusBar
import eu.me2d.cmlmobile.ui.theme.CmlApplicationTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request location permissions on startup for WiFi SSID access
        CmlMobileApp.appModule.networkService.requestLocationPermissionIfNeeded(this)
        enableEdgeToEdge()
        setContent {
            CmlApplicationTheme {
                val globalStateViewModel: GlobalStateViewModel = viewModel()
                val navController = rememberNavController()
                val state = globalStateViewModel.state.collectAsState().value
                val items = listOf(Dial, List, Settings)
                LaunchedEffect(state.currentPage) {
                    val pageObj = items.getOrNull(state.currentPage)
                    if (pageObj != null) {
                        navController.navigate(pageObj.toString())
                    }
                }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination?.route
                            val itemIndices = items.withIndex().associate { it.value to it.index }
                            items.forEach { item ->
                                NavigationBarItem(
                                    icon = {
                                        when (item) {
                                            Dial -> Icon(
                                                Icons.Filled.Home,
                                                contentDescription = stringResource(R.string.content_description_pad)
                                            )

                                            List -> Icon(
                                                Icons.Filled.Menu,
                                                contentDescription = stringResource(R.string.content_description_list)
                                            )

                                            Settings -> Icon(
                                                Icons.Filled.Settings,
                                                contentDescription = stringResource(R.string.content_description_settings)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            when (item) {
                                                Dial -> stringResource(R.string.nav_pad)
                                                List -> stringResource(R.string.nav_list)
                                                Settings -> stringResource(R.string.nav_settings)
                                                else -> ""
                                            }
                                        )
                                    },
                                    selected = currentDestination == item.toString(),
                                    onClick = {
                                        globalStateViewModel.setCurrentPage(itemIndices[item] ?: 0)
                                        navController.navigate(item.toString())
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        NavHost(
                            navController = navController,
                            startDestination = Dial.toString(),
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(Dial.toString()) {
                                DialScreen(
                                    globalStateViewModel = globalStateViewModel,
                                    onSecretCode = { navController.navigate(Log.toString()) }
                                )
                            }
                            composable(List.toString()) { ListScreen(globalStateViewModel) }
                            composable(Settings.toString()) { SettingsScreen(globalStateViewModel) }
                            composable(Log.toString()) { LogScreen() }
                        }
                        ApiStatusBar(
                            apiState = state.apiState,
                            onDismiss = { globalStateViewModel.clearApiState() }
                        )
                    }
                }
            }
        }
    }
}

@Serializable
data object Dial

@Serializable
data object Settings

@Serializable
data object Log

@Serializable
data object List