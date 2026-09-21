package com.example.yakallim.ui.ocr.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.yakallim.R
import com.example.yakallim.domain.model.PrescribedMedicine
import com.example.yakallim.domain.model.Alarm
import com.example.yakallim.ui.theme.HighlightCoral
import com.example.yakallim.ui.theme.MintBorder
import com.example.yakallim.ui.theme.Primary
import com.example.yakallim.ui.theme.Secondary
import com.example.yakallim.ui.theme.Success
import com.example.yakallim.ui.theme.SuccessBorder
import com.example.yakallim.ui.theme.SuccessContainer
import com.example.yakallim.ui.theme.Warning

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OcrMedicineCard(
    medicineInfo: PrescribedMedicine,
    isAlarmRegistered: Boolean,
    alarm: Alarm?,
    alarmSoundName: String,
    highlightedMedicineName: String?,
    isCardExpanded: Boolean,
    onToggleExpansionClick: () -> Unit,
    onRegisterAlarmClick: (String, String, Int, Int) -> Unit,
    onCancelAlarmClick: (String) -> Unit,
) {
    val medicineName = medicineInfo.name ?: stringResource(R.string.error_unknown_medicine)
    val isHighlighted = highlightedMedicineName == medicineName
    val isLowConfidence = medicineInfo.isLowConfidence

    val animatedContainerColor by animateColorAsState(
        targetValue = if (isAlarmRegistered) SuccessContainer else Color.White,
        label = "cardContainerColor"
    )
    val targetBorderColor = when {
        isAlarmRegistered -> Success.copy(alpha = 0.5f)
        isHighlighted -> Secondary
        isLowConfidence -> Warning.copy(alpha = 0.4f)
        else -> Secondary.copy(alpha = 0.15f)
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        label = "cardBorderColor"
    )
    val animatedInternalBorderColor by animateColorAsState(
        targetValue = if (isAlarmRegistered) SuccessBorder else MintBorder,
        label = "internalBorderColor"
    )

    var dosagePerTake by remember(medicineInfo.name) {
        mutableStateOf(medicineInfo.dosagePerTake.orEmpty().filter { (it.isDigit() || it == '.') })
    }
    val defaultDosageUnit = stringResource(R.string.ocr_unit_tablet)
    var dosageUnit by remember(medicineInfo.name) {
        mutableStateOf(medicineInfo.dosagePerTake.orEmpty().filter { !it.isDigit() && it != '.' }.trim().ifEmpty { defaultDosageUnit })
    }
    var frequency by remember(medicineInfo.name) { mutableStateOf(medicineInfo.dailyFrequency?.toString().orEmpty()) }
    var durationDays by remember(medicineInfo.name) { mutableStateOf(medicineInfo.durationDays?.toString().orEmpty()) }

    val isDosageInfoComplete = dosagePerTake.isNotBlank() &&
        (frequency.toIntOrNull() ?: 0) > 0 &&
        (durationDays.toIntOrNull() ?: 0) > 0

    val combinedDescription = if (isDosageInfoComplete) {
        stringResource(
            R.string.alarm_card_format,
            frequency.toIntOrNull() ?: 0,
            durationDays.toIntOrNull() ?: 0,
            "$dosagePerTake$dosageUnit"
        )
    } else {
        stringResource(R.string.prescription_confirmation_required)
    }

    val registerAlarm = {
        onRegisterAlarmClick(
            medicineName,
            "$dosagePerTake$dosageUnit",
            frequency.toIntOrNull() ?: 0,
            durationDays.toIntOrNull() ?: 0
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCardExpanded,
                onClick = onToggleExpansionClick
            ),
        shape = RoundedCornerShape(size = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = animatedContainerColor,
            disabledContainerColor = animatedContainerColor
        ), border = BorderStroke(
            width = if (isHighlighted) 2.5.dp else 1.2.dp,
            color = animatedBorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            AnimatedVisibility(
                visible = isLowConfidence && !isAlarmRegistered && isCardExpanded,
                enter = fadeIn(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(size = 12.dp),
                    color = Warning.copy(alpha = 0.05f),
                    border = BorderStroke(width = 1.dp, color = Warning.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(all = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.common_cd_warning_icon),
                            tint = Warning.copy(alpha = 0.7f),
                            modifier = Modifier.size(size = 18.dp)
                        )
                        Text(
                            text = stringResource(R.string.error_low_confidence),
                            color = Warning.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicineName,
                        color = if (isAlarmRegistered) Success else Primary,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (!isCardExpanded) {
                        Spacer(modifier = Modifier.height(height = 4.dp))
                        Text(
                            text = combinedDescription,
                            color = Primary.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (isAlarmRegistered && alarm != null && alarm.times.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(height = 4.dp))
                            Text(
                                text = stringResource(
                                    R.string.alarm_card_active_summary,
                                    alarm.times.joinToString(", "),
                                    alarmSoundName
                                ),
                                color = Primary.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isCardExpanded) {
                        if (isAlarmRegistered) {
                            Button(
                                onClick = { onCancelAlarmClick(medicineName) },
                                modifier = Modifier.height(height = 36.dp),
                                shape = RoundedCornerShape(size = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HighlightCoral,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.alarm_btn_unregister),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isDosageInfoComplete) registerAlarm() else onToggleExpansionClick()
                                },
                                modifier = Modifier.height(height = 36.dp),
                                shape = RoundedCornerShape(size = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Secondary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.alarm_btn_register),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onToggleExpansionClick,
                            modifier = Modifier.size(size = 36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = stringResource(R.string.common_cd_toggle_icon),
                                tint = if (isAlarmRegistered) Success else Secondary
                            )
                        }
                    }
                }
            }
            if (isCardExpanded) {
                HorizontalDivider(color = animatedInternalBorderColor.copy(alpha = 0.25f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
                ) {
                    MedicineInputField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        labelTextResId = R.string.prescription_daily_frequency,
                        unitText = stringResource(R.string.ocr_unit_times),
                        modifier = Modifier.weight(weight = 1f),
                        borderColor = animatedInternalBorderColor,
                        isAlarmRegistered = isAlarmRegistered
                    )
                    MedicineInputField(
                        value = durationDays,
                        onValueChange = { durationDays = it },
                        labelTextResId = R.string.prescription_duration_days,
                        unitText = stringResource(R.string.ocr_unit_days),
                        modifier = Modifier.weight(weight = 1f),
                        borderColor = animatedInternalBorderColor,
                        isAlarmRegistered = isAlarmRegistered
                    )
                    MedicineInputField(
                        value = dosagePerTake,
                        onValueChange = { dosagePerTake = it },
                        labelTextResId = R.string.prescription_dosage_per_take,
                        unitText = dosageUnit,
                        modifier = Modifier.weight(1f),
                        borderColor = animatedInternalBorderColor,
                        isAlarmRegistered = isAlarmRegistered
                    )
                }
                if (!isDosageInfoComplete) {
                    Text(
                        text = stringResource(R.string.alarm_dosage_info_required),
                        color = Warning,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = 12.dp),
                    color = if (isAlarmRegistered) Color.White.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.3f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = animatedInternalBorderColor.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = stringResource(R.string.prescription_label),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(height = 8.dp))
                        Text(
                            text = combinedDescription,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (isAlarmRegistered && alarm != null && alarm.times.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(size = 12.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = animatedInternalBorderColor.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Text(
                                text = stringResource(R.string.alarm_card_registered_detail),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(height = 6.dp))
                            Text(
                                text = stringResource(R.string.alarm_card_time_format, alarm.times.joinToString(", ")),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(height = 4.dp))
                            Text(
                                text = stringResource(R.string.alarm_card_sound_format, alarmSoundName),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                if (isAlarmRegistered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = registerAlarm,
                            enabled = isDosageInfoComplete,
                            modifier = Modifier
                                .weight(1f)
                                .height(height = 48.dp),
                            shape = RoundedCornerShape(size = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Secondary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.alarm_btn_modify),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { onCancelAlarmClick(medicineName) },
                            modifier = Modifier
                                .weight(1f)
                                .height(height = 48.dp),
                            shape = RoundedCornerShape(size = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HighlightCoral,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.alarm_btn_unregister),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = registerAlarm,
                        enabled = isDosageInfoComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height = 48.dp),
                        shape = RoundedCornerShape(size = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Secondary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.alarm_btn_register),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineInputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelTextResId: Int,
    unitText: String,
    modifier: Modifier,
    borderColor: Color,
    isAlarmRegistered: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(size = 12.dp),
        color = if (isAlarmRegistered) Color.White.copy(alpha = 0.5f) else Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor.copy(alpha = if (isAlarmRegistered) 0.35f else 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(labelTextResId),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                softWrap = false,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(height = 4.dp))
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .width(intrinsicSize = IntrinsicSize.Min)
                        .widthIn(min = 4.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    cursorBrush = SolidColor(if (isAlarmRegistered) Success else Secondary)
                )
                Text(
                    text = unitText,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    softWrap = false,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
