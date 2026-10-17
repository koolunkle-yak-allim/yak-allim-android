package io.github.koolunkle.yakallim.data.datasource.remote

import io.github.koolunkle.yakallim.data.datasource.remote.dto.OcrJobResponse
import io.github.koolunkle.yakallim.domain.model.Progress
import kotlinx.coroutines.flow.Flow
import java.io.File

interface OcrRemoteDataSource {
    suspend fun enqueueOcrJob(imageFile: File, fcmToken: String?): OcrJobResponse
    suspend fun getOcrJob(jobId: String): OcrJobResponse
    suspend fun cancelOcrJob(jobId: String)
    fun observeOcrProgress(jobId: String): Flow<Progress>
}
