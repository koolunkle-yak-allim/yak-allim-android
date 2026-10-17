package io.github.koolunkle.yakallim.domain.alarm

interface AlarmDispatcher {
    fun notifyAlarm(
        medicineName: String,
        dosagePerTake: String,
        dailyFrequency: Int,
        durationDays: Int,
        soundUri: String?
    )
}
