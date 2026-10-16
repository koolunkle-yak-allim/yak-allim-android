package com.example.yakallim.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicineResponse(
    @SerialName("medicineName") val medicineName: String?,
    @SerialName("dosagePerTake") val dosagePerTake: String? = null,
    @SerialName("dailyFrequency") val dailyFrequency: Int? = null,
    @SerialName("durationDays") val durationDays: Int? = null
)
