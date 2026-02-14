package com.uteacher.attenote.ui.screen.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uteacher.attenote.data.repository.BackupSupportRepository
import com.uteacher.attenote.data.repository.SessionFormat
import com.uteacher.attenote.data.repository.SettingsPreferencesRepository
import com.uteacher.attenote.data.repository.internal.RepositoryResult
import com.uteacher.attenote.domain.model.FabPosition
import com.uteacher.attenote.util.BiometricHelper
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsPreferencesRepository,
    private val backupRepository: BackupSupportRepository,
    private val biometricHelper: BiometricHelper,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                combine(
                    settingsRepository.name,
                    settingsRepository.institute,
                    settingsRepository.profileImagePath,
                    settingsRepository.biometricEnabled,
                    settingsRepository.sessionFormat
                ) { name, institute, profilePath, biometric, sessionFormat ->
                    CoreSettingsSnapshot(
                        name = name,
                        institute = institute,
                        profilePath = profilePath,
                        biometric = biometric,
                        sessionFormat = sessionFormat
                    )
                },
                settingsRepository.fabPosition
            ) { core, fabPosition ->
                val isDeviceSecure = biometricHelper.isDeviceSecure()
                SettingsUiState(
                    userName = core.name,
                    instituteName = core.institute,
                    profileImagePath = core.profilePath,
                    biometricEnabled = core.biometric,
                    canEnableBiometric = isDeviceSecure,
                    sessionFormat = core.sessionFormat,
                    sessionPreview = computeSessionPreview(core.sessionFormat),
                    fabPosition = fabPosition,
                    showBiometricDisabledWarningDialog = core.biometric && !isDeviceSecure,
                    isLoading = false,
                    exportSuccess = _uiState.value.exportSuccess,
                    exportError = _uiState.value.exportError,
                    exportedFilePath = _uiState.value.exportedFilePath,
                    isExporting = _uiState.value.isExporting,
                    isImporting = _uiState.value.isImporting,
                    importSuccess = _uiState.value.importSuccess,
                    importError = _uiState.value.importError,
                    showExportConfirmDialog = _uiState.value.showExportConfirmDialog,
                    showImportConfirmDialog = _uiState.value.showImportConfirmDialog,
                    pendingImportUri = _uiState.value.pendingImportUri,
                    profileSaveSuccess = _uiState.value.profileSaveSuccess,
                    profileSaveError = _uiState.value.profileSaveError,
                    showImagePicker = _uiState.value.showImagePicker,
                    showImageAdjustDialog = _uiState.value.showImageAdjustDialog,
                    pendingProfileImageUri = _uiState.value.pendingProfileImageUri,
                    pendingImageRotationQuarterTurns = _uiState.value.pendingImageRotationQuarterTurns,
                    cropLeft = _uiState.value.cropLeft,
                    cropTop = _uiState.value.cropTop,
                    cropRight = _uiState.value.cropRight,
                    cropBottom = _uiState.value.cropBottom
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun computeSessionPreview(format: SessionFormat): String {
        val currentDate = LocalDate.now()
        return when (format) {
            SessionFormat.CURRENT_YEAR -> currentDate.year.toString()
            SessionFormat.ACADEMIC_YEAR -> {
                val cutoffDate = LocalDate.of(currentDate.year, 6, 30)
                if (currentDate <= cutoffDate) {
                    "${currentDate.year - 1}-${currentDate.year}"
                } else {
                    "${currentDate.year}-${currentDate.year + 1}"
                }
            }
        }
    }

    fun onUserNameChanged(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun onInstituteNameChanged(institute: String) {
        _uiState.update { it.copy(instituteName = institute) }
    }

    fun onProfileImagePickRequested() {
        _uiState.update { it.copy(showImagePicker = true) }
    }

    fun onImagePickerDismissed() {
        _uiState.update { it.copy(showImagePicker = false) }
    }

    fun onProfileImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                showImagePicker = false,
                showImageAdjustDialog = true,
                pendingProfileImageUri = uri,
                pendingImageRotationQuarterTurns = 0,
                cropLeft = 0.08f,
                cropTop = 0.08f,
                cropRight = 0.92f,
                cropBottom = 0.92f,
                profileSaveError = null
            )
        }
    }

    fun onRotateImageLeft() {
        _uiState.update {
            val next = (it.pendingImageRotationQuarterTurns + 3) % 4
            it.copy(pendingImageRotationQuarterTurns = next)
        }
    }

    fun onRotateImageRight() {
        _uiState.update {
            val next = (it.pendingImageRotationQuarterTurns + 1) % 4
            it.copy(pendingImageRotationQuarterTurns = next)
        }
    }

    fun onCropLeftDragged(deltaFraction: Float) {
        _uiState.update { state ->
            val next = (state.cropLeft + deltaFraction).coerceIn(0f, state.cropRight - MIN_CROP_SIZE)
            state.copy(cropLeft = next)
        }
    }

    fun onCropTopDragged(deltaFraction: Float) {
        _uiState.update { state ->
            val next = (state.cropTop + deltaFraction).coerceIn(0f, state.cropBottom - MIN_CROP_SIZE)
            state.copy(cropTop = next)
        }
    }

    fun onCropRightDragged(deltaFraction: Float) {
        _uiState.update { state ->
            val next = (state.cropRight + deltaFraction).coerceIn(state.cropLeft + MIN_CROP_SIZE, 1f)
            state.copy(cropRight = next)
        }
    }

    fun onCropBottomDragged(deltaFraction: Float) {
        _uiState.update { state ->
            val next = (state.cropBottom + deltaFraction).coerceIn(state.cropTop + MIN_CROP_SIZE, 1f)
            state.copy(cropBottom = next)
        }
    }

    fun onImageAdjustmentDismissed() {
        _uiState.update {
            it.copy(
                showImageAdjustDialog = false,
                pendingProfileImageUri = null,
                pendingImageRotationQuarterTurns = 0,
                cropLeft = 0.08f,
                cropTop = 0.08f,
                cropRight = 0.92f,
                cropBottom = 0.92f
            )
        }
    }

    fun onImageAdjustmentConfirmed() {
        val state = _uiState.value
        val uri = state.pendingProfileImageUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileSaveError = null) }

            try {
                val targetDir = File(context.filesDir, APP_IMAGES_DIR)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }

                val targetFile = File(targetDir, "profile_${System.currentTimeMillis()}.jpg")
                val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: throw IllegalStateException("Selected image could not be read")

                val exifRotation = readExifRotation(uri)
                val exifNormalizedQuarterTurns = ((exifRotation / 90) % 4 + 4) % 4
                val bitmapAfterExif = rotateBitmap(decodedBitmap, exifNormalizedQuarterTurns)
                if (bitmapAfterExif !== decodedBitmap) {
                    decodedBitmap.recycle()
                }

                val rotatedBitmap = rotateBitmap(bitmapAfterExif, state.pendingImageRotationQuarterTurns)
                if (rotatedBitmap !== bitmapAfterExif) {
                    bitmapAfterExif.recycle()
                }
                if (rotatedBitmap !== decodedBitmap) {
                    runCatching { decodedBitmap.recycle() }
                }
                val finalBitmap = cropByRect(
                    source = rotatedBitmap,
                    leftFraction = state.cropLeft,
                    topFraction = state.cropTop,
                    rightFraction = state.cropRight,
                    bottomFraction = state.cropBottom
                )
                if (finalBitmap !== rotatedBitmap) {
                    rotatedBitmap.recycle()
                }

                FileOutputStream(targetFile).use { output ->
                    if (!finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                        throw IllegalStateException("Failed to encode selected image")
                    }
                }
                finalBitmap.recycle()

                _uiState.value.profileImagePath?.let { oldPath ->
                    runCatching { File(oldPath).delete() }
                }

                settingsRepository.setProfileImagePath(targetFile.absolutePath)
                _uiState.update {
                    it.copy(
                        profileImagePath = targetFile.absolutePath,
                        isLoading = false,
                        showImageAdjustDialog = false,
                        pendingProfileImageUri = null,
                        pendingImageRotationQuarterTurns = 0,
                        cropLeft = 0.08f,
                        cropTop = 0.08f,
                        cropRight = 0.92f,
                        cropBottom = 0.92f
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveError = "Failed to save image: ${e.message}",
                        showImageAdjustDialog = false,
                        pendingProfileImageUri = null,
                        pendingImageRotationQuarterTurns = 0,
                        cropLeft = 0.08f,
                        cropTop = 0.08f,
                        cropRight = 0.92f,
                        cropBottom = 0.92f
                    )
                }
            }
        }
    }

    fun onProfileImageRemoved() {
        viewModelScope.launch {
            _uiState.value.profileImagePath?.let { path ->
                runCatching { File(path).delete() }
            }
            settingsRepository.setProfileImagePath(null)
            _uiState.update { it.copy(profileImagePath = null) }
        }
    }

    fun onBiometricToggled(enabled: Boolean) {
        if (enabled && !_uiState.value.canEnableBiometric) return

        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(enabled)
            _uiState.update { it.copy(biometricEnabled = enabled) }
        }
    }

    fun onBiometricDisabledWarningDismissed() {
        viewModelScope.launch {
            settingsRepository.setBiometricEnabled(false)
            _uiState.update {
                it.copy(
                    biometricEnabled = false,
                    showBiometricDisabledWarningDialog = false
                )
            }
        }
    }

    fun onSaveProfileClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileSaveError = null) }
            try {
                settingsRepository.setName(_uiState.value.userName.trim())
                settingsRepository.setInstitute(_uiState.value.instituteName.trim())

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveSuccess = true
                    )
                }
                delay(2000)
                _uiState.update { it.copy(profileSaveSuccess = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profileSaveError = "Failed to save profile: ${e.message}"
                    )
                }
            }
        }
    }

    fun onSessionFormatChanged(format: SessionFormat) {
        viewModelScope.launch {
            settingsRepository.setSessionFormat(format)
            _uiState.update {
                it.copy(
                    sessionFormat = format,
                    sessionPreview = computeSessionPreview(format)
                )
            }
        }
    }

    fun onFabPositionChanged(position: FabPosition) {
        viewModelScope.launch {
            settingsRepository.setFabPosition(position)
            _uiState.update { it.copy(fabPosition = position) }
        }
    }

    fun onExportBackupRequested() {
        _uiState.update { it.copy(showExportConfirmDialog = true) }
    }

    fun onExportConfirmed() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showExportConfirmDialog = false,
                    isExporting = true,
                    exportSuccess = false,
                    exportError = null,
                    exportedFilePath = null
                )
            }

            when (val result = backupRepository.exportBackup()) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccess = true,
                            exportedFilePath = result.data
                        )
                    }
                    delay(5000)
                    _uiState.update { it.copy(exportSuccess = false, exportedFilePath = null) }
                }

                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onExportCancelled() {
        _uiState.update { it.copy(showExportConfirmDialog = false) }
    }

    fun onImportBackupRequested() {
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null) }
    }

    fun onImportFileSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                pendingImportUri = uri,
                showImportConfirmDialog = true
            )
        }
    }

    fun onImportConfirmed() {
        val importUri = _uiState.value.pendingImportUri ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    showImportConfirmDialog = false,
                    isImporting = true,
                    importSuccess = false,
                    importError = null,
                    pendingImportUri = null
                )
            }

            when (val result = backupRepository.importBackup(importUri)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importSuccess = true
                        )
                    }
                }

                is RepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onImportCancelled() {
        _uiState.update { it.copy(showImportConfirmDialog = false, pendingImportUri = null) }
    }

    fun onImportSuccessAcknowledged() {
        _uiState.update { it.copy(importSuccess = false) }
    }

    fun onErrorDismissed() {
        _uiState.update {
            it.copy(
                exportError = null,
                importError = null,
                profileSaveError = null
            )
        }
    }

    private companion object {
        const val APP_IMAGES_DIR = "app_images"
        const val MIN_CROP_SIZE = 0.08f
    }

    private fun rotateBitmap(source: Bitmap, quarterTurns: Int): Bitmap {
        val normalizedTurns = ((quarterTurns % 4) + 4) % 4
        if (normalizedTurns == 0) return source
        val matrix = Matrix().apply { postRotate(90f * normalizedTurns) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun cropByRect(
        source: Bitmap,
        leftFraction: Float,
        topFraction: Float,
        rightFraction: Float,
        bottomFraction: Float
    ): Bitmap {
        val left = (source.width * leftFraction).toInt().coerceIn(0, source.width - 1)
        val top = (source.height * topFraction).toInt().coerceIn(0, source.height - 1)
        val right = (source.width * rightFraction).toInt().coerceIn(left + 1, source.width)
        val bottom = (source.height * bottomFraction).toInt().coerceIn(top + 1, source.height)
        val width = (right - left).coerceAtLeast(1)
        val height = (bottom - top).coerceAtLeast(1)
        return Bitmap.createBitmap(source, left, top, width, height)
    }

    private fun readExifRotation(uri: Uri): Int {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                when (ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        }.getOrDefault(0)
    }
}

private data class CoreSettingsSnapshot(
    val name: String,
    val institute: String,
    val profilePath: String?,
    val biometric: Boolean,
    val sessionFormat: SessionFormat
)
