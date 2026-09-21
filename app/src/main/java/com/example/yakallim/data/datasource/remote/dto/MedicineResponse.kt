package com.example.yakallim.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MedicineResponse(
    @SerialName("medicineName") val medicineName: String?,
    @SerialName("dosagePerTake") val dosagePerTake: String? = "",
    @SerialName("dailyFrequency") val dailyFrequency: Int? = 0,
    @SerialName("durationDays") val durationDays: Int? = 0,
    @SerialName("bounds") val bounds: List<PolygonResponse>? = emptyList(),
    @SerialName("rawName") val rawName: String? = null,
    @SerialName("autoCorrected") val autoCorrected: Boolean? = null,
    @SerialName("confidence") val confidence: Float? = null
)
