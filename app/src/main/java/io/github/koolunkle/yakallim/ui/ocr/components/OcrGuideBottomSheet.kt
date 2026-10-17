package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.Primary
import io.github.koolunkle.yakallim.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrGuideBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Primary.copy(alpha = 0.1f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 32.dp)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(size = 28.dp),
                    tint = Secondary
                )
                Text(
                    text = stringResource(R.string.help_title_ocr),
                    color = Primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 24.dp)
            ) {
                OcrGuideItem(
                    number = "1",
                    title = stringResource(R.string.help_item_analysis_title),
                    desc = stringResource(R.string.help_item_analysis_desc)
                )
                OcrGuideItem(
                    number = "2",
                    title = stringResource(R.string.help_item_alarm_title),
                    desc = stringResource(R.string.help_item_alarm_desc)
                )
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 56.dp),
                shape = RoundedCornerShape(size = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.common_confirm),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun OcrGuideItem(number: String, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 16.dp)) {
        Surface(
            modifier = Modifier.size(size = 24.dp),
            shape = RoundedCornerShape(size = 8.dp),
            color = Secondary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = Secondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(
                text = title,
                color = Primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = desc,
                color = Primary.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
