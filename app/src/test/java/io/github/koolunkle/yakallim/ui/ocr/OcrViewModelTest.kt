package io.github.koolunkle.yakallim.ui.ocr

import android.content.Context
import android.net.Uri
import io.github.koolunkle.yakallim.domain.model.JobStatus
import io.github.koolunkle.yakallim.domain.model.Prescription
import io.github.koolunkle.yakallim.domain.model.PrescribedMedicine
import io.github.koolunkle.yakallim.domain.model.Progress
import io.github.koolunkle.yakallim.domain.notification.PushMessage
import io.github.koolunkle.yakallim.domain.usecase.CancelAnalysisUseCase
import io.github.koolunkle.yakallim.domain.usecase.ClearLastAnalysisResultUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetCachedImageUriUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetLastAnalysisResultUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetPendingAnalysisJobIdUseCase
import io.github.koolunkle.yakallim.domain.usecase.GetAnalysisResultUseCase
import io.github.koolunkle.yakallim.domain.usecase.ObserveAnalysisProgressUseCase
import io.github.koolunkle.yakallim.domain.usecase.PrepareImageUseCase
import io.github.koolunkle.yakallim.domain.usecase.StartAnalysisUseCase
import io.github.koolunkle.yakallim.testutil.FakeImageProcessor
import io.github.koolunkle.yakallim.testutil.FakeOcrRepository
import io.github.koolunkle.yakallim.testutil.FakePushNotificationObserver
import io.github.koolunkle.yakallim.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
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
            startAnalysisUseCase = StartAnalysisUseCase(fakeRepository),
            getAnalysisResultUseCase = GetAnalysisResultUseCase(fakeRepository),
            getPendingAnalysisJobIdUseCase = GetPendingAnalysisJobIdUseCase(fakeRepository),
            getLastAnalysisResultUseCase = GetLastAnalysisResultUseCase(fakeRepository),
            clearLastAnalysisResultUseCase = ClearLastAnalysisResultUseCase(fakeRepository),
            cancelAnalysisUseCase = CancelAnalysisUseCase(fakeRepository),
            observeAnalysisProgressUseCase = ObserveAnalysisProgressUseCase(fakeRepository),
            prepareImageUseCase = PrepareImageUseCase(fakeImageProcessor),
            getCachedImageUriUseCase = GetCachedImageUriUseCase(fakeRepository),
            pushNotificationObserver = fakePushObserver.observer,
            context = context
        )
    }

    private fun samplePrescription() = Prescription(
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
            Progress(JobStatus.ANALYZING, "", 30, false),
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
    fun onAnalysisRequested_whenSseReportsFailedWithMessage_showsServerMessage() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        fakeRepository.progressFlow = flowOf(
            Progress(JobStatus.ANALYZING, "", 30, false),
            Progress(JobStatus.FAILED, "복약 안내서를 분석하지 못했습니다.", 100, true)
        )

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(OcrError.ServerError("복약 안내서를 분석하지 못했습니다."), state.error)
    }

    @Test
    fun onAnalysisRequested_whenSseReportsCancelled_showsCancelledInsteadOfFailure() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        var cancelledEventCount = 0
        val collectorJob = launch { viewModel.analysisCancelledEvents.collect { cancelledEventCount++ } }

        fakeRepository.progressFlow = flowOf(
            Progress(JobStatus.ANALYZING, "", 30, false),
            Progress(JobStatus.CANCELLED, "작업이 취소되었습니다.", 100, true)
        )

        viewModel.onImageSelected(mock<Uri>())
        viewModel.onAnalysisRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        // 취소는 오류 카드가 아니라 안내(스낵바)로 보여 준다.
        assertNull(state.error)
        assertNull(state.progress)
        assertTrue(state.hasImage)
        assertTrue(fakeRepository.fetchedResultJobIds.isEmpty())
        assertEquals(1, cancelledEventCount)
        collectorJob.cancel()
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
    fun onAnalysisCancelRequested_duringActiveAnalysis_stopsAnalysisAndShowsCancelledNotice() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        var cancelledEventCount = 0
        val collectorJob = launch { viewModel.analysisCancelledEvents.collect { cancelledEventCount++ } }

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
        val cancelCallsBeforeCancellation = fakeRepository.cancelAnalysisCallCount

        viewModel.onAnalysisCancelRequested()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.progress)
        assertTrue(state.hasImage)
        assertEquals(1, cancelledEventCount)
        assertEquals(cancelCallsBeforeCancellation + 1, fakeRepository.cancelAnalysisCallCount)
        // The cancelled coroutine must not resume and overwrite the cancelled state.
        assertNull(state.analysisResult)
        collectorJob.cancel()
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

    @Test
    fun fetchAnalysisResult_whenJobNotFound_clearsActiveJobIdSoRetryStartsNewAnalysis() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onImageSelected(mock<Uri>())

        val notFound = HttpException(Response.error<Any>(404, "".toResponseBody(null)))
        fakeRepository.resultFlow = flowOf(Result.failure(notFound))

        viewModel.fetchAnalysisResult("job-dead")
        advanceUntilIdle()

        val stateAfterFailure = viewModel.uiState.value
        assertFalse(stateAfterFailure.isLoading)
        assertTrue(stateAfterFailure.error is OcrError.AnalysisFailed)

        // A 404 means the job no longer exists on the server (e.g. wiped by a redeploy), so
        // retrying must submit a brand-new job instead of re-fetching the dead jobId forever.
        fakeRepository.progressFlow = flowOf(Progress(JobStatus.COMPLETED, "", 100, true))
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        viewModel.retryAnalysis()
        advanceUntilIdle()

        assertNotNull(fakeRepository.requestedFile)
        assertEquals("job-1", fakeRepository.fetchedResultJobIds.last())
    }

    @Test
    fun onNotificationOpened_withJobId_fetchesThatJobsResult() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        fakeRepository.resultFlow = flowOf(Result.success(samplePrescription()))

        viewModel.onNotificationOpened("job-from-push")
        advanceUntilIdle()

        assertEquals(listOf("job-from-push"), fakeRepository.fetchedResultJobIds)
        assertEquals(1, viewModel.uiState.value.analysisResult?.medicines?.size)
    }

    @Test
    fun onNotificationOpened_withoutJobId_doesNothing() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onNotificationOpened(null)
        viewModel.onNotificationOpened("")
        advanceUntilIdle()

        assertTrue(fakeRepository.fetchedResultJobIds.isEmpty())
    }

    @Test
    fun onNotificationOpened_withSameJobTwice_fetchesOnlyOnce() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        // 화면 회전 등으로 Activity가 다시 만들어지면 같은 Intent로 다시 호출된다.
        viewModel.onNotificationOpened("job-from-push")
        viewModel.onNotificationOpened("job-from-push")
        advanceUntilIdle()

        assertEquals(listOf("job-from-push"), fakeRepository.fetchedResultJobIds)
    }
}
