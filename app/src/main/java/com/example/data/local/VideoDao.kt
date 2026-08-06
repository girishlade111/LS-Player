package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FavoriteVideoEntity
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistItemEntity
import com.example.data.model.VideoEntity
import com.example.data.model.VideoProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    // --- Cached Videos ---
    @Query("SELECT * FROM videos ORDER BY dateAddedMs DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("DELETE FROM videos WHERE uriString LIKE 'http%' OR uriString LIKE '%commondatastorage%'")
    suspend fun deleteSampleVideos()

    // --- Video Progress ---
    @Query("SELECT * FROM video_progress WHERE path = :path")
    suspend fun getProgressForPath(path: String): VideoProgressEntity?

    @Query("SELECT * FROM video_progress ORDER BY lastPlayedTimestamp DESC")
    fun getAllProgress(): Flow<List<VideoProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: VideoProgressEntity)

    @Query("DELETE FROM video_progress WHERE path = :path")
    suspend fun clearProgress(path: String)

    // --- Favorites ---
    @Query("SELECT * FROM favorite_videos")
    fun getAllFavorites(): Flow<List<FavoriteVideoEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_videos WHERE path = :path)")
    suspend fun isFavorite(path: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteVideoEntity)

    @Query("DELETE FROM favorite_videos WHERE path = :path")
    suspend fun removeFavorite(path: String)

    // --- Playlists ---
    @Query("SELECT * FROM playlists ORDER BY createdAtMs DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getItemsForPlaylist(playlistId: Long): Flow<List<PlaylistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItem(item: PlaylistItemEntity)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND videoPath = :videoPath")
    suspend fun removePlaylistItem(playlistId: Long, videoPath: String)
}
