package io.github.koolunkle.yakallim.data.infrastructure.alarm

/** 알람 PendingIntent(AlarmSchedulerImpl → AlarmReceiver)에 담는 값의 키. */
object AlarmExtraSpec {
    const val KEY_MEDICINE_NAME = "MEDICINE_NAME"
    const val KEY_DOSAGE_PER_TAKE = "DOSAGE_PER_TAKE"
    const val KEY_DAILY_FREQUENCY = "DAILY_FREQUENCY"
    const val KEY_DURATION_DAYS = "DURATION_DAYS"
    const val KEY_SOUND_URI = "SOUND_URI"
}
