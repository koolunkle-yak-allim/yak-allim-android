package io.github.koolunkle.yakallim.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrJobResponse(
    @SerialName("jobId") val jobId: String,
    @SerialName("status") val status: String? = null,
    @SerialName("result") val result: OcrResponse? = null,
    @SerialName("error") val error: String? = null
)