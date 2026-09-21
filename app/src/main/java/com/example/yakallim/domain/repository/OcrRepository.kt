package com.example.yakallim.domain.repository

import android.net.Uri
import com.example.yakallim.domain.model.Progress
import com.example.yakallim.domain.model.Prescription
import kotlinx.coroutines.flow.Flow
import java.io.File

interface OcrRepository {
    suspend fun requestPrescription(imageFile: File): String
    fun fetchPrescriptionResult(jobId: String): Flow<Result<Prescription>>
    suspend fun cancelPrescription()
    suspend fun getPendingPrescriptionJobId(): String?
    suspend fun getLastPrescription(): Prescription?
    suspend fun clearLastPrescription()
    fun observeOcrProgress(jobId: String): Flow<Progress>
    fun getCachedImageUri(jobId: String): Uri?
    fun getLastCachedImageUri(): Uri?
}