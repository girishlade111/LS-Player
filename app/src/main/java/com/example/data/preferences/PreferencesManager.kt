package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AspectRatioMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ls_player_settings")

data class UserPreferences(
    val playbackSpeed: Float = 1.0f,
    val defaultAspectRatio: AspectRatioMode = AspectRatioMode.FIT,
    val isGestureSeekEnabled: Boolean = true,
    val doubleTapSeekSeconds: Int = 10,
    val isSwipeBrightnessEnabled: Boolean = true,
    val isSwipeVolumeEnabled: Boolean = true,
    val isBackgroundPlaybackEnabled: Boolean = false,
    val isResumePlaybackEnabled: Boolean = true,
    val isAutoPlayNextEnabled: Boolean = true,
    val subtitleTextSizeSp: Int = 18,
    val isDarkMode: Boolean = true,
    val isGridMode: Boolean = false,
    val sortOptionName: String = "NAME",
    val isSortAscending: Boolean = true,
    val decoderMode: DecoderMode = DecoderMode.AUTO,
    val languageCode: String = "",
    val isBatterySaverEnabled: Boolean = true
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val ASPECT_RATIO = stringPreferencesKey("aspect_ratio")
        val GESTURE_SEEK = booleanPreferencesKey("gesture_seek")
        val DOUBLE_TAP_SEEK = intPreferencesKey("double_tap_seek")
        val SWIPE_BRIGHTNESS = booleanPreferencesKey("swipe_brightness")
        val SWIPE_VOLUME = booleanPreferencesKey("swipe_volume")
        val BACKGROUND_PLAYBACK = booleanPreferencesKey("background_playback")
        val RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
        val AUTO_PLAY_NEXT = booleanPreferencesKey("auto_play_next")
        val SUBTITLE_SIZE = intPreferencesKey("subtitle_size")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val GRID_MODE = booleanPreferencesKey("grid_mode")
        val SORT_OPTION = stringPreferencesKey("sort_option")
        val SORT_ASCENDING = booleanPreferencesKey("sort_ascending")
        val DECODER_MODE = stringPreferencesKey("decoder_mode")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val BATTERY_SAVER = booleanPreferencesKey("battery_saver_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            defaultAspectRatio = try {
                AspectRatioMode.valueOf(prefs[Keys.ASPECT_RATIO] ?: AspectRatioMode.FIT.name)
            } catch (e: Exception) {
                AspectRatioMode.FIT
            },
            isGestureSeekEnabled = prefs[Keys.GESTURE_SEEK] ?: true,
            doubleTapSeekSeconds = prefs[Keys.DOUBLE_TAP_SEEK] ?: 10,
            isSwipeBrightnessEnabled = prefs[Keys.SWIPE_BRIGHTNESS] ?: true,
            isSwipeVolumeEnabled = prefs[Keys.SWIPE_VOLUME] ?: true,
            isBackgroundPlaybackEnabled = prefs[Keys.BACKGROUND_PLAYBACK] ?: false,
            isResumePlaybackEnabled = prefs[Keys.RESUME_PLAYBACK] ?: true,
            isAutoPlayNextEnabled = prefs[Keys.AUTO_PLAY_NEXT] ?: true,
            subtitleTextSizeSp = prefs[Keys.SUBTITLE_SIZE] ?: 18,
            isDarkMode = prefs[Keys.DARK_MODE] ?: true,
            isGridMode = prefs[Keys.GRID_MODE] ?: false,
            sortOptionName = prefs[Keys.SORT_OPTION] ?: "NAME",
            isSortAscending = prefs[Keys.SORT_ASCENDING] ?: true,
            decoderMode = try {
                DecoderMode.valueOf(prefs[Keys.DECODER_MODE] ?: DecoderMode.AUTO.name)
            } catch (e: Exception) {
                DecoderMode.AUTO
            },
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "",
            isBatterySaverEnabled = prefs[Keys.BATTERY_SAVER] ?: true
        )
    }

    suspend fun updatePlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun updateAspectRatio(mode: AspectRatioMode) {
        context.dataStore.edit { it[Keys.ASPECT_RATIO] = mode.name }
    }

    suspend fun updateBackgroundPlayback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BACKGROUND_PLAYBACK] = enabled }
    }

    suspend fun updateResumePlayback(enabled: Boolean) {
        context.dataStore.edit { it[Keys.RESUME_PLAYBACK] = enabled }
    }

    suspend fun updateGridMode(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.GRID_MODE] = isGrid }
    }

    suspend fun updateSortOption(optionName: String, isAscending: Boolean) {
        context.dataStore.edit {
            it[Keys.SORT_OPTION] = optionName
            it[Keys.SORT_ASCENDING] = isAscending
        }
    }

    suspend fun updateDoubleTapSeek(seconds: Int) {
        context.dataStore.edit { it[Keys.DOUBLE_TAP_SEEK] = seconds }
    }

    suspend fun updateDecoderMode(mode: DecoderMode) {
        context.dataStore.edit { it[Keys.DECODER_MODE] = mode.name }
    }

    suspend fun updateLanguageCode(code: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun updateBatterySaverEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BATTERY_SAVER] = enabled }
    }
}
