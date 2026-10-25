package io.github.koolunkle.yakallim.ui.alarm

import java.util.Locale
import kotlin.math.abs

/** 알람 설정 시트가 쓰는 기본값과 복용량 문자열 처리. 화면과 분리해 단위 테스트한다. */
object AlarmFormDefaults {

    const val MIN_AMOUNT = 0.5
    const val MAX_AMOUNT = 10.0
    const val AMOUNT_STEP = 0.5
    const val MIN_FREQUENCY = 1
    const val MAX_FREQUENCY = 8
    const val MIN_DURATION_DAYS = 1
    const val MAX_DURATION_DAYS = 120

    /** 1회 복용량 "1정", "0.5정", "2캡슐"을 숫자와 단위로 나눈다. 숫자가 없으면 amount는 null. */
    data class Dosage(val amount: Double?, val unit: String)

    fun parseDosage(dosagePerTake: String?, medicineName: String): Dosage {
        val text = dosagePerTake?.trim().orEmpty()
        val number = text.takeWhile { it.isDigit() || it == '.' }
        val unit = text.drop(number.length).trim().ifEmpty { defaultUnit(medicineName) }
        return Dosage(amount = number.toDoubleOrNull()?.takeIf { it > 0 }, unit = unit)
    }

    /** 약품명으로 단위를 정한다. 캡슐·과립이 아니면 정. */
    fun defaultUnit(medicineName: String): String = when {
        "캡슐" in medicineName -> "캡슐"
        "과립" in medicineName -> "포"
        else -> "정"
    }

    /** 0.5 단위 복용량을 "1", "0.5"처럼 소수점 없이 짧게 쓴다. */
    fun formatAmount(amount: Double): String =
        if (amount % 1.0 == 0.0) amount.toInt().toString() else String.format(Locale.US, "%.1f", amount)

    /**
     * 하루 횟수에 맞는 기본 복용 시각. 식사 시간을 기준으로 하고, 4회부터 취침 전을 넣는다.
     * 5회 이상은 07:00~22:00을 30분 단위로 나눈다.
     */
    fun defaultTimes(dailyFrequency: Int): List<String> = when (dailyFrequency) {
        1 -> listOf("08:00")
        2 -> listOf("08:00", "19:00")
        3 -> listOf("08:00", "12:30", "18:30")
        4 -> listOf("08:00", "12:30", "18:30", "22:00")
        else -> {
            val count = dailyFrequency.coerceAtLeast(1)
            val start = 7 * 60
            val end = 22 * 60
            List(count) { index ->
                val minutes = start + (end - start) * index / (count - 1)
                val rounded = (minutes + 15) / 30 * 30
                String.format(Locale.US, "%02d:%02d", rounded / 60, rounded % 60)
            }
        }
    }

    /**
     * 횟수가 바뀔 때의 복용 시각. 새 횟수의 기본값에서 시작해, 사용자가 고친 시각은 그대로 남긴다.
     * 고친 시각 하나가 가장 가까운 기본값 하나를 대신하고, 결과는 시간순이다.
     * 고친 시각이 새 횟수보다 많으면 이른 시각부터 남긴다.
     */
    fun resizeTimes(current: List<String>, dailyFrequency: Int): List<String> {
        val defaults = defaultTimes(dailyFrequency)
        if (current.isEmpty()) return defaults
        // 시각은 고칠 때마다 정렬되어 자리가 바뀌므로, 자리가 아니라 값으로 이전 기본값과 비교해 고친 시각을 고른다.
        val unmatchedDefaults = defaultTimes(current.size).toMutableList()
        val kept = current.filterNot { unmatchedDefaults.remove(it) }.sorted().take(dailyFrequency)
        val remaining = defaults.toMutableList()
        kept.forEach { time -> remaining.remove(remaining.minBy { abs(minutesOf(it) - minutesOf(time)) }) }
        return (kept + remaining).sorted()
    }

    /** 시각 칩 하나를 [time]으로 고친 뒤 시간순으로 다시 놓는다. 칩과 알람 카드가 늘 시간순으로 보이게 한다. */
    fun replaceTime(times: List<String>, index: Int, time: String): List<String> =
        times.toMutableList().also { it[index] = time }.sorted()

    private fun minutesOf(time: String): Int =
        time.substringBefore(":").toInt() * 60 + time.substringAfter(":").toInt()

    /** "08:00"이 하루 중 언제인지 짧게 부른다. 시각 칩의 이름표에 쓴다. */
    fun timeOfDayLabel(time: String): TimeOfDay {
        val hour = time.substringBefore(":").toIntOrNull() ?: 0
        return when (hour) {
            in 4..10 -> TimeOfDay.MORNING
            in 11..15 -> TimeOfDay.NOON
            in 16..20 -> TimeOfDay.EVENING
            else -> TimeOfDay.BEDTIME
        }
    }

    enum class TimeOfDay { MORNING, NOON, EVENING, BEDTIME }
}
