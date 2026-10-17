package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.HighlightCoral
import io.github.koolunkle.yakallim.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun OcrLoadingContent(
    progress: Int,
    message: String,
    isSseActive: Boolean,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
    // 서버가 세부 진행률을 알 수 없는 분석 단계(ANALYZING)에서는 진행률 숫자 대신 무한 진행 표시를 쓴다.
    isIndeterminate: Boolean = false
) {
    val lottieComposition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(resId = R.raw.pill))
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever,
        speed = 0.5f
    )

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
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = lottieComposition,
            progress = { lottieProgress },
            modifier = Modifier.size(size = 240.dp)
        )
        Spacer(modifier = Modifier.height(height = 32.dp))
        Text(
            text = message.ifEmpty { stringResource(R.string.ocr_status_analyzing) },
            color = Primary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(height = 12.dp))
        Text(
            text = if (displayedProgress > 0f && !isIndeterminate) "${(displayedProgress * 100).toInt()}%" else stringResource(R.string.ocr_status_subtext),
            modifier = Modifier.padding(horizontal = 40.dp),
            color = Primary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        if (isIndeterminate) {
            Spacer(modifier = Modifier.height(height = 16.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = HighlightCoral,
                trackColor = Primary.copy(alpha = 0.1f)
            )
        } else if (displayedProgress > 0f) {
            Spacer(modifier = Modifier.height(height = 16.dp))
            LinearProgressIndicator(
                progress = { displayedProgress },
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = HighlightCoral,
                trackColor = Primary.copy(alpha = 0.1f)
            )
        }
        Spacer(modifier = Modifier.height(height = 48.dp))
        TextButton(
            onClick = onCancelClick,
            modifier = Modifier.height(height = 48.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = HighlightCoral.copy(alpha = 0.8f))
        ) {
            Text(
                text = stringResource(R.string.ocr_btn_cancel),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
