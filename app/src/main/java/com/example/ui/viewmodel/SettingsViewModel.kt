package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AspectRatioMode
import com.example.data.preferences.DecoderMode
import com.example.data.preferences.PreferencesManager
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.VideoSettingsManager
import com.example.data.local.LsPlayerDatabase
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    val videoSettingsManager = VideoSettingsManager(application)
    private val db = LsPlayerDatabase.getDatabase(application)
    private val mediaRepository = MediaRepository(application, db.videoDao())

    private val _isRescanning = MutableStateFlow(false)
    val isRescanning: StateFlow<Boolean> = _isRescanning.asStateFlow()

    private val _rescanMessage = MutableStateFlow<String?>(null)
    val rescanMessage: StateFlow<String?> = _rescanMessage.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = preferencesManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setPlaybackSpeed(speed: Float) {
        viewModelScope.launch { preferencesManager.updatePlaybackSpeed(speed) }
    }

    fun setAspectRatio(mode: AspectRatioMode) {
        viewModelScope.launch { preferencesManager.updateAspectRatio(mode) }
    }

    fun setBackgroundPlayback(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateBackgroundPlayback(enabled) }
    }

    fun setResumePlayback(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateResumePlayback(enabled) }
    }

    fun setDoubleTapSeek(seconds: Int) {
        viewModelScope.launch { preferencesManager.updateDoubleTapSeek(seconds) }
    }

    fun setDecoderMode(mode: DecoderMode) {
        viewModelScope.launch { videoSettingsManager.setDecoderMode(mode) }
    }

    fun setLanguageCode(code: String) {
        viewModelScope.launch { preferencesManager.updateLanguageCode(code) }
    }

    fun setBatterySaverEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateBatterySaverEnabled(enabled) }
    }

    fun rescanLibrary() {
        if (_isRescanning.value) return
        viewModelScope.launch {
            _isRescanning.value = true
            _rescanMessage.value = null
            try {
                val updatedVideos = mediaRepository.rescanAndRebuildDatabase()
                _rescanMessage.value = "Clean rebuild complete. Found ${updatedVideos.size} video(s)."
            } catch (e: Exception) {
                _rescanMessage.value = "Rescan failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isRescanning.value = false
            }
        }
    }

    fun clearRescanMessage() {
        _rescanMessage.value = null
    }

    fun testReportNonFatal() {
        try {
            throw RuntimeException("Diagnostic Test Exception reported to Firebase Crashlytics")
        } catch (e: Exception) {
            com.example.utils.CrashlyticsLogger.logException(e, tag = "DiagnosticTest")
            _rescanMessage.value = "Test exception recorded in Firebase Crashlytics."
        }
    }

    fun triggerTestCrash() {
        com.example.utils.CrashlyticsLogger.log("User initiated test crash from Settings diagnostics.")
        throw RuntimeException("Firebase Crashlytics Test Crash initiated by user.")
    }
}
