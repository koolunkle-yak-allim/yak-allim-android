package com.example.yakallim.ui.ocr

import android.content.Context
import android.net.Uri
import com.example.yakallim.domain.model.JobStatus
import com.example.yakallim.domain.model.Prescription
import com.example.yakallim.domain.model.PrescribedMedicine
import com.example.yakallim.domain.model.Progress
import com.example.yakallim.domain.notification.PushMessage
import com.example.yakallim.domain.usecase.CancelPrescriptionUseCase
import com.example.yakallim.domain.usecase.ClearLastPrescriptionUseCase
import com.example.yakallim.domain.usecase.GetCachedImageUriUseCase
import com.example.yakallim.domain.usecase.GetLastPrescriptionUseCase
import com.example.yakallim.domain.usecase.GetPendingPrescriptionUseCase
import com.example.yakallim.domain.usecase.GetPrescriptionResultUseCase
import com.example.yakallim.domain.usecase.ObserveProgressUseCase
import com.example.yakallim.domain.usecase.PrepareImageUseCase
import com.example.yakallim.domain.usecase.RequestPrescriptionUseCase
import com.example.yakallim.testutil.FakeImageProcessor
import com.example.yakallim.testutil.FakeOcrRepository
import com.example.yakallim.testutil.FakePushNotificationObserver
import com.example.yakallim.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class OcrViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeOcrRepository
    private lateinit var fakeImageProcessor: FakeImageProcessor
    private lateinit var fakePushObserver: FakePushNotificationObserver

    @Before
    fun setUp() {
        fakeRepository = FakeOcrRepository()
        fakeImageProcessor = FakeImageProcessor()
        fakePushObserver = FakePushNotificationObserver()
    }

    private fun createViewModel(): OcrViewModel {
        val context: Context = mock {
            on { getString(any()) } doReturn "stubbed message"
        }
        return OcrViewModel(
            requestPrescriptionUseCase = RequestPrescriptionUseCase(fakeRepository),
            getPrescriptionResultUseCase = GetPrescriptionResultUseCase(fakeRepository),
            getPendingPrescriptionUseCase = GetPendingPrescriptionUseCase(fakeRepository),
            getLastPrescriptionUseCase = GetLastPrescriptionUseCase(fakeRepository),
            clearLastPrescriptionUseCase = ClearLastPrescriptionUseCase(fakeRepository),
            cancelPrescriptionUseCase = CancelPrescriptionUseCase(fakeRepository),
            observeProgressUseCase = ObserveProgressUseCase(fakeRepository),
            prepareImageUseCase = PrepareImageUseCase(fakeImageProcessor),
            getCachedImageUriUseCase = GetCachedImageUriUseCase(fakeRepository),
            pushNotificationObserver = fakePushObserver.observer,
            context = context
        )
    }

    private fun samplePrescription() = Prescription(
        rawText = "타이레놀정 1정 1일 3회 5일분",
        medicines = listOf(
            PrescribedMedicine(
                id = "medicine-1",
                name = "타이레놀정",
                dosagePerTake = "1정",
                dailyFrequency = 3,
                durationDays = 5
            )
        )
    )

    @Test
    fun onAnalysisRequested_whenSseReportsCompleted_fetchesAndStoresAnalysisResult() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.progressFlow = flowOf(
            Progress(JobStatus.ENQUEUED, "", 10, false),
            Progress(JobStatus.TEXT_DETECTION, "", 50, false),
            Progress(JobStatus.COMPLETED, "", 100, true)
        )
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.analysisResult?.medicines?.size)
        assertEquals("job-1", fakeRepository.fetchedResultJobIds.last())
    }

    @Test
    fun onAnalysisRequested_whenSseNeverFinishes_fallsBackToPushNotification() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // SSE completes without ever reporting a finished status (connection dropped).
        fakeRepository.progressFlow = emptyFlow()
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        // At this point the ViewModel should be suspended waiting on a push notification.
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.progress?.isSseActive ?: true)

        fakePushObserver.emit(PushMessage(jobId = "job-1", status = JobStatus.COMPLETED))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.analysisResult)
    }

    @Test
    fun onAnalysisRequested_whenPushNotificationReportsFailure_setsServerError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.progressFlow = emptyFlow()

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        fakePushObserver.emit(PushMessage(jobId = "job-1", status = JobStatus.FAILED, errorMessage = "OCR 실패"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error is OcrError.ServerError)
        assertEquals("OCR 실패", (state.error as OcrError.ServerError).message)
    }

    @Test
    fun onAppForeground_whenPendingJobExists_recoversIt() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.pendingPrescriptionJobId = "job-pending"
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        viewModel.onAppForeground()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("job-pending", fakeRepository.fetchedResultJobIds.last())
        assertNotNull(state.analysisResult)
    }

    @Test
    fun onAnalysisCancelRequested_duringActiveAnalysis_stopsAnalysisAndShowsCancelledError() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // A progress flow that only resolves after a long delay, simulating an in-flight analysis.
        fakeRepository.progressFlow = flow {
            emit(Progress(JobStatus.ENQUEUED, "", 10, false))
            delay(60_000)
            emit(Progress(JobStatus.COMPLETED, "", 100, true))
        }

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceTimeBy(100)
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoading)

        // onImageSelected() also stops any prior analysis, so capture the baseline here
        // rather than assuming this is the first cancellation of the test.
        val cancelCallsBeforeCancellation = fakeRepository.cancelPrescriptionCallCount

        viewModel.onAnalysisCancelRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.error is OcrError.Unknown)
        assertNull(state.progress)
        assertEquals(cancelCallsBeforeCancellation + 1, fakeRepository.cancelPrescriptionCallCount)
        // The cancelled coroutine must not resume and overwrite the cancelled state.
        assertNull(state.analysisResult)
    }

    @Test
    fun updateMedicineDosage_updatesOnlyTheMatchingMedicineInAnalysisResult() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.progressFlow = flowOf(Progress(JobStatus.COMPLETED, "", 100, true))
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))
        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        // AlarmViewModel calls this after a successful registration to reflect the
        // dosage/frequency/duration the user confirmed in the alarm dialog.
        viewModel.updateMedicineDosage(
            medicineId = "medicine-1",
            dosagePerTake = "2정",
            dailyFrequency = 2,
            durationDays = 7
        )

        val medicine = viewModel.uiState.value.analysisResult?.medicines?.single()
        assertEquals("2정", medicine?.dosagePerTake)
        assertEquals(2, medicine?.dailyFrequency)
        assertEquals(7, medicine?.durationDays)
    }

    @Test
    fun onAnalysisRequested_whenAnalysisActuallyStarts_emitsAnalysisStartedEvent() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.progressFlow = flowOf(Progress(JobStatus.COMPLETED, "", 100, true))
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        var eventCount = 0
        val collectorJob = launch { viewModel.analysisStartedEvents.collect { eventCount++ } }

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        assertEquals(1, eventCount)
        collectorJob.cancel()
    }
}
