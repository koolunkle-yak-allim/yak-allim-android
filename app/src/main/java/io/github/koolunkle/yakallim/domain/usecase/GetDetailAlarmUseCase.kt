package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import io.github.koolunkle.yakallim.domain.model.Alarm
import javax.inject.Inject

class GetDetailAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(medicineName: String): Alarm? {
        return alarmScheduler.getDetailAlarm(medicineName)
    }
}
