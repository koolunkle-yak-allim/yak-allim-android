package io.github.koolunkle.yakallim.ui.ocr.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.ui.theme.HighlightCoral
import io.github.koolunkle.yakallim.ui.theme.Secondary
import io.github.koolunkle.yakallim.util.showToast
import java.util.Locale

private const val TIME_FORMAT = "%02d:%02d"
private const val TIME_DELIMITER = ":"
private const val MAX_INPUT_LENGTH = 2
private const val MAX_ITEMS_PER_ROW = 3

private const val MIN_HOUR = 0
private const val MAX_HOUR = 23
private const val MIN_MINUTE = 0
private const val MAX_MINUTE = 59

private const val DEFAULT_HOUR_STRING = "09"
private const val DEFAULT_MINUTE_STRING = "00"

private const val ALPHA_SURFACE_BG = 0.4f
private const val ALPHA_BORDER_OUTLINE = 0.1f

private object AlarmTimeFactory {
    private const val TIME_FORMAT_HOUR_ONLY = "%02d:00"
    private const val DEFAULT_TIME_MORNING = "09:00"
    private const val DEFAULT_TIME_AFTERNOON = "13:00"
    private const val DEFAULT_TIME_EVENING = "19:00"

    private const val START_HOUR = 8
    private const val ALLOCATION_WINDOW_HOURS = 12

    fun getDefaultTimes(dailyFrequency: Int): List<String> {
        return when (dailyFrequency) {
            1 -> listOf(DEFAULT_TIME_MORNING)
            2 -> listOf(DEFAULT_TIME_MORNING, DEFAULT_TIME_EVENING)
            3 -> listOf(DEFAULT_TIME_MORNING, DEFAULT_TIME_AFTERNOON, DEFAULT_TIME_EVENING)
            else -> List(dailyFrequency) { index ->
                val hour =
                    START_HOUR + (index * (ALLOCATION_WINDOW_HOURS / dailyFrequency.coerceAtLeast(1)))
                String.format(Locale.US, TIME_FORMAT_HOUR_ONLY, hour.coerceIn(MIN_HOUR, MAX_HOUR))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrAlarmDialog(
    medicineName: String,
    dailyFrequency: Int,
    initialSoundUri: String?,
    initialSoundName: String,
    initialAlarmTimes: List<String>? = null,
    onDismiss: () -> Unit,
    onConfirm: (alarmTimes: List<String>, soundUri: String?) -> Unit,
    onSelectDeviceAudio: () -> Unit,
    onSelectRingtone: () -> Unit,
) {

    val defaultTimes = remember(dailyFrequency) {
        AlarmTimeFactory.getDefaultTimes(dailyFrequency)
    }

    val alarmTimes = remember {
        mutableStateListOf<String>().apply {
            if (initialAlarmTimes != null) {
                addAll(initialAlarmTimes)
            } else {
                addAll(defaultTimes)
            }
        }
    }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = stringResource(R.string.alarm_setting_title, medicineName),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(space = 24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 18.dp)
                        )
                        Text(
                            text = stringResource(R.string.alarm_setting_section_time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                        alarmTimes.indices.chunked(MAX_ITEMS_PER_ROW).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                            ) {
                                chunk.forEach { index ->
                                    val time = alarmTimes[index]
                                    val parts = time.split(TIME_DELIMITER)
                                    val hourVal = parts.getOrNull(0) ?: DEFAULT_HOUR_STRING
                                    val minuteVal = parts.getOrNull(1) ?: DEFAULT_MINUTE_STRING

                                    Surface(
                                        modifier = Modifier.weight(weight = 1f),
                                        shape = RoundedCornerShape(size = 12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_BG),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = ALPHA_BORDER_OUTLINE)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
                                        ) {
                                            Text(
                                                text = stringResource(
                                                    R.string.alarm_setting_time_index_short,
                                                    index + 1
                                                ),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                BasicTextField(
                                                    value = hourVal,
                                                    onValueChange = { input ->
                                                        val filtered = input.filter { it.isDigit() }
                                                            .take(MAX_INPUT_LENGTH)
                                                        alarmTimes[index] =
                                                            "$filtered$TIME_DELIMITER$minuteVal"
                                                    },
                                                    textStyle = TextStyle(
                                                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Next
                                                    ),
                                                    singleLine = true,
                                                    modifier = Modifier.width(width = 28.dp)
                                                )
                                                Text(
                                                    text = TIME_DELIMITER,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.padding(horizontal = 2.dp)
                                                )
                                                BasicTextField(
                                                    value = minuteVal,
                                                    onValueChange = { input ->
                                                        val filtered = input.filter { it.isDigit() }
                                                            .take(MAX_INPUT_LENGTH)
                                                        alarmTimes[index] =
                                                            "$hourVal$TIME_DELIMITER$filtered"
                                                    },
                                                    textStyle = TextStyle(
                                                        fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        textAlign = TextAlign.Center
                                                    ),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number,
                                                        imeAction = ImeAction.Done
                                                    ),
                                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                                    singleLine = true,
                                                    modifier = Modifier.width(width = 28.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(MAX_ITEMS_PER_ROW - chunk.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(size = 18.dp)
                        )
                        Text(
                            text = stringResource(R.string.alarm_setting_section_sound),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(size = 14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ALPHA_SURFACE_BG),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = ALPHA_BORDER_OUTLINE)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(all = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = initialSoundName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectRingtone() },
                                    shape = RoundedCornerShape(size = 8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = ALPHA_BORDER_OUTLINE)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.alarm_setting_btn_ringtone_short),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectDeviceAudio() },
                                    shape = RoundedCornerShape(size = 8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = ALPHA_BORDER_OUTLINE)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.alarm_setting_btn_device_audio_short),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val allValid = alarmTimes.all { time ->
                        val parts = time.split(TIME_DELIMITER)
                        val hour = parts.getOrNull(0)?.toIntOrNull()
                        val minute = parts.getOrNull(1)?.toIntOrNull()
                        hour != null && hour in MIN_HOUR..MAX_HOUR && minute != null && minute in MIN_MINUTE..MAX_MINUTE
                    }
                    if (allValid) {
                        val formattedTimes = alarmTimes.map { time ->
                            val parts = time.split(TIME_DELIMITER)
                            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            String.format(Locale.US, TIME_FORMAT, hour, minute)
                        }
                        onConfirm(formattedTimes, initialSoundUri)
                    } else {
                        context.showToast(R.string.error_invalid_time_format)
                    }
                },
                shape = RoundedCornerShape(size = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary, contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_confirm),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(size = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighlightCoral, contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        shape = RoundedCornerShape(size = 24.dp),
        properties = DialogProperties(usePlatformDefaultWidth = true)
    )
}
