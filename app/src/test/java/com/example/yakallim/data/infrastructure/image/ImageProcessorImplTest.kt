package com.example.yakallim.data.infrastructure.image

import org.junit.Assert.assertEquals
import org.junit.Test

class ImageProcessorImplTest {

    @Test
    fun `이미지가 이미 최대 크기 이하이면 축소하지 않는다`() {
        val sample = calculateInSampleSize(width = 1000, height = 800, maxDimension = 1920)
        assertEquals(1, sample)
    }

    @Test
    fun `4K 이미지는 2배로 축소된다`() {
        val sample = calculateInSampleSize(width = 3840, height = 2160, maxDimension = 1920)
        assertEquals(2, sample)
    }

    @Test
    fun `48MP급 고해상도 이미지는 4배로 축소된다`() {
        val sample = calculateInSampleSize(width = 8000, height = 6000, maxDimension = 1920)
        assertEquals(4, sample)
    }

    @Test
    fun `가로세로 중 큰 값을 기준으로 판단한다`() {
        val sample = calculateInSampleSize(width = 500, height = 5000, maxDimension = 1920)
        assertEquals(2, sample)
    }

    @Test
    fun `경계값에서 과도하게 축소하지 않는다`() {
        // maxOf(w,h)/2 >= maxDimension 조건이 참이 되는 최소 지점 확인
        val justBelow = calculateInSampleSize(width = 3839, height = 1, maxDimension = 1920)
        val justAt = calculateInSampleSize(width = 3840, height = 1, maxDimension = 1920)

        assertEquals(1, justBelow)
        assertEquals(2, justAt)
    }
}
