package com.streamflux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamflux.data.models.PlayerEngine
import com.streamflux.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferencesManager = PreferencesManager(application)
    
    val isDarkMode = preferencesManager.isDarkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    
    val playerEngine = preferencesManager.playerEngine
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerEngine.EXOPLAYER)
    
    val hardwareAcceleration = preferencesManager.hardwareAcceleration
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    
    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(enabled)
        }
    }
    
    fun setPlayerEngine(engine: PlayerEngine) {
        viewModelScope.launch {
            preferencesManager.setPlayerEngine(engine)
        }
    }
    
    fun setHardwareAcceleration(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHardwareAcceleration(enabled)
        }
    }
}
