package io.github.koolunkle.yakallim.domain.usecase

import io.github.koolunkle.yakallim.domain.model.Prescription
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPrescriptionResultUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    operator fun invoke(jobId: String): Flow<Result<Prescription>> {
        return ocrRepository.fetchPrescriptionResult(jobId)
    }
}
