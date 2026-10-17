package io.github.koolunkle.yakallim.ui.camera

data class CameraUiState(
    val isInitialized: Boolean = false,
    val isCapturing: Boolean = false,
    val errorMessage: String? = null
)
