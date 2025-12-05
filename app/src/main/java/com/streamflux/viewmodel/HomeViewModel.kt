package com.streamflux.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.streamflux.data.models.VideoFile
import com.streamflux.data.models.VideoFolder
import com.streamflux.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val mediaRepository = MediaRepository(application)
    
    private val _folders = MutableStateFlow<List<VideoFolder>>(emptyList())
    val folders: StateFlow<List<VideoFolder>> = _folders.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun loadFolders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val folderList = mediaRepository.getVideoFolders()
                _folders.value = folderList
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val folderList = mediaRepository.getVideoFolders()
                _folders.value = folderList
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
