package com.streamflux.player

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.IVLCVout

class VlcPlayerEngine(
    private val context: Context,
    private val hardwareAcceleration: Boolean = true
) : PlayerEngine {
    
    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var stateListener: ((PlayerState) -> Unit)? = null
    private var surfaceView: SurfaceView? = null
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        val options = ArrayList<String>().apply {
            add("--audio-time-stretch")
            add("--aout=opensles")
            add("--avcodec-codec=h264")
            if (hardwareAcceleration) {
                add("--codec=mediacodec_ndk,mediacodec_jni,all")
                add("--video-filter=transform")
            }
            add("-vvv")
        }
        
        libVLC = LibVLC(context, options)
        mediaPlayer = MediaPlayer(libVLC).apply {
            setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Opening -> 
                        stateListener?.invoke(PlayerState.Preparing)
                    MediaPlayer.Event.Buffering -> 
                        stateListener?.invoke(PlayerState.Buffering)
                    MediaPlayer.Event.Playing -> 
                        stateListener?.invoke(PlayerState.Playing)
                    MediaPlayer.Event.Paused -> 
                        stateListener?.invoke(PlayerState.Paused)
                    MediaPlayer.Event.Stopped -> 
                        stateListener?.invoke(PlayerState.Idle)
                    MediaPlayer.Event.EndReached -> 
                        stateListener?.invoke(PlayerState.Ended)
                    MediaPlayer.Event.EncounteredError -> 
                        stateListener?.invoke(PlayerState.Error("Playback error"))
                }
            }
        }
    }
    
    override fun prepare(uri: Uri) {
        val media = Media(libVLC, uri).apply {
            setHWDecoderEnabled(hardwareAcceleration, hardwareAcceleration)
        }
        mediaPlayer?.media = media
        media.release()
    }
    
    override fun play() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.play()
        }
    }
    
    override fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }
    
    override fun seekTo(positionMs: Long) {
        mediaPlayer?.time = positionMs
    }
    
    override fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.vlcVout?.detachViews()
        mediaPlayer?.release()
        libVLC?.release()
        mediaPlayer = null
        libVLC = null
        surfaceView = null
    }
    
    override fun setPlaybackSpeed(speed: Float) {
        mediaPlayer?.rate = speed
    }
    
    override fun setAudioTrack(trackIndex: Int) {
        mediaPlayer?.setAudioTrack(trackIndex)
    }
    
    override fun setSubtitleTrack(trackIndex: Int) {
        mediaPlayer?.setSpuTrack(trackIndex)
    }
    
    override fun getAvailableAudioTracks(): List<TrackInfo> {
        val tracks = mutableListOf<TrackInfo>()
        mediaPlayer?.audioTracks?.forEach { track ->
            tracks.add(
                TrackInfo(
                    index = track.id,
                    language = track.language,
                    label = track.name ?: "Audio Track ${track.id}"
                )
            )
        }
        return tracks
    }
    
    override fun getAvailableSubtitleTracks(): List<TrackInfo> {
        val tracks = mutableListOf<TrackInfo>()
        mediaPlayer?.spuTracks?.forEach { track ->
            tracks.add(
                TrackInfo(
                    index = track.id,
                    language = track.language,
                    label = track.name ?: "Subtitle Track ${track.id}"
                )
            )
        }
        return tracks
    }
    
    override fun getCurrentPosition(): Long = mediaPlayer?.time ?: 0L
    
    override fun getDuration(): Long = mediaPlayer?.length ?: 0L
    
    override fun getBufferedPosition(): Long {
        // VLC doesn't provide exact buffered position
        return getCurrentPosition()
    }
    
    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
    
    override fun setOnStateChangedListener(listener: (PlayerState) -> Unit) {
        stateListener = listener
    }
    
    @Composable
    override fun VideoSurface() {
        val context = LocalContext.current
        val player = remember { this.mediaPlayer }
        
        DisposableEffect(Unit) {
            onDispose {
                player?.vlcVout?.detachViews()
            }
        }
        
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    surfaceView = this
                    player?.vlcVout?.apply {
                        setVideoView(this@apply)
                        attachViews()
                    }
                }
            }
        )
    }
}
