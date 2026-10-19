package io.github.koolunkle.yakallim.ui.ocr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.domain.model.JobStatus
import io.github.koolunkle.yakallim.domain.model.PrescribedMedicine
import io.github.koolunkle.yakallim.ui.alarm.AlarmEvent
import io.github.koolunkle.yakallim.ui.alarm.AlarmUiState
import io.github.koolunkle.yakallim.ui.alarm.AlarmViewModel
import io.github.koolunkle.yakallim.ui.alarm.AlarmSettingSheet
import io.github.koolunkle.yakallim.ui.camera.CameraScreen
import io.github.koolunkle.yakallim.ui.ocr.components.OcrActionButton
import io.github.koolunkle.yakallim.ui.ocr.components.OcrErrorCard
import io.github.koolunkle.yakallim.ui.ocr.components.OcrGuideBottomSheet
import io.github.koolunkle.yakallim.ui.ocr.components.OcrImageViewer
import io.github.koolunkle.yakallim.ui.ocr.components.OcrLoadingContent
import io.github.koolunkle.yakallim.ui.ocr.components.OcrMedicineCard
import io.github.koolunkle.yakallim.ui.ocr.components.OcrResetButton
import io.github.koolunkle.yakallim.ui.ocr.components.OcrSourcePicker
import io.github.koolunkle.yakallim.ui.ocr.components.OcrTopBar
import io.github.koolunkle.yakallim.util.checkAlarmPermissions
import io.github.koolunkle.yakallim.util.getFileName
import io.github.koolunkle.yakallim.util.hasPermission
import io.github.koolunkle.yakallim.util.openExactAlarmSettings
import io.github.koolunkle.yakallim.util.requiredNotificationPermission
import io.github.koolunkle.yakallim.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OcrScreen(
    modifier: Modifier = Modifier,
    viewModel: OcrViewModel = hiltViewModel(),
    alarmViewModel: AlarmViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()
    val alarmUiState by alarmViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val lazyListState = rememberLazyListState()

    val showCamera = remember { mutableStateOf(false) }
    val showGuideSheet = remember { mutableStateOf(false) }

    val unknownMedicineLabel = stringResource(R.string.error_unknown_medicine)
    val alarmSettingSoundTitle = stringResource(R.string.alarm_sheet_section_sound)
    val resources = LocalResources.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppForeground()
                alarmViewModel.refreshActiveAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel, alarmViewModel) {
        viewModel.analysisStartedEvents.collect {
            alarmViewModel.clearAllRegisteredAlarms()
        }
    }

    LaunchedEffect(alarmViewModel) {
        alarmViewModel.events.collect { event ->
            when (event) {
                is AlarmEvent.Registered -> {
                    viewModel.updateMedicineDosage(
                        event.medicineId, event.dosagePerTake, event.dailyFrequency, event.durationDays
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = resources.getString(R.string.alarm_snack_turned_on, event.medicineName),
                        duration = SnackbarDuration.Short
                    )
                }

                is AlarmEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) context.showToast(R.string.alarm_permission_denied)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) showCamera.value =
                true else context.showToast(R.string.camera_permission_required)
        }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { viewModel.onImageSelected(it) }
    }

    // 시트는 약 id로 연다. 같은 이름의 약이 여러 개여도 누른 카드의 값이 들어가게 한다.
    val sheetMedicineId = remember { mutableStateOf<String?>(null) }
    val selectedSoundUri = remember { mutableStateOf<String?>(null) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(), onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.let { intent ->
                    IntentCompat.getParcelableExtra(
                        intent, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                    )
                }
                selectedSoundUri.value = uri?.toString()
            }
        })

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                selectedSoundUri.value = it.toString()
            }
        })

    if (showCamera.value) {
        CameraScreen(
            onCaptureSuccess = { viewModel.onImageCaptured(it); showCamera.value = false },
            onDismiss = { showCamera.value = false }
        )
        return
    }

    val onMedicineAlarmClick = { medicine: PrescribedMedicine ->
        context.checkAlarmPermissions(onNotificationRequired = {
            requiredNotificationPermission?.let { notificationLauncher.launch(it) }
        }, onExactAlarmRequired = {
            context.showToast(R.string.alarm_permission_required, Toast.LENGTH_LONG)
            context.openExactAlarmSettings()
        }, onAllGranted = {
            val name = medicine.name ?: unknownMedicineLabel
            selectedSoundUri.value = alarmUiState.registeredAlarms[name]?.soundUri
            sheetMedicineId.value = medicine.id
        })
    }

    OcrScreenContent(
        modifier = modifier,
        uiState = uiState,
        alarmUiState = alarmUiState,
        snackbarHostState = snackbarHostState,
        lazyListState = lazyListState,
        isGuideSheetVisible = showGuideSheet.value, unknownMedicineLabel = unknownMedicineLabel,
        onGuideClick = { showGuideSheet.value = true },
        onGuideDismissRequest = { showGuideSheet.value = false },
        onCameraCaptureClick = {
            if (context.hasPermission(Manifest.permission.CAMERA)) {
                showCamera.value = true
            } else {
                cameraLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onGallerySelectClick = { galleryLauncher.launch("image/*") },
        onStartAnalysisClick = viewModel::retryAnalysis,
        onCancelAnalysisClick = viewModel::onAnalysisCancelRequested,
        onMedicineAlarmClick = onMedicineAlarmClick,
        onResetAnalysisClick = {
            viewModel.resetAnalysisResult()
            alarmViewModel.clearAllRegisteredAlarms()
        }
    )

    val sheetMedicine = sheetMedicineId.value?.let { id ->
        uiState.analysisResult?.medicines?.firstOrNull { it.id == id }
    }
    sheetMedicine?.let { medicine ->
        val name = medicine.name ?: unknownMedicineLabel
        val existingAlarm = alarmUiState.registeredAlarms[name]
        AlarmSettingSheet(
            medicineName = name,
            initialDosagePerTake = medicine.dosagePerTake,
            initialFrequency = medicine.dailyFrequency,
            initialDuration = medicine.durationDays,
            initialTimes = existingAlarm?.times,
            soundUri = selectedSoundUri.value,
            soundName = alarmSoundName(selectedSoundUri.value),
            isRegistered = existingAlarm != null,
            onSelectRingtone = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, alarmSettingSoundTitle)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    selectedSoundUri.value?.let {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it.toUri())
                    }
                }
                ringtonePickerLauncher.launch(intent)
            },
            onSelectDeviceAudio = { audioPickerLauncher.launch(arrayOf("audio/*")) },
            onConfirm = { result ->
                alarmViewModel.registerMedicineAlarm(
                    medicine.id,
                    name,
                    result.dosagePerTake,
                    result.dailyFrequency,
                    result.durationDays,
                    result.alarmTimes,
                    selectedSoundUri.value
                )
                sheetMedicineId.value = null
            },
            onTurnOff = {
                sheetMedicineId.value = null
                existingAlarm?.let { alarm ->
                    alarmViewModel.unregisterMedicineAlarm(name)
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = resources.getString(R.string.alarm_snack_turned_off, name),
                            actionLabel = resources.getString(R.string.alarm_snack_undo),
                            duration = SnackbarDuration.Long
                        )
                        // 끄기 전 값으로 다시 켠다. 카드의 복용 정보는 그대로라 같은 값이 들어간다.
                        if (result == SnackbarResult.ActionPerformed) {
                            alarmViewModel.registerMedicineAlarm(
                                medicine.id,
                                name,
                                medicine.dosagePerTake.orEmpty(),
                                medicine.dailyFrequency ?: alarm.times.size,
                                medicine.durationDays ?: 1,
                                alarm.times,
                                alarm.soundUri
                            )
                        }
                    }
                }
            },
            onDismiss = { sheetMedicineId.value = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OcrScreenContent(
    modifier: Modifier,
    uiState: OcrUiState,
    alarmUiState: AlarmUiState,
    snackbarHostState: SnackbarHostState,
    lazyListState: LazyListState,
    isGuideSheetVisible: Boolean, unknownMedicineLabel: String,
    onGuideClick: () -> Unit,
    onGuideDismissRequest: () -> Unit,
    onCameraCaptureClick: () -> Unit,
    onGallerySelectClick: () -> Unit,
    onResetAnalysisClick: () -> Unit,
    onStartAnalysisClick: () -> Unit,
    onCancelAnalysisClick: () -> Unit,
    onMedicineAlarmClick: (PrescribedMedicine) -> Unit
) {
    Scaffold(
        topBar = { OcrTopBar(onGuideButtonClick = onGuideClick) },
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isGuideSheetVisible) OcrGuideBottomSheet(onDismiss = onGuideDismissRequest)

        if (!uiState.isInitialized || !alarmUiState.isInitialized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        val hasResult = uiState.analysisResult != null && uiState.analysisResult.medicines.isNotEmpty()

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(height = 8.dp))
                if (uiState.isLoading) {
                    val progress = uiState.progress ?: OcrProgress()
                    OcrLoadingContent(
                        progress = progress.percent,
                        message = progress.message,
                        isSseActive = progress.isSseActive,
                        isIndeterminate = progress.jobStatus == JobStatus.ANALYZING,
                        onCancelClick = onCancelAnalysisClick
                    )
                } else {
                    OcrImageViewer(image = uiState.selectedImage)
                }
            }

            if (!hasResult && !uiState.isLoading) {
                item { OcrSourcePicker(onCameraCaptureClick, onGallerySelectClick, true) }
                item {
                OcrActionButton(
                    isLoading = false,
                    enabled = uiState.hasImage || uiState.error != null,
                    hasError = uiState.error != null,
                    onStartAnalysisClick = onStartAnalysisClick
                )
                }
            }

            uiState.error?.let { item { OcrErrorCard(it) } }

            uiState.analysisResult?.let { result ->
                if (result.medicines.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                            OcrResetButton(onResetAnalysisClick)
                            Text(
                                text = stringResource(R.string.ocr_result_verify_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                            // 서버는 생략 여부를 알 수 없어, 결과가 있을 때 항상 안내한다.
                            Text(
                                text = stringResource(R.string.ocr_result_omitted_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    items(result.medicines, key = { it.id }) { medicine ->
                        val name = medicine.name ?: unknownMedicineLabel
                        val alarm = alarmUiState.registeredAlarms[name]
                        val alarmSoundName = if (alarm != null) {
                            alarmSoundName(alarm.soundUri)
                        } else {
                            stringResource(R.string.alarm_sound_default)
                        }
                        OcrMedicineCard(
                            medicine = medicine,
                            displayName = name,
                            alarm = alarm,
                            alarmSoundName = alarmSoundName,
                            onAlarmClick = { onMedicineAlarmClick(medicine) },
                            modifier = Modifier.testTag(medicineCardTag(medicine.id))
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(height = 32.dp)) }
        }
    }
}

internal fun medicineCardTag(medicineId: String) = "medicineCard:$medicineId"

@Composable
private fun alarmSoundName(soundUri: String?): String {
    val context = LocalContext.current
    var systemSoundName by remember(soundUri) { mutableStateOf<String?>(null) }

    LaunchedEffect(soundUri) {
        if (soundUri != null) {
            withContext(Dispatchers.IO) {
                val uriObj = soundUri.toUri()
                val ringtone = RingtoneManager.getRingtone(context, uriObj)
                val title = ringtone?.getTitle(context) ?: context.getFileName(uriObj)
                systemSoundName = title
            }
        } else {
            systemSoundName = null
        }
    }

    return when {
        soundUri == null -> stringResource(R.string.alarm_sound_default)
        systemSoundName != null -> systemSoundName!!
        else -> stringResource(R.string.alarm_sound_device)
    }
}
