package eu.me2d.cmlmobile.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.me2d.cmlmobile.R

@Composable
fun RegistrationRequiredHint() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = stringResource(R.string.content_description_info),
                tint = Color(0xFF2196F3),
                modifier = Modifier
                    .height(48.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.registration_required_title) + "\n" + stringResource(
                    R.string.registration_required_subtitle
                ),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                modifier = Modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationRequiredHintPreview() {
    RegistrationRequiredHint()
}
