package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.ocr.OcrImage

@Composable
fun OcrImageViewer(image: OcrImage?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 300.dp)
            .clip(RoundedCornerShape(size = 24.dp))
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(size = 24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when (image) {
            is OcrImage.UriSource -> AsyncImage(
                model = image.uri,
                contentDescription = stringResource(R.string.ocr_cd_prescription),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            is OcrImage.BitmapSource -> Image(
                bitmap = image.bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.camera_cd_prescription),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            null -> Column(
                verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = stringResource(R.string.ocr_cd_upload_icon),
                    modifier = Modifier.size(size = 64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.ocr_status_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
