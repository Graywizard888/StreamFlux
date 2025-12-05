package com.streamflux.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamflux.data.models.PlaybackState
import com.streamflux.data.models.VideoFile
import com.streamflux.data.models.VideoOrientation
import com.streamflux.data.preferences.PreferencesManager
import com.streamflux.player.PlayerController
import com.streamflux.player.PlayerEngine
import com.streamflux.player.PlayerState
import com.streamflux.player.TrackInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val playerController = PlayerController(application)
    private val preferencesManager = PreferencesManager(application)
    
    var playerEngine: PlayerEngine? by mutableStateOf(null)
        private set
    
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    private val _currentVideo = MutableStateFlow<VideoFile?>(null)
    val currentVideo: StateFlow<VideoFile?> = _currentVideo.asStateFlow()
    
    private val _playlist = MutableStateFlow<List<VideoFile>>(emptyList())
    val playlist: StateFlow<List<VideoFile>> = _playlist.asStateFlow()
    
    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()
    
    private val _audioTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val audioTracks: StateFlow<List<TrackInfo>> = _audioTracks.asStateFlow()
    
    private val _subtitleTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    val subtitleTracks: StateFlow<List<TrackInfo>> = _subtitleTracks.asStateFlow()
    
    private val _videoOrientation = MutableStateFlow(VideoOrientation.VIDEO_ORIENTATION)
    val videoOrientation: StateFlow<VideoOrientation> = _videoOrientation.asStateFlow()
    
    private var progressUpdateJob: Job? = null
    
    init {
        viewModelScope.launch {
            val engineType = preferencesManager.playerEngine.first()
            val hwAccel = preferencesManager.hardwareAcceleration.first()
            
            playerEngine = playerController.initializeEngine(engineType, hwAccel)
            playerEngine?.setOnStateChangedListener { state ->
                handlePlayerStateChange(state)
            }
        }
    }
    
    fun playVideo(video: VideoFile, playlist: List<VideoFile> = emptyList()) {
        _currentVideo.value = video
        _playlist.value = playlist
        
        playerEngine?.apply {
            prepare(video.uri)
            play()
        }
        
        startProgressUpdates()
    }
    
    fun togglePlayPause() {
        playerEngine?.let { engine ->
            if (engine.isPlaying()) {
                engine.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
            } else {
                engine.play()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
            }
        }
    }
    
    fun seekTo(positionMs: Long) {
        playerEngine?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
    }
    
    fun setPlaybackSpeed(speed: Float) {
        playerEngine?.setPlaybackSpeed(speed)
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
    }
    
    fun setAudioTrack(trackIndex: Int) {
        playerEngine?.setAudioTrack(trackIndex)
        _playbackState.value = _playbackState.value.copy(audioTrack = trackIndex)
    }
    
    fun setSubtitleTrack(trackIndex: Int) {
        playerEngine?.setSubtitleTrack(trackIndex)
        _playbackState.value = _playbackState.value.copy(subtitleTrack = trackIndex)
    }
    
    fun loadTracks() {
        playerEngine?.let { engine ->
            _audioTracks.value = engine.getAvailableAudioTracks()
            _subtitleTracks.value = engine.getAvailableSubtitleTracks()
        }
    }
    
    fun setVideoOrientation(orientation: VideoOrientation) {
        _videoOrientation.value = orientation
    }
    
    fun toggleControls() {
        _showControls.value = !_showControls.value
    }
    
    fun showControls() {
        _showControls.value = true
    }
    
    fun hideControls() {
        _showControls.value = false
    }
    
    fun playNext() {
        val current = _currentVideo.value ?: return
        val playlist = _playlist.value
        val currentIndex = playlist.indexOf(current)
        
        if (currentIndex >= 0 && currentIndex < playlist.size - 1) {
            playVideo(playlist[currentIndex + 1], playlist)
        }
    }
    
    fun playPrevious() {
        val current = _currentVideo.value ?: return
        val playlist = _playlist.value
        val currentIndex = playlist.indexOf(current)
        
        if (currentIndex > 0) {
            playVideo(playlist[currentIndex - 1], playlist)
        }
    }
    
    private fun handlePlayerStateChange(state: PlayerState) {
        when (state) {
            is PlayerState.Playing -> {
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressUpdates()
            }
            is PlayerState.Paused -> {
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                stopProgressUpdates()
            }
            is PlayerState.Ready -> {
                playerEngine?.let { engine ->
                    _playbackState.value = _playbackState.value.copy(
                        duration = engine.getDuration()
                    )
                }
                loadTracks()
            }
            is PlayerState.Ended -> {
                playNext()
            }
            else -> {}
        }
    }
    
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = viewModelScope.launch {
            while (isActive) {
                playerEngine?.let { engine ->
                    _playbackState.value = _playbackState.value.copy(
                        currentPosition = engine.getCurrentPosition(),
                        bufferedPosition = engine.getBufferedPosition()
                    )
                }
                delay(100)
            }
        }
    }
    
    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        playerController.release()
    }
}
