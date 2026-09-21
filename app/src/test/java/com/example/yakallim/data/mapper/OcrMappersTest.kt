package com.example.yakallim.data.mapper

import com.example.yakallim.data.datasource.remote.dto.MedicineResponse
import com.example.yakallim.data.datasource.remote.dto.OcrProgressResponse
import com.example.yakallim.data.datasource.remote.dto.OcrResponse
import com.example.yakallim.domain.model.JobStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
