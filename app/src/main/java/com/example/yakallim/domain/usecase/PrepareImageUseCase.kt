package com.example.yakallim.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.example.yakallim.domain.image.ImageProcessor
import java.io.File
import javax.inject.Inject

class PrepareImageUseCase @Inject constructor(
    private val imageProcessor: ImageProcessor
) {
    suspend fun fromUri(uri: Uri): File? = imageProcessor.uriToFile(uri)
    suspend fun fromBitmap(bitmap: Bitmap): File? = imageProcessor.bitmapToFile(bitmap)
}
