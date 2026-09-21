package com.example.yakallim.testutil

import android.graphics.Bitmap
import android.net.Uri
import com.example.yakallim.domain.image.ImageProcessor
import java.io.File

class FakeImageProcessor : ImageProcessor {
    var fileToReturn: File? = File("fake-prescription-image.jpg")

    override suspend fun preprocess(file: File): File = file

    override suspend fun uriToFile(uri: Uri): File? = fileToReturn

    override suspend fun bitmapToFile(bitmap: Bitmap): File? = fileToReturn
}
