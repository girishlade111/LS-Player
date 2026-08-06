# 🎬 LS-Player — Modern Android Video & Media Player

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/Player-AndroidX_Media3_ExoPlayer-FF6F00?style=for-the-badge&logo=youtube&logoColor=white)](https://developer.android.com/guide/topics/media/media3)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

<p align="center">
  <b>A sleek, high-performance, open-source Android video player app engineered with Jetpack Compose, Material 3 design, and AndroidX Media3 ExoPlayer.</b>
</p>

[Key Features](#-key-features) • [Screenshots & UI](#-screens--ui-components) • [Tech Stack](#-tech-stack--architecture) • [Project Structure](#-project-structure) • [Getting Started](#-getting-started) • [Testing](#-testing) • [License](#-license)

</div>

---

## 📖 Overview

**LS-Player** is a feature-rich, privacy-focused local media player application built for modern Android devices. Designed from the ground up using **Jetpack Compose** and **Clean MVVM Architecture**, LS-Player provides fluid playback for local videos, HLS streams, folder navigation, playback queue management, and advanced player controls (gestures, audio tracks, subtitles, sleep timer, and speed controls).

---

## ✨ Key Features

### 🎥 **Advanced Video Playback**
- **AndroidX Media3 ExoPlayer**: Seamless hardware-accelerated rendering supporting MP4, MKV, WebM, TS, HLS (`.m3u8`), and more.
- **Multi-Audio Track Selection**: Switch on-the-fly between dual-audio and multi-language streams.
- **Subtitle Engine**: Full support for embedded and external subtitle files (`.srt`, `.vtt`, `.ass`), with customizable subtitle sizes, colors, and delay offsets.
- **Variable Playback Speed**: Precise speed adjustments ranging from `0.25x` up to `2.0x` without audio pitch distortion.
- **Aspect Ratio Control**: Dynamic video scaling modes: *Fit*, *Crop*, *Fill*, *Stretch*, *16:9*, and *4:3*.

### 👆 **Intuitive Touch & Gesture Controls**
- **Vertical Brightness Control**: Swipe vertically on the left side of the player screen to adjust brightness.
- **Vertical Volume Control**: Swipe vertically on the right side of the player screen to adjust audio level.
- **Horizontal Seeking**: Drag horizontally across the player screen for fine-grained video scrubbing with thumbnail preview feedback.
- **Double-Tap Seek**: Double-tap left or right to skip backward/forward by 10 seconds.
- **Pinch to Zoom**: Zoom in/out smoothly on video frames.
- **Screen Lock Toggle**: Lock touch inputs to prevent accidental touches during playback.

### 📂 **Smart Media Library & Folder Management**
- **Automatic Storage Scanning**: Detects and indexes local video files instantly upon grant of media permissions.
- **Folder Navigation**: Browse videos organized cleanly by physical storage directory structures.
- **Recently Played**: Quickly resume watching from where you left off with accurate progress tracking.
- **Instant Search & Filters**: Search videos by title, extension, or folder. Sort by Name, Date Added, Size, or Duration.

### 🎵 **Mini Player & Playback Queue**
- **Background & Mini Player**: Persistent mini-player bar at the bottom of the screen allows seamless app navigation while keeping audio/video controls active.
- **Queue & Playlist Manager**: Reorder, add, or clear video playback queues dynamically.

### ⏱️ **Utilities & Enhancements**
- **Sleep Timer**: Set automatic pause timers (15m, 30m, 45m, 60m, or custom duration).
- **Audio Waveform Visualizer**: Real-time waveform visualization for audio-focused media files.
- **Battery-Aware Optimization**: Automatically toggles power-saving modes when battery falls below thresholds.
- **Haptic Feedback**: Tactile feedback on button presses and gesture adjustments.
- **Multi-Language Support**: Built-in localization support for multi-language UI rendering.

---

## 🏗 Tech Stack & Architecture

LS-Player is architected according to modern Android development best practices, following **Clean Architecture** with a unidirectional data flow (UDF) powered by **MVVM**.

```
                           +-------------------------------------+
                           |            UI Layer                 |
                           |  Jetpack Compose + Material 3 + HUD |
                           +------------------+------------------+
                                              |
                                              v
                           +-------------------------------------+
                           |          ViewModel Layer            |
                           |  (MediaViewModel, PlayerViewModel)  |
                           +------------------+------------------+
                                              |
                                              v
                           +-------------------------------------+
                           |          Repository Layer           |
                           |  (MediaRepository, VideoRepository) |
                           +--------+-------------------+--------+
                                    |                   |
                                    v                   v
                     +----------------------+   +-----------------------+
                     | Room Local Database  |   | DataStore Preferences |
                     +----------------------+   +-----------------------+
```

| Domain | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | [Kotlin 2.x](https://kotlinlang.org/) | 100% Kotlin codebase with Coroutines & Flow |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Declarative UI engine with Material 3 design |
| **Media Core** | [AndroidX Media3 ExoPlayer](https://developer.android.com/guide/topics/media/media3) | Modern media playback engine with HLS module |
| **Database** | [Room Database](https://developer.android.com/training/data-storage/room) | Local SQLite persistence for media metadata |
| **Preferences** | [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Key-value async preference storage |
| **Image & Video** | [Coil Video](https://coil-kt.github.io/coil/) | Video frame extraction and thumbnail caching |
| **Dependency Processing**| [KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html) | Faster annotation processing for Room |
| **Testing** | [Roborazzi](https://github.com/takahirom/roborazzi) + [Robolectric](https://robolectric.org/) | Screenshot testing and JVM unit testing |
| **Crash Analytics** | [Firebase Crashlytics](https://firebase.google.com/docs/crashlytics) | Automated crash logging and reporting |

---

## 📁 Project Structure

```
LS-Player/
├── app/
│   ├── build.gradle.kts           # App-level Gradle dependencies and build configuration
│   ├── proguard-rules.pro         # Proguard/R8 optimization rules
│   └── src/
│       ├── androidTest/           # Android UI instrumentation tests
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── LsPlayerApplication.kt   # Application entry point & initializers
│       │   │   ├── MainActivity.kt          # Host Activity with Compose Navigation
│       │   │   ├── data/
│       │   │   │   ├── local/               # Room DB, DAOs, VideoEntity
│       │   │   │   ├── model/               # Data classes (VideoItem, VideoEntity)
│       │   │   │   ├── preferences/         # DataStore & VideoSettingsManager
│       │   │   │   └── repository/          # Media & Video Repositories
│       │   │   ├── player/
│       │   │   │   ├── PlayerController.kt      # ExoPlayer state & session wrapper
│       │   │   │   └── VideoPlaybackHelper.kt   # Playback helper utilities
│       │   │   ├── ui/
│       │   │   │   ├── components/          # Reusable UI components & bottom sheets
│       │   │   │   ├── screens/             # Main Screens (Library, Player, Settings, Folder)
│       │   │   │   ├── theme/               # Material 3 Color, Type, and Theme definition
│       │   │   │   └── viewmodel/           # MediaViewModel, PlayerViewModel, SettingsViewModel
│       │   │   └── utils/                   # Battery, Haptic, Permission, Locale, Crashlytics
│       │   └── res/                         # Icons, Drawables, Strings, Launcher Assets
│       └── test/                            # Unit tests & Roborazzi screenshot tests
├── gradle/
│   └── libs.versions.toml         # Version catalog managing all dependencies
├── build.gradle.kts               # Root Gradle configuration
├── settings.gradle.kts            # Plugin repositories and module definitions
├── .env.example                   # Environment configuration template
└── README.md                      # Project Documentation
```

---

## 📱 Screens & UI Components

### 🟢 **Core Screens**
1. **Library Screen** (`LibraryScreen.kt`): Main media hub displaying recent videos, folder directories, video lists, search bar, and view toggles.
2. **Folder Detail Screen** (`FolderDetailScreen.kt`): Dedicated view for videos contained inside a selected folder.
3. **Player Screen** (`PlayerScreen.kt`): Immersive full-screen video player featuring top/bottom controls, HUD gesture overlays, playback sliders, and lock mode.
4. **Settings Screen** (`SettingsScreen.kt`): Preference screen for adjusting theme modes, gesture sensitivities, hardware acceleration, and audio parameters.

### 🎛 **Interactive Dialogs & Sheets**
- `GestureHUD.kt` — On-screen gesture indicators for Volume, Brightness, and Seeking.
- `MiniPlayerBar.kt` — Floating mini player bar displayed across non-player screens.
- `AudioTrackBottomSheet.kt` — Audio track selector sheet.
- `SubtitleOptionsBottomSheet.kt` — Subtitle track picker and styling settings.
- `PlaybackSpeedDialog.kt` — Speed adjustment modal dialog.
- `SleepTimerDialog.kt` — Playback sleep timer control.
- `SortFilterBottomSheet.kt` — Media library sorting and filtering sheet.
- `QueueBottomSheet.kt` — Live playback queue editor.

---

## 🚀 Getting Started

### 📋 Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or newer recommended.
- **JDK**: Java 11 or Java 17 configured in Android Studio.
- **Android SDK**: `minSdk = 24` (Android 7.0), `targetSdk = 36`.
- **Gradle**: 8.x+ (managed via Gradle Wrapper).

### ⚙️ Installation & Build Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/girishlade111/LS-Player.git
   cd LS-Player
   ```

2. **Environment Configuration**
   Copy `.env.example` to `.env` in the root directory:
   ```bash
   cp .env.example .env
   ```

3. **Build the Debug APK**
   Using Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Connected Device / Emulator**
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Testing

LS-Player includes both unit tests and screenshot tests powered by **Roborazzi** and **Robolectric**.

### Running Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Running Screenshot Tests (Roborazzi)
To verify UI golden images:
```bash
./gradlew verifyRoborazziDebug
```

To record updated screenshot goldens:
```bash
./gradlew recordRoborazziDebug
```

---

## 🛡 License

Distributed under the MIT License. See `LICENSE` for more information.

---

<div align="center">
  <sub>Built with ❤️ using Jetpack Compose and Kotlin.</sub>
</div>
