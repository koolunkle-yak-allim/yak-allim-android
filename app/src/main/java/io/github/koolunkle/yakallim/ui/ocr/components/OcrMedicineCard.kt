package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.domain.model.Alarm
import io.github.koolunkle.yakallim.domain.model.PrescribedMedicine
import io.github.koolunkle.yakallim.ui.alarm.AlarmFormDefaults
import io.github.koolunkle.yakallim.ui.theme.YakTheme

/** 약 카드의 상태. 색과 함께 반드시 글자(칩)로도 보여 준다. */
enum class MedicineCardStatus { ON, OFF, NEEDS_CHECK }

fun PrescribedMedicine.hasCompleteDosage(): Boolean =
    AlarmFormDefaults.parseDosage(dosagePerTake, name.orEmpty()).amount != null &&
        (dailyFrequency ?: 0) > 0 &&
        (durationDays ?: 0) > 0

/**
 * 보기 전용 약 카드. 값 변경과 알람 켜기·끄기는 [onAlarmClick]으로 여는 알람 설정 시트에서 한다.
 * 복용 정보는 약 봉투 표기 순서(1회 → 1일 → 일분)를 따르고, 비어 있는 값은 그 자리에 "?"로 보여 준다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OcrMedicineCard(
    medicine: PrescribedMedicine,
    displayName: String,
    alarm: Alarm?,
    alarmSoundName: String,
    onAlarmClick: () -> Unit,
) {
    val status = when {
        alarm != null -> MedicineCardStatus.ON
        !medicine.hasCompleteDosage() -> MedicineCardStatus.NEEDS_CHECK
        else -> MedicineCardStatus.OFF
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = displayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AnimatedContent(
                    targetState = status,
                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                    label = "medicineStatus"
                ) { target -> MedicineStatusChip(target) }
            }

            MedicineSpecLine(medicine = medicine, displayName = displayName)

            if (alarm != null && alarm.times.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.medicine_alarm_summary,
                        alarm.times.joinToString(" · "),
                        alarmSoundName
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                when (status) {
                    MedicineCardStatus.ON -> TextButton(
                        onClick = onAlarmClick,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) { Text(stringResource(R.string.medicine_btn_change_alarm)) }

                    MedicineCardStatus.OFF -> FilledTonalButton(
                        onClick = onAlarmClick,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(stringResource(R.string.medicine_btn_set_alarm)) }

                    MedicineCardStatus.NEEDS_CHECK -> FilledTonalButton(
                        onClick = onAlarmClick,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) { Text(stringResource(R.string.medicine_btn_enter_dosage)) }
                }
            }
        }
    }
}

@Composable
private fun MedicineStatusChip(status: MedicineCardStatus) {
    val (container, content, label) = when (status) {
        MedicineCardStatus.ON -> Triple(
            YakTheme.status.successContainer, YakTheme.status.success, R.string.medicine_status_on
        )
        MedicineCardStatus.OFF -> Triple(
            MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant,
            R.string.medicine_status_off
        )
        MedicineCardStatus.NEEDS_CHECK -> Triple(
            YakTheme.status.warningContainer, YakTheme.status.warning, R.string.medicine_status_needs_check
        )
    }
    Surface(shape = CircleShape, color = container) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = content, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelMedium,
                color = content
            )
        }
    }
}

/** "1회 1정 · 1일 3회 · 5일분". 비어 있는 값은 주의 색 "?"로 표시한다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicineSpecLine(medicine: PrescribedMedicine, displayName: String) {
    val dosage = AlarmFormDefaults.parseDosage(medicine.dosagePerTake, displayName)
    val missing = stringResource(R.string.medicine_spec_missing)
    val amountText = dosage.amount?.let { AlarmFormDefaults.formatAmount(it) + dosage.unit }
    val frequencyText = medicine.dailyFrequency?.takeIf { it > 0 }?.toString()
    val durationText = medicine.durationDays?.takeIf { it > 0 }?.toString()

    val labels = listOf(
        R.string.medicine_spec_amount to amountText,
        R.string.medicine_spec_frequency to frequencyText,
        R.string.medicine_spec_duration to durationText,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { (formatRes, value) ->
            SpecItem(
                format = stringResource(formatRes, PLACEHOLDER),
                value = value ?: missing,
                isMissing = value == null
            )
        }
    }
}

private const val PLACEHOLDER = "\u0000"

@Composable
private fun SpecItem(format: String, value: String, isMissing: Boolean) {
    val (before, after) = format.split(PLACEHOLDER).let { it.first() to it.getOrElse(1) { "" } }
    val valueColor = if (isMissing) YakTheme.status.warning else MaterialTheme.colorScheme.onSurface
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append(before) }
            withStyle(SpanStyle(color = valueColor, fontWeight = FontWeight.SemiBold)) { append(value) }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append(after) }
        },
        style = MaterialTheme.typography.bodyLarge.copy(fontFeatureSettings = "tnum"),
        color = Color.Unspecified
    )
}
