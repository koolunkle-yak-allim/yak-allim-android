package com.example.yakallim.data.infrastructure.fcm

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 서버(yak-allim-server)의 docs/api-contract.md에 문서화된 FCM data payload 키와
 * 일치하는지 확인한다. 서버는 실패 사유를 "error"가 아닌 "message" 키로 보낸다.
 */
class FcmPayloadSpecTest {

    @Test
    fun keyErrorMatchesServerContract() {
        assertEquals("message", FcmPayloadSpec.KEY_ERROR)
    }

    @Test
    fun keyJobIdMatchesServerContract() {
        assertEquals("jobId", FcmPayloadSpec.KEY_JOB_ID)
    }

    @Test
    fun keyStatusMatchesServerContract() {
        assertEquals("status", FcmPayloadSpec.KEY_STATUS)
    }
}
