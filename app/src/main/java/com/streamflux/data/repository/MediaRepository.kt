package com.streamflux.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.streamflux.data.models.VideoFile
import com.streamflux.data.models.VideoFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {
    
    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v",
        "3gp", "mpg", "mpeg", "ts", "m2ts", "vob", "ogv"
    )
    
    suspend fun getVideoFolders(): List<VideoFolder> = withContext(Dispatchers.IO) {
        val folders = mutableMapOf<String, MutableList<VideoFile>>()
        
        getVideoFiles().forEach { video ->
            val folderVideos = folders.getOrPut(video.folderPath) { mutableListOf() }
            folderVideos.add(video)
        }
        
        folders.map { (path, videos) ->
            val folderName = path.substringAfterLast('/')
            VideoFolder(
                path = path,
                name = folderName.ifEmpty { "Root" },
                videoCount = videos.size,
                thumbnailUri = videos.firstOrNull()?.thumbnailUri
            )
        }.sortedBy { it.name }
    }
    
    suspend fun getVideosInFolder(folderPath: String): List<VideoFile> = 
        withContext(Dispatchers.IO) {
            getVideoFiles().filter { it.folderPath == folderPath }
                .sortedBy { it.displayName }
        }
    
    private suspend fun getVideoFiles(): List<VideoFile> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoFile>()
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA
        )
        
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        
        context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val mimeType = cursor.getString(mimeTypeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val data = cursor.getString(dataColumn)
                
                val uri = ContentUris.withAppendedId(collection, id)
                val folderPath = data.substringBeforeLast('/')
                
                val extension = name.substringAfterLast('.', "").lowercase()
                if (extension in videoExtensions) {
                    videos.add(
                        VideoFile(
                            id = id,
                            uri = uri,
                            displayName = name,
                            duration = duration,
                            size = size,
                            width = width,
                            height = height,
                            mimeType = mimeType,
                            dateAdded = dateAdded,
                            folderPath = folderPath,
                            thumbnailUri = uri
                        )
                    )
                }
            }
        }
        
        videos
    }
}
