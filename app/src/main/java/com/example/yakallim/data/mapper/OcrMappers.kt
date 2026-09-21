package com.example.yakallim.data.mapper

import com.example.yakallim.data.datasource.remote.dto.OcrResponse
import com.example.yakallim.domain.model.Point
import com.example.yakallim.domain.model.Polygon
import com.example.yakallim.domain.model.PrescribedMedicine
import com.example.yakallim.domain.model.Prescription
import com.example.yakallim.domain.model.TextBlock

fun OcrResponse.toDomain(): Prescription {
    val textBlocks = this.textBlocks ?: emptyList()
    val rawText = textBlocks.joinToString(separator = "\n") { it.text }

    val mappedMedicines = this.medicines?.map { medicine ->
        val matchedTextBlock = textBlocks.find { it.text.contains(medicine.medicineName ?: "", ignoreCase = true) }
        val matchedConfidence = matchedTextBlock?.confidence ?: 1.0f

        PrescribedMedicine(
            name = medicine.medicineName,
            dosagePerTake = medicine.dosagePerTake?.takeIf { it.isNotBlank() },
            dailyFrequency = medicine.dailyFrequency?.takeIf { it > 0 },
            durationDays = medicine.durationDays?.takeIf { it > 0 },
            isLowConfidence = matchedConfidence < 0.8f,
            bounds = medicine.bounds?.map { polygon ->
                Polygon(
                    polygon.points.map { coordinate -> Point(coordinate.x, coordinate.y) }
                )
            } ?: emptyList()
        )
    } ?: emptyList()

    val mappedTextBlocks = textBlocks.map { textBlock ->
        TextBlock(
            text = textBlock.text,
            confidence = textBlock.confidence,
            bounds = textBlock.bounds.map { Point(it.x, it.y) }
        )
    }

    return Prescription(
        rawText = rawText,
        medicines = mappedMedicines,
        textBlocks = mappedTextBlocks
    )
}
