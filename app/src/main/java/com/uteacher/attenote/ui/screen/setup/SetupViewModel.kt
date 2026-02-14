package com.uteacher.attenote.ui.screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import com.uteacher.attenote.util.BiometricHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    private val settingsRepo: SettingsPreferencesRepository,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isDeviceSecure = biometricHelper.isDeviceSecure()) }
    }

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun onInstituteChanged(institute: String) {
        _uiState.update { it.copy(institute = institute, error = null) }
    }

    fun onProfileImageSelected(imagePath: String?) {
        _uiState.update { it.copy(profileImagePath = imagePath, error = null) }
    }

    fun onBiometricToggled(enabled: Boolean) {
        if (_uiState.value.isDeviceSecure) {
            _uiState.update { it.copy(biometricEnabled = enabled, error = null) }
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.name.trim().isEmpty()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            runCatching {
                settingsRepo.setName(state.name.trim())
                settingsRepo.setInstitute(state.institute.trim())
                settingsRepo.setProfileImagePath(state.profileImagePath)
                settingsRepo.setBiometricEnabled(state.biometricEnabled)
                settingsRepo.setSetupComplete(true)
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSaveComplete = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save: ${throwable.message}"
                    )
                }
            }
        }
    }
}
