package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.Primary

@Composable
fun OcrTopBar(onGuideButtonClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 48.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier.align(alignment = Alignment.Center),
                color = Primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = onGuideButtonClick,
                modifier = Modifier.align(alignment = Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = stringResource(R.string.help_title_guide),
                    tint = Primary
                )
            }
        }
    }
}
