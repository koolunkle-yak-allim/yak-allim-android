package com.example.yakallim.ui.ocr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.yakallim.R
import com.example.yakallim.ui.theme.Secondary

@Composable
fun OcrSourcePicker(
    onCameraCaptureClick: () -> Unit,
    onGallerySelectClick: () -> Unit,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        OutlinedButton(
            onClick = onCameraCaptureClick,
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 48.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(size = 12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
            border = BorderStroke(width = 1.dp, color = Secondary.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(size = 20.dp)
            )
            Spacer(modifier = Modifier.width(width = 8.dp))
            Text(
                text = stringResource(R.string.camera_btn_capture),
                fontWeight = FontWeight.Bold
            )
        }
        OutlinedButton(
            onClick = onGallerySelectClick,
            modifier = Modifier
                .weight(weight = 1f)
                .height(height = 48.dp),
            enabled = isEnabled,
            shape = RoundedCornerShape(size = 12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
            border = BorderStroke(width = 1.dp, color = Secondary.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(size = 20.dp)
            )
            Spacer(modifier = Modifier.width(width = 8.dp))
            Text(
                text = stringResource(R.string.ocr_btn_gallery_select),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
