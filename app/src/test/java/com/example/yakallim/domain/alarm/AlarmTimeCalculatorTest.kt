package com.example.yakallim.domain.alarm

import com.example.yakallim.domain.model.AlarmSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmTimeCalculatorTest {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    private fun epochMillisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant().toEpochMilli()

    @Test
    fun `하루 2회 3일치 총 6개의 발생 시각을 생성한다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 2,
            durationDays = 3,
            alarmTimes = listOf("09:00", "21:00"),
            soundUri = null,
            startEpochMillis = start
        )

        val triggers = AlarmTimeCalculator.allTriggers(spec, zone)

        assertEquals(6, triggers.size)
    }

    @Test
    fun `날짜 경계를 넘어가며 day가 순서대로 증가한다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 1,
            durationDays = 3,
            alarmTimes = listOf("09:00"),
            soundUri = null,
            startEpochMillis = start
        )

        val triggers = AlarmTimeCalculator.allTriggers(spec, zone)

        assertEquals(listOf(0, 1, 2), triggers.map { it.day })
        val expectedMillis = listOf(
            epochMillisAt(2026, 1, 1, 9, 0),
            epochMillisAt(2026, 1, 2, 9, 0),
            epochMillisAt(2026, 1, 3, 9, 0)
        )
        assertEquals(expectedMillis, triggers.map { it.triggerMillis })
    }

    @Test
    fun `이미 지난 시각은 제외하고 남은 시각만 반환한다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 2,
            durationDays = 2,
            alarmTimes = listOf("09:00", "21:00"),
            soundUri = null,
            startEpochMillis = start
        )

        // 1일차 09:00은 이미 지났고, 1일차 21:00부터가 미래인 시점
        val now = epochMillisAt(2026, 1, 1, 12, 0)

        val remaining = AlarmTimeCalculator.triggerTimes(spec, now, zone)

        assertEquals(3, remaining.size)
        assertTrue(remaining.all { it.triggerMillis > now })
    }

    @Test
    fun `모든 시각이 지났으면 빈 목록을 반환한다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 1,
            durationDays = 1,
            alarmTimes = listOf("09:00"),
            soundUri = null,
            startEpochMillis = start
        )

        val now = epochMillisAt(2026, 1, 2, 0, 0)

        val remaining = AlarmTimeCalculator.triggerTimes(spec, now, zone)

        assertTrue(remaining.isEmpty())
    }

    @Test
    fun `dailyFrequency가 alarmTimes 개수보다 작으면 totalAlarms만큼만 생성한다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 1,
            durationDays = 2,
            alarmTimes = listOf("09:00", "21:00"),
            soundUri = null,
            startEpochMillis = start
        )

        val triggers = AlarmTimeCalculator.allTriggers(spec, zone)

        // totalAlarms = dailyFrequency(1) * durationDays(2) = 2
        assertEquals(2, triggers.size)
    }

    @Test
    fun `같은 입력에 다른 시간대를 적용하면 발생 시각(epoch millis)이 달라진다`() {
        val start = epochMillisAt(2026, 1, 1, 8, 0)
        val spec = AlarmSpec(
            medicineName = "타이레놀",
            dosagePerTake = "1정",
            dailyFrequency = 1,
            durationDays = 1,
            alarmTimes = listOf("09:00"),
            soundUri = null,
            startEpochMillis = start
        )

        val seoulTrigger = AlarmTimeCalculator.allTriggers(spec, ZoneId.of("Asia/Seoul")).single()
        val utcTrigger = AlarmTimeCalculator.allTriggers(spec, ZoneId.of("UTC")).single()

        assertTrue(seoulTrigger.triggerMillis != utcTrigger.triggerMillis)
    }

    @Test
    fun `약품명과 day, timeIndex가 같으면 항상 같은 요청 코드를 반환한다`() {
        val codeA = AlarmTimeCalculator.requestCode("타이레놀", day = 2, timeIndex = 1)
        val codeB = AlarmTimeCalculator.requestCode("타이레놀", day = 2, timeIndex = 1)

        assertEquals(codeA, codeB)
    }

    @Test
    fun `day나 timeIndex가 다르면 다른 요청 코드를 반환한다`() {
        val base = AlarmTimeCalculator.requestCode("타이레놀", day = 0, timeIndex = 0)
        val differentDay = AlarmTimeCalculator.requestCode("타이레놀", day = 1, timeIndex = 0)
        val differentTimeIndex = AlarmTimeCalculator.requestCode("타이레놀", day = 0, timeIndex = 1)

        assertTrue(base != differentDay)
        assertTrue(base != differentTimeIndex)
    }
}
