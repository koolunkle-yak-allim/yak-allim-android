package io.github.koolunkle.yakallim.domain.model

data class PrescribedMedicine(
    val id: String,
    val name: String?,
    val dosagePerTake: String?,
    val dailyFrequency: Int?,
    val durationDays: Int?
)
