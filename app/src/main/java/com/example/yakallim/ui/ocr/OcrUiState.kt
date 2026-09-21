package com.example.yakallim.ui.ocr

import android.graphics.Bitmap
import android.net.Uri
import com.example.yakallim.domain.model.Prescription
import com.example.yakallim.domain.model.JobStatus
import com.example.yakallim.domain.model.Alarm

sealed interface OcrError {
    data object Network : OcrError
    data object Timeout : OcrError
    data object EmptyResult : OcrError
    data object AnalysisFailed : OcrError
    data class ServerError(val message: String) : OcrError
    data class Unknown(val message: String) : OcrError
}

sealed interface OcrImage {
    data class UriSource(val uri: Uri) : OcrImage
    data class BitmapSource(val bitmap: Bitmap) : OcrImage
}

data class OcrProgress(
    val jobStatus: JobStatus = JobStatus.ENQUEUED,
    val percent: Int = 0,
    val message: String = "",
    val isSseActive: Boolean = true
)

data class OcrUiState(
    val selectedImage: OcrImage? = null,
    val analysisResult: Prescription? = null,
    val registeredAlarms: Map<String, Alarm> = emptyMap(),
    val cardExpansionMap: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val isInitialized: Boolean = false,
    val error: OcrError? = null,
    val progress: OcrProgress? = null
) {
    val hasImage: Boolean get() = selectedImage != null
}

data class PendingAlarm(
    val medicineId: String,
    val medicineName: String,
    val dosagePerTake: String,
    val dailyFrequency: Int,
    val durationDays: Int
)
