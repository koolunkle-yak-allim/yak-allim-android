package com.example.yakallim.ui.ocr

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
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.yakallim.R
import com.example.yakallim.ui.alarm.AlarmEvent
import com.example.yakallim.ui.alarm.AlarmUiState
import com.example.yakallim.ui.alarm.AlarmViewModel
import com.example.yakallim.ui.alarm.PendingAlarm
import com.example.yakallim.ui.camera.CameraScreen
import com.example.yakallim.ui.ocr.components.OcrActionButton
import com.example.yakallim.ui.ocr.components.OcrAlarmDialog
import com.example.yakallim.ui.ocr.components.OcrErrorCard
import com.example.yakallim.ui.ocr.components.OcrExpansionControls
import com.example.yakallim.ui.ocr.components.OcrGuideBottomSheet
import com.example.yakallim.ui.ocr.components.OcrImageViewer
import com.example.yakallim.ui.ocr.components.OcrLoadingContent
import com.example.yakallim.ui.ocr.components.OcrMedicineCard
import com.example.yakallim.ui.ocr.components.OcrResetButton
import com.example.yakallim.ui.ocr.components.OcrSourcePicker
import com.example.yakallim.ui.ocr.components.OcrTopBar
import com.example.yakallim.util.checkAlarmPermissions
import com.example.yakallim.util.getFileName
import com.example.yakallim.util.hasPermission
import com.example.yakallim.util.openExactAlarmSettings
import com.example.yakallim.util.requiredNotificationPermission
import com.example.yakallim.util.showToast
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
    val density = LocalDensity.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()
    val alarmUiState by alarmViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val highlightedMedicineName = remember { mutableStateOf<String?>(null) }
    val showCamera = remember { mutableStateOf(false) }
    val showGuideSheet = remember { mutableStateOf(false) }

    val unknownMedicineLabel = stringResource(R.string.error_unknown_medicine)
    val alarmSettingSoundTitle = stringResource(R.string.alarm_setting_section_sound)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onAppForeground()
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
                is AlarmEvent.Registered -> viewModel.updateMedicineDosage(
                    event.medicineId, event.dosagePerTake, event.dailyFrequency, event.durationDays
                )

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

    val pendingAlarmData = remember { mutableStateOf<PendingAlarm?>(null) }
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

    val onRegisterAlarm =
        { medicineId: String, medicineName: String, dosagePerTake: String, dailyFrequency: Int, durationDays: Int ->
            context.checkAlarmPermissions(onNotificationRequired = {
                requiredNotificationPermission?.let { notificationLauncher.launch(it) }
            }, onExactAlarmRequired = {
                context.showToast(R.string.alarm_permission_required, Toast.LENGTH_LONG)
                context.openExactAlarmSettings()
            }, onAllGranted = {
                val existingDetail = alarmUiState.registeredAlarms[medicineName]
                selectedSoundUri.value = existingDetail?.soundUri
                pendingAlarmData.value = PendingAlarm(
                    medicineId, medicineName, dosagePerTake, dailyFrequency, durationDays
                )
            })
        }

    OcrScreenContent(
        modifier = modifier,
        uiState = uiState,
        alarmUiState = alarmUiState,
        snackbarHostState = snackbarHostState,
        lazyListState = lazyListState,
        highlightedMedicineName = highlightedMedicineName.value,
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
        onRegisterAlarmClick = onRegisterAlarm,
        onCancelAlarmClick = alarmViewModel::unregisterMedicineAlarm,
        onResetAnalysisClick = {
            viewModel.resetAnalysisResult()
            alarmViewModel.clearAllRegisteredAlarms()
        },
        onMedicineTextClick = { name ->
            val result = uiState.analysisResult
            val medicines = result?.medicines ?: emptyList()
            val matchedMedication =
                medicines.find { (it.name ?: unknownMedicineLabel) == name }

            highlightedMedicineName.value = matchedMedication?.name ?: unknownMedicineLabel

            val index = medicines.indexOf(matchedMedication)
            if (index != -1) {
                coroutineScope.launch {
                    val hasResult = result != null && result.medicines.isNotEmpty()
                    var target = 1

                    if (!hasResult || uiState.isLoading) target += 2
                    if (uiState.error != null) target += 1
                    if (hasResult) target += 1

                    val offset = with(density) { (-16).dp.roundToPx() }
                    lazyListState.animateScrollToItem(target + index, offset)
                }
            }
        },
        onToggleCardExpansion = viewModel::toggleCardExpansion,
        onToggleAllCardsExpansion = viewModel::setAllCardsExpansion
    )

    pendingAlarmData.value?.let { pendingData ->
        OcrAlarmDialog(
            medicineName = pendingData.medicineName,
            dailyFrequency = pendingData.dailyFrequency,
            initialSoundUri = selectedSoundUri.value,
            initialSoundName = alarmSoundName(selectedSoundUri.value),
            initialAlarmTimes = alarmUiState.registeredAlarms[pendingData.medicineName]?.times,
            onDismiss = {
                pendingAlarmData.value = null
            },
            onConfirm = { alarmTimes, soundUri ->
                alarmViewModel.registerMedicineAlarm(
                    pendingData.medicineId,
                    pendingData.medicineName,
                    pendingData.dosagePerTake,
                    pendingData.dailyFrequency,
                    pendingData.durationDays,
                    alarmTimes,
                    soundUri
                )
                pendingAlarmData.value = null
            },
            onSelectDeviceAudio = {
                audioPickerLauncher.launch(arrayOf("audio/*"))
            },
            onSelectRingtone = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(
                        RingtoneManager.EXTRA_RINGTONE_TITLE, alarmSettingSoundTitle
                    )
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    selectedSoundUri.value?.let {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, it.toUri())
                    }
                }
                ringtonePickerLauncher.launch(intent)
            })
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
    highlightedMedicineName: String?,
    isGuideSheetVisible: Boolean, unknownMedicineLabel: String,
    onGuideClick: () -> Unit,
    onGuideDismissRequest: () -> Unit,
    onCameraCaptureClick: () -> Unit,
    onGallerySelectClick: () -> Unit,
    onResetAnalysisClick: () -> Unit,
    onStartAnalysisClick: () -> Unit,
    onCancelAnalysisClick: () -> Unit,
    onMedicineTextClick: (String) -> Unit, onRegisterAlarmClick: (String, String, String, Int, Int) -> Unit,
    onCancelAlarmClick: (String) -> Unit,
    onToggleCardExpansion: (String) -> Unit,
    onToggleAllCardsExpansion: (Boolean) -> Unit
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
        val isAllExpanded = uiState.cardExpansionMap.values.all { it } && uiState.cardExpansionMap.isNotEmpty()

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
                        onCancelClick = onCancelAnalysisClick
                    )
                } else {
                    OcrImageViewer(
                        image = uiState.selectedImage,
                        analysisResult = uiState.analysisResult,
                        registeredAlarmMedicineNames = alarmUiState.registeredAlarms.keys,
                        onMedicineTextClick = onMedicineTextClick
                    )
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
                            OcrExpansionControls(isAllExpanded, onToggleAllCardsExpansion)
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
                            medicineInfo = medicine,
                            isAlarmRegistered = alarmUiState.registeredAlarms.containsKey(
                                name
                            ),
                            alarm = alarm,
                            alarmSoundName = alarmSoundName,
                            highlightedMedicineName = highlightedMedicineName,
                            isCardExpanded = uiState.cardExpansionMap[medicine.id] ?: true,
                            onToggleExpansionClick = { onToggleCardExpansion(medicine.id) },
                            onRegisterAlarmClick = onRegisterAlarmClick,
                            onCancelAlarmClick = onCancelAlarmClick
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(height = 32.dp)) }
        }
    }
}

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
