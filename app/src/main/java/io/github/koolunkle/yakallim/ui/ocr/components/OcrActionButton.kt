package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R

@Composable
fun OcrActionButton(
    isLoading: Boolean,
    enabled: Boolean,
    hasError: Boolean,
    onStartAnalysisClick: () -> Unit
) {
    if (!isLoading) {
        Button(
            onClick = onStartAnalysisClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 56.dp),
            enabled = enabled,
            shape = RoundedCornerShape(size = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(if (hasError) R.string.ocr_btn_retry else R.string.ocr_btn_start),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
