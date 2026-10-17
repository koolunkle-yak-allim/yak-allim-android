package io.github.koolunkle.yakallim.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrResponse(
    @SerialName("message") val message: String,
    @SerialName("medicines") val medicines: List<MedicineResponse>? = emptyList()
)
