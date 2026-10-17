package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.model.Prescription
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import javax.inject.Inject

class GetLastAnalysisResultUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(): Prescription? = ocrRepository.getLastAnalysisResult()
}
