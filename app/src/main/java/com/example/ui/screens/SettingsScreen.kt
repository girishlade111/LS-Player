package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Info
import com.example.utils.CrashlyticsLogger
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.DecoderMode
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SettingsViewModel
import com.example.utils.BatteryUtils
import com.example.utils.LocaleHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val isRescanning by viewModel.isRescanning.collectAsState()
    val rescanMessage by viewModel.rescanMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(rescanMessage) {
        rescanMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearRescanMessage()
        }
    }

    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCrashConfirmDialog by remember { mutableStateOf(false) }

    val currentLang = LocaleHelper.getLanguageByCode(prefs.languageCode)

    val batteryStatus by produceState(initialValue = BatteryUtils.getBatteryStatus(context)) {
        BatteryUtils.observeBatteryStatus(context).collect { value = it }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Section: Language & Localization
            SettingsSectionHeader(title = "LANGUAGE & LOCALIZATION")
            SettingsRow(
                icon = Icons.Default.Language,
                title = "App Language",
                subtitle = "${currentLang.displayName} (${currentLang.nativeName})",
                onClick = { showLanguageDialog = true }
            )

            // Section: Library Management
            SettingsSectionHeader(title = "LIBRARY & MEDIA STORAGE")
            SettingsRow(
                icon = Icons.Default.Refresh,
                title = "Library Rescan",
                subtitle = if (isRescanning) "Scanning MediaStore & rebuilding Room database..." else "Force clean rebuild of Room database by re-scanning MediaStore",
                onClick = { viewModel.rescanLibrary() },
                enabled = !isRescanning,
                testTag = "library_rescan_btn",
                trailingContent = {
                    if (isRescanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = PrimaryIndigo,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            )

            // Section: Power & Battery
            SettingsSectionHeader(title = "POWER & BATTERY")
            val isLowBat = batteryStatus.isLowBattery
            val batterySub = if (prefs.isBatterySaverEnabled && isLowBat) {
                "ACTIVE (${batteryStatus.percentage}% battery) • Capping frame rate & pausing UI animations"
            } else if (prefs.isBatterySaverEnabled) {
                "Lowers playback frame rate & disables non-essential animations below 20% (Current: ${batteryStatus.percentage}%)"
            } else {
                "Disabled (Current: ${batteryStatus.percentage}%)"
            }
            SettingsSwitchRow(
                icon = Icons.Default.BatterySaver,
                title = "Battery Saver Mode",
                subtitle = batterySub,
                checked = prefs.isBatterySaverEnabled,
                onCheckedChange = { viewModel.setBatterySaverEnabled(it) },
                testTag = "battery_saver_switch"
            )

            // Section: Playback
            SettingsSectionHeader(title = "PLAYBACK ENGINE")
            val currentDecoder = prefs.decoderMode
            SettingsRow(
                icon = Icons.Default.Memory,
                title = "Video Decoder Mode",
                subtitle = "${currentDecoder.displayName} • Tap to change",
                onClick = {
                    val nextMode = when (currentDecoder) {
                        DecoderMode.AUTO -> DecoderMode.HARDWARE
                        DecoderMode.HARDWARE -> DecoderMode.SOFTWARE
                        DecoderMode.SOFTWARE -> DecoderMode.AUTO
                    }
                    viewModel.setDecoderMode(nextMode)
                }
            )
            SettingsSwitchRow(
                icon = Icons.Default.PlayCircle,
                title = "Resume Playback",
                subtitle = "Automatically resume videos from last saved position",
                checked = prefs.isResumePlaybackEnabled,
                onCheckedChange = { viewModel.setResumePlayback(it) },
                testTag = "resume_playback_switch"
            )
            SettingsSwitchRow(
                icon = Icons.Default.PlayCircle,
                title = "Background Playback",
                subtitle = "Keep audio playing when app is in background or screen off",
                checked = prefs.isBackgroundPlaybackEnabled,
                onCheckedChange = { viewModel.setBackgroundPlayback(it) },
                testTag = "background_playback_switch"
            )

            // Section: Gestures
            SettingsSectionHeader(title = "GESTURES & CONTROLS")
            SettingsRow(
                icon = Icons.Default.Gesture,
                title = "Double Tap Seek Interval",
                subtitle = "${prefs.doubleTapSeekSeconds} seconds",
                onClick = { viewModel.setDoubleTapSeek(if (prefs.doubleTapSeekSeconds == 10) 15 else 10) }
            )

            // Section: Subtitles
            SettingsSectionHeader(title = "SUBTITLES")
            SettingsRow(
                icon = Icons.Default.Subtitles,
                title = "Subtitle Size & Style",
                subtitle = "Font size ${prefs.subtitleTextSizeSp}sp, White with black outline",
                onClick = {}
            )

            // Section: Privacy & About
            SettingsSectionHeader(title = "PRIVACY & ABOUT")
            // Section: App Info & Diagnostics
            SettingsSectionHeader(title = "DIAGNOSTICS & CRASH REPORTING")
            SettingsRow(
                icon = Icons.Default.BugReport,
                title = "Firebase Crashlytics Status",
                subtitle = "Active monitoring • Exception & stack trace collection enabled",
                onClick = {},
                testTag = "crashlytics_status_row"
            )
            SettingsRow(
                icon = Icons.Default.Info,
                title = "Log Test Non-Fatal Exception",
                subtitle = "Record a diagnostic exception in Crashlytics without crashing",
                onClick = { viewModel.testReportNonFatal() },
                testTag = "test_non_fatal_btn"
            )
            SettingsRow(
                icon = Icons.Default.BugReport,
                title = "Simulate App Crash Test",
                subtitle = "Triggers an uncaught exception to verify Crashlytics crash collection flow",
                onClick = { showCrashConfirmDialog = true },
                testTag = "test_crash_btn"
            )

            SettingsRow(
                icon = Icons.Default.Security,
                title = "Privacy Policy",
                subtitle = "LS Player is 100% offline-first. No telemetries or ad trackers.",
                onClick = {}
            )
            SettingsRow(
                icon = Icons.Default.Info,
                title = "About LS Player",
                subtitle = "Version 1.0.0 Enterprise • Engine Media3 ExoPlayer",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showLanguageDialog) {
            LanguageSelectionDialog(
                currentLanguageCode = prefs.languageCode,
                onLanguageSelected = { code ->
                    viewModel.setLanguageCode(code)
                    LocaleHelper.applyLocale(context, code)
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        if (showCrashConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCrashConfirmDialog = false },
                title = {
                    Text(
                        text = "Simulate Crash Test",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "This will trigger a test RuntimeException to verify that Firebase Crashlytics captures and reports uncaught application crashes. The app will close after reporting.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCrashConfirmDialog = false
                            viewModel.triggerTestCrash()
                        }
                    ) {
                        Text("Trigger Crash", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCrashConfirmDialog = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            )
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select App Language",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                LocaleHelper.supportedLanguages.forEach { language ->
                    val isSelected = language.code.equals(currentLanguageCode, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(language.code)
                                onDismiss()
                            }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onLanguageSelected(language.code)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryIndigo)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = language.displayName,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (language.nativeName.isNotEmpty() && language.nativeName != language.displayName) {
                                Text(
                                    text = language.nativeName,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PrimaryIndigo)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        color = PrimaryIndigo,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TextSecondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PrimaryIndigo,
                    checkedTrackColor = PrimaryIndigo.copy(alpha = 0.3f)
                )
            )
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    testTag: String? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) TextSecondary else TextMuted
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
            }
        }
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), thickness = 0.5.dp)
}
