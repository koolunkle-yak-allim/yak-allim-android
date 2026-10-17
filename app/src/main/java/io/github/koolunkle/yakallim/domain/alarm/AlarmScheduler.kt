package io.github.koolunkle.yakallim.domain.alarm

import io.github.koolunkle.yakallim.domain.model.Alarm

interface AlarmScheduler {
    suspend fun schedule(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        alarmTimes: List<String>,
        soundUri: String?
    )
    suspend fun cancel(medicineName: String)
    suspend fun getActiveAlarm(): Set<String>
    suspend fun getAlarmDetail(medicineName: String): Alarm?
    suspend fun rescheduleAll()
}
