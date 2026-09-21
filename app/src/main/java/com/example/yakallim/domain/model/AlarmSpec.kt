package com.example.yakallim.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AlarmSpec(
    val medicineName: String,
    val dosagePerTake: String,
    val dailyFrequency: Int,
    val durationDays: Int,
    val alarmTimes: List<String>,
    val soundUri: String?,
    val startEpochMillis: Long
)
