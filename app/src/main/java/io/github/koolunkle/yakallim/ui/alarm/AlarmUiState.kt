package io.github.koolunkle.yakallim.ui.alarm

import io.github.koolunkle.yakallim.domain.model.Alarm

data class AlarmUiState(
    val registeredAlarms: Map<String, Alarm> = emptyMap(),
    val isInitialized: Boolean = false
)

sealed interface AlarmEvent {
    data class Registered(
        val medicineId: String,
        val medicineName: String,
        val dosagePerTake: String,
        val dailyFrequency: Int,
        val durationDays: Int
    ) : AlarmEvent

    data class Error(val message: String) : AlarmEvent
}
