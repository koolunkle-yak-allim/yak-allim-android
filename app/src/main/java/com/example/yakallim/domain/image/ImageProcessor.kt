package com.example.yakallim.domain.image

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface ImageProcessor {
    suspend fun preprocess(file: File): File
    suspend fun uriToFile(uri: Uri): File?
    suspend fun bitmapToFile(bitmap: Bitmap): File?
}
