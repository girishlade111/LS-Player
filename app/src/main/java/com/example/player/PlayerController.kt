package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.AudioTrackInfo
import com.example.data.model.SubtitleTrack
import com.example.data.model.VideoItem
import com.example.data.preferences.DecoderMode
import com.example.data.preferences.VideoSettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@OptIn(UnstableApi::class)
class PlayerController(private val context: Context) {

    val playbackHelper = VideoPlaybackHelper(context)

    val videoSettingsManager: VideoSettingsManager = playbackHelper.videoSettingsManager
    val currentDecoderMode: StateFlow<DecoderMode> = playbackHelper.currentDecoderMode

    val isPlaying: StateFlow<Boolean> = playbackHelper.isPlaying
    val currentPosition: StateFlow<Long> = playbackHelper.currentPosition
    val duration: StateFlow<Long> = playbackHelper.duration
    val playbackSpeed: StateFlow<Float> = playbackHelper.playbackSpeed
    val isBuffering: StateFlow<Boolean> = playbackHelper.isBuffering
    val errorState: StateFlow<PlayerErrorState?> = playbackHelper.errorState

    val availableSubtitles: StateFlow<List<SubtitleTrack>> = playbackHelper.availableSubtitles
    val selectedSubtitle: StateFlow<SubtitleTrack?> = playbackHelper.selectedSubtitle
    val subtitleOffsetMs: StateFlow<Long> = playbackHelper.subtitleOffsetMs
    val availableAudioTracks: StateFlow<List<AudioTrackInfo>> = playbackHelper.availableAudioTracks
    val selectedAudioTrackIndex: StateFlow<Int> = playbackHelper.selectedAudioTrackIndex

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _queue = MutableStateFlow<List<VideoItem>>(emptyList())
    val queue: StateFlow<List<VideoItem>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow<Int>(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    init {
        playbackHelper.onVideoCompleted = {
            playNext()
        }
    }

    fun getPlayer(): ExoPlayer {
        return playbackHelper.getOrCreatePlayer()
    }

    fun playVideo(video: VideoItem, startPositionMs: Long = 0L) {
        _currentVideo.value = video
        val currentList = _queue.value
        val existingIndex = currentList.indexOfFirst { it.id == video.id }
        if (existingIndex >= 0) {
            _currentIndex.value = existingIndex
        } else {
            val newList = currentList + video
            _queue.value = newList
            _currentIndex.value = newList.size - 1
        }
        playbackHelper.playVideo(video.uriString, startPositionMs)
    }

    fun setQueue(items: List<VideoItem>, startIndex: Int = 0) {
        _queue.value = items
        if (items.isNotEmpty()) {
            val idx = startIndex.coerceIn(0, items.size - 1)
            _currentIndex.value = idx
            _currentVideo.value = items[idx]
            playbackHelper.playVideo(items[idx].uriString, 0L)
        }
    }

    fun addToQueue(video: VideoItem) {
        val currentList = _queue.value.toMutableList()
        currentList.add(video)
        _queue.value = currentList
        if (_currentIndex.value == -1 || _currentVideo.value == null) {
            _currentIndex.value = currentList.size - 1
            playVideo(video)
        }
    }

    fun addToQueueAsNext(video: VideoItem) {
        val currentList = _queue.value.toMutableList()
        val insertIdx = if (_currentIndex.value >= 0) _currentIndex.value + 1 else 0
        currentList.add(insertIdx.coerceIn(0, currentList.size), video)
        _queue.value = currentList
        if (_currentIndex.value == -1 || _currentVideo.value == null) {
            _currentIndex.value = 0
            playVideo(video)
        }
    }

    fun hasNext(): Boolean {
        return _currentIndex.value >= 0 && _currentIndex.value + 1 < _queue.value.size
    }

    fun hasPrevious(): Boolean {
        return _currentIndex.value > 0 || currentPosition.value > 3000L
    }

    fun playNext(): Boolean {
        val q = _queue.value
        val curIdx = _currentIndex.value
        if (curIdx >= 0 && curIdx + 1 < q.size) {
            val nextIdx = curIdx + 1
            _currentIndex.value = nextIdx
            val nextVideo = q[nextIdx]
            _currentVideo.value = nextVideo
            playbackHelper.playVideo(nextVideo.uriString, 0L)
            return true
        }
        return false
    }

    fun playPrevious(): Boolean {
        val q = _queue.value
        val curIdx = _currentIndex.value
        if (currentPosition.value > 3000L) {
            seekTo(0L)
            return true
        }
        if (curIdx > 0 && curIdx < q.size) {
            val prevIdx = curIdx - 1
            _currentIndex.value = prevIdx
            val prevVideo = q[prevIdx]
            _currentVideo.value = prevVideo
            playbackHelper.playVideo(prevVideo.uriString, 0L)
            return true
        } else {
            seekTo(0L)
            return false
        }
    }

    fun playQueueIndex(index: Int) {
        val q = _queue.value
        if (index in q.indices) {
            _currentIndex.value = index
            val video = q[index]
            _currentVideo.value = video
            playbackHelper.playVideo(video.uriString, 0L)
        }
    }

    fun removeFromQueue(index: Int) {
        val currentList = _queue.value.toMutableList()
        if (index !in currentList.indices) return
        currentList.removeAt(index)
        _queue.value = currentList

        val curIdx = _currentIndex.value
        if (index == curIdx) {
            if (currentList.isEmpty()) {
                _currentIndex.value = -1
                _currentVideo.value = null
                playbackHelper.pause()
            } else if (index < currentList.size) {
                _currentIndex.value = index
                val video = currentList[index]
                _currentVideo.value = video
                playbackHelper.playVideo(video.uriString, 0L)
            } else {
                _currentIndex.value = index - 1
                val video = currentList[index - 1]
                _currentVideo.value = video
                playbackHelper.playVideo(video.uriString, 0L)
            }
        } else if (index < curIdx) {
            _currentIndex.value = curIdx - 1
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
    }

    fun closeVideo() {
        playbackHelper.pause()
        _currentVideo.value = null
    }

    fun play() = playbackHelper.play()

    fun pause() = playbackHelper.pause()

    fun togglePlayPause() = playbackHelper.togglePlayPause()

    fun seekTo(positionMs: Long) = playbackHelper.seekTo(positionMs)

    fun seekBy(offsetMs: Long) = playbackHelper.seekBy(offsetMs)

    fun setSpeed(speed: Float) = playbackHelper.setSpeed(speed)

    fun retryCurrentVideo() {
        val video = _currentVideo.value ?: return
        val currentPos = currentPosition.value
        playbackHelper.retryPlayback(video.uriString, currentPos)
    }

    fun switchDecoderMode(mode: DecoderMode) {
        val video = _currentVideo.value
        val currentPos = currentPosition.value
        playbackHelper.switchDecoderMode(mode, video?.uriString, currentPos)
    }

    fun addExternalSubtitle(uri: Uri) {
        val video = _currentVideo.value ?: return
        playbackHelper.addExternalSubtitle(uri, video.uriString)
    }

    fun setSubtitleOffsetMs(offsetMs: Long) = playbackHelper.setSubtitleOffsetMs(offsetMs)

    fun adjustSubtitleOffsetMs(deltaMs: Long) = playbackHelper.adjustSubtitleOffsetMs(deltaMs)

    fun resetSubtitleOffset() = playbackHelper.resetSubtitleOffset()

    fun release() {
        playbackHelper.release()
    }
}
