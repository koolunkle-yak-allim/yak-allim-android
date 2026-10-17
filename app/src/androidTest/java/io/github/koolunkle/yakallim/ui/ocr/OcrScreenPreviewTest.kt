package io.github.koolunkle.yakallim.ui.ocr

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

class OcrScreenPreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ocrScreenContentPreview_withDuplicateMedicineNames_shouldNotCrash() {
        composeTestRule.setContent {
            OcrScreenContentPreview()
        }
    }

    @Test
    fun ocrScreenContentPreview_rendersACardForEveryDuplicateMedicine() {
        composeTestRule.setContent {
            OcrScreenContentPreview()
        }

        // The preview intentionally seeds two "타이레놀" entries to demonstrate the
        // duplicate-key fix; both must be rendered as separate cards.
        composeTestRule.onAllNodesWithText("타이레놀", useUnmergedTree = true)
            .assertCountEquals(2)
    }
}