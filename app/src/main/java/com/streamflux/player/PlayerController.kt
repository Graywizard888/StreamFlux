package com.streamflux.player

import android.content.Context
import android.net.Uri
import com.streamflux.data.models.PlayerEngine as PlayerEngineType

class PlayerController(
    private val context: Context
) {
    private var currentEngine: PlayerEngine? = null
    
    fun initializeEngine(
        engineType: PlayerEngineType,
        hardwareAcceleration: Boolean = true
    ): PlayerEngine {
        currentEngine?.release()
        
        currentEngine = when (engineType) {
            PlayerEngineType.EXOPLAYER -> ExoPlayerEngine(context, hardwareAcceleration)
            PlayerEngineType.VLC -> VlcPlayerEngine(context, hardwareAcceleration)
        }
        
        return currentEngine!!
    }
    
    fun release() {
        currentEngine?.release()
        currentEngine = null
    }
}
