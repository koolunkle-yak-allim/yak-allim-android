package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import javax.inject.Inject

class ClearLastAnalysisResultUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke() = ocrRepository.clearLastAnalysisResult()
}
