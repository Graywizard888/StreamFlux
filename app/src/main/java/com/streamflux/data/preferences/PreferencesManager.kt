package com.streamflux.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.streamflux.data.models.PlayerEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PLAYER_ENGINE = stringPreferencesKey("player_engine")
        val HARDWARE_ACCELERATION = booleanPreferencesKey("hardware_acceleration")
    }
    
    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[Keys.DARK_MODE]
    }
    
    val playerEngine: Flow<PlayerEngine> = context.dataStore.data.map { preferences ->
        val engineName = preferences[Keys.PLAYER_ENGINE] ?: PlayerEngine.EXOPLAYER.name
        PlayerEngine.valueOf(engineName)
    }
    
    val hardwareAcceleration: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.HARDWARE_ACCELERATION] ?: true
    }
    
    suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(Keys.DARK_MODE)
            } else {
                preferences[Keys.DARK_MODE] = enabled
            }
        }
    }
    
    suspend fun setPlayerEngine(engine: PlayerEngine) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PLAYER_ENGINE] = engine.name
        }
    }
    
    suspend fun setHardwareAcceleration(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HARDWARE_ACCELERATION] = enabled
        }
    }
}
