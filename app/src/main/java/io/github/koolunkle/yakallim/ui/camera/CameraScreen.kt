package io.github.koolunkle.yakallim.ui.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.koolunkle.yakallim.R

/** 복약 안내서는 대개 A4를 접은 세로 종이라, 가이드 테두리를 세로로 긴 비율로 둔다. */
private const val GUIDE_ASPECT_RATIO = 3f / 4f

@Composable
fun CameraScreen(
    onCaptureSuccess: (Bitmap) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }

    val shutterDescription = stringResource(R.string.camera_cd_shutter)

    BackHandler(onBack = onDismiss)

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner = lifecycleOwner,
                        cameraSelector = cameraSelector,
                        preview, imageCapture
                    )
                    viewModel.setInitialized(true)
                } catch (e: Exception) {
                    Log.e("CameraScreen", "카메라 미리보기 및 촬영 기능 초기화 실패")
                    viewModel.setError(e.message)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        // 문서 가이드 테두리. 카메라 영상 위라 테마와 관계없이 흰 선을 쓴다.
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(GUIDE_ASPECT_RATIO)
                    .border(width = 2.dp, color = Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(16.dp))
            )
            Text(
                text = stringResource(R.string.camera_guide_hint),
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .statusBarsPadding()
                .padding(all = 16.dp)
                .size(48.dp)
                .align(alignment = Alignment.TopStart)
                .background(color = Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.camera_cd_close),
                tint = Color.White
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .align(alignment = Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(size = 72.dp)
                    .border(width = 4.dp, color = Color.White, shape = CircleShape)
                    .padding(all = 6.dp)
                    .background(color = Color.Transparent, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (uiState.isInitialized && !uiState.isCapturing) {
                            viewModel.setCapturing(true)
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        try {
                                            val rotation = imageProxy.imageInfo.rotationDegrees
                                            val bitmap = imageProxy.toBitmap()
                                            val rotatedBitmap = if (rotation != 0) {
                                                val matrix =
                                                    Matrix().apply { postRotate(rotation.toFloat()) }
                                                Bitmap.createBitmap(
                                                    bitmap,
                                                    0,
                                                    0,
                                                    bitmap.width,
                                                    bitmap.height,
                                                    matrix,
                                                    true
                                                )
                                            } else {
                                                bitmap
                                            }
                                            viewModel.setCapturing(false)
                                            onCaptureSuccess(rotatedBitmap)
                                        } catch (e: Exception) {
                                            Log.e("CameraScreen", "이미지 회전 및 변환 실패")
                                            viewModel.setError(e.message)
                                        } finally {
                                            imageProxy.close()
                                        }
                                    }

                                    override fun onError(e: ImageCaptureException) {
                                        Log.e("CameraScreen", "카메라 하드웨어 촬영 처리 실패")
                                        viewModel.setError(e.message)
                                    }
                                })
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = shutterDescription },
                    enabled = !uiState.isCapturing,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = if (uiState.isCapturing) Color.Gray else Color.White),
                    contentPadding = PaddingValues(all = 0.dp)
                ) {}
            }
        }
    }
}
