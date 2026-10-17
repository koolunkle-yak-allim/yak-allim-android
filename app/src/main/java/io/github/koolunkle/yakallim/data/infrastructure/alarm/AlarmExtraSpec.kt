package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.content.Intent

/** 알람 PendingIntent(AlarmSchedulerImpl → AlarmReceiver)에 담는 값의 키. */
object AlarmExtraSpec {
    const val KEY_MEDICINE_NAME = "MEDICINE_NAME"
    const val KEY_DOSAGE_PER_TAKE = "DOSAGE_PER_TAKE"
    const val KEY_DAILY_FREQUENCY = "DAILY_FREQUENCY"
    const val KEY_DURATION_DAYS = "DURATION_DAYS"
    const val KEY_SOUND_URI = "SOUND_URI"

    // 알람은 복용 기간 전체를 미리 예약하므로, 이전 키("KEY_SOUND_URI")로 예약된 알람이 남아 있을 수 있다.
    // 그런 알람이 모두 울리거나 다시 예약된 뒤에 지운다.
    private const val LEGACY_KEY_SOUND_URI = "KEY_SOUND_URI"

    fun readSoundUri(intent: Intent): String? =
        intent.getStringExtra(KEY_SOUND_URI) ?: intent.getStringExtra(LEGACY_KEY_SOUND_URI)
}
