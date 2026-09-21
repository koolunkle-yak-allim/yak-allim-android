package com.example.yakallim.domain.alarm

import com.example.yakallim.domain.model.Alarm

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
    suspend fun getDetailAlarm(medicineName: String): Alarm?
    suspend fun rescheduleAll()
}
