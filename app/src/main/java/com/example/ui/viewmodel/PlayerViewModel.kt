package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AspectRatioMode
import com.example.data.model.PlayerOrientationMode
import com.example.data.model.SubtitleTrack
import com.example.data.model.VideoItem
import com.example.player.PlayerController
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GestureOverlay {
    object None : GestureOverlay()
    data class Brightness(val percent: Int) : GestureOverlay()
    data class Volume(val percent: Int) : GestureOverlay()
    data class Seek(val deltaSeconds: Int, val isForward: Boolean) : GestureOverlay()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    val userPreferences: StateFlow<UserPreferences> = preferencesManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    val playerController = PlayerController(application)

    private val _areControlsVisible = MutableStateFlow(true)
    val areControlsVisible: StateFlow<Boolean> = _areControlsVisible.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _aspectRatioMode = MutableStateFlow(AspectRatioMode.FIT)
    val aspectRatioMode: StateFlow<AspectRatioMode> = _aspectRatioMode.asStateFlow()

    private val _orientationMode = MutableStateFlow(PlayerOrientationMode.SENSOR)
    val orientationMode: StateFlow<PlayerOrientationMode> = _orientationMode.asStateFlow()

    private val _gestureOverlay = MutableStateFlow<GestureOverlay>(GestureOverlay.None)
    val gestureOverlay: StateFlow<GestureOverlay> = _gestureOverlay.asStateFlow()

    private val _showQueueSheet = MutableStateFlow(false)
    val showQueueSheet: StateFlow<Boolean> = _showQueueSheet.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    val queue: StateFlow<List<VideoItem>> = playerController.queue
    val currentIndex: StateFlow<Int> = playerController.currentIndex

    private val _showVideoInfoDialog = MutableStateFlow(false)
    val showVideoInfoDialog: StateFlow<Boolean> = _showVideoInfoDialog.asStateFlow()

    private val _showSubtitleSheet = MutableStateFlow(false)
    val showSubtitleSheet: StateFlow<Boolean> = _showSubtitleSheet.asStateFlow()

    private val _showAudioSheet = MutableStateFlow(false)
    val showAudioSheet: StateFlow<Boolean> = _showAudioSheet.asStateFlow()

    private val _showSleepTimerDialog = MutableStateFlow(false)
    val showSleepTimerDialog: StateFlow<Boolean> = _showSleepTimerDialog.asStateFlow()

    private var autoHideJob: Job? = null
    private var gestureJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun loadAndPlay(video: VideoItem, startPosMs: Long = 0L) {
        playerController.playVideo(video, startPosMs)
        resetAutoHideTimer()
    }

    fun toggleControls() {
        _areControlsVisible.value = !_areControlsVisible.value
        if (_areControlsVisible.value) {
            resetAutoHideTimer()
        }
    }

    fun resetAutoHideTimer() {
        autoHideJob?.cancel()
        autoHideJob = viewModelScope.launch {
            delay(4000)
            if (!_isLocked.value) {
                _areControlsVisible.value = false
            }
        }
    }

    fun toggleLock() {
        _isLocked.value = !_isLocked.value
        if (_isLocked.value) {
            _areControlsVisible.value = false
        } else {
            _areControlsVisible.value = true
            resetAutoHideTimer()
        }
    }

    fun cycleAspectRatio() {
        val modes = AspectRatioMode.values()
        val nextOrdinal = (_aspectRatioMode.value.ordinal + 1) % modes.size
        _aspectRatioMode.value = modes[nextOrdinal]
    }

    fun cycleOrientationMode(): PlayerOrientationMode {
        val modes = PlayerOrientationMode.values()
        val nextOrdinal = (_orientationMode.value.ordinal + 1) % modes.size
        val nextMode = modes[nextOrdinal]
        _orientationMode.value = nextMode
        return nextMode
    }

    fun showGestureHUD(overlay: GestureOverlay) {
        _gestureOverlay.value = overlay
        gestureJob?.cancel()
        gestureJob = viewModelScope.launch {
            delay(1200)
            _gestureOverlay.value = GestureOverlay.None
        }
    }

    private var initialDragPercent: Int = 0
    private var lastReportedVolumePercent: Int = -1
    private var lastReportedBrightnessPercent: Int = -1

    fun startVolumeDrag(context: android.content.Context) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager ?: return
        val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        if (maxVol <= 0) return
        val curVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        initialDragPercent = (curVol.toFloat() / maxVol * 100).toInt()
        lastReportedVolumePercent = initialDragPercent
        showGestureHUD(GestureOverlay.Volume(initialDragPercent))
    }

    fun updateVolumeDrag(context: android.content.Context, deltaPercent: Float) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager ?: return
        val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        if (maxVol <= 0) return
        val targetPercent = (initialDragPercent + deltaPercent.toInt()).coerceIn(0, 100)
        if (targetPercent != lastReportedVolumePercent) {
            lastReportedVolumePercent = targetPercent
            com.example.utils.HapticUtils.performTick(context)
        }
        val targetVol = (targetPercent * maxVol / 100f).toInt()
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, 0)
        showGestureHUD(GestureOverlay.Volume(targetPercent))
    }

    fun startBrightnessDrag(activity: android.app.Activity) {
        val lp = activity.window.attributes
        var curBrightness = lp.screenBrightness
        if (curBrightness < 0f) {
            try {
                val sysBrightness = android.provider.Settings.System.getInt(
                    activity.contentResolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
                )
                curBrightness = sysBrightness / 255f
            } catch (e: Exception) {
                curBrightness = 0.5f
            }
        }
        initialDragPercent = (curBrightness * 100).toInt().coerceIn(1, 100)
        lastReportedBrightnessPercent = initialDragPercent
        showGestureHUD(GestureOverlay.Brightness(initialDragPercent))
    }

    fun updateBrightnessDrag(activity: android.app.Activity, deltaPercent: Float) {
        val lp = activity.window.attributes
        val targetPercent = (initialDragPercent + deltaPercent.toInt()).coerceIn(1, 100)
        if (targetPercent != lastReportedBrightnessPercent) {
            lastReportedBrightnessPercent = targetPercent
            com.example.utils.HapticUtils.performTick(activity)
        }
        lp.screenBrightness = targetPercent / 100f
        activity.window.attributes = lp
        showGestureHUD(GestureOverlay.Brightness(targetPercent))
    }

    fun adjustVolume(context: android.content.Context, deltaPercent: Float) {
        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? android.media.AudioManager ?: return
        val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        if (maxVol <= 0) return
        val curVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val curPercent = (curVol.toFloat() / maxVol * 100).toInt()
        val newPercent = (curPercent + deltaPercent.toInt()).coerceIn(0, 100)
        val newVol = (newPercent * maxVol / 100f).toInt()
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVol, 0)
        showGestureHUD(GestureOverlay.Volume(newPercent))
    }

    fun adjustBrightness(activity: android.app.Activity, deltaPercent: Float) {
        val lp = activity.window.attributes
        var curBrightness = lp.screenBrightness
        if (curBrightness < 0f) {
            try {
                val sysBrightness = android.provider.Settings.System.getInt(
                    activity.contentResolver,
                    android.provider.Settings.System.SCREEN_BRIGHTNESS
                )
                curBrightness = sysBrightness / 255f
            } catch (e: Exception) {
                curBrightness = 0.5f
            }
        }
        val curPercent = (curBrightness * 100).toInt()
        val newPercent = (curPercent + deltaPercent.toInt()).coerceIn(1, 100)
        lp.screenBrightness = newPercent / 100f
        activity.window.attributes = lp
        showGestureHUD(GestureOverlay.Brightness(newPercent))
    }

    fun doubleTapSeek(isForward: Boolean, stepSec: Int = 10) {
        val delta = if (isForward) stepSec else -stepSec
        playerController.seekBy(delta * 1000L)
        showGestureHUD(GestureOverlay.Seek(deltaSeconds = stepSec, isForward = isForward))
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
        sleepTimerJob?.cancel()
        if (minutes != null && minutes > 0) {
            sleepTimerJob = viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                playerController.pause()
                _sleepTimerMinutes.value = null
            }
        }
    }

    fun openQueueSheet() { _showQueueSheet.value = true }
    fun closeQueueSheet() { _showQueueSheet.value = false }

    fun playNext() {
        if (playerController.playNext()) {
            resetAutoHideTimer()
        }
    }

    fun playPrevious() {
        if (playerController.playPrevious()) {
            resetAutoHideTimer()
        }
    }

    fun playQueueIndex(index: Int) {
        playerController.playQueueIndex(index)
        resetAutoHideTimer()
    }

    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }

    fun clearQueue() {
        playerController.clearQueue()
    }

    fun openSubtitleSheet() { _showSubtitleSheet.value = true }
    fun closeSubtitleSheet() { _showSubtitleSheet.value = false }

    fun openAudioSheet() { _showAudioSheet.value = true }
    fun closeAudioSheet() { _showAudioSheet.value = false }

    fun openSleepTimerDialog() { _showSleepTimerDialog.value = true }
    fun closeSleepTimerDialog() { _showSleepTimerDialog.value = false }

    fun openVideoInfoDialog() { _showVideoInfoDialog.value = true }
    fun closeVideoInfoDialog() { _showVideoInfoDialog.value = false }

    fun addExternalSubtitleUri(uri: Uri) {
        playerController.addExternalSubtitle(uri)
    }

    fun setSubtitleOffset(offsetMs: Long) {
        playerController.setSubtitleOffsetMs(offsetMs)
    }

    fun adjustSubtitleOffset(deltaMs: Long) {
        playerController.adjustSubtitleOffsetMs(deltaMs)
    }

    fun resetSubtitleOffset() {
        playerController.resetSubtitleOffset()
    }

    fun retryPlayback() {
        playerController.retryCurrentVideo()
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
