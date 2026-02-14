package com.uteacher.attenote.ui.screen.settings

import android.net.Uri
import com.uteacher.attenote.data.repository.SessionFormat
import com.uteacher.attenote.domain.model.FabPosition

data class SettingsUiState(
    val userName: String = "",
    val instituteName: String = "",
    val profileImagePath: String? = null,
    val biometricEnabled: Boolean = false,
    val canEnableBiometric: Boolean = true,

    val sessionFormat: SessionFormat = SessionFormat.CURRENT_YEAR,
    val sessionPreview: String = "",

    val fabPosition: FabPosition = FabPosition.RIGHT,

    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val exportedFilePath: String? = null,

    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val importError: String? = null,

    val showExportConfirmDialog: Boolean = false,
    val showImportConfirmDialog: Boolean = false,
    val pendingImportUri: Uri? = null,
    val showBiometricDisabledWarningDialog: Boolean = false,

    val isLoading: Boolean = true,
    val profileSaveSuccess: Boolean = false,
    val profileSaveError: String? = null,

    val showImagePicker: Boolean = false
)
