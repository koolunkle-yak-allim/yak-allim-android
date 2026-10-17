package io.github.koolunkle.yakallim.ui.camera

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState = _uiState.asStateFlow()

    fun setInitialized(initialized: Boolean) {
        _uiState.update { it.copy(isInitialized = initialized) }
    }

    fun setCapturing(capturing: Boolean) {
        _uiState.update { it.copy(isCapturing = capturing) }
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(errorMessage = message, isCapturing = false) }
    }
}
