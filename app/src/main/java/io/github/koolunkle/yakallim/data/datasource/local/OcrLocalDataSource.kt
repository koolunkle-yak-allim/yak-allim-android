package io.github.koolunkle.yakallim.data.datasource.local

interface OcrLocalDataSource {
    suspend fun getPendingJobId(): String?
    suspend fun savePendingJobId(jobId: String)
    suspend fun clearPendingJobId()
    suspend fun isAnalysisCancelled(jobId: String): Boolean
    suspend fun setAnalysisCancelled(jobId: String)
    suspend fun removeAnalysisCancelled(jobId: String)
    suspend fun saveLastPrescriptionJson(json: String)
    suspend fun getLastPrescriptionJson(): String?
    suspend fun clearLastPrescriptionJson()
}
