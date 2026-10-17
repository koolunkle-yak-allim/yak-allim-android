package io.github.koolunkle.yakallim.testutil

import android.net.Uri
import io.github.koolunkle.yakallim.domain.model.Prescription
import io.github.koolunkle.yakallim.domain.model.Progress
import io.github.koolunkle.yakallim.domain.notification.PushMessage
import io.github.koolunkle.yakallim.domain.notification.PushNotificationObserver
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow

class FakeOcrRepository : OcrRepository {
    var requestedFile: File? = null
    var startAnalysisResult: Result<String> = Result.success("job-1")
    var progressFlow: Flow<Progress> = emptyFlow()
    var resultFlow: Flow<Result<Prescription>> = emptyFlow()
    var pendingPrescriptionJobId: String? = null
    var lastPrescription: Prescription? = null
    var cachedImageUri: Uri? = null
    var lastCachedUri: Uri? = null

    val fetchedResultJobIds = mutableListOf<String>()
    val observedProgressJobIds = mutableListOf<String>()
    var cancelAnalysisCallCount = 0
    var clearLastAnalysisResultCallCount = 0

    override suspend fun startAnalysis(imageFile: File): String {
        requestedFile = imageFile
        return startAnalysisResult.getOrThrow()
    }

    override fun fetchAnalysisResult(jobId: String): Flow<Result<Prescription>> {
        fetchedResultJobIds += jobId
        return resultFlow
    }

    override suspend fun cancelAnalysis() {
        cancelAnalysisCallCount++
    }

    override suspend fun getPendingAnalysisJobId(): String? = pendingPrescriptionJobId

    override suspend fun getLastAnalysisResult(): Prescription? = lastPrescription

    override suspend fun clearLastAnalysisResult() {
        clearLastAnalysisResultCallCount++
    }

    override fun observeAnalysisProgress(jobId: String): Flow<Progress> {
        observedProgressJobIds += jobId
        return progressFlow
    }

    override fun getCachedImageUri(jobId: String): Uri? = cachedImageUri

    override fun getLastCachedImageUri(): Uri? = lastCachedUri
}

class FakePushNotificationObserver {
    private val flow = MutableSharedFlow<PushMessage>(replay = 0, extraBufferCapacity = 4)

    val observer = object : PushNotificationObserver {
        override val messages: Flow<PushMessage> = flow

        override suspend fun emitMessage(message: PushMessage) {
            flow.emit(message)
        }
    }

    suspend fun emit(message: PushMessage) {
        flow.emit(message)
    }
}
