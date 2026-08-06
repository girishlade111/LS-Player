package com.example.data.model

import android.content.pm.ActivityInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_progress")
data class VideoProgressEntity(
    @PrimaryKey val path: String,
    val title: String,
    val lastPositionMs: Long,
    val durationMs: Long,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_videos")
data class FavoriteVideoEntity(
    @PrimaryKey val path: String,
    val dateAddedMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAtMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_items", primaryKeys = ["playlistId", "videoPath"])
data class PlaylistItemEntity(
    val playlistId: Long,
    val videoPath: String,
    val orderIndex: Int
)

data class VideoItem(
    val id: Long,
    val title: String,
    val uriString: String,
    val path: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val width: Int = 0,
    val height: Int = 0,
    val mimeType: String = "video/*",
    val dateAddedMs: Long,
    val folderName: String,
    val folderPath: String,
    val lastPlayedPositionMs: Long = 0L,
    val isFavorite: Boolean = false
) {
    val resolutionFormatted: String
        get() = if (width > 0 && height > 0) "${width}x${height}" else "Unknown"

    val sizeFormatted: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }

    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }

    val fileExtension: String
        get() = path.substringAfterLast('.', "MP4").uppercase()
}

data class FolderItem(
    val folderName: String,
    val folderPath: String,
    val videoCount: Int,
    val totalSizeBytes: Long
) {
    val totalSizeFormatted: String
        get() {
            val mb = totalSizeBytes / (1024.0 * 1024.0)
            val gb = mb / 1024.0
            return if (gb >= 1.0) String.format("%.2f GB", gb) else String.format("%.1f MB", mb)
        }
}

data class SubtitleTrack(
    val id: String,
    val label: String,
    val language: String? = null,
    val uriString: String? = null,
    val isExternal: Boolean = false
)

data class AudioTrackInfo(
    val index: Int,
    val label: String,
    val language: String? = null,
    val channelCount: Int = 2
)

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill / Stretch"),
    CROP("Zoom / Crop"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3")
}

enum class SortOption(val label: String) {
    NAME("Name"),
    DATE("Date Added"),
    DURATION("Duration"),
    SIZE("File Size")
}

enum class SizeFilter(val label: String) {
    ALL("All Sizes"),
    SMALL("Small (< 50 MB)"),
    MEDIUM("Medium (50–500 MB)"),
    LARGE("Large (> 500 MB)")
}

enum class PlayerOrientationMode(val label: String, val activityInfoOrientation: Int) {
    SENSOR("Auto", ActivityInfo.SCREEN_ORIENTATION_SENSOR),
    LANDSCAPE("Landscape", ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE),
    PORTRAIT("Portrait", ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
}
