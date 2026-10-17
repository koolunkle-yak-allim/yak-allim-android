package io.github.koolunkle.yakallim.testutil

import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import io.github.koolunkle.yakallim.domain.model.Alarm

class FakeAlarmScheduler : AlarmScheduler {
    var scheduleShouldThrow: Boolean = false
    var activeAlarms: Set<String> = emptySet()
    var detailAlarms: Map<String, Alarm> = emptyMap()

    val scheduledCalls = mutableListOf<String>()
    val cancelledCalls = mutableListOf<String>()

    override suspend fun schedule(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        alarmTimes: List<String>,
        soundUri: String?
    ) {
        if (scheduleShouldThrow) throw IllegalStateException("schedule failed")
        scheduledCalls += medicineName
    }

    override suspend fun cancel(medicineName: String) {
        cancelledCalls += medicineName
    }

    override suspend fun getActiveAlarm(): Set<String> = activeAlarms

    override suspend fun getAlarmDetail(medicineName: String): Alarm? = detailAlarms[medicineName]

    override suspend fun rescheduleAll() {
        // no-op for ViewModel-level tests
    }
}
