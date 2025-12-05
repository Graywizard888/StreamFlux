package com.streamflux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.streamflux.data.models.VideoFile
import com.streamflux.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FolderViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    
    private val mediaRepository = MediaRepository(application)
    
    private val _videos = MutableStateFlow<List<VideoFile>>(emptyList())
    val videos: StateFlow<List<VideoFile>> = _videos.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadVideos(folderPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val videoList = mediaRepository.getVideosInFolder(folderPath)
                _videos.value = videoList
            } finally {
                _isLoading.value = false
            }
        }
    }
}
