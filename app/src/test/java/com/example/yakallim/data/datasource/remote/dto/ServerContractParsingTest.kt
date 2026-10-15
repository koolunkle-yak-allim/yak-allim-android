package com.example.yakallim.data.datasource.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 서버(yak-allim-server docs/api-contract.md)가 실제로 보내는 JSON을 그대로 파싱할 수 있는지 검증한다.
 * Json 설정은 NetworkModule.provideJson()과 같게 맞춘다.
 */
class ServerContractParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Test
    fun parsesCompletedJobWithoutRemovedFields() {
        val body = """
            {
              "jobId": "job-1",
              "status": "COMPLETED",
              "result": {
                "message": "복약 안내서 분석이 완료되었습니다.",
                "medicines": [
                  { "medicineName": "타이레놀정", "dosagePerTake": "1정", "dailyFrequency": 3, "durationDays": 5 },
                  { "medicineName": "게보린정", "dosagePerTake": null, "dailyFrequency": null, "durationDays": null }
                ]
              },
              "error": null
            }
        """.trimIndent()

        val job = json.decodeFromString<OcrJobResponse>(body)
        val medicines = job.result?.medicines.orEmpty()

        assertEquals(2, medicines.size)
        assertEquals("타이레놀정", medicines[0].medicineName)
        assertNull(medicines[1].dosagePerTake)
        assertNull(medicines[1].dailyFrequency)
    }

    @Test
    fun parsesProgressEventWithIsFinishedKey() {
        val body = """{ "step": "CANCELLED", "message": "작업이 취소되었습니다.", "progress": 100, "isFinished": true }"""

        val progress = json.decodeFromString<OcrProgressResponse>(body)

        assertEquals("CANCELLED", progress.step)
        assertTrue(progress.isFinished)
    }

    @Test
    fun parsesPreviouslySavedResultThatStillHasRemovedFields() {
        // 이전 버전 앱이 DataStore에 저장해 둔 결과(fileName, textBlocks, bounds 포함)도 읽을 수 있어야 한다.
        val saved = """
            {
              "fileName": "n8n_ocr_job-1",
              "message": "완료",
              "textBlocks": [],
              "medicines": [ { "medicineName": "타이레놀정", "bounds": [], "rawName": "타이레놀정", "autoCorrected": false } ]
            }
        """.trimIndent()

        val response = json.decodeFromString<OcrResponse>(saved)

        assertEquals("타이레놀정", response.medicines?.single()?.medicineName)
    }
}
