package com.uteacher.attenote.ui.screen.auth

import androidx.lifecycle.ViewModel
import com.uteacher.attenote.util.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthGateViewModel(
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthGateUiState())
    val uiState: StateFlow<AuthGateUiState> = _uiState.asStateFlow()

    fun onAuthSuccess() {
        _uiState.update { it.copy(authState = AuthState.SUCCESS) }
    }

    fun onAuthFailure(message: String) {
        _uiState.update {
            it.copy(
                authState = AuthState.FAILURE,
                errorMessage = message
            )
        }
    }

    fun onAuthError(message: String) {
        _uiState.update {
            it.copy(
                authState = AuthState.ERROR,
                errorMessage = message
            )
        }
    }

    fun onRetryClicked() {
        _uiState.update {
            it.copy(
                authState = AuthState.IDLE,
                errorMessage = null
            )
        }
    }
}
