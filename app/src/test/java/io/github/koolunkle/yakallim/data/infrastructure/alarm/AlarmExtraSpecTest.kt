package io.github.koolunkle.yakallim.data.infrastructure.alarm

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/** 키 값을 바꾼 뒤에도 이전 키로 예약된 알람의 알람음을 읽을 수 있는지 확인한다. */
class AlarmExtraSpecTest {

    private fun intentWith(extras: Map<String, String>): Intent = mock {
        on { getStringExtra(any()) } doReturn null
        extras.forEach { (key, value) -> on { getStringExtra(key) } doReturn value }
    }

    @Test
    fun readSoundUri_readsCurrentKey() {
        val intent = intentWith(mapOf("SOUND_URI" to "content://sound/current"))

        assertEquals("content://sound/current", AlarmExtraSpec.readSoundUri(intent))
    }

    @Test
    fun readSoundUri_fallsBackToLegacyKeyForAlarmsScheduledBeforeRename() {
        val intent = intentWith(mapOf("KEY_SOUND_URI" to "content://sound/legacy"))

        assertEquals("content://sound/legacy", AlarmExtraSpec.readSoundUri(intent))
    }

    @Test
    fun readSoundUri_prefersCurrentKeyWhenBothExist() {
        val intent = intentWith(
            mapOf("SOUND_URI" to "content://sound/current", "KEY_SOUND_URI" to "content://sound/legacy")
        )

        assertEquals("content://sound/current", AlarmExtraSpec.readSoundUri(intent))
    }

    @Test
    fun readSoundUri_returnsNullWhenNoSoundWasChosen() {
        assertNull(AlarmExtraSpec.readSoundUri(intentWith(emptyMap())))
    }
}
