package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.VideoDao
import com.example.data.model.VideoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VideoRepository(
    private val context: Context,
    private val videoDao: VideoDao
) {
    val cachedVideos: Flow<List<VideoEntity>> = videoDao.getAllVideos()

    suspend fun fetchAndCacheVideos(): List<VideoEntity> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoEntity>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED
        )

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthColumn = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightColumn = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val mimeColumn = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Video $id"
                    val path = cursor.getString(dataColumn) ?: ""
                    val duration = if (durationColumn != -1) cursor.getLong(durationColumn) else 0L
                    val size = cursor.getLong(sizeColumn)
                    val width = if (widthColumn != -1) cursor.getInt(widthColumn) else 0
                    val height = if (heightColumn != -1) cursor.getInt(heightColumn) else 0
                    val mime = if (mimeColumn != -1) cursor.getString(mimeColumn) ?: "video/*" else "video/*"
                    val dateAdded = cursor.getLong(dateColumn) * 1000L

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val folderPath = if (path.contains("/")) path.substringBeforeLast("/") else "Internal Storage"
                    val folderName = if (folderPath.contains("/")) folderPath.substringAfterLast("/") else folderPath

                    videoList.add(
                        VideoEntity(
                            id = id,
                            title = name,
                            uriString = contentUri,
                            path = path,
                            durationMs = duration,
                            sizeBytes = size,
                            width = width,
                            height = height,
                            mimeType = mime,
                            dateAddedMs = dateAdded,
                            folderName = folderName,
                            folderPath = folderPath
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delete old sample videos from database
        videoDao.deleteSampleVideos()

        // Cache scanned videos into Room database
        if (videoList.isNotEmpty()) {
            videoDao.insertVideos(videoList)
        }
        return@withContext videoList
    }

    suspend fun importVideoUri(uri: android.net.Uri): VideoEntity = withContext(Dispatchers.IO) {
        var title = "Imported Video"
        var size = 0L

        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (nameIdx != -1) {
                        val displayName = cursor.getString(nameIdx)
                        if (!displayName.isNullOrBlank()) title = displayName
                    }
                    if (sizeIdx != -1) {
                        size = cursor.getLong(sizeIdx)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val mimeType = context.contentResolver.getType(uri) ?: "video/*"
        var durationMs = 0L
        var width = 0
        var height = 0

        try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val dur = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = dur?.toLongOrNull() ?: 0L
            val w = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            width = w?.toIntOrNull() ?: 0
            val h = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            height = h?.toIntOrNull() ?: 0
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val rawId = uri.toString().hashCode().toLong()
        val id = if (rawId < 0) -rawId else rawId
        val uriStr = uri.toString()

        val entity = VideoEntity(
            id = id,
            title = title,
            uriString = uriStr,
            path = uriStr,
            durationMs = durationMs,
            sizeBytes = size,
            width = width,
            height = height,
            mimeType = mimeType,
            dateAddedMs = System.currentTimeMillis(),
            folderName = "Imported",
            folderPath = "Imported"
        )

        videoDao.insertVideos(listOf(entity))
        entity
    }
}
