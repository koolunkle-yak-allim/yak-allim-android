package io.github.koolunkle.yakallim.domain.repository

import android.net.Uri
import io.github.koolunkle.yakallim.domain.model.Progress
import io.github.koolunkle.yakallim.domain.model.Prescription
import kotlinx.coroutines.flow.Flow
import java.io.File

interface OcrRepository {
    suspend fun startAnalysis(imageFile: File): String
    fun fetchAnalysisResult(jobId: String): Flow<Result<Prescription>>
    suspend fun cancelAnalysis()
    suspend fun getPendingAnalysisJobId(): String?
    suspend fun getLastAnalysisResult(): Prescription?
    suspend fun clearLastAnalysisResult()
    fun observeAnalysisProgress(jobId: String): Flow<Progress>
    fun getCachedImageUri(jobId: String): Uri?
    fun getLastCachedImageUri(): Uri?
}