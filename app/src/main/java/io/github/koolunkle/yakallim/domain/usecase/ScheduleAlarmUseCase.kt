package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        alarmTimes: List<String>,
        soundUri: String?
    ) {
        alarmScheduler.schedule(medicineName, dosagePerTake, dailyFrequency, durationDays, alarmTimes, soundUri)
    }
}
