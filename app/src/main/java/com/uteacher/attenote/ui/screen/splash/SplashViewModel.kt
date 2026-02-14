package com.uteacher.attenote.ui.screen.splash

import androidx.lifecycle.ViewModel
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SplashViewModel(
    private val settingsRepo: SettingsPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun onStartClicked() {
        _uiState.update { it.copy(isNavigating = true) }
    }
}
