package com.example.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.model.AudioTrackInfo
import com.example.data.model.SubtitleTrack
import com.example.data.preferences.DecoderMode
import com.example.data.preferences.VideoSettingsManager
import com.example.utils.CrashlyticsLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerErrorState(
    val errorCode: Int,
    val message: String,
    val isCodecError: Boolean = false,
    val isSourceError: Boolean = false
)

@OptIn(UnstableApi::class)
class VideoPlaybackHelper(private val context: Context) {

    val videoSettingsManager = VideoSettingsManager(context)
    private var activeDecoderMode: DecoderMode = DecoderMode.AUTO

    private val _currentDecoderMode = MutableStateFlow(DecoderMode.AUTO)
    val currentDecoderMode: StateFlow<DecoderMode> = _currentDecoderMode.asStateFlow()

    private var exoPlayer: ExoPlayer? = null

    var onVideoCompleted: (() -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _errorState = MutableStateFlow<PlayerErrorState?>(null)
    val errorState: StateFlow<PlayerErrorState?> = _errorState.asStateFlow()

    private val _availableSubtitles = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val availableSubtitles: StateFlow<List<SubtitleTrack>> = _availableSubtitles.asStateFlow()

    private val _selectedSubtitle = MutableStateFlow<SubtitleTrack?>(null)
    val selectedSubtitle: StateFlow<SubtitleTrack?> = _selectedSubtitle.asStateFlow()

    private val _subtitleOffsetMs = MutableStateFlow(0L)
    val subtitleOffsetMs: StateFlow<Long> = _subtitleOffsetMs.asStateFlow()

    fun setSubtitleOffsetMs(offsetMs: Long) {
        _subtitleOffsetMs.value = offsetMs
    }

    fun adjustSubtitleOffsetMs(deltaMs: Long) {
        _subtitleOffsetMs.value += deltaMs
    }

    fun resetSubtitleOffset() {
        _subtitleOffsetMs.value = 0L
    }

    private val _availableAudioTracks = MutableStateFlow<List<AudioTrackInfo>>(emptyList())
    val availableAudioTracks: StateFlow<List<AudioTrackInfo>> = _availableAudioTracks.asStateFlow()

    private val _selectedAudioTrackIndex = MutableStateFlow(0)
    val selectedAudioTrackIndex: StateFlow<Int> = _selectedAudioTrackIndex.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    @Synchronized
    fun getOrCreatePlayer(decoderMode: DecoderMode = activeDecoderMode): ExoPlayer {
        if (exoPlayer == null || decoderMode != activeDecoderMode) {
            exoPlayer?.release()
            activeDecoderMode = decoderMode
            _currentDecoderMode.value = decoderMode

            val renderersFactory = DefaultRenderersFactory(context)
            videoSettingsManager.applyDecoderConfig(renderersFactory, decoderMode)

            exoPlayer = ExoPlayer.Builder(context, renderersFactory)
                .setSeekBackIncrementMs(10000)
                .setSeekForwardIncrementMs(10000)
                .setHandleAudioBecomingNoisy(true)
                .build().apply {
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                            if (isPlaying) {
                                startProgressTracker()
                            } else {
                                stopProgressTracker()
                            }
                        }

                        override fun onPlaybackStateChanged(state: Int) {
                            _isBuffering.value = (state == Player.STATE_BUFFERING)
                            if (state == Player.STATE_READY) {
                                _duration.value = duration.coerceAtLeast(0L)
                                _errorState.value = null
                            } else if (state == Player.STATE_ENDED) {
                                _isPlaying.value = false
                                stopProgressTracker()
                                onVideoCompleted?.invoke()
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            val isCodec = isCodecException(error)
                            val isSource = isSourceException(error)

                            CrashlyticsLogger.setCustomKey("error_code_name", error.errorCodeName)
                            CrashlyticsLogger.setCustomKey("is_codec_error", isCodec)
                            CrashlyticsLogger.logException(error, tag = "ExoPlayerError")

                            val userMessage = when {
                                isCodec -> "Codec decoding failure: unsupported media format."
                                isSource -> "Source error: failed to read video stream."
                                else -> "Playback error (${error.errorCodeName}): ${error.localizedMessage ?: "Unknown error"}"
                            }

                            _errorState.value = PlayerErrorState(
                                errorCode = error.errorCode,
                                message = userMessage,
                                isCodecError = isCodec,
                                isSourceError = isSource
                            )
                            _isPlaying.value = false
                            _isBuffering.value = false
                            stopProgressTracker()
                        }

                        override fun onTracksChanged(tracks: Tracks) {
                            extractTracks(tracks)
                        }
                    })
                }
        }
        return exoPlayer!!
    }

    fun playVideo(uriString: String, startPositionMs: Long = 0L, externalSubtitleUri: Uri? = null) {
        val player = getOrCreatePlayer()
        _errorState.value = null

        val uri = Uri.parse(uriString)
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(uri)
            .setMediaId(uriString)

        if (externalSubtitleUri != null) {
            val subtitleConfig = MediaItem.SubtitleConfiguration.Builder(externalSubtitleUri)
                .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                .setLanguage("custom")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
            mediaItemBuilder.setSubtitleConfigurations(listOf(subtitleConfig))
        }

        player.setMediaItem(mediaItemBuilder.build())
        player.prepare()
        if (startPositionMs > 0L) {
            player.seekTo(startPositionMs)
        }
        player.playWhenReady = true
    }

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun seekBy(offsetMs: Long) {
        exoPlayer?.let {
            val target = (it.currentPosition + offsetMs).coerceIn(0L, it.duration.coerceAtLeast(0L))
            it.seekTo(target)
            _currentPosition.value = target
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.playbackParameters = PlaybackParameters(speed)
    }

    fun retryPlayback(uriString: String, currentPos: Long = 0L) {
        _errorState.value = null
        exoPlayer?.release()
        exoPlayer = null
        playVideo(uriString, currentPos)
    }

    fun switchDecoderMode(mode: DecoderMode, uriString: String? = null, currentPos: Long = 0L) {
        _errorState.value = null
        scope.launch {
            videoSettingsManager.setDecoderMode(mode)
        }
        val uri = uriString ?: exoPlayer?.currentMediaItem?.mediaId
        val pos = if (currentPos > 0L) currentPos else (exoPlayer?.currentPosition ?: 0L)
        exoPlayer?.release()
        exoPlayer = null
        getOrCreatePlayer(decoderMode = mode)
        if (uri != null) {
            playVideo(uri, pos)
        }
    }

    fun clearError() {
        _errorState.value = null
    }

    fun addExternalSubtitle(uri: Uri, currentVideoUriString: String) {
        val player = exoPlayer ?: return
        val currentPos = player.currentPosition

        playVideo(currentVideoUriString, currentPos, uri)

        val externalSub = SubtitleTrack(
            id = uri.toString(),
            label = uri.lastPathSegment ?: "External Subtitle",
            language = "External",
            uriString = uri.toString(),
            isExternal = true
        )
        _availableSubtitles.value = _availableSubtitles.value + externalSub
        _selectedSubtitle.value = externalSub
    }

    private fun extractTracks(tracks: Tracks) {
        val subs = mutableListOf<SubtitleTrack>()
        val audios = mutableListOf<AudioTrackInfo>()

        for (group in tracks.groups) {
            if (group.type == C.TRACK_TYPE_TEXT) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    subs.add(
                        SubtitleTrack(
                            id = "sub_$i",
                            label = format.label ?: format.language ?: "Track ${subs.size + 1}",
                            language = format.language
                        )
                    )
                }
            } else if (group.type == C.TRACK_TYPE_AUDIO) {
                for (i in 0 until group.length) {
                    val format = group.getTrackFormat(i)
                    audios.add(
                        AudioTrackInfo(
                            index = i,
                            label = format.label ?: format.language ?: "Audio Track ${audios.size + 1}",
                            language = format.language,
                            channelCount = format.channelCount
                        )
                    )
                }
            }
        }
        _availableSubtitles.value = subs
        _availableAudioTracks.value = audios
    }

    private fun isCodecException(error: PlaybackException): Boolean {
        return error.errorCode in listOf(
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
            PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FAILED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED
        )
    }

    private fun isSourceException(error: PlaybackException): Boolean {
        return error.errorCode >= PlaybackException.ERROR_CODE_IO_UNSPECIFIED &&
                error.errorCode <= PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let {
                    _currentPosition.value = it.currentPosition.coerceAtLeast(0L)
                    _duration.value = it.duration.coerceAtLeast(0L)
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }
}
