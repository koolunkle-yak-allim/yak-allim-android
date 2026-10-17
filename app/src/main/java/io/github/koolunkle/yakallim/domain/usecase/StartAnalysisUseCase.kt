package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import java.io.File
import javax.inject.Inject

class StartAnalysisUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(imageFile: File): String {
        return ocrRepository.startAnalysis(imageFile)
    }
}
