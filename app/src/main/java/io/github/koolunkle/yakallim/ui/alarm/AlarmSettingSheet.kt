package io.github.koolunkle.yakallim.ui.alarm

import android.media.Ringtone
import android.media.RingtoneManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.alarm.AlarmFormDefaults.TimeOfDay
import io.github.koolunkle.yakallim.ui.theme.NumeralTextStyle
import io.github.koolunkle.yakallim.ui.theme.YakTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 알람 설정 시트가 확정한 값. dosagePerTake는 "1정"처럼 숫자와 단위를 붙여 쓴다. */
data class AlarmSheetResult(
    val dosagePerTake: String,
    val dailyFrequency: Int,
    val durationDays: Int,
    val alarmTimes: List<String>,
)

private const val REPEAT_START_DELAY_MS = 400L
private const val REPEAT_INTERVAL_MS = 100L
private const val DISABLED_ALPHA = 0.38f
private val DURATION_PRESETS = listOf(3, 5, 7, 14, 30)
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN)

/**
 * 복용 정보, 복용 시각, 알람음을 한 화면에서 정하고 알람을 켠다.
 * 사진에 없어 비어 있던 값은 "선택"으로 두고, 모두 정할 때까지 확인 버튼을 막는다.
 * 알람음은 시스템 선택기를 거쳐 화면이 들고 있으므로 [soundUri]로 받는다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmSettingSheet(
    medicineName: String,
    initialDosagePerTake: String?,
    initialFrequency: Int?,
    initialDuration: Int?,
    initialTimes: List<String>?,
    soundUri: String?,
    soundName: String,
    isRegistered: Boolean,
    onSelectRingtone: () -> Unit,
    onSelectDeviceAudio: () -> Unit,
    onConfirm: (AlarmSheetResult) -> Unit,
    onTurnOff: () -> Unit,
    onDismiss: () -> Unit,
) {
    val initialDosage = remember(initialDosagePerTake, medicineName) {
        AlarmFormDefaults.parseDosage(initialDosagePerTake, medicineName)
    }
    val hadMissingValue = remember {
        initialDosage.amount == null || (initialFrequency ?: 0) <= 0 || (initialDuration ?: 0) <= 0
    }

    var amount by rememberSaveable { mutableStateOf(initialDosage.amount) }
    var frequency by rememberSaveable { mutableStateOf(initialFrequency?.takeIf { it > 0 }) }
    var duration by rememberSaveable { mutableStateOf(initialDuration?.takeIf { it > 0 }) }
    var times by rememberSaveable {
        mutableStateOf(
            frequency?.let { freq ->
                initialTimes?.takeIf { it.size == freq } ?: AlarmFormDefaults.defaultTimes(freq)
            } ?: emptyList()
        )
    }
    var editingTimeIndex by remember { mutableStateOf<Int?>(null) }

    fun updateFrequency(value: Int) {
        frequency = value
        times = AlarmFormDefaults.resizeTimes(times, value)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = medicineName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (hadMissingValue) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = YakTheme.status.warningContainer
                ) {
                    Text(
                        text = stringResource(R.string.alarm_sheet_missing_hint),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = YakTheme.status.warning
                    )
                }
            }

            SectionTitle(stringResource(R.string.alarm_sheet_section_dosage))
            StepperRow(
                label = stringResource(R.string.alarm_sheet_amount),
                valueText = amount?.let { AlarmFormDefaults.formatAmount(it) + initialDosage.unit },
                canDecrease = amount == null || amount!! > AlarmFormDefaults.MIN_AMOUNT,
                canIncrease = amount == null || amount!! < AlarmFormDefaults.MAX_AMOUNT,
                onDecrease = {
                    amount = (amount?.minus(AlarmFormDefaults.AMOUNT_STEP) ?: 1.0)
                        .coerceAtLeast(AlarmFormDefaults.MIN_AMOUNT)
                },
                onIncrease = {
                    amount = (amount?.plus(AlarmFormDefaults.AMOUNT_STEP) ?: 1.0)
                        .coerceAtMost(AlarmFormDefaults.MAX_AMOUNT)
                }
            )
            StepperRow(
                label = stringResource(R.string.alarm_sheet_frequency),
                valueText = frequency?.let { stringResource(R.string.alarm_sheet_value_frequency, it) },
                canDecrease = frequency == null || frequency!! > AlarmFormDefaults.MIN_FREQUENCY,
                canIncrease = frequency == null || frequency!! < AlarmFormDefaults.MAX_FREQUENCY,
                onDecrease = { updateFrequency(((frequency ?: 4) - 1).coerceAtLeast(AlarmFormDefaults.MIN_FREQUENCY)) },
                onIncrease = { updateFrequency(((frequency ?: 2) + 1).coerceAtMost(AlarmFormDefaults.MAX_FREQUENCY)) }
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StepperRow(
                    label = stringResource(R.string.alarm_sheet_duration),
                    valueText = duration?.let { stringResource(R.string.alarm_sheet_value_duration, it) },
                    canDecrease = duration == null || duration!! > AlarmFormDefaults.MIN_DURATION_DAYS,
                    canIncrease = duration == null || duration!! < AlarmFormDefaults.MAX_DURATION_DAYS,
                    onDecrease = {
                        duration = ((duration ?: 8) - 1).coerceAtLeast(AlarmFormDefaults.MIN_DURATION_DAYS)
                    },
                    onIncrease = {
                        duration = ((duration ?: 6) + 1).coerceAtMost(AlarmFormDefaults.MAX_DURATION_DAYS)
                    }
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DURATION_PRESETS.forEach { days ->
                        FilterChip(
                            selected = duration == days,
                            onClick = { duration = days },
                            label = { Text(stringResource(R.string.alarm_sheet_value_duration, days)) }
                        )
                    }
                }
                duration?.let { days ->
                    val today = LocalDate.now()
                    Text(
                        text = stringResource(
                            R.string.alarm_sheet_duration_range,
                            today.format(DATE_FORMAT),
                            today.plusDays(days - 1L).format(DATE_FORMAT)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            frequency?.let { freq ->
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                SectionTitle(stringResource(R.string.alarm_sheet_section_times, freq))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    times.forEachIndexed { index, time ->
                        TimeChip(time = time, onClick = { editingTimeIndex = index })
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            SectionTitle(stringResource(R.string.alarm_sheet_section_sound))
            SoundRow(soundUri = soundUri, soundName = soundName, onClick = onSelectRingtone)
            TextButton(
                onClick = onSelectDeviceAudio,
                modifier = Modifier.heightIn(min = 48.dp)
            ) { Text(stringResource(R.string.alarm_sheet_btn_device_audio)) }

            val canConfirm = amount != null && frequency != null && duration != null
            Button(
                onClick = {
                    onConfirm(
                        AlarmSheetResult(
                            dosagePerTake = AlarmFormDefaults.formatAmount(amount!!) + initialDosage.unit,
                            dailyFrequency = frequency!!,
                            durationDays = duration!!,
                            alarmTimes = times
                        )
                    )
                },
                enabled = canConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(
                        if (isRegistered) R.string.alarm_sheet_btn_save else R.string.alarm_sheet_btn_turn_on
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (isRegistered) {
                TextButton(
                    onClick = onTurnOff,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.alarm_sheet_btn_turn_off),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    editingTimeIndex?.let { index ->
        TimePickerDialog(
            initialTime = times[index],
            onDismiss = { editingTimeIndex = null },
            onConfirm = { picked ->
                times = AlarmFormDefaults.replaceTime(times, index, picked)
                editingTimeIndex = null
            }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** 값이 null이면 "선택"을 주의 색으로 보여 준다. 버튼은 48dp 터치 영역을 그대로 쓴다. */
@Composable
private fun StepperRow(
    label: String,
    valueText: String?,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        RepeatingIconButton(onClick = onDecrease, enabled = canDecrease) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = stringResource(R.string.alarm_sheet_cd_decrease, label)
            )
        }
        Text(
            text = valueText ?: stringResource(R.string.alarm_sheet_value_empty),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.titleMedium.merge(NumeralTextStyle),
            color = if (valueText == null) YakTheme.status.warning else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        RepeatingIconButton(onClick = onIncrease, enabled = canIncrease) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.alarm_sheet_cd_increase, label)
            )
        }
    }
}

/**
 * 누르면 한 번, 길게 누르면 400ms 뒤부터 100ms마다 반복해서 [onClick]을 부른다.
 * 범위 끝에 닿아 [enabled]가 false가 되면 반복도 멈춘다. 접근성 서비스에는 일반 버튼으로 보인다.
 */
@Composable
private fun RepeatingIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .indication(interactionSource, ripple())
            .semantics {
                role = Role.Button
                if (!enabled) disabled()
                onClick {
                    if (enabled) currentOnClick()
                    enabled
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val press = PressInteraction.Press(down.position)
                        launch { interactionSource.emit(press) }
                        val repeat = launch {
                            currentOnClick()
                            delay(REPEAT_START_DELAY_MS)
                            while (true) {
                                currentOnClick()
                                delay(REPEAT_INTERVAL_MS)
                            }
                        }
                        val up = waitForUpOrCancellation()
                        repeat.cancel()
                        launch {
                            interactionSource.emit(
                                if (up != null) PressInteraction.Release(press) else PressInteraction.Cancel(press)
                            )
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) contentColor else contentColor.copy(alpha = DISABLED_ALPHA)
        ) { content() }
    }
}

@Composable
private fun TimeChip(time: String, onClick: () -> Unit) {
    val label = stringResource(
        when (AlarmFormDefaults.timeOfDayLabel(time)) {
            TimeOfDay.MORNING -> R.string.alarm_time_morning
            TimeOfDay.NOON -> R.string.alarm_time_noon
            TimeOfDay.EVENING -> R.string.alarm_time_evening
            TimeOfDay.BEDTIME -> R.string.alarm_time_bedtime
        }
    )
    val description = stringResource(R.string.alarm_sheet_cd_edit_time, label, time)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .widthIn(min = 88.dp)
            .semantics { contentDescription = description },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge.merge(NumeralTextStyle),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 알람음 이름을 누르면 시스템 선택기를, 재생 버튼을 누르면 그 소리를 미리 들려준다. */
@Composable
private fun SoundRow(soundUri: String?, soundName: String, onClick: () -> Unit) {
    val context = LocalContext.current
    var ringtone by remember { mutableStateOf<Ringtone?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    fun stop() {
        ringtone?.stop()
        ringtone = null
        isPlaying = false
    }

    DisposableEffect(soundUri) {
        onDispose { stop() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = soundName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = {
            if (isPlaying) {
                stop()
            } else {
                val uri = soundUri?.toUri() ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ringtone = RingtoneManager.getRingtone(context, uri)?.also { it.play() }
                isPlaying = ringtone != null
            }
        }) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isPlaying) R.string.alarm_sheet_cd_preview_stop else R.string.alarm_sheet_cd_preview_play
                ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val (hour, minute) = initialTime.split(":").map { it.toIntOrNull() ?: 0 }
        .let { it.getOrElse(0) { 8 } to it.getOrElse(1) { 0 } }
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alarm_time_picker_title)) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(String.format(Locale.US, "%02d:%02d", state.hour, state.minute))
            }) { Text(stringResource(R.string.common_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
