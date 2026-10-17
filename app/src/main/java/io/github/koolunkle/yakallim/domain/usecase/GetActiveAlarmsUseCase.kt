package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.alarm.AlarmScheduler
import javax.inject.Inject

class GetActiveAlarmsUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(): Set<String> = alarmScheduler.getActiveAlarm()
}
