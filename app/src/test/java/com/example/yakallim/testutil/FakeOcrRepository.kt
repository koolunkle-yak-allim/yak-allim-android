package com.example.yakallim.testutil

import android.net.Uri
import com.example.yakallim.domain.model.Prescription
import com.example.yakallim.domain.model.Progress
import com.example.yakallim.domain.notification.PushMessage
import com.example.yakallim.domain.notification.PushNotificationObserver
import com.example.yakallim.domain.repository.OcrRepository
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow

class FakeOcrRepository : OcrRepository {
    var requestedFile: File? = null
    var requestPrescriptionResult: Result<String> = Result.success("job-1")
    var progressFlow: Flow<Progress> = emptyFlow()
    var resultFlow: Flow<Result<Prescription>> = emptyFlow()
    var pendingPrescriptionJobId: String? = null
    var lastPrescription: Prescription? = null
    var cachedImageUri: Uri? = null
    var lastCachedUri: Uri? = null

    val fetchedResultJobIds = mutableListOf<String>()
    val observedProgressJobIds = mutableListOf<String>()
    var cancelPrescriptionCallCount = 0
    var clearLastPrescriptionCallCount = 0

    override suspend fun requestPrescription(imageFile: File): String {
        requestedFile = imageFile
        return requestPrescriptionResult.getOrThrow()
    }

    override fun fetchPrescriptionResult(jobId: String): Flow<Result<Prescription>> {
        fetchedResultJobIds += jobId
        return resultFlow
    }

    override suspend fun cancelPrescription() {
        cancelPrescriptionCallCount++
    }

    override suspend fun getPendingPrescriptionJobId(): String? = pendingPrescriptionJobId

    override suspend fun getLastPrescription(): Prescription? = lastPrescription

    override suspend fun clearLastPrescription() {
        clearLastPrescriptionCallCount++
    }

    override fun observeOcrProgress(jobId: String): Flow<Progress> {
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
