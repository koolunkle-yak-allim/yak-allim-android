package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R

/** 홈 화면 하단에 고정되는 주 버튼. 사진이 있을 때만 누를 수 있다. */
@Composable
fun OcrActionButton(
    enabled: Boolean,
    hasError: Boolean,
    onStartAnalysisClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Button(
                onClick = onStartAnalysisClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(if (hasError) R.string.ocr_btn_retry else R.string.ocr_btn_start),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
