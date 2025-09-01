package eu.me2d.cmlmobile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.me2d.cmlmobile.R
import eu.me2d.cmlmobile.state.ApiCallStatus
import eu.me2d.cmlmobile.state.ApiState
import kotlinx.coroutines.delay

@Composable
private fun getLocalizedApiMessage(message: String): String {
    val context = LocalContext.current

    return when {
        message.startsWith("api_calling_format|") -> {
            val callType = message.substringAfter("api_calling_format|")
            context.getString(R.string.api_calling_format, callType)
        }

        message.startsWith("api_command_executed|") -> {
            val commandNumber = message.substringAfter("api_command_executed|").toIntOrNull() ?: 0
            context.getString(R.string.api_command_executed, commandNumber)
        }

        message.startsWith("api_commands_fetched|") -> {
            val count = message.substringAfter("api_commands_fetched|").toIntOrNull() ?: 0
            context.getString(R.string.api_commands_fetched, count)
        }

        message == "api_success_default" -> {
            context.getString(R.string.api_success_default)
        }

        message == "api_unknown_error" -> {
            context.getString(R.string.api_unknown_error)
        }

        else -> message // Return as-is if not a special format
    }
}

@Composable
fun ApiStatusBar(
    apiState: ApiState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {}
) {
    // Automatically dismiss after 10 seconds when SUCCESS or ERROR occurs
    val autoDismissDurationMillis = 10_000L
    val showProgressBar =
        apiState.status == ApiCallStatus.SUCCESS || apiState.status == ApiCallStatus.ERROR

    // Animated progress value (from 1f to 0f)
    val progress = remember(apiState.status) { Animatable(1f) }

    LaunchedEffect(apiState.status) {
        if (showProgressBar) {
            progress.snapTo(1f)
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = autoDismissDurationMillis.toInt(),
                    easing = LinearEasing
                )
            )
        }
    }

    LaunchedEffect(apiState.status) {
        if (showProgressBar) {
            delay(autoDismissDurationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = apiState.status != ApiCallStatus.IDLE,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val backgroundColor = when (apiState.status) {
            ApiCallStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
            ApiCallStatus.SUCCESS -> MaterialTheme.colorScheme.secondaryContainer
            ApiCallStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
            ApiCallStatus.IDLE -> Color.Transparent
        }

        val contentColor = when (apiState.status) {
            ApiCallStatus.IN_PROGRESS -> MaterialTheme.colorScheme.onPrimaryContainer
            ApiCallStatus.SUCCESS -> MaterialTheme.colorScheme.onSecondaryContainer
            ApiCallStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
            ApiCallStatus.IDLE -> Color.Transparent
        }

        // Use Column to stack bar and progress indicator
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(
                        enabled = apiState.status == ApiCallStatus.SUCCESS || apiState.status == ApiCallStatus.ERROR,
                        onClick = onDismiss
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (apiState.status) {
                    ApiCallStatus.IN_PROGRESS -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                            color = contentColor
                        )
                    }

                    ApiCallStatus.SUCCESS -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.content_description_success),
                            tint = contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    ApiCallStatus.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.content_description_error),
                            tint = contentColor,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }

                    ApiCallStatus.IDLE -> {
                        // Empty - should not be visible anyway due to AnimatedVisibility
                    }
                }

                Text(
                    text = getLocalizedApiMessage(apiState.statusMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor
                )
            }

            // Animated progress line for timer
            if (showProgressBar) {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = contentColor,
                    trackColor = backgroundColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ApiStatusBarPreviewLoading() {
    ApiStatusBar(
        apiState = ApiState(
            status = ApiCallStatus.IN_PROGRESS,
            statusMessage = "Calling registration...",
            lastCallType = "registration"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ApiStatusBarPreviewSuccess() {
    ApiStatusBar(
        apiState = ApiState(
            status = ApiCallStatus.SUCCESS,
            statusMessage = "Registration successful",
            lastCallType = "registration"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ApiStatusBarPreviewError() {
    ApiStatusBar(
        apiState = ApiState(
            status = ApiCallStatus.ERROR,
            statusMessage = "Network error occurred",
            lastCallType = "registration"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ApiStatusBarPreviewIdle() {
    ApiStatusBar(
        apiState = ApiState(
            status = ApiCallStatus.IDLE,
            statusMessage = "",
            lastCallType = ""
        )
    )
}