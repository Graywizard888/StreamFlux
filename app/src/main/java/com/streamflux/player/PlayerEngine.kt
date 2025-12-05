package com.streamflux.player

import android.net.Uri
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

interface PlayerEngine {
    fun prepare(uri: Uri)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
    fun setPlaybackSpeed(speed: Float)
    fun setAudioTrack(trackIndex: Int)
    fun setSubtitleTrack(trackIndex: Int)
    fun getAvailableAudioTracks(): List<TrackInfo>
    fun getAvailableSubtitleTracks(): List<TrackInfo>
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun getBufferedPosition(): Long
    fun isPlaying(): Boolean
    fun setOnStateChangedListener(listener: (PlayerState) -> Unit)
    
    @Composable
    fun VideoSurface()
}

data class TrackInfo(
    val index: Int,
    val language: String?,
    val label: String
)

sealed class PlayerState {
    object Idle : PlayerState()
    object Preparing : PlayerState()
    object Ready : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    object Buffering : PlayerState()
    data class Error(val message: String) : PlayerState()
    object Ended : PlayerState()
}
