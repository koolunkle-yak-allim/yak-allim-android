package com.example.yakallim.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.yakallim.data.datasource.local.OcrLocalDataSource
import com.example.yakallim.data.datasource.remote.OcrRemoteDataSource
import com.example.yakallim.data.datasource.remote.dto.OcrResponse
import com.example.yakallim.domain.image.ImageProcessor
import com.example.yakallim.data.mapper.toDomain
import com.example.yakallim.domain.model.Progress
import com.example.yakallim.domain.model.Prescription
import com.example.yakallim.domain.notification.PushTokenProvider
import com.example.yakallim.domain.repository.OcrRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class OcrRepositoryImpl @Inject constructor(
    private val ocrRemoteDataSource: OcrRemoteDataSource,
    private val imageProcessor: ImageProcessor,
    private val pushTokenProvider: PushTokenProvider,
    private val ocrLocalDataSource: OcrLocalDataSource,
    private val json: Json,
    @param:ApplicationContext private val context: Context
) : OcrRepository {

    override suspend fun requestPrescription(imageFile: File): String =
        withContext(Dispatchers.IO) {
            val processedFile = imageProcessor.preprocess(imageFile)
            val fcmToken = pushTokenProvider.getFcmToken()

            val jobId = ocrRemoteDataSource.enqueueOcrJob(processedFile, fcmToken).jobId
            ocrLocalDataSource.savePendingJobId(jobId)
            cacheProcessedImage(processedFile, jobId)
            jobId
        }

    override fun fetchPrescriptionResult(jobId: String): Flow<Result<Prescription>> = flow {
        try {
            val ocrJob = ocrRemoteDataSource.getOcrJob(jobId)
            val ocrJobResult = ocrJob.result
                ?: throw NoSuchElementException("OCR job [${ocrJob.jobId}] completed, but the result data is missing.")

            try {
                val jsonString = json.encodeToString(ocrJobResult)
                ocrLocalDataSource.saveLastPrescriptionJson(jsonString)
                val srcCache = File(context.cacheDir, "ocr_image_$jobId.jpg")
                if (srcCache.exists()) {
                    val dstCache = File(context.cacheDir, "ocr_image_last.jpg")
                    srcCache.copyTo(dstCache, overwrite = true)
                }
            } catch (e: Exception) {
                Log.e("OcrRepositoryImpl", "마지막 분석 결과 저장 실패: ${e.message}")
            }

            emit(Result.success(ocrJobResult.toDomain()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        } finally {
            ocrLocalDataSource.clearPendingJobId()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun cancelPrescription() {
        ocrLocalDataSource.getPendingJobId()?.let { jobId ->
            ocrLocalDataSource.setAnalysisCancelled(jobId)
            try {
                ocrRemoteDataSource.cancelOcrJob(jobId)
            } catch (e: Exception) {
                Log.e("OcrRepositoryImpl", "작업 취소 API 호출 실패: ${e.message}")
            } finally {
                ocrLocalDataSource.clearPendingJobId()
            }
        }
    }

    override suspend fun getPendingPrescriptionJobId(): String? {
        return ocrLocalDataSource.getPendingJobId()
    }

    private fun cacheProcessedImage(file: File, jobId: String) {
        try {
            val cacheFile = File(context.cacheDir, "ocr_image_$jobId.jpg")
            file.copyTo(cacheFile, overwrite = true)
        } catch (e: Exception) {
            Log.e("OcrRepositoryImpl", "이미지 캐싱 실패", e)
        }
    }

    override suspend fun getLastPrescription(): Prescription? {
        val jsonString = ocrLocalDataSource.getLastPrescriptionJson() ?: return null
        return try {
            val ocrJobResult = json.decodeFromString<OcrResponse>(jsonString)
            ocrJobResult.toDomain()
        } catch (e: Exception) {
            Log.e("OcrRepositoryImpl", "마지막 분석 결과 복원 실패: ${e.message}")
            null
        }
    }

    override suspend fun clearLastPrescription() {
        ocrLocalDataSource.clearLastPrescriptionJson()
        try {
            val lastCache = File(context.cacheDir, "ocr_image_last.jpg")
            if (lastCache.exists()) {
                lastCache.delete()
            }
        } catch (e: Exception) {
            Log.e("OcrRepositoryImpl", "마지막 캐시 이미지 삭제 실패: ${e.message}")
        }
    }

    override fun observeOcrProgress(jobId: String): Flow<Progress> {
        return ocrRemoteDataSource.observeOcrProgress(jobId)
    }

    override fun getCachedImageUri(jobId: String): Uri? {
        val cacheFile = File(context.cacheDir, "ocr_image_$jobId.jpg")
        return cacheFile.takeIf { it.exists() }?.let { Uri.fromFile(it) }
    }

    override fun getLastCachedImageUri(): Uri? {
        val cacheFile = File(context.cacheDir, "ocr_image_last.jpg")
        return cacheFile.takeIf { it.exists() }?.let { Uri.fromFile(it) }
    }
}
