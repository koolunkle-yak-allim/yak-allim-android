package com.example.yakallim.domain.alarm

import com.example.yakallim.domain.model.AlarmSpec
import java.time.Instant
import java.time.ZoneId

/**
 * [AlarmSpec]으로부터 알람 발생 시각을 계산하는 순수 함수 모음.
 * 부작용이 없어 재부팅 복구(BootReceiver)와 최초 등록(AlarmSchedulerImpl) 양쪽에서
 * 동일한 로직을 재사용할 수 있다.
 */
object AlarmTimeCalculator {

    data class Trigger(val day: Int, val timeIndex: Int, val triggerMillis: Long)

    /** [spec] 기준으로 아직 지나지 않은(> [nowMillis]) 발생 시각만 반환한다. */
    fun triggerTimes(spec: AlarmSpec, nowMillis: Long, zone: ZoneId): List<Trigger> =
        allTriggers(spec, zone).filter { it.triggerMillis > nowMillis }

    /** day×시각의 전체 격자를 계산한다(과거 시각 포함). 요청 코드 계산 등에 쓰인다. */
    fun allTriggers(spec: AlarmSpec, zone: ZoneId): List<Trigger> {
        if (spec.dailyFrequency <= 0 || spec.durationDays <= 0 || spec.alarmTimes.isEmpty()) return emptyList()

        val totalAlarms = spec.dailyFrequency * spec.durationDays
        val startDate = Instant.ofEpochMilli(spec.startEpochMillis).atZone(zone).toLocalDate()

        val triggers = mutableListOf<Trigger>()
        var index = 0

        outer@ for (day in 0 until spec.durationDays) {
            for ((timeIndex, timeStr) in spec.alarmTimes.withIndex()) {
                if (index >= totalAlarms) break@outer

                val (hour, minute) = parseTime(timeStr)
                val triggerMillis = startDate.plusDays(day.toLong())
                    .atStartOfDay(zone)
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .withNano(0)
                    .toInstant()
                    .toEpochMilli()

                triggers.add(Trigger(day, timeIndex, triggerMillis))
                index++
            }
        }
        return triggers
    }

    /** 약품명 + 날짜/시각 슬롯 기준의 결정적(deterministic) 요청 코드. 재부팅 후에도 동일한 값을 재현한다. */
    fun requestCode(medicineName: String, day: Int, timeIndex: Int): Int {
        val composite = "$medicineName:$day:$timeIndex"
        return composite.hashCode() and 0x7FFFFFFF
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour to minute
    }
}
