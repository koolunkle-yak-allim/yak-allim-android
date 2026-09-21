package com.example.yakallim.domain.model

enum class JobStatus {
    ENQUEUED,
    IMAGE_PROCESSING,
    TEXT_DETECTION,
    TEXT_RECOGNITION,
    PARSING,
    COMPLETED,
    FAILED,
    UNKNOWN;

    val isFinished: Boolean get() = this == COMPLETED || this == FAILED
}
