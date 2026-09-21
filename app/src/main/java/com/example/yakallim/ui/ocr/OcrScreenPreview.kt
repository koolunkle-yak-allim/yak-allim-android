package com.example.yakallim.ui.ocr

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.yakallim.domain.model.PrescribedMedicine
import com.example.yakallim.domain.model.Prescription

@Preview(showBackground = true)
@Composable
internal fun OcrScreenContentPreview() {
    MaterialTheme {
        val duplicateMedicines = listOf(
            PrescribedMedicine(
                id = "preview-1",
                name = "타이레놀",
                dosagePerTake = "1",
                dailyFrequency = 3,
                durationDays = 3
            ),
            PrescribedMedicine(
                id = "preview-2",
                name = "타이레놀",
                dosagePerTake = "2",
                dailyFrequency = 2,
                durationDays = 5
            )
        )

        OcrScreenContent(
            modifier = Modifier,
            uiState = OcrUiState(
                isInitialized = true,
                analysisResult = Prescription(
                    rawText = "Preview Raw Text",
                    medicines = duplicateMedicines
                )
            ),
            lazyListState = rememberLazyListState(),
            highlightedMedicineName = null,
            isGuideSheetVisible = false,
            unknownMedicineLabel = "알 수 없는 약품",
            onGuideClick = {},
            onGuideDismissRequest = {},
            onCameraCaptureClick = {},
            onGallerySelectClick = {},
            onResetAnalysisClick = {},
            onStartAnalysisClick = {},
            onCancelAnalysisClick = {},
            onMedicineTextClick = {},
            onRegisterAlarmClick = { _, _, _, _, _ -> },
            onCancelAlarmClick = {},
            onToggleCardExpansion = {},
            onToggleAllCardsExpansion = {}
        )
    }
}
