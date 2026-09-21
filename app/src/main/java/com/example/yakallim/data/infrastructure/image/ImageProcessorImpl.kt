package com.example.yakallim.data.infrastructure.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.example.yakallim.domain.image.ImageProcessor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessorImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageProcessor {

    override suspend fun preprocess(file: File): File = withContext(Dispatchers.IO) {
        try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            var bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return@withContext file
            val width = bitmap.width
            val height = bitmap.height

            val maxDimension = 1920

            if (width > maxDimension || height > maxDimension) {
                val newWidth: Int
                val newHeight: Int
                if (width > height) {
                    newWidth = maxDimension
                    newHeight = (height * (maxDimension.toDouble() / width)).toInt()
                } else {
                    newHeight = maxDimension
                    newWidth = (width * (maxDimension.toDouble() / height)).toInt()
                }

                val scaledBitmap = bitmap.scale(newWidth, newHeight)
                if (bitmap != scaledBitmap) {
                    bitmap.recycle()
                }

                bitmap = scaledBitmap
            }

            if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (bitmap != rotatedBitmap) {
                    bitmap.recycle()
                }
                bitmap = rotatedBitmap
            }

            val processedFile = File(context.cacheDir, "ocr_processed_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(processedFile)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            bitmap.recycle()

            processedFile
        } catch (e: Exception) {
            e.printStackTrace()
            file
        }
    }

    override suspend fun uriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream =
                contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(context.cacheDir, "ocr_img_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun bitmapToFile(bitmap: Bitmap): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "ocr_img_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}