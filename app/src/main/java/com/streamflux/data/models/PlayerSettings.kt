package com.streamflux.data.models

enum class PlayerEngine {
    EXOPLAYER,
    VLC
}

enum class VideoOrientation {
    PORTRAIT,
    LANDSCAPE,
    LANDSCAPE_REVERSE,
    VIDEO_ORIENTATION
}

data class PlayerSettings(
    val engine: PlayerEngine = PlayerEngine.EXOPLAYER,
    val hardwareAcceleration: Boolean = true,
    val preferredOrientation: VideoOrientation = VideoOrientation.VIDEO_ORIENTATION
)

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val audioTrack: Int = 0,
    val subtitleTrack: Int = -1
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration else 0f
}
