package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.ui.components.MiniPlayerBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.VideoItem
import com.example.ui.screens.FolderDetailScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LSPlayerTheme
import com.example.ui.viewmodel.MediaViewModel
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SettingsViewModel

import com.example.ui.components.PermissionRationaleDialog
import com.example.utils.PermissionUtils

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.util.Rational

sealed class Screen {
    object Library : Screen()
    data class FolderDetail(val folderPath: String) : Screen()
    data class Player(val video: VideoItem) : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private var isPlayerActive = false
    private val _isInPipMode = mutableStateOf(false)

    fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isPlayerActive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        _isInPipMode.value = isInPictureInPictureMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LSPlayerTheme {
                val mediaViewModel: MediaViewModel = viewModel()
                val playerViewModel: PlayerViewModel = viewModel()
                val settingsViewModel: SettingsViewModel = viewModel()

                val userPrefs by settingsViewModel.userPreferences.collectAsState()

                LaunchedEffect(userPrefs.languageCode) {
                    com.example.utils.LocaleHelper.applyLocale(this@MainActivity, userPrefs.languageCode)
                }

                // Request permissions
                var hasStoragePermission by remember {
                    mutableStateOf(PermissionUtils.hasStoragePermission(this@MainActivity))
                }
                var showRationaleDialog by remember { mutableStateOf(false) }
                var isPermanentlyDenied by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val videoGranted = permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: false
                    val storageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
                    hasStoragePermission = videoGranted || storageGranted
                    
                    if (hasStoragePermission) {
                        mediaViewModel.scanMedia()
                    } else {
                        val shouldRationale = PermissionUtils.shouldShowRationale(this@MainActivity)
                        isPermanentlyDenied = !shouldRationale
                        showRationaleDialog = true
                    }
                }

                LaunchedEffect(Unit) {
                    if (!hasStoragePermission) {
                        permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                    } else {
                        mediaViewModel.scanMedia()
                    }
                }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }

                val activeVideo by playerViewModel.playerController.currentVideo.collectAsState()
                val isPlaying by playerViewModel.playerController.isPlaying.collectAsState()
                val currentPosition by playerViewModel.playerController.currentPosition.collectAsState()
                val duration by playerViewModel.playerController.duration.collectAsState()

                val isInPipMode by _isInPipMode

                LaunchedEffect(currentScreen) {
                    isPlayerActive = (currentScreen is Screen.Player)
                }

                val showMiniPlayer = (currentScreen !is Screen.Player) && (activeVideo != null)
                val screenModifier = if (showMiniPlayer) Modifier.padding(bottom = 72.dp) else Modifier

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val screen = currentScreen) {
                        is Screen.Library -> {
                            LibraryScreen(
                                viewModel = mediaViewModel,
                                modifier = screenModifier,
                                onVideoClick = { video ->
                                    if (activeVideo?.id != video.id) {
                                        val allVideos = mediaViewModel.getFilteredVideos()
                                        val startIdx = allVideos.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
                                        playerViewModel.playerController.setQueue(allVideos, startIdx)
                                    }
                                    currentScreen = Screen.Player(video)
                                },
                                onFolderClick = { folderPath ->
                                    currentScreen = Screen.FolderDetail(folderPath)
                                },
                                onOpenSettings = {
                                    currentScreen = Screen.Settings
                                },
                                onPlayNext = { video ->
                                    playerViewModel.playerController.addToQueueAsNext(video)
                                },
                                onAddToQueue = { video ->
                                    playerViewModel.playerController.addToQueue(video)
                                }
                            )
                        }

                        is Screen.FolderDetail -> {
                            FolderDetailScreen(
                                folderPath = screen.folderPath,
                                viewModel = mediaViewModel,
                                modifier = screenModifier,
                                onBack = { currentScreen = Screen.Library },
                                onVideoClick = { video ->
                                    if (activeVideo?.id != video.id) {
                                        val folderVideos = mediaViewModel.getVideosForFolder(screen.folderPath)
                                        val startIdx = folderVideos.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
                                        playerViewModel.playerController.setQueue(folderVideos, startIdx)
                                    }
                                    currentScreen = Screen.Player(video)
                                },
                                onPlayNext = { video ->
                                    playerViewModel.playerController.addToQueueAsNext(video)
                                },
                                onAddToQueue = { video ->
                                    playerViewModel.playerController.addToQueue(video)
                                }
                            )
                        }

                        is Screen.Player -> {
                            PlayerScreen(
                                video = screen.video,
                                playerViewModel = playerViewModel,
                                mediaViewModel = mediaViewModel,
                                isInPipMode = isInPipMode,
                                onEnterPip = { enterPipMode() },
                                onBack = { currentScreen = Screen.Library }
                            )
                        }

                        is Screen.Settings -> {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                modifier = screenModifier,
                                onBack = { currentScreen = Screen.Library }
                            )
                        }
                    }

                    if (showMiniPlayer) {
                        MiniPlayerBar(
                            video = activeVideo,
                            isPlaying = isPlaying,
                            currentPosition = currentPosition,
                            duration = duration,
                            onTogglePlayPause = { playerViewModel.playerController.togglePlayPause() },
                            onOpenFullScreen = {
                                activeVideo?.let { currentScreen = Screen.Player(it) }
                            },
                            onClose = { playerViewModel.playerController.closeVideo() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }

                    if (showRationaleDialog) {
                        PermissionRationaleDialog(
                            isPermanentlyDenied = isPermanentlyDenied,
                            onRequestPermission = {
                                permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                            },
                            onOpenSettings = {
                                PermissionUtils.openAppSettings(this@MainActivity)
                            },
                            onDismiss = { showRationaleDialog = false }
                        )
                    }
                }
            }
        }
    }
}
