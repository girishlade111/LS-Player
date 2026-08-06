package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LsPlayerDatabase
import com.example.data.model.FolderItem
import com.example.data.model.PlaylistEntity
import com.example.data.model.SizeFilter
import com.example.data.model.SortOption
import com.example.data.model.VideoItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Success(val videos: List<VideoItem>) : ScanState()
    data class Error(val message: String) : ScanState()
}

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = LsPlayerDatabase.getDatabase(application)
    private val repository = MediaRepository(application, db.videoDao())

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val _allVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val allVideos: StateFlow<List<VideoItem>> = _allVideos.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NAME)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    private val _sizeFilter = MutableStateFlow(SizeFilter.ALL)
    val sizeFilter: StateFlow<SizeFilter> = _sizeFilter.asStateFlow()

    private val _selectedFolderFilter = MutableStateFlow<String?>(null)
    val selectedFolderFilter: StateFlow<String?> = _selectedFolderFilter.asStateFlow()

    private val _isGridMode = MutableStateFlow(false)
    val isGridMode: StateFlow<Boolean> = _isGridMode.asStateFlow()

    private val _selectedVideoPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedVideoPaths: StateFlow<Set<String>> = _selectedVideoPaths.asStateFlow()

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .combine(_allVideos) { plList, _ -> plList }
        .let { flow ->
            val state = MutableStateFlow<List<PlaylistEntity>>(emptyList())
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
            state.asStateFlow()
        }

    val recentlyPlayed: StateFlow<List<VideoItem>> = repository.allProgress
        .combine(_allVideos) { progressList, videos ->
            val videoMap = videos.associateBy { it.path }
            progressList.mapNotNull { progress ->
                val match = videoMap[progress.path]
                if (match != null) {
                    match.copy(lastPlayedPositionMs = progress.lastPositionMs)
                } else {
                    VideoItem(
                        id = progress.path.hashCode().toLong(),
                        title = progress.title,
                        uriString = progress.path,
                        path = progress.path,
                        durationMs = progress.durationMs,
                        sizeBytes = 0L,
                        dateAddedMs = progress.lastPlayedTimestamp,
                        folderName = "Storage",
                        folderPath = "",
                        lastPlayedPositionMs = progress.lastPositionMs
                    )
                }
            }.take(5)
        }.let { flow ->
            val state = MutableStateFlow<List<VideoItem>>(emptyList())
            viewModelScope.launch {
                flow.collect { state.value = it }
            }
            state.asStateFlow()
        }

    init {
        viewModelScope.launch {
            repository.cachedVideosFlow.collect { videos ->
                _allVideos.value = videos
                if (_scanState.value !is ScanState.Error && videos.isNotEmpty()) {
                    _scanState.value = ScanState.Success(videos)
                }
            }
        }
        scanMedia()
    }

    fun scanMedia() {
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning
            try {
                val videos = repository.scanLocalVideos()
                _allVideos.value = videos
                _scanState.value = ScanState.Success(videos)
            } catch (e: Exception) {
                _scanState.value = ScanState.Error(e.message ?: "Failed to scan videos")
            }
        }
    }

    fun importVideo(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                repository.importVideoFile(uri)
                scanMedia()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        if (_sortOption.value == option) {
            _sortAscending.value = !_sortAscending.value
        } else {
            _sortOption.value = option
            _sortAscending.value = true
        }
    }

    fun setSortAscending(ascending: Boolean) {
        _sortAscending.value = ascending
    }

    fun setSizeFilter(filter: SizeFilter) {
        _sizeFilter.value = filter
    }

    fun setFolderFilter(folderPath: String?) {
        _selectedFolderFilter.value = folderPath
    }

    fun resetFilters() {
        _sortOption.value = SortOption.NAME
        _sortAscending.value = true
        _sizeFilter.value = SizeFilter.ALL
        _selectedFolderFilter.value = null
    }

    fun toggleGridMode() {
        _isGridMode.value = !_isGridMode.value
    }

    fun toggleFavorite(video: VideoItem) {
        viewModelScope.launch {
            repository.toggleFavorite(video.path, video.isFavorite)
            scanMedia()
        }
    }

    fun toggleSelection(videoPath: String) {
        val current = _selectedVideoPaths.value.toMutableSet()
        if (current.contains(videoPath)) {
            current.remove(videoPath)
        } else {
            current.add(videoPath)
        }
        _selectedVideoPaths.value = current
    }

    fun clearSelection() {
        _selectedVideoPaths.value = emptySet()
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun saveProgress(video: VideoItem, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.saveVideoProgress(video.path, video.title, positionMs, durationMs)
        }
    }

    fun getFilteredVideos(): List<VideoItem> {
        val query = _searchQuery.value.trim().lowercase()
        val folderFilter = _selectedFolderFilter.value
        val sizeFilter = _sizeFilter.value

        val rawList = _allVideos.value.filter { video ->
            val matchesQuery = query.isEmpty() ||
                    video.title.lowercase().contains(query) ||
                    video.folderName.lowercase().contains(query)

            val matchesFolder = folderFilter == null || video.folderPath == folderFilter

            val sizeMb = video.sizeBytes / (1024 * 1024)
            val matchesSize = when (sizeFilter) {
                SizeFilter.ALL -> true
                SizeFilter.SMALL -> sizeMb < 50
                SizeFilter.MEDIUM -> sizeMb in 50..500
                SizeFilter.LARGE -> sizeMb > 500
            }

            matchesQuery && matchesFolder && matchesSize
        }

        val sorted = when (_sortOption.value) {
            SortOption.NAME -> rawList.sortedBy { it.title.lowercase() }
            SortOption.DATE -> rawList.sortedBy { it.dateAddedMs }
            SortOption.SIZE -> rawList.sortedBy { it.sizeBytes }
            SortOption.DURATION -> rawList.sortedBy { it.durationMs }
        }

        return if (_sortAscending.value) sorted else sorted.reversed()
    }

    fun getFolders(): List<FolderItem> {
        return repository.groupVideosByFolder(_allVideos.value)
    }

    fun getVideosForFolder(folderPath: String): List<VideoItem> {
        return getFilteredVideos().filter { it.folderPath == folderPath }
    }

    fun getFavorites(): List<VideoItem> {
        return getFilteredVideos().filter { it.isFavorite }
    }

    fun getContinueWatching(): List<VideoItem> {
        return getFilteredVideos().filter { it.lastPlayedPositionMs > 5000L }
            .sortedByDescending { it.lastPlayedPositionMs }
    }
}
