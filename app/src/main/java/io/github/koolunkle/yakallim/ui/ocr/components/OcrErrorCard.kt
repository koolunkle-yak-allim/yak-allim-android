package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.ocr.OcrError

/** 원인과 해결 방법을 한 카드에 보여 준다. 다시 시도 버튼은 화면 아래 고정 버튼이 맡는다. */
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isNetworkError) Icons.Default.WifiOff else Icons.Default.Warning,
                contentDescription = stringResource(R.string.error_cd_icon),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.error_title),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = displayMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
