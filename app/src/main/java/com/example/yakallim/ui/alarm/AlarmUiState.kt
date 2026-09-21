package com.example.yakallim.ui.alarm

import com.example.yakallim.domain.model.Alarm

data class AlarmUiState(
    val registeredAlarms: Map<String, Alarm> = emptyMap(),
    val isInitialized: Boolean = false
)

sealed interface AlarmEvent {
    data class Registered(
        val medicineId: String,
        val dosagePerTake: String,
        val dailyFrequency: Int,
        val durationDays: Int
    ) : AlarmEvent

    data class Error(val message: String) : AlarmEvent
}

data class PendingAlarm(
    val medicineId: String,
    val medicineName: String,
    val dosagePerTake: String,
    val dailyFrequency: Int,
    val durationDays: Int
)
