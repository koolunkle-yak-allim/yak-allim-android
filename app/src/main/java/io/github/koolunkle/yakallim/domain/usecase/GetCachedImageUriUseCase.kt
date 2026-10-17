package io.github.koolunkle.yakallim.domain.usecase

import android.net.Uri
import io.github.koolunkle.yakallim.domain.repository.OcrRepository
import javax.inject.Inject

class GetCachedImageUriUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    operator fun invoke(jobId: String): Uri? = ocrRepository.getCachedImageUri(jobId)
    fun last(): Uri? = ocrRepository.getLastCachedImageUri()
}
