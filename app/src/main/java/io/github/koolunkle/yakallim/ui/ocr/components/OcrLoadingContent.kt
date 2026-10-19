package io.github.koolunkle.yakallim.ui.ocr.components

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.ocr.OcrImage
import kotlinx.coroutines.delay

/**
 * 분석 중 화면. 무엇을 분석하는지 보이도록 사진 썸네일을 위에 두고, 아래 카드에 단계 문구와 진행 막대를 둔다.
 * 시스템의 "애니메이션 줄이기"가 켜져 있으면 움직이는 그림 대신 정지 아이콘을 쓴다.
 */
@Composable
fun OcrLoadingContent(
    image: OcrImage?,
    progress: Int,
    message: String,
    isSseActive: Boolean,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    // 서버가 세부 진행률을 알 수 없는 분석 단계(ANALYZING)에서는 진행률 숫자 대신 무한 진행 표시를 쓴다.
    isIndeterminate: Boolean = false
) {
    val context = LocalContext.current
    val isReducedMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    val targetProgress = progress / 100f
    var displayedProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(targetProgress, isSseActive) {
        val startProgress = displayedProgress
        if (targetProgress > startProgress) {
            val steps = 30
            val delayTime = 300L / steps
            val increment = (targetProgress - startProgress) / steps
            (1..steps).forEach { _ ->
                displayedProgress += increment
                delay(delayTime)
            }
            displayedProgress = targetProgress
        } else {
            displayedProgress = targetProgress
        }

        if (targetProgress < 1f && isSseActive) {
            while (true) {
                delay(1000)
                if (displayedProgress < 0.95f) {
                    displayedProgress = (displayedProgress + 0.01f).coerceAtMost(0.95f)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        image?.let { OcrPhotoThumbnail(image = it) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isReducedMotion) {
                    Icon(
                        imageVector = Icons.Outlined.Medication,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    PillAnimation()
                }
                Text(
                    text = message.ifEmpty { stringResource(R.string.ocr_status_analyzing) },
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (displayedProgress > 0f && !isIndeterminate) {
                        "${(displayedProgress * 100).toInt()}%"
                    } else {
                        stringResource(R.string.ocr_status_subtext)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                val barModifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                if (isIndeterminate && !isReducedMotion) {
                    LinearProgressIndicator(
                        modifier = barModifier,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                } else if (displayedProgress > 0f) {
                    LinearProgressIndicator(
                        progress = { displayedProgress },
                        modifier = barModifier,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        TextButton(
            onClick = onCancelClick,
            modifier = Modifier.heightIn(min = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.ocr_btn_cancel),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PillAnimation() {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId = R.raw.pill))
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.5f
    )
    LottieAnimation(
        composition = composition,
        progress = { lottieProgress },
        modifier = Modifier.size(160.dp)
    )
}
