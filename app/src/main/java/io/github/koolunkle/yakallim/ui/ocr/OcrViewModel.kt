package io.github.koolunkle.yakallim.ui.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.koolunkle.yakallim.R
import io.github.koolunkle.yakallim.domain.notification.PushNotificationObserver
import io.github.koolunkle.yakallim.domain.usecase.RequestPrescriptionUseCase
import io.github.koolunkle.yakallim.domain.usecase.CancelPrescriptionUseCase
import io.github.koolunkle.yakallim.domain.usecase.ClearLastPrescriptionUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetCachedImageUriUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetPrescriptionResultUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetLastPrescriptionUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetPendingPrescriptionUseCase
import io.github.koolunkle.yakallim.domain.usecase.PrepareImageUseCase
import io.github.koolunkle.yakallim.domain.usecase.ObserveProgressUseCase
import io.github.koolunkle.yakallim.domain.model.JobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val requestPrescriptionUseCase: RequestPrescriptionUseCase,
    private val getPrescriptionResultUseCase: GetPrescriptionResultUseCase,
    private val getPendingPrescriptionUseCase: GetPendingPrescriptionUseCase,
    private val getLastPrescriptionUseCase: GetLastPrescriptionUseCase,
    private val clearLastPrescriptionUseCase: ClearLastPrescriptionUseCase,
    private val cancelPrescriptionUseCase: CancelPrescriptionUseCase,
    private val observeProgressUseCase: ObserveProgressUseCase,
    private val prepareImageUseCase: PrepareImageUseCase,
    private val getCachedImageUriUseCase: GetCachedImageUriUseCase,
    private val pushNotificationObserver: PushNotificationObserver,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    /** 새 분석이 실제로 시작될 때(hasImage 검증을 통과한 뒤) 발행된다. 등록된 알람 정리는 AlarmViewModel의 몫이라, 화면이 이 이벤트를 받아 위임한다. */
    private val _analysisStartedEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val analysisStartedEvents: SharedFlow<Unit> = _analysisStartedEvents.asSharedFlow()

    private var job: Job? = null
    private var fetchJob: Job? = null
    private var activeJobId: String? = null

    init {
        viewModelScope.launch {
            recoverActiveJob()
            _uiState.update { it.copy(isInitialized = true) }
        }
    }

    private suspend fun recoverActiveJob() {
        if (_uiState.value.hasImage && !_uiState.value.isLoading) {
            return
        }
        val jobId = getPendingPrescriptionUseCase()
        if (!jobId.isNullOrBlank() && jobId != activeJobId) {
            activeJobId = jobId
            fetchAnalysisResult(jobId)
        } else if (jobId.isNullOrBlank()) {
            recoverLastPrescription()
        }
    }

    private suspend fun recoverLastPrescription() {
        val lastPrescription = getLastPrescriptionUseCase()
        if (lastPrescription != null) {
            val restoredUri = getCachedImageUriUseCase.last()
            _uiState.update { state ->
                val initialExpanded = lastPrescription.medicines.associate { medicine ->
                    medicine.id to false
                }
                state.copy(
                    analysisResult = lastPrescription,
                    selectedImage = restoredUri?.let { OcrImage.UriSource(it) },
                    cardExpansionMap = initialExpanded
                )
            }
        }
    }

    /** 완료 알림을 눌러 화면이 열렸을 때 호출된다. 알림이 아닌 실행이면 jobId가 null이라 아무것도 하지 않는다. */
    fun onNotificationOpened(jobId: String?) {
        if (!jobId.isNullOrBlank() && jobId != activeJobId) {
            activeJobId = jobId
            fetchAnalysisResult(jobId)
        }
    }

    fun onImageSelected(uri: Uri) {
        stopActiveAnalysis()
        activeJobId = null
        _uiState.update {
            it.copy(selectedImage = OcrImage.UriSource(uri), analysisResult = null, error = null)
        }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        stopActiveAnalysis()
        activeJobId = null
        _uiState.update {
            it.copy(selectedImage = OcrImage.BitmapSource(bitmap), analysisResult = null, error = null)
        }
    }
    fun resetAnalysisResult() {
        stopActiveAnalysis()
        activeJobId = null
        viewModelScope.launch {
            clearLastPrescriptionUseCase()
        }
        _uiState.update {
            it.copy(
                analysisResult = null,
                error = null,
                selectedImage = null,
                cardExpansionMap = emptyMap(),
                progress = null
            )
        }
    }

    fun onAnalysisRequested() {
        val currentState = _uiState.value
        if (!currentState.hasImage) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = OcrError.Unknown(context.getString(R.string.error_failed_image_selected))
                )
            }
            return
        }

        activeJobId = null
        job?.cancel()
        job = viewModelScope.launch {
            _analysisStartedEvents.emit(Unit)
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    analysisResult = null,
                    cardExpansionMap = emptyMap(),
                    progress = OcrProgress(
                        jobStatus = JobStatus.ENQUEUED,
                        percent = 3,
                        message = context.getString(R.string.ocr_status_uploading),
                        isSseActive = true
                    )
                )
            }

            val file = when (val image = currentState.selectedImage) {
                is OcrImage.UriSource -> prepareImageUseCase.fromUri(image.uri)
                is OcrImage.BitmapSource -> prepareImageUseCase.fromBitmap(image.bitmap)
                null -> null
            }
            if (file == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = OcrError.Unknown(context.getString(R.string.error_failed_image_fetched))
                    )
                }
                return@launch
            }

            val jobId = try {
                requestPrescriptionUseCase(file)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toOcrError()
                    )
                }
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    progress = state.progress?.copy(
                        percent = 5
                    )
                )
            }

            val finalProgress = try {
                observeProgressUseCase(jobId)
                    .onEach { progress ->
                        _uiState.update { state ->
                            val currentProgress = state.progress
                            state.copy(
                                progress = currentProgress?.copy(
                                    jobStatus = if (progress.jobStatus == JobStatus.UNKNOWN) {
                                        currentProgress.jobStatus
                                    } else {
                                        progress.jobStatus
                                    },
                                    percent = progress.percent,
                                    message = progress.message
                                )
                            )
                        }
                    }
                    .firstOrNull { it.isFinished }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }

            val isOcrCompleted = finalProgress != null
            if (finalProgress != null) {
                when (finalProgress.jobStatus) {
                    JobStatus.COMPLETED -> fetchAnalysisResult(jobId)
                    JobStatus.CANCELLED -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = OcrError.Unknown(context.getString(R.string.ocr_status_cancelled)),
                            progress = null
                        )
                    }
                    // 서버는 실패 시 사용자에게 그대로 보여줄 수 있는 메시지를 보낸다.
                    else -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = finalProgress.message.takeIf { message -> message.isNotBlank() }
                                ?.let { message -> OcrError.ServerError(message) }
                                ?: OcrError.AnalysisFailed
                        )
                    }
                }
            }

            if (!isOcrCompleted) {
                _uiState.update { state ->
                    state.copy(
                        progress = state.progress?.copy(
                            isSseActive = false,
                            message = context.getString(R.string.ocr_status_network_unstable)
                        )
                    )
                }
                val pushMessage = try {
                    pushNotificationObserver.messages.filter { it.jobId == jobId }
                        .first()
                } catch (ex: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ex.toOcrError()
                        )
                    }
                    return@launch
                }

                if (pushMessage.status == JobStatus.COMPLETED) {
                    fetchAnalysisResult(jobId)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = if (pushMessage.errorMessage != null) OcrError.ServerError(pushMessage.errorMessage) else OcrError.AnalysisFailed
                        )
                    }
                }
            }
        }
    }

    fun retryAnalysis() {
        val jobId = activeJobId
        if (!jobId.isNullOrBlank()) {
            fetchAnalysisResult(jobId)
        } else {
            onAnalysisRequested()
        }
    }

    fun fetchAnalysisResult(jobId: String) {
        activeJobId = jobId
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val restoredUri = getCachedImageUriUseCase(jobId)

            getPrescriptionResultUseCase(jobId).collect { result ->
                result.onSuccess { analysisResult ->
                    val isValid = analysisResult.medicines.isNotEmpty()

                    _uiState.update { state ->
                        val initialExpanded = if (isValid) {
                            analysisResult.medicines.associate { medicine -> medicine.id to false }
                        } else emptyMap()
                        
                        state.copy(
                            isLoading = false,
                            analysisResult = if (isValid) analysisResult else null,
                            selectedImage = restoredUri?.let { OcrImage.UriSource(it) } ?: state.selectedImage,
                            cardExpansionMap = initialExpanded,
                            error = if (isValid) null else OcrError.EmptyResult
                        )
                    }
                }.onFailure { exception ->
                    if (exception is CancellationException) throw exception
                    if (exception.isJobNotFound()) {
                        // 서버에 더는 존재하지 않는 작업(재배포 등으로 상태 소실)이므로,
                        // 같은 jobId로의 재조회 대신 다음 재시도가 새 작업 제출로 이어지도록 초기화한다.
                        activeJobId = null
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.toOcrError()
                        )
                    }
                }
            }
        }
    }

    fun onAppForeground() {
        viewModelScope.launch {
            recoverActiveJob()
        }
    }

    /** AlarmViewModel이 알람 등록에 성공했을 때, 화면이 이 값으로 처방 카드의 복용 정보 표시를 갱신한다. */
    fun updateMedicineDosage(
        medicineId: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int
    ) {
        _uiState.update { state ->
            val updatedResult = state.analysisResult?.let { prescription ->
                prescription.copy(
                    medicines = prescription.medicines.map { medicine ->
                        if (medicine.id == medicineId) {
                            medicine.copy(
                                dosagePerTake = dosagePerTake,
                                dailyFrequency = dailyFrequency,
                                durationDays = durationDays
                            )
                        } else medicine
                    }
                )
            }
            state.copy(analysisResult = updatedResult)
        }
    }

    fun onAnalysisCancelRequested() {
        stopActiveAnalysis()
        _uiState.update {
            it.copy(
                isLoading = false,
                error = OcrError.Unknown(context.getString(R.string.ocr_status_cancelled)),
                progress = null
            )
        }
    }

    fun toggleCardExpansion(medicineId: String) {
        _uiState.update {
            val current = it.cardExpansionMap[medicineId] ?: false
            it.copy(cardExpansionMap = it.cardExpansionMap + (medicineId to !current))
        }
    }

    fun setAllCardsExpansion(expanded: Boolean) {
        _uiState.update { state ->
            val newExpanded = state.analysisResult?.medicines?.associate { medicine ->
                medicine.id to expanded
            } ?: emptyMap()
            state.copy(cardExpansionMap = newExpanded)
        }
    }

    private fun stopActiveAnalysis() {
        job?.cancel()
        viewModelScope.launch {
            cancelPrescriptionUseCase()
        }
    }

    private fun Throwable.isJobNotFound(): Boolean = this is HttpException && code() == 404

    private fun Throwable.toOcrError(): OcrError {
        val msg = this.localizedMessage ?: ""
        return when {
            this is NoSuchElementException -> OcrError.EmptyResult
            isJobNotFound() -> OcrError.AnalysisFailed
            msg.contains("timeout", ignoreCase = true) -> OcrError.Timeout
            msg.contains("connect", ignoreCase = true) || msg.contains("network", ignoreCase = true) -> OcrError.Network
            else -> OcrError.Unknown(msg)
        }
    }

}
