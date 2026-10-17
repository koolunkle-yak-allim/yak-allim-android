package io.github.koolunkle.yakallim.data.datasource.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OcrProgressResponse(
    @SerialName("step") val step: String?,
    @SerialName("message") val message: String?,
    @SerialName("progress") val progress: Int?,
    @SerialName("isFinished") val isFinished: Boolean
)
