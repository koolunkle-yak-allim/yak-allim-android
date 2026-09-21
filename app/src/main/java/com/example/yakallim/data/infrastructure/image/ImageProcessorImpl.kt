package com.example.yakallim.data.infrastructure.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
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

            val maxDimension = 1920

            // 원본 전체를 메모리에 올리지 않고 크기만 먼저 확인한 뒤, 필요한 만큼만 축소 디코딩한다.
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight, maxDimension)
            }
            var bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return@withContext file
            val width = bitmap.width
            val height = bitmap.height

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
            FileOutputStream(processedFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            }
            bitmap.recycle()

            processedFile
        } catch (e: Exception) {
            Log.e(TAG, "이미지 전처리 실패", e)
            file
        }
    }

    override suspend fun uriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream =
                contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(context.cacheDir, "ocr_img_${UUID.randomUUID()}.jpg")

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "URI로부터 파일 생성 실패", e)
            null
        }
    }

    override suspend fun bitmapToFile(bitmap: Bitmap): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "ocr_img_${UUID.randomUUID()}.jpg")

            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "Bitmap 파일 저장 실패", e)
            null
        }
    }

    companion object {
        private const val TAG = "ImageProcessorImpl"
    }
}

/**
 * [width]x[height] 원본을 [maxDimension] 근처까지 줄이기 위한 [BitmapFactory.Options.inSampleSize] 값을 계산한다.
 * 2의 거듭제곱 단위로만 축소되므로, 이후 정확한 목표 크기로의 추가 축소가 필요할 수 있다.
 */
internal fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var w = width
    var h = height
    var sample = 1
    while (maxOf(w, h) / 2 >= maxDimension) {
        w /= 2
        h /= 2
        sample *= 2
    }
    return sample
}
