package com.example.yakallim.data.mapper

import com.example.yakallim.data.datasource.remote.dto.MedicineResponse
import com.example.yakallim.data.datasource.remote.dto.OcrResponse
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
}
