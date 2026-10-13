# eX Player 🎬

> **Liquid Glass Media Player for Android**  
> A modern, feature-packed Android video player powered by Jetpack Media3 (ExoPlayer), featuring a Liquid Glass UI design system, Picture-in-Picture (PiP), background playback, subtitle/audio track selection, gestures, and automated CI/CD release builds.

---

## ✨ Features

- 🎥 **Jetpack Media3 (ExoPlayer)**: High-performance hardware-accelerated video playback supporting various video codecs and containers.
- 🎨 **Liquid Glass UI**: Translucent design system with glassmorphic cards, custom control bars, smooth animations, and theme modes (Light, Dark, System Default).
- 📺 **Picture-in-Picture (PiP)**: Seamless PiP support for multitasking while watching videos.
- 🎧 **Background Playback**: Keep playing audio in the background using `MediaSessionService` and foreground notifications.
- 🎛️ **Advanced Player Controls**:
  - Double-tap gesture seeking (rewind/fast-forward 10 seconds).
  - Brightness and Volume gesture controls.
  - Aspect Ratio options (Fit to screen, Fill screen, Zoom, Fixed Width, Fixed Height).
  - Playback Speed adjustment (0.25x – 2.0x).
  - Audio and Subtitle track selector dialogs.
  - Immersive full-screen display toggle and screen lock control.
- 📂 **Media Library & Navigation**:
  - Automatically scans device video files (`READ_MEDIA_VIDEO` / `READ_EXTERNAL_STORAGE`).
  - Sorting options: Title (A-Z), Date Added, File Size, Duration.
  - Instant search bar filtering by video title.
  - "Resume Last Played" floating action button.
- 🚀 **Automated CI/CD**: Built-in GitHub Actions workflow ([`.github/workflows/android.yml`](.github/workflows/android.yml)) to compile release APKs and publish tagged GitHub releases.

---

## 📱 Tech Stack & Architecture

- **Language**: Kotlin & Java
- **Minimum SDK**: API 29 (Android 10)
- **Target SDK**: API 37 (Android 16)
- **Architecture**: MVVM (Model-View-ViewModel) with Android Architecture Components (`ViewModel`, `LiveData`)
- **Core Libraries**:
  - [AndroidX Media3 (ExoPlayer)](https://developer.android.com/guide/topics/media/media3) - Playback engine, MediaSession, UI controls
  - [Glide](https://github.com/bumptech/glide) - High-performance thumbnail loading and caching
  - [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Asynchronous background operations
  - [Material Components](https://github.com/material-components/material-components-android) - UI components
  - [SwipeRefreshLayout](https://developer.android.com/jetpack/androidx/releases/swiperefreshlayout) - Pull-to-refresh media library

---

## ⚙️ Getting Started & Building

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish / 2024.1+
- **JDK**: Java 17 or higher
- **Android SDK**: API 37 compiled SDK installed

### Building Locally

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Hamraj37/eX_Player.git
   cd eX_Player
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Build Release APK**:
   ```bash
   ./gradlew assembleRelease
   ```

Output APK path:  
`app/build/outputs/apk/debug/app-debug.apk` or `app/build/outputs/apk/release/app-release.apk`

---

## 🛠️ GitHub Actions Release Pipeline

The repository includes a GitHub Actions workflow [`.github/workflows/android.yml`](.github/workflows/android.yml) that allows building signed or unsigned release APKs on demand:

1. Go to the **Actions** tab on your GitHub repository.
2. Select **Android Release APK**.
3. Click **Run workflow**. You can optionally provide custom notes in the "What's new" input or leave it blank to automatically extract notes from git commit messages.
4. The workflow compiles the release APK and automatically creates a new **GitHub Release** tagged with the version name defined in [`app/build.gradle.kts`](app/build.gradle.kts).

---

## 📄 License

```text
Copyright (c) 2025 Hamraj37

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
