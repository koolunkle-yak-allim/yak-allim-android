package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.YakTheme

/**
 * 결과 맨 위 요약. 찾은 약 수를 가장 크게 보여 주고, 알람이 몇 개 켜졌는지와
 * 사진에 없어 직접 정해야 하는 약이 몇 개인지를 한 줄로 알린다.
 */
@Composable
fun OcrResultSummary(
    medicineCount: Int,
    alarmOnCount: Int,
    needsCheckCount: Int,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.ocr_result_summary, medicineCount),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.ocr_result_alarm_count, alarmOnCount, medicineCount),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (needsCheckCount > 0) {
                Text(
                    text = stringResource(R.string.ocr_result_needs_check_count, needsCheckCount),
                    style = MaterialTheme.typography.bodyLarge,
                    color = YakTheme.status.warning
                )
            }
        }
    }
}

/** 결과를 믿기 전에 확인할 점을 한 곳에 모은 안내 배너. */
@Composable
fun OcrResultHintBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.ocr_result_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
