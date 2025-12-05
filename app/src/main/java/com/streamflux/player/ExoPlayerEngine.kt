package com.streamflux.player

import android.content.Context
import android.net.Uri
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.video.VideoSize

class ExoPlayerEngine(
    private val context: Context,
    private val hardwareAcceleration: Boolean = true
) : PlayerEngine {
    
    private var player: ExoPlayer? = null
    private var stateListener: ((PlayerState) -> Unit)? = null
    private var surfaceView: SurfaceView? = null
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredAudioLanguage("en")
                    .build()
            )
        }
        
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val state = when (playbackState) {
                            Player.STATE_IDLE -> PlayerState.Idle
                            Player.STATE_BUFFERING -> PlayerState.Buffering
                            Player.STATE_READY -> {
                                if (isPlaying) PlayerState.Playing else PlayerState.Ready
                            }
                            Player.STATE_ENDED -> PlayerState.Ended
                            else -> PlayerState.Idle
                        }
                        stateListener?.invoke(state)
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            stateListener?.invoke(PlayerState.Playing)
                        } else if (playbackState == Player.STATE_READY) {
                            stateListener?.invoke(PlayerState.Paused)
                        }
                    }
                    
                    override fun onPlayerError(error: com.google.android.exoplayer2.PlaybackException) {
                        stateListener?.invoke(PlayerState.Error(error.message ?: "Unknown error"))
                    }
                })
                
                playWhenReady = false
            }
    }
    
    override fun prepare(uri: Uri) {
        player?.apply {
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
        }
        stateListener?.invoke(PlayerState.Preparing)
    }
    
    override fun play() {
        player?.play()
    }
    
    override fun pause() {
        player?.pause()
    }
    
    override fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }
    
    override fun release() {
        player?.release()
        player = null
        surfaceView = null
    }
    
    override fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackParameters(
            PlaybackParameters(speed)
        )
    }
    
    override fun setAudioTrack(trackIndex: Int) {
        player?.let { exoPlayer ->
            val trackSelector = exoPlayer.trackSelector as? DefaultTrackSelector
            trackSelector?.parameters = trackSelector?.buildUponParameters()
                ?.setPreferredAudioLanguage(null)
                ?.setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                ?.build() ?: return
        }
    }
    
    override fun setSubtitleTrack(trackIndex: Int) {
        player?.let { exoPlayer ->
            val trackSelector = exoPlayer.trackSelector as? DefaultTrackSelector
            if (trackIndex == -1) {
                trackSelector?.parameters = trackSelector?.buildUponParameters()
                    ?.setRendererDisabled(C.TRACK_TYPE_TEXT, true)
                    ?.build() ?: return
            } else {
                trackSelector?.parameters = trackSelector?.buildUponParameters()
                    ?.setRendererDisabled(C.TRACK_TYPE_TEXT, false)
                    ?.build() ?: return
            }
        }
    }
    
    override fun getAvailableAudioTracks(): List<TrackInfo> {
        val tracks = mutableListOf<TrackInfo>()
        player?.currentTracks?.groups?.forEachIndexed { index, group ->
            if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    tracks.add(
                        TrackInfo(
                            index = tracks.size,
                            language = format.language,
                            label = format.label ?: "Audio Track ${tracks.size + 1}"
                        )
                    )
                }
            }
        }
        return tracks
    }
    
    override fun getAvailableSubtitleTracks(): List<TrackInfo> {
        val tracks = mutableListOf<TrackInfo>()
        player?.currentTracks?.groups?.forEachIndexed { index, group ->
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    tracks.add(
                        TrackInfo(
                            index = tracks.size,
                            language = format.language,
                            label = format.label ?: "Subtitle Track ${tracks.size + 1}"
                        )
                    )
                }
            }
        }
        return tracks
    }
    
    override fun getCurrentPosition(): Long = player?.currentPosition ?: 0L
    
    override fun getDuration(): Long = player?.duration?.takeIf { it != C.TIME_UNSET } ?: 0L
    
    override fun getBufferedPosition(): Long = player?.bufferedPosition ?: 0L
    
    override fun isPlaying(): Boolean = player?.isPlaying ?: false
    
    override fun setOnStateChangedListener(listener: (PlayerState) -> Unit) {
        stateListener = listener
    }
    
    @Composable
    override fun VideoSurface() {
        val context = LocalContext.current
        val player = remember { this.player }
        
        DisposableEffect(Unit) {
            onDispose { }
        }
        
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    surfaceView = this
                    player?.setVideoSurfaceView(this)
                }
            }
        )
    }
}
