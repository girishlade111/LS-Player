package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.VideoDao
import com.example.data.model.FavoriteVideoEntity
import com.example.data.model.FolderItem
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemEntity
import com.example.data.model.SortOption
import com.example.data.model.VideoItem
import com.example.data.model.VideoProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class MediaRepository(
    private val context: Context,
    private val videoDao: VideoDao
) {

    val videoRepository = VideoRepository(context, videoDao)

    val allFavorites: Flow<List<FavoriteVideoEntity>> = videoDao.getAllFavorites()
    val allProgress: Flow<List<VideoProgressEntity>> = videoDao.getAllProgress()
    val allPlaylists: Flow<List<PlaylistEntity>> = videoDao.getAllPlaylists()

    val cachedVideosFlow: Flow<List<VideoItem>> = combine(
        videoRepository.cachedVideos,
        allFavorites,
        allProgress
    ) { entities, favs, progressList ->
        val favPaths = favs.map { it.path }.toSet()
        val progressMap = progressList.associate { it.path to it.lastPositionMs }

        entities.map { entity ->
            entity.toVideoItem(
                lastPlayedPositionMs = progressMap[entity.path] ?: 0L,
                isFavorite = favPaths.contains(entity.path)
            )
        }
    }

    suspend fun rescanAndRebuildDatabase(): List<VideoItem> = withContext(Dispatchers.IO) {
        videoDao.deleteAllVideos()
        return@withContext scanLocalVideos()
    }

    suspend fun scanLocalVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val entities = videoRepository.fetchAndCacheVideos()
        val favs = videoDao.getAllFavorites()
        return@withContext entities.map { entity ->
            val isFav = videoDao.isFavorite(entity.path)
            val progressObj = videoDao.getProgressForPath(entity.path)
            entity.toVideoItem(
                lastPlayedPositionMs = progressObj?.lastPositionMs ?: 0L,
                isFavorite = isFav
            )
        }
    }

    suspend fun importVideoFile(uri: Uri): VideoItem = withContext(Dispatchers.IO) {
        val entity = videoRepository.importVideoUri(uri)
        val isFav = videoDao.isFavorite(entity.path)
        val progressObj = videoDao.getProgressForPath(entity.path)
        entity.toVideoItem(
            lastPlayedPositionMs = progressObj?.lastPositionMs ?: 0L,
            isFavorite = isFav
        )
    }

    fun groupVideosByFolder(videos: List<VideoItem>): List<FolderItem> {
        val grouped = videos.groupBy { it.folderPath }
        return grouped.map { (path, videoGroup) ->
            FolderItem(
                folderName = videoGroup.firstOrNull()?.folderName ?: "Folder",
                folderPath = path,
                videoCount = videoGroup.size,
                totalSizeBytes = videoGroup.sumOf { it.sizeBytes }
            )
        }.sortedBy { it.folderName.lowercase() }
    }

    suspend fun toggleFavorite(videoPath: String, currentFavoriteStatus: Boolean) {
        if (currentFavoriteStatus) {
            videoDao.removeFavorite(videoPath)
        } else {
            videoDao.addFavorite(FavoriteVideoEntity(path = videoPath))
        }
    }

    suspend fun saveVideoProgress(path: String, title: String, positionMs: Long, durationMs: Long) {
        if (positionMs <= 0 || durationMs <= 0) return
        videoDao.saveProgress(
            VideoProgressEntity(
                path = path,
                title = title,
                lastPositionMs = positionMs,
                durationMs = durationMs,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun createPlaylist(name: String): Long {
        return videoDao.insertPlaylist(PlaylistEntity(title = name))
    }

    suspend fun addVideoToPlaylist(playlistId: Long, videoPath: String) {
        videoDao.insertPlaylistItem(
            PlaylistItemEntity(
                playlistId = playlistId,
                videoPath = videoPath,
                orderIndex = (System.currentTimeMillis() % 10000).toInt()
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) {
        videoDao.deletePlaylist(playlistId)
    }
}
