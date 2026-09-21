package com.example.yakallim.data.mapper

import com.example.yakallim.data.datasource.remote.dto.MedicineResponse
import com.example.yakallim.data.datasource.remote.dto.OcrProgressResponse
import com.example.yakallim.data.datasource.remote.dto.OcrResponse
import com.example.yakallim.domain.model.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrMappersTest {

    @Test
    fun toDomain_whenDosagePerTakeIsNull_keepsItNullInsteadOfFakingAValue() {
        val rawResponse = OcrResponse(
            fileName = "test.jpg",
            message = "Success",
            textBlocks = emptyList(),
            medicines = listOf(
                MedicineResponse(
                    medicineName = "타이레놀",
                    dosagePerTake = null,
                    dailyFrequency = 3,
                    durationDays = 5,
                    bounds = null
                )
            )
        )

        val domainPrescription = rawResponse.toDomain()
        val mappedMedication = domainPrescription.medicines.first()

        assertNull(mappedMedication.dosagePerTake)
    }

    @Test
    fun toDomain_whenDailyFrequencyOrDurationDaysIsMissingOrZero_keepsThemNull() {
        val rawResponse = OcrResponse(
            fileName = "test.jpg",
            message = "Success",
            textBlocks = emptyList(),
            medicines = listOf(
                MedicineResponse(
                    medicineName = "타이레놀",
                    dosagePerTake = "1정",
                    dailyFrequency = null,
                    durationDays = 0,
                    bounds = null
                )
            )
        )

        val domainPrescription = rawResponse.toDomain()
        val mappedMedication = domainPrescription.medicines.first()

        assertNull(mappedMedication.dailyFrequency)
        assertNull(mappedMedication.durationDays)
    }

    @Test
    fun toDomain_whenDosagePerTakeIsProvided_preservesValue() {
        val rawResponse = OcrResponse(
            fileName = "test_image.png",
            message = "Fetched successfully",
            textBlocks = emptyList(),
            medicines = listOf(
                MedicineResponse(
                    medicineName = "아스피린",
                    dosagePerTake = "2정",
                    dailyFrequency = 2,
                    durationDays = 7,
                    bounds = null
                )
            )
        )

        val domainPrescription = rawResponse.toDomain()
        val mappedMedication = domainPrescription.medicines.first()

        assertEquals("2정", mappedMedication.dosagePerTake)
    }

    @Test
    fun toDomain_whenServerReportsLowConfidenceAfterNameCorrection_marksAsLowConfidence() {
        // 원문 텍스트에는 교정 전 이름("아르레온정")만 있고, 서버가 교정한 이름("타이레놀정")은 없다.
        // 과거에는 textBlocks.find로 매칭에 실패해 기본값 1.0(신뢰도 높음)으로 처리되던 케이스.
        val rawResponse = OcrResponse(
            fileName = "test.jpg",
            message = "Success",
            textBlocks = emptyList(),
            medicines = listOf(
                MedicineResponse(
                    medicineName = "타이레놀정",
                    dosagePerTake = "1정",
                    dailyFrequency = 3,
                    durationDays = 5,
                    bounds = null,
                    rawName = "아르레온정",
                    autoCorrected = true,
                    confidence = 0.4f
                )
            )
        )

        val mappedMedication = rawResponse.toDomain().medicines.first()

        assertTrue(mappedMedication.isLowConfidence)
        assertTrue(mappedMedication.autoCorrected)
        assertEquals("아르레온정", mappedMedication.rawName)
    }

    @Test
    fun toDomain_whenConfidenceIsMissing_defaultsToNotLowConfidence() {
        val rawResponse = OcrResponse(
            fileName = "test.jpg",
            message = "Success",
            textBlocks = emptyList(),
            medicines = listOf(
                MedicineResponse(
                    medicineName = "게보린정",
                    dosagePerTake = "1정",
                    dailyFrequency = 3,
                    durationDays = 5,
                    bounds = null,
                    confidence = null
                )
            )
        )

        val mappedMedication = rawResponse.toDomain().medicines.first()

        assertFalse(mappedMedication.isLowConfidence)
        assertFalse(mappedMedication.autoCorrected)
        assertNull(mappedMedication.rawName)
    }

    @Test
    fun progressToDomain_mapsAllServerStepsToMatchingJobStatus() {
        val expected = mapOf(
            "ACCEPTED" to JobStatus.ENQUEUED,
            "IMAGE_PROCESSING" to JobStatus.IMAGE_PROCESSING,
            "TEXT_DETECTION" to JobStatus.TEXT_DETECTION,
            "TEXT_RECOGNITION" to JobStatus.TEXT_RECOGNITION,
            "PARSING" to JobStatus.PARSING,
            "COMPLETED" to JobStatus.COMPLETED,
            "FAILED" to JobStatus.FAILED
        )

        expected.forEach { (step, jobStatus) ->
            val response = OcrProgressResponse(
                step = step,
                message = "message",
                progress = 50,
                isFinished = false
            )

            assertEquals(jobStatus, response.toDomain().jobStatus)
        }
    }

    @Test
    fun progressToDomain_whenStepIsUnrecognized_mapsToUnknown() {
        val response = OcrProgressResponse(
            step = "SOME_NEW_STEP",
            message = "message",
            progress = 50,
            isFinished = false
        )

        assertEquals(JobStatus.UNKNOWN, response.toDomain().jobStatus)
    }

    @Test
    fun progressToDomain_whenStepIsNull_mapsToUnknown() {
        val response = OcrProgressResponse(
            step = null,
            message = "message",
            progress = 50,
            isFinished = false
        )

        assertEquals(JobStatus.UNKNOWN, response.toDomain().jobStatus)
    }
}
