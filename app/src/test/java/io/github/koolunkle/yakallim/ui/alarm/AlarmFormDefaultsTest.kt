package io.github.koolunkle.yakallim.ui.alarm

import io.github.koolunkle.yakallim.ui.alarm.AlarmFormDefaults.TimeOfDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AlarmFormDefaultsTest {

    @Test
    fun `복용량 문자열을 숫자와 단위로 나눈다`() {
        assertEquals(AlarmFormDefaults.Dosage(1.0, "정"), AlarmFormDefaults.parseDosage("1정", "타이레놀정"))
        assertEquals(AlarmFormDefaults.Dosage(0.5, "정"), AlarmFormDefaults.parseDosage("0.5정", "타이레놀정"))
        assertEquals(AlarmFormDefaults.Dosage(2.0, "캡슐"), AlarmFormDefaults.parseDosage("2캡슐", "이모튼캡슐"))
    }

    @Test
    fun `단위가 없으면 약품명으로 단위를 정한다`() {
        assertEquals("캡슐", AlarmFormDefaults.parseDosage("1", "이모튼캡슐").unit)
        assertEquals("포", AlarmFormDefaults.parseDosage("1", "뮤코펙트과립").unit)
        assertEquals("정", AlarmFormDefaults.parseDosage("1", "타이레놀").unit)
    }

    @Test
    fun `사진에 없어 비어 있는 복용량은 숫자를 null로 둔다`() {
        assertNull(AlarmFormDefaults.parseDosage(null, "타이레놀정").amount)
        assertNull(AlarmFormDefaults.parseDosage("", "타이레놀정").amount)
        assertNull(AlarmFormDefaults.parseDosage("0정", "타이레놀정").amount)
        assertEquals("정", AlarmFormDefaults.parseDosage(null, "타이레놀정").unit)
    }

    @Test
    fun `복용량은 정수면 소수점 없이 쓴다`() {
        assertEquals("1", AlarmFormDefaults.formatAmount(1.0))
        assertEquals("0.5", AlarmFormDefaults.formatAmount(0.5))
        assertEquals("2.5", AlarmFormDefaults.formatAmount(2.5))
    }

    @Test
    fun `하루 횟수별 기본 시각은 식사 시간을 따른다`() {
        assertEquals(listOf("08:00"), AlarmFormDefaults.defaultTimes(1))
        assertEquals(listOf("08:00", "19:00"), AlarmFormDefaults.defaultTimes(2))
        assertEquals(listOf("08:00", "12:30", "18:30"), AlarmFormDefaults.defaultTimes(3))
        assertEquals(listOf("08:00", "12:30", "18:30", "22:00"), AlarmFormDefaults.defaultTimes(4))
    }

    @Test
    fun `5회 이상은 07시부터 22시까지 30분 단위로 나눈다`() {
        val times = AlarmFormDefaults.defaultTimes(6)
        assertEquals(6, times.size)
        assertEquals("07:00", times.first())
        assertEquals("22:00", times.last())
        times.forEach { assertEquals(0, it.substringAfter(":").toInt() % 30) }
        assertEquals(times.sorted(), times)
    }

    @Test
    fun `횟수가 늘면 고친 시각은 남기고 나머지를 기본값으로 채운다`() {
        assertEquals(
            listOf("07:30", "12:30", "18:30"),
            AlarmFormDefaults.resizeTimes(listOf("07:30"), 3)
        )
    }

    @Test
    fun `횟수가 줄면 늦은 시각부터 뺀다`() {
        assertEquals(
            listOf("07:30", "13:00"),
            AlarmFormDefaults.resizeTimes(listOf("07:30", "13:00", "20:00"), 2)
        )
        assertEquals(
            listOf("07:30", "13:00"),
            AlarmFormDefaults.resizeTimes(listOf("20:00", "07:30", "13:00"), 2)
        )
    }

    @Test
    fun `시각을 고치지 않았으면 새 횟수의 기본값을 쓴다`() {
        (1..8).forEach { from ->
            (1..8).forEach { to ->
                assertEquals(
                    AlarmFormDefaults.defaultTimes(to),
                    AlarmFormDefaults.resizeTimes(AlarmFormDefaults.defaultTimes(from), to)
                )
            }
        }
    }

    @Test
    fun `고친 시각은 가장 가까운 기본값 대신 들어가고 시간순으로 둔다`() {
        assertEquals(
            listOf("08:00", "12:30", "21:00"),
            AlarmFormDefaults.resizeTimes(listOf("08:00", "21:00"), 3)
        )
        assertEquals(
            listOf("08:00", "12:30", "21:00"),
            AlarmFormDefaults.resizeTimes(listOf("21:00", "19:00"), 3)
        )
    }

    @Test
    fun `횟수가 줄어도 고친 시각을 먼저 남긴다`() {
        assertEquals(listOf("21:00"), AlarmFormDefaults.resizeTimes(listOf("21:00", "19:00"), 1))
    }

    @Test
    fun `채우는 기본값은 남긴 시각과 겹치지 않는다`() {
        assertEquals(
            listOf("08:00", "12:30", "18:30"),
            AlarmFormDefaults.resizeTimes(listOf("12:30"), 3)
        )
    }

    @Test
    fun `시각을 하루 중 때로 나눈다`() {
        assertEquals(TimeOfDay.MORNING, AlarmFormDefaults.timeOfDayLabel("08:00"))
        assertEquals(TimeOfDay.NOON, AlarmFormDefaults.timeOfDayLabel("12:30"))
        assertEquals(TimeOfDay.EVENING, AlarmFormDefaults.timeOfDayLabel("18:30"))
        assertEquals(TimeOfDay.BEDTIME, AlarmFormDefaults.timeOfDayLabel("22:00"))
        assertEquals(TimeOfDay.BEDTIME, AlarmFormDefaults.timeOfDayLabel("02:00"))
    }
}
