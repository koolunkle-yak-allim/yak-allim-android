package io.github.koolunkle.yakallim.data.mapper

import io.github.koolunkle.yakallim.data.datasource.remote.dto.OcrProgressResponse
import io.github.koolunkle.yakallim.data.datasource.remote.dto.OcrResponse
import io.github.koolunkle.yakallim.domain.model.JobStatus
import io.github.koolunkle.yakallim.domain.model.PrescribedMedicine
import io.github.koolunkle.yakallim.domain.model.Prescription
import io.github.koolunkle.yakallim.domain.model.Progress
import java.util.UUID

fun OcrProgressResponse.toDomain(): Progress {
    val jobStatus = when (step) {
        "ACCEPTED" -> JobStatus.ENQUEUED
        "ANALYZING" -> JobStatus.ANALYZING
        "COMPLETED" -> JobStatus.COMPLETED
        "FAILED" -> JobStatus.FAILED
        "CANCELLED" -> JobStatus.CANCELLED
        else -> JobStatus.UNKNOWN
    }

    return Progress(
        jobStatus = jobStatus,
        message = message ?: "",
        percent = progress ?: 0,
        isFinished = isFinished
    )
}

fun OcrResponse.toDomain(): Prescription {
    val mappedMedicines = this.medicines?.map { medicine ->
        PrescribedMedicine(
            id = UUID.randomUUID().toString(),
            name = medicine.medicineName,
            dosagePerTake = medicine.dosagePerTake?.takeIf { it.isNotBlank() },
            dailyFrequency = medicine.dailyFrequency?.takeIf { it > 0 },
            durationDays = medicine.durationDays?.takeIf { it > 0 }
        )
    } ?: emptyList()

    return Prescription(medicines = mappedMedicines)
}
