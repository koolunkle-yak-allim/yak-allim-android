package com.example.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yakallim.R
import com.example.yakallim.ui.ocr.OcrError

@Composable
fun OcrErrorCard(error: OcrError) {
    val displayMessage = when (error) {
        is OcrError.Network -> stringResource(R.string.error_network)
        is OcrError.Timeout -> stringResource(R.string.error_timeout)
        is OcrError.EmptyResult -> stringResource(R.string.error_failed_parsing)
        is OcrError.AnalysisFailed -> stringResource(R.string.error_failed_analysis)
        is OcrError.ServerError -> error.message
        is OcrError.Unknown -> stringResource(R.string.error_failed_proceed, error.message)
    }
    val isNetworkError = error is OcrError.Network

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.Warning,
                contentDescription = stringResource(R.string.error_cd_icon),
                modifier = Modifier.size(size = 20.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = displayMessage,
                modifier = Modifier.weight(weight = 1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
