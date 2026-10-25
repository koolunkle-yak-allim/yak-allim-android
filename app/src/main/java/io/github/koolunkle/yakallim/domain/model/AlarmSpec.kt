package io.github.koolunkle.yakallim.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AlarmSpec(
    val medicineName: String,
    val dosagePerTake: String,
    val dailyFrequency: Int,
    val durationDays: Int,
    val alarmTimes: List<String>,
    val soundUri: String?,
    val startEpochMillis: Long,
    /** 등록할 때의 시간대. 복용 시작일을 이 시간대의 날짜로 고정한다. 이전에 저장한 알람에는 없다(null). */
    val startZoneId: String? = null
)
