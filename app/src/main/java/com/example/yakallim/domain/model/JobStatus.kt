package com.example.yakallim.domain.model

enum class JobStatus {
    ENQUEUED,
    ANALYZING,
    COMPLETED,
    FAILED,
    CANCELLED,
    UNKNOWN;

    val isFinished: Boolean get() = this == COMPLETED || this == FAILED || this == CANCELLED
}
