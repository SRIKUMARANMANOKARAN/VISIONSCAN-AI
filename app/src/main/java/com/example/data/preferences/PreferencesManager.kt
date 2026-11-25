package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "visionscan_prefs")

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val largeTextMode: Boolean = false,
    val cameraConsentGranted: Boolean = false,
    val screenBreakReminderEnabled: Boolean = true,
    val screenBreakIntervalMinutes: Int = 20,
    val activeUserId: Long = -1L
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LANGUAGE = stringPreferencesKey("language_code")
        val LARGE_TEXT = booleanPreferencesKey("large_text_mode")
        val CAMERA_CONSENT = booleanPreferencesKey("camera_consent_granted")
        val SCREEN_BREAK_ENABLED = booleanPreferencesKey("screen_break_enabled")
        val SCREEN_BREAK_MINUTES = intPreferencesKey("screen_break_minutes")
        val ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            isDarkMode = prefs[Keys.DARK_MODE] ?: false,
            language = AppLanguage.fromCode(prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.code),
            largeTextMode = prefs[Keys.LARGE_TEXT] ?: false,
            cameraConsentGranted = prefs[Keys.CAMERA_CONSENT] ?: false,
            screenBreakReminderEnabled = prefs[Keys.SCREEN_BREAK_ENABLED] ?: true,
            screenBreakIntervalMinutes = prefs[Keys.SCREEN_BREAK_MINUTES] ?: 20,
            activeUserId = prefs[Keys.ACTIVE_USER_ID] ?: -1L
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    suspend fun setLargeTextMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LARGE_TEXT] = enabled }
    }

    suspend fun setCameraConsent(granted: Boolean) {
        context.dataStore.edit { it[Keys.CAMERA_CONSENT] = granted }
    }

    suspend fun setScreenBreakReminder(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SCREEN_BREAK_ENABLED] = enabled }
    }

    suspend fun setActiveUserId(userId: Long) {
        context.dataStore.edit { it[Keys.ACTIVE_USER_ID] = userId }
    }
}
