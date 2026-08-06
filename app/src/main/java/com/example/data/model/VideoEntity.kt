package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: Long,
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
    val folderPath: String
) {
    fun toVideoItem(lastPlayedPositionMs: Long = 0L, isFavorite: Boolean = false): VideoItem {
        return VideoItem(
            id = id,
            title = title,
            uriString = uriString,
            path = path,
            durationMs = durationMs,
            sizeBytes = sizeBytes,
            width = width,
            height = height,
            mimeType = mimeType,
            dateAddedMs = dateAddedMs,
            folderName = folderName,
            folderPath = folderPath,
            lastPlayedPositionMs = lastPlayedPositionMs,
            isFavorite = isFavorite
        )
    }
}
