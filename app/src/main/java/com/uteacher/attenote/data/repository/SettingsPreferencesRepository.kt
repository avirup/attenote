package com.uteacher.attenote.data.repository

import com.uteacher.attenote.domain.model.FabPosition
import kotlinx.coroutines.flow.Flow

enum class SessionFormat {
    CURRENT_YEAR,
    ACADEMIC_YEAR
}

interface SettingsPreferencesRepository {
    val isSetupComplete: Flow<Boolean>
    val name: Flow<String>
    val institute: Flow<String>
    val profileImagePath: Flow<String?>
    val biometricEnabled: Flow<Boolean>
    val sessionFormat: Flow<SessionFormat>
    val fabPosition: Flow<FabPosition>
    val notesOnlyModeEnabled: Flow<Boolean>

    suspend fun setSetupComplete(complete: Boolean)
    suspend fun setName(name: String)
    suspend fun setInstitute(institute: String)
    suspend fun setProfileImagePath(path: String?)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setSessionFormat(format: SessionFormat)
    suspend fun setFabPosition(position: FabPosition)
    suspend fun setNotesOnlyModeEnabled(enabled: Boolean)

    suspend fun clearAll()
}
