package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore(name = "house12_prefs")

data class GameSettings(
    val lookSensitivity: Float = 1.0f,
    val psxJitterEnabled: Boolean = true,
    val crtScanlinesEnabled: Boolean = true,
    val masterVolume: Float = 0.85f,
    val unlockedEndings: Set<String> = emptySet()
)

class GamePreferences(private val context: Context) {
    private object Keys {
        val LOOK_SENSITIVITY = floatPreferencesKey("look_sensitivity")
        val PSX_JITTER = booleanPreferencesKey("psx_jitter")
        val CRT_SCANLINES = booleanPreferencesKey("crt_scanlines")
        val MASTER_VOLUME = floatPreferencesKey("master_volume")
        val UNLOCKED_ENDINGS = stringSetPreferencesKey("unlocked_endings")
    }

    val settingsFlow: Flow<GameSettings> = context.gameDataStore.data.map { prefs ->
        GameSettings(
            lookSensitivity = prefs[Keys.LOOK_SENSITIVITY] ?: 1.0f,
            psxJitterEnabled = prefs[Keys.PSX_JITTER] ?: true,
            crtScanlinesEnabled = prefs[Keys.CRT_SCANLINES] ?: true,
            masterVolume = prefs[Keys.MASTER_VOLUME] ?: 0.85f,
            unlockedEndings = prefs[Keys.UNLOCKED_ENDINGS] ?: emptySet()
        )
    }

    suspend fun setLookSensitivity(value: Float) {
        context.gameDataStore.edit { it[Keys.LOOK_SENSITIVITY] = value.coerceIn(0.4f, 2.2f) }
    }

    suspend fun setPsxJitter(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.PSX_JITTER] = enabled }
    }

    suspend fun setCrtScanlines(enabled: Boolean) {
        context.gameDataStore.edit { it[Keys.CRT_SCANLINES] = enabled }
    }

    suspend fun setMasterVolume(value: Float) {
        context.gameDataStore.edit { it[Keys.MASTER_VOLUME] = value.coerceIn(0f, 1f) }
    }

    suspend fun recordEndingUnlocked(endingId: String) {
        context.gameDataStore.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_ENDINGS] ?: emptySet()
            prefs[Keys.UNLOCKED_ENDINGS] = current + endingId
        }
    }
}
