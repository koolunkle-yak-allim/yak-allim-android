package io.github.koolunkle.yakallim.domain.alarm

import io.github.koolunkle.yakallim.domain.model.AlarmSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // 서울 1월 1일 07:00에 2일치를 등록하고, 파리(서울보다 8시간 늦음)로 옮겨 파리 1월 1일 00:00에 다시 계산한다.
    private val travelSpec = AlarmSpec(
        medicineName = "타이레놀",
        dosagePerTake = "1정",
        dailyFrequency = 2,
        durationDays = 2,
        alarmTimes = listOf("08:00", "19:00"),
        soundUri = null,
        startEpochMillis = epochMillisAt(2026, 1, 1, 7, 0),
        startZoneId = "Asia/Seoul"
    )
    private val paris: ZoneId = ZoneId.of("Europe/Paris")
    private val parisMidnight = ZonedDateTime.of(2026, 1, 1, 0, 0, 0, 0, paris).toInstant().toEpochMilli()

    private fun localTimes(spec: AlarmSpec): List<String> =
        AlarmTimeCalculator.triggerTimes(spec, parisMidnight, paris).map {
            Instant.ofEpochMilli(it.triggerMillis).atZone(paris).toLocalDateTime().toString()
        }

    @Test
    fun `시간대가 바뀌면 시작일은 그대로 두고 새 시간대의 벽시계 시각으로 계산한다`() {
        assertEquals(
            listOf("2026-01-01T08:00", "2026-01-01T19:00", "2026-01-02T08:00", "2026-01-02T19:00"),
            localTimes(travelSpec)
        )
    }

    @Test
    fun `등록 시간대가 없는 이전 알람은 지금 시간대의 날짜를 시작일로 쓴다`() {
        // 서울 1월 1일 07:00은 파리로 12월 31일이라, 첫날 두 번은 이미 지난 시각이 된다.
        assertEquals(
            listOf("2026-01-01T08:00", "2026-01-01T19:00"),
            localTimes(travelSpec.copy(startZoneId = null))
        )
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

    private fun oneDaySpecStartingAt(start: Long) = AlarmSpec(
        medicineName = "카두라엑스엘서방정",
        dosagePerTake = "1정",
        dailyFrequency = 2,
        durationDays = 1,
        alarmTimes = listOf("09:00", "19:00"),
        soundUri = null,
        startEpochMillis = start
    )

    @Test
    fun `마지막 알람 전이면 끝나지 않은 것으로 본다`() {
        val spec = oneDaySpecStartingAt(epochMillisAt(2026, 1, 1, 8, 0))

        // 09:00은 지났지만 19:00이 남아 있다.
        assertFalse(AlarmTimeCalculator.isFinished(spec, epochMillisAt(2026, 1, 1, 12, 0), zone))
    }

    @Test
    fun `마지막 알람이 지나면 끝난 것으로 본다`() {
        val spec = oneDaySpecStartingAt(epochMillisAt(2026, 1, 1, 8, 0))

        assertTrue(AlarmTimeCalculator.isFinished(spec, epochMillisAt(2026, 1, 1, 19, 1), zone))
    }
}
