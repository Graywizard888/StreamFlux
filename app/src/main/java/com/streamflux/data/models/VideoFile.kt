package com.streamflux.data.models

import android.net.Uri

data class VideoFile(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val duration: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val dateAdded: Long,
    val folderPath: String,
    val thumbnailUri: Uri? = null
) {
    val resolution: String
        get() = "${width}x${height}"
    
    val is4K: Boolean
        get() = width >= 3840 || height >= 2160
    
    val durationFormatted: String
        get() {
            val seconds = duration / 1000
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%d:%02d", minutes, secs)
            }
        }
}

data class VideoFolder(
    val path: String,
    val name: String,
    val videoCount: Int,
    val thumbnailUri: Uri? = null
)
