package com.example.yakallim.ui.ocr

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import com.example.yakallim.domain.model.PrescribedMedicine
import com.example.yakallim.domain.model.Prescription
import org.junit.Rule
import org.junit.Test

class OcrScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val unknownMedicineLabel = "알 수 없는 약품"

    /**
     * Renders [OcrScreenContent] with the given [medicines] and returns once composition
     * (and any crash) would have occurred. Centralizing the default callback wiring keeps
     * each test focused on the specific [uiState]/[medicines] scenario being verified.
     */
    private fun setOcrScreenContent(
        medicines: List<PrescribedMedicine>,
        highlightedMedicineName: String? = null
    ) {
        val uiState = OcrUiState(
            isInitialized = true,
            analysisResult = Prescription(
                rawText = "Dummy Raw Text",
                medicines = medicines
            )
        )

        composeTestRule.setContent {
            OcrScreenContent(
                modifier = Modifier,
                uiState = uiState,
                lazyListState = rememberLazyListState(),
                highlightedMedicineName = highlightedMedicineName,
                isGuideSheetVisible = false,
                unknownMedicineLabel = unknownMedicineLabel,
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

    @Test
    fun ocrScreen_withDuplicateMedicineNames_shouldNotCrash() {
        val duplicateMedicines = listOf(
            PrescribedMedicine(
                id = "medicine-1",
                name = "타이레놀",
                dosagePerTake = "1",
                dailyFrequency = 3,
                durationDays = 3
            ),
            PrescribedMedicine(
                id = "medicine-2",
                name = "타이레놀",
                dosagePerTake = "2",
                dailyFrequency = 2,
                durationDays = 5
            )
        )

        setOcrScreenContent(medicines = duplicateMedicines)
    }

    @Test
    fun ocrScreen_withDuplicateMedicineNames_rendersACardForEveryMedicine() {
        val duplicateMedicines = listOf(
            PrescribedMedicine(
                id = "medicine-1",
                name = "타이레놀",
                dosagePerTake = "1",
                dailyFrequency = 3,
                durationDays = 3
            ),
            PrescribedMedicine(
                id = "medicine-2",
                name = "타이레놀",
                dosagePerTake = "2",
                dailyFrequency = 2,
                durationDays = 5
            )
        )

        setOcrScreenContent(medicines = duplicateMedicines)

        // Regression check for the duplicate-key crash fix: both medicine cards must be
        // rendered even though they share the same name, instead of one being dropped or
        // the LazyColumn throwing an IllegalArgumentException on the duplicate key.
        composeTestRule.onAllNodesWithText("타이레놀", useUnmergedTree = true)
            .assertCountEquals(2)
    }

    @Test
    fun ocrScreen_withThreeMedicinesSharingTheSameName_rendersThreeCards() {
        val triplicateMedicines = List(3) { index ->
            PrescribedMedicine(
                id = "medicine-$index",
                name = "이지엔6",
                dosagePerTake = "${index + 1}",
                dailyFrequency = 2,
                durationDays = 4
            )
        }

        setOcrScreenContent(medicines = triplicateMedicines)

        composeTestRule.onAllNodesWithText("이지엔6", useUnmergedTree = true)
            .assertCountEquals(3)
    }

    @Test
    fun ocrScreen_withMultipleUnnamedMedicines_shouldNotCrashAndRendersFallbackLabelForEach() {
        val unnamedMedicines = listOf(
            PrescribedMedicine(
                id = "medicine-1",
                name = null,
                dosagePerTake = "1",
                dailyFrequency = 1,
                durationDays = 1
            ),
            PrescribedMedicine(
                id = "medicine-2",
                name = null,
                dosagePerTake = "2",
                dailyFrequency = 2,
                durationDays = 2
            )
        )

        setOcrScreenContent(medicines = unnamedMedicines)

        // Both entries fall back to the same unknown-medicine label, so the stable per-medicine
        // id must still keep both cards distinct and rendered.
        composeTestRule.onAllNodesWithText(unknownMedicineLabel, useUnmergedTree = true)
            .assertCountEquals(2)
    }

    @Test
    fun ocrScreen_withUniqueMedicineNames_rendersEachCardExactlyOnce() {
        val uniqueMedicines = listOf(
            PrescribedMedicine(
                id = "medicine-1",
                name = "타이레놀",
                dosagePerTake = "1",
                dailyFrequency = 3,
                durationDays = 3
            ),
            PrescribedMedicine(
                id = "medicine-2",
                name = "게보린",
                dosagePerTake = "2",
                dailyFrequency = 2,
                durationDays = 5
            )
        )

        setOcrScreenContent(medicines = uniqueMedicines)

        composeTestRule.onAllNodesWithText("타이레놀", useUnmergedTree = true).assertCountEquals(1)
        composeTestRule.onAllNodesWithText("게보린", useUnmergedTree = true).assertCountEquals(1)
    }

    @Test
    fun ocrScreen_withEmptyMedicinesList_shouldNotCrash() {
        // Edge case for the key lambda: an empty list must not be evaluated
        // and must not crash the LazyColumn.
        setOcrScreenContent(medicines = emptyList())
    }
}
