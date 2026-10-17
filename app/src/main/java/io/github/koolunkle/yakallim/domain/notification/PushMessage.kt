package io.github.koolunkle.yakallim.domain.notification

import io.github.koolunkle.yakallim.domain.model.JobStatus

data class PushMessage(
    val jobId: String,
    val status: JobStatus,
    val errorMessage: String? = null
)
