package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.WordmarkTextStyle

/**
 * 앱 이름은 본문과 같은 왼쪽 축(20dp)에 맞추고, 결과 요약보다 작게 둔다.
 * 도움말은 보조 동작이라 아이콘을 보조 글자색으로 낮춘다. 아이콘 바깥 여백(8dp)과
 * 버튼 안쪽 여백(12dp)을 더해 오른쪽도 20dp 축에 맞는다.
 */
@Composable
fun OcrTopBar(onGuideButtonClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 20.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = WordmarkTextStyle
        )
        IconButton(onClick = onGuideButtonClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = stringResource(R.string.help_title_guide),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
