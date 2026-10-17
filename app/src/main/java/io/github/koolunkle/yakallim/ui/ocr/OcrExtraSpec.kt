package io.github.koolunkle.yakallim.ui.ocr

import android.content.Intent

/** 알림을 눌러 OCR 화면을 열 때 Intent에 담는 값의 키. 보내는 쪽(FcmMessagingService)도 이 상수를 쓴다. */
object OcrExtraSpec {
    const val KEY_JOB_ID = "jobId"

    fun readJobId(intent: Intent?): String? = intent?.getStringExtra(KEY_JOB_ID)
}
