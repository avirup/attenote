package com.uteacher.attenote.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.uteacher.attenote.data.repository.internal.InputNormalizer
import com.uteacher.attenote.domain.model.FabPosition
import com.uteacher.attenote.domain.model.toFabPosition
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SettingsPreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsPreferencesRepository {

    private object PreferenceKeys {
        val IsSetupComplete = booleanPreferencesKey("is_setup_complete")
        val UserName = stringPreferencesKey("user_name")
        val UserInstitute = stringPreferencesKey("user_institute")
        val ProfileImagePath = stringPreferencesKey("profile_image_path")
        val BiometricEnabled = booleanPreferencesKey("biometric_enabled")
        val SessionFormat = stringPreferencesKey("session_format")
        val FabPosition = stringPreferencesKey("fab_position")
        val NotesOnlyModeEnabled = booleanPreferencesKey("notes_only_mode_enabled")
    }

    private val safeData: Flow<Preferences> = dataStore.data.catch { throwable ->
        if (throwable is IOException) {
            emit(emptyPreferences())
        } else {
            throw throwable
        }
    }

    override val isSetupComplete: Flow<Boolean> =
        safeData.map { it[PreferenceKeys.IsSetupComplete] ?: false }

    override val name: Flow<String> =
        safeData.map { it[PreferenceKeys.UserName] ?: "" }

    override val institute: Flow<String> =
        safeData.map { it[PreferenceKeys.UserInstitute] ?: "" }

    override val profileImagePath: Flow<String?> =
        safeData.map { it[PreferenceKeys.ProfileImagePath] }

    override val biometricEnabled: Flow<Boolean> =
        safeData.map { it[PreferenceKeys.BiometricEnabled] ?: false }

    override val sessionFormat: Flow<SessionFormat> =
        safeData.map { preferences ->
            val stored = preferences[PreferenceKeys.SessionFormat] ?: SessionFormat.CURRENT_YEAR.name
            runCatching { SessionFormat.valueOf(stored) }.getOrDefault(SessionFormat.CURRENT_YEAR)
        }

    override val fabPosition: Flow<FabPosition> =
        safeData.map { preferences ->
            (preferences[PreferenceKeys.FabPosition] ?: FabPosition.RIGHT.name).toFabPosition()
        }

    override val notesOnlyMode: Flow<Boolean> =
        safeData.map { preferences ->
            preferences[PreferenceKeys.NotesOnlyModeEnabled] ?: false
        }

    override suspend fun setSetupComplete(complete: Boolean) {
        dataStore.edit { it[PreferenceKeys.IsSetupComplete] = complete }
    }

    override suspend fun setName(name: String) {
        dataStore.edit { it[PreferenceKeys.UserName] = InputNormalizer.normalize(name) }
    }

    override suspend fun setInstitute(institute: String) {
        dataStore.edit { it[PreferenceKeys.UserInstitute] = InputNormalizer.normalize(institute) }
    }

    override suspend fun setProfileImagePath(path: String?) {
        dataStore.edit { preferences ->
            val normalized = path?.trim()?.ifBlank { null }
            if (normalized == null) {
                preferences.remove(PreferenceKeys.ProfileImagePath)
            } else {
                preferences[PreferenceKeys.ProfileImagePath] = normalized
            }
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.BiometricEnabled] = enabled }
    }

    override suspend fun setSessionFormat(format: SessionFormat) {
        dataStore.edit { it[PreferenceKeys.SessionFormat] = format.name }
    }

    override suspend fun setFabPosition(position: FabPosition) {
        dataStore.edit { it[PreferenceKeys.FabPosition] = position.name }
    }

    override suspend fun setNotesOnlyMode(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.NotesOnlyModeEnabled] = enabled }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
