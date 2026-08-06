package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.data.model.AspectRatioMode
import com.example.data.model.PlayerOrientationMode
import com.example.data.model.VideoItem
import com.example.utils.BatteryUtils
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import com.example.ui.theme.PrimaryIndigo
import androidx.compose.ui.BiasAlignment
import com.example.ui.components.AudioTrackBottomSheet
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GestureHUD
import com.example.ui.components.PlaybackSpeedBottomSheet
import com.example.ui.components.QueueBottomSheet
import com.example.ui.components.SleepTimerDialog
import com.example.ui.components.SubtitleOptionsBottomSheet
import com.example.ui.components.VideoInfoDialog
import com.example.utils.HapticUtils
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.PlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    video: VideoItem,
    playerViewModel: PlayerViewModel,
    mediaViewModel: MediaViewModel,
    onBack: () -> Unit,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by playerViewModel.playerController.isPlaying.collectAsState()
    val currentPosition by playerViewModel.playerController.currentPosition.collectAsState()
    val duration by playerViewModel.playerController.duration.collectAsState()
    val playbackSpeed by playerViewModel.playerController.playbackSpeed.collectAsState()
    val availableSubtitles by playerViewModel.playerController.availableSubtitles.collectAsState()
    val selectedSubtitle by playerViewModel.playerController.selectedSubtitle.collectAsState()
    val subtitleOffsetMs by playerViewModel.playerController.subtitleOffsetMs.collectAsState()
    val availableAudioTracks by playerViewModel.playerController.availableAudioTracks.collectAsState()
    val selectedAudioIndex by playerViewModel.playerController.selectedAudioTrackIndex.collectAsState()
    val isBuffering by playerViewModel.playerController.isBuffering.collectAsState()
    val errorState by playerViewModel.playerController.errorState.collectAsState()

    val areControlsVisible by playerViewModel.areControlsVisible.collectAsState()
    val isLocked by playerViewModel.isLocked.collectAsState()
    val aspectRatioMode by playerViewModel.aspectRatioMode.collectAsState()
    val orientationMode by playerViewModel.orientationMode.collectAsState()
    val gestureOverlay by playerViewModel.gestureOverlay.collectAsState()
    val sleepTimerMinutes by playerViewModel.sleepTimerMinutes.collectAsState()

    val currentVideoState by playerViewModel.playerController.currentVideo.collectAsState()
    val activeVideo = currentVideoState ?: video

    val queue by playerViewModel.queue.collectAsState()
    val currentIndex by playerViewModel.currentIndex.collectAsState()
    val showQueueSheet by playerViewModel.showQueueSheet.collectAsState()

    val showVideoInfo by playerViewModel.showVideoInfoDialog.collectAsState()
    val showSubtitleSheet by playerViewModel.showSubtitleSheet.collectAsState()
    val showAudioSheet by playerViewModel.showAudioSheet.collectAsState()
    val showSleepTimerDialog by playerViewModel.showSleepTimerDialog.collectAsState()

    var showSpeedMenu by remember { mutableStateOf(false) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableLongStateOf(0L) }
    var lastHapticPos by remember { mutableLongStateOf(0L) }

    val userPreferences by playerViewModel.userPreferences.collectAsState()
    val batteryStatus by produceState(initialValue = BatteryUtils.getBatteryStatus(context)) {
        BatteryUtils.observeBatteryStatus(context).collect { value = it }
    }
    val isBatterySaverActive = userPreferences.isBatterySaverEnabled && batteryStatus.isLowBattery

    // Lower display frame rate when battery saver mode is active (< 20% battery)
    LaunchedEffect(isBatterySaverActive) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                context.findActivity()?.window?.let { window ->
                    val params = window.attributes
                    // Force refresh rate preference if needed
                    window.attributes = params
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Apply orientation mode change
    LaunchedEffect(orientationMode) {
        context.findActivity()?.requestedOrientation = orientationMode.activityInfoOrientation
    }

    // Start playing video when entering screen if queue is empty or active video changed
    LaunchedEffect(video.id) {
        if (queue.isEmpty() || queue.none { it.id == video.id }) {
            playerViewModel.loadAndPlay(video, video.lastPlayedPositionMs)
        }
    }

    // Save progress periodically & on exit
    DisposableEffect(activeVideo) {
        onDispose {
            mediaViewModel.saveProgress(activeVideo, playerViewModel.playerController.currentPosition.value, playerViewModel.playerController.duration.value)
            context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    BackHandler {
        context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        mediaViewModel.saveProgress(activeVideo, currentPosition, duration)
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("player_screen_root")
    ) {
        // Media3 ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerViewModel.playerController.getPlayer()
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = playerViewModel.playerController.getPlayer()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    try {
                        val surfaceView = view.videoSurfaceView as? android.view.SurfaceView
                        val frameRate = if (isBatterySaverActive) 24f else 0f
                        val compatibility = if (isBatterySaverActive) {
                            android.view.Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE
                        } else {
                            android.view.Surface.FRAME_RATE_COMPATIBILITY_DEFAULT
                        }
                        surfaceView?.holder?.surface?.setFrameRate(frameRate, compatibility)
                    } catch (e: Exception) {
                        // Fallback
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isInPipMode) {
                    if (isInPipMode) return@pointerInput
                    detectTapGestures(
                        onTap = { playerViewModel.toggleControls() },
                        onDoubleTap = { offset ->
                            val isRightSide = offset.x > (size.width / 2)
                            HapticUtils.performClick(context)
                            playerViewModel.doubleTapSeek(isForward = isRightSide)
                        }
                    )
                }
                .pointerInput(isLocked, isInPipMode) {
                    if (isLocked || isInPipMode) return@pointerInput
                    var isLeftSide = false
                    var startY = 0f

                    detectDragGestures(
                        onDragStart = { offset ->
                            isLeftSide = offset.x < size.width / 2f
                            startY = offset.y
                            val activity = context.findActivity()
                            if (isLeftSide && activity != null) {
                                playerViewModel.startBrightnessDrag(activity)
                            } else {
                                playerViewModel.startVolumeDrag(context)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val deltaY = startY - change.position.y
                            val dragRatio = deltaY / (size.height * 0.7f)
                            val deltaPercent = dragRatio * 100f
                            val activity = context.findActivity()
                            if (isLeftSide && activity != null) {
                                playerViewModel.updateBrightnessDrag(activity, deltaPercent)
                            } else {
                                playerViewModel.updateVolumeDrag(context, deltaPercent)
                            }
                        }
                    )
                }
        )

        // Buffering Indicator
        if (isBuffering && errorState == null) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Error Overlay
        errorState?.let { error ->
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (error.isCodecError) "Playback Codec Error" else "Playback Error",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onBack) {
                            Text("Go Back")
                        }
                        if (error.isCodecError) {
                            TextButton(
                                onClick = {
                                    playerViewModel.playerController.switchDecoderMode(com.example.data.preferences.DecoderMode.SOFTWARE)
                                }
                            ) {
                                Text("Software Decoder", fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                            }
                        } else {
                            TextButton(
                                onClick = { playerViewModel.retryPlayback() }
                            ) {
                                Text("Retry", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Gesture Overlay (Volume, Brightness, Double-tap seek HUD)
        if (!isInPipMode) {
            GestureHUD(
                overlayState = gestureOverlay,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }

        // Lock Toggle Button (When locked)
        if (isLocked && !isInPipMode) {
            IconButton(
                onClick = {
                    HapticUtils.performLockToggle(context, isNowLocked = false)
                    playerViewModel.toggleLock()
                },
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .testTag("unlock_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Unlock Controls",
                    tint = Color.White
                )
            }
        }

        // Full Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible && !isLocked && !isInPipMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            context.findActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            mediaViewModel.saveProgress(activeVideo, currentPosition, duration)
                            onBack()
                        },
                        modifier = Modifier.testTag("player_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = activeVideo.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isBatterySaverActive) {
                        Surface(
                            color = Color(0xFFE53935).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BatterySaver,
                                    contentDescription = "Battery Saver Active",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "24 FPS Saver",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Queue Button
                    IconButton(
                        onClick = { playerViewModel.openQueueSheet() },
                        modifier = Modifier.testTag("queue_sheet_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Playing Queue",
                            tint = if (queue.isNotEmpty()) PrimaryIndigo else Color.White
                        )
                    }

                    // Subtitle Button
                    IconButton(
                        onClick = { playerViewModel.openSubtitleSheet() },
                        modifier = Modifier.testTag("subtitle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Subtitles",
                            tint = if (selectedSubtitle != null) PrimaryIndigo else Color.White
                        )
                    }

                    // Audio Track Button
                    IconButton(
                        onClick = { playerViewModel.openAudioSheet() },
                        modifier = Modifier.testTag("audio_track_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = "Audio Tracks",
                            tint = Color.White
                        )
                    }

                    // Sleep Timer Button
                    IconButton(
                        onClick = { playerViewModel.openSleepTimerDialog() },
                        modifier = Modifier.testTag("sleep_timer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Sleep Timer",
                            tint = if (sleepTimerMinutes != null) PrimaryIndigo else Color.White
                        )
                    }

                    // Picture-in-Picture Button
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        IconButton(
                            onClick = { onEnterPip() },
                            modifier = Modifier.testTag("pip_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPicture,
                                contentDescription = "Picture in Picture",
                                tint = Color.White
                            )
                        }
                    }

                    // Info Button
                    IconButton(
                        onClick = { playerViewModel.openVideoInfoDialog() },
                        modifier = Modifier.testTag("player_info_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Media Info",
                            tint = Color.White
                        )
                    }
                }

                // Center Transport Controls
                val hasPrev = playerViewModel.playerController.hasPrevious()
                val hasNxt = playerViewModel.playerController.hasNext()

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Video
                    IconButton(
                        onClick = {
                            HapticUtils.performClick(context)
                            playerViewModel.playPrevious()
                        },
                        enabled = hasPrev,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("skip_previous_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Video",
                            tint = if (hasPrev) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Rewind 10s
                    IconButton(
                        onClick = {
                            HapticUtils.performClick(context)
                            playerViewModel.playerController.seekBy(-10000L)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("rewind_10s_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play / Pause
                    Surface(
                        onClick = {
                            HapticUtils.performClick(context)
                            playerViewModel.playerController.togglePlayPause()
                        },
                        shape = CircleShape,
                        color = PrimaryIndigo,
                        modifier = Modifier
                            .size(60.dp)
                            .testTag("play_pause_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Fast Forward 10s
                    IconButton(
                        onClick = {
                            HapticUtils.performClick(context)
                            playerViewModel.playerController.seekBy(10000L)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("fast_forward_10s_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Fast Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next Video
                    IconButton(
                        onClick = {
                            HapticUtils.performClick(context)
                            playerViewModel.playNext()
                        },
                        enabled = hasNxt,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("skip_next_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Video",
                            tint = if (hasNxt) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Bottom Controls Bar & Timeline
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Scrubbing Thumbnail Preview
                    AnimatedVisibility(
                        visible = isScrubbing,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        val progressFraction = if (duration > 0) (scrubPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                        val alignmentBias = (progressFraction * 2f) - 1f

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = BiasAlignment(horizontalBias = alignmentBias, verticalBias = 0f)
                        ) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, PrimaryIndigo),
                                colors = CardDefaults.cardColors(containerColor = Color.Black),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier
                                    .width(130.dp)
                                    .height(76.dp)
                                    .testTag("scrub_preview_card")
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    val imageModel = remember(activeVideo.uriString, activeVideo.path, scrubPosition) {
                                        ImageRequest.Builder(context)
                                            .data(activeVideo.uriString.ifEmpty { activeVideo.path })
                                            .videoFrameMillis(scrubPosition)
                                            .crossfade(false)
                                            .build()
                                    }

                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = "Seek frame preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Timestamp label overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.8f))
                                            .padding(vertical = 3.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = "${formatTime(scrubPosition)} / ${formatTime(duration)}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Audio Waveform Visualizer
                    AudioWaveformVisualizer(
                        isPlaying = isPlaying,
                        isBatterySaverActive = isBatterySaverActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )

                    // Timeline Slider & Time Labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayedPos = if (isScrubbing) scrubPosition else currentPosition
                        val currentFormatted = formatTime(displayedPos)
                        val totalFormatted = formatTime(duration)

                        Text(
                            text = currentFormatted,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Slider(
                            value = (if (isScrubbing) scrubPosition else currentPosition).toFloat(),
                            onValueChange = { newValue ->
                                isScrubbing = true
                                val newPos = newValue.toLong()
                                if (kotlin.math.abs(newPos - lastHapticPos) >= 1000L) {
                                    HapticUtils.performTick(context)
                                    lastHapticPos = newPos
                                }
                                scrubPosition = newPos
                            },
                            onValueChangeFinished = {
                                HapticUtils.performClick(context)
                                playerViewModel.playerController.seekTo(scrubPosition)
                                isScrubbing = false
                            },
                            valueRange = 0f..(duration.coerceAtLeast(1L).toFloat()),
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryIndigo,
                                activeTrackColor = PrimaryIndigo,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .testTag("timeline_slider")
                        )

                        Text(
                            text = totalFormatted,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Quick Actions Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                HapticUtils.performLockToggle(context, isNowLocked = !isLocked)
                                playerViewModel.toggleLock()
                            },
                            modifier = Modifier.testTag("lock_btn")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Lock Controls", tint = Color.White)
                        }

                        // Rotation Lock Button
                        IconButton(
                            onClick = {
                                val newMode = playerViewModel.cycleOrientationMode()
                                context.findActivity()?.requestedOrientation = newMode.activityInfoOrientation
                            },
                            modifier = Modifier.testTag("rotation_lock_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ScreenRotation,
                                    contentDescription = "Rotation Lock (${orientationMode.label})",
                                    tint = if (orientationMode != PlayerOrientationMode.SENSOR) PrimaryIndigo else Color.White
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = orientationMode.label,
                                    color = if (orientationMode != PlayerOrientationMode.SENSOR) PrimaryIndigo else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Speed Button
                        IconButton(
                            onClick = { showSpeedMenu = true },
                            modifier = Modifier.testTag("speed_selector_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed", tint = if (playbackSpeed != 1.0f) PrimaryIndigo else Color.White)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${playbackSpeed}x",
                                    color = if (playbackSpeed != 1.0f) PrimaryIndigo else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Aspect Ratio Toggle
                        IconButton(
                            onClick = { playerViewModel.cycleAspectRatio() },
                            modifier = Modifier.testTag("aspect_ratio_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AspectRatio, contentDescription = "Aspect Ratio", tint = Color.White)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = aspectRatioMode.label,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs and BottomSheets
    if (!isInPipMode) {
        if (showQueueSheet) {
            QueueBottomSheet(
                queue = queue,
                currentIndex = currentIndex,
                onSelectIndex = { idx ->
                    playerViewModel.playQueueIndex(idx)
                    playerViewModel.closeQueueSheet()
                },
                onRemoveIndex = { idx ->
                    playerViewModel.removeFromQueue(idx)
                },
                onClearQueue = {
                    playerViewModel.clearQueue()
                },
                onDismiss = { playerViewModel.closeQueueSheet() }
            )
        }

        if (showSubtitleSheet) {
            SubtitleOptionsBottomSheet(
                subtitles = availableSubtitles,
                selectedSubtitle = selectedSubtitle,
                subtitleOffsetMs = subtitleOffsetMs,
                onAdjustSubtitleOffset = { delta -> playerViewModel.adjustSubtitleOffset(delta) },
                onSetSubtitleOffset = { offset -> playerViewModel.setSubtitleOffset(offset) },
                onSelectSubtitle = { track ->
                    playerViewModel.closeSubtitleSheet()
                },
                onExternalSubtitlePicked = { uri ->
                    playerViewModel.addExternalSubtitleUri(uri)
                    playerViewModel.closeSubtitleSheet()
                },
                onDismiss = { playerViewModel.closeSubtitleSheet() }
            )
        }

        if (showAudioSheet) {
            AudioTrackBottomSheet(
                audioTracks = availableAudioTracks,
                selectedIndex = selectedAudioIndex,
                onSelectTrack = { index ->
                    playerViewModel.closeAudioSheet()
                },
                onDismiss = { playerViewModel.closeAudioSheet() }
            )
        }

        if (showSleepTimerDialog) {
            SleepTimerDialog(
                currentMinutes = sleepTimerMinutes,
                onSelectMinutes = { mins -> playerViewModel.setSleepTimer(mins) },
                onDismiss = { playerViewModel.closeSleepTimerDialog() }
            )
        }

        if (showVideoInfo) {
            VideoInfoDialog(
                video = video,
                onDismiss = { playerViewModel.closeVideoInfoDialog() }
            )
        }

        if (showSpeedMenu) {
            PlaybackSpeedBottomSheet(
                currentSpeed = playbackSpeed,
                onSpeedSelected = { speed ->
                    playerViewModel.playerController.setSpeed(speed)
                },
                onDismiss = { showSpeedMenu = false }
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
