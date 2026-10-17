package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.model.Progress
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAnalysisProgressUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    operator fun invoke(jobId: String): Flow<Progress> {
        return ocrRepository.observeAnalysisProgress(jobId)
    }
}
