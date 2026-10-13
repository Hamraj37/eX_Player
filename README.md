# eX Player 🎬

**eX Player** is a modern, feature-rich Android video player app featuring a Liquid Glass UI design, smooth playback powered by **AndroidX Media3 (ExoPlayer)**, background audio playback, Picture-in-Picture (PiP) support, gesture controls, and flexible media library management.

Built purely in **Java** with native Android components and AndroidX Architecture Libraries.

---

## ✨ Features

### 🎥 Player & Playback
- **Media3 ExoPlayer Integration:** High-performance video decoding and seamless playback.
- **Picture-in-Picture (PiP):** Keep watching in a floating window while using other apps.
- **Background Audio Playback:** Foreground service with media notification controls (`MediaSessionService`).
- **Aspect Ratio Controls:** Toggle between Fit, Fill, Zoom, Fixed Width, and Fixed Height modes.
- **Audio & Subtitle Track Selectors:** Easily switch between multi-audio streams and embedded subtitle tracks.
- **Variable Playback Speed:** Fine-tune playback speed from 0.25x to 2.0x.
- **Gesture Controls:**
  - Swipe vertically on the left side for **Screen Brightness**.
  - Swipe vertically on the right side for **Media Volume**.
  - Double-tap to seek **Forward/Rewind 10s**.
  - Pinch-to-zoom for scaling.
- **Immersive Display Mode:** Hide navigation and status bars for distraction-free viewing.

### 📚 Media Library & UI
- **Liquid Glass Design:** Glassmorphism-inspired UI components and sleek layouts.
- **Local Media Scanner:** Automatic indexing of local videos via Android's `MediaStore`.
- **Search & Filter:** Instant real-time video search by title.
- **Sorting Options:** Sort videos by Title (A-Z), Date Added (Newest), File Size, or Duration.
- **Quick Resume FAB:** One-tap action to continue playing the last watched video.
- **Theme Modes:** System Default, Light Mode, and Dark Mode.

---

## 🛠 Tech Stack

- **Language:** Java 11
- **Min SDK:** 29 (Android 10) | **Target SDK:** 37 (Android 15)
- **Build System:** Gradle (Groovy DSL) with Version Catalog (`libs.versions.toml`)
- **Media Engine:** AndroidX Media3 (`exoplayer`, `ui`, `session`)
- **UI Architecture:** Material Components, ViewBinding, ViewModel, LiveData
- **Image Loading:** Glide
- **Asynchronous & Service:** MediaSessionService Foreground Service

---

## 📁 Project Structure

```text
eXPlayer/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/hamraj37/explayer/
│   │   │   │   ├── adapter/             # RecyclerView Adapters (VideoAdapter)
│   │   │   │   ├── model/               # Data Models (VideoItem)
│   │   │   │   ├── repository/          # MediaStore Repositories (VideoRepository)
│   │   │   │   ├── service/             # Background Playback Service
│   │   │   │   ├── ui/                  # Custom Views (MarqueeTextView)
│   │   │   │   ├── viewmodel/           # Architecture ViewModels (VideoViewModel)
│   │   │   │   ├── MainActivity.java    # Library & Search UI
│   │   │   │   └── PlayerActivity.java  # Video Player & Controls UI
│   │   │   ├── res/                     # Layouts, Drawables, Styles & Resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                        # Unit Tests (JUnit)
│   │   └── androidTest/                 # Instrumentation Tests (Espresso)
│   └── build.gradle                     # Module build file (Groovy DSL)
├── gradle/
│   └── libs.versions.toml               # Gradle Version Catalog
├── build.gradle                         # Root build file
├── settings.gradle                      # Project settings
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or newer recommended
- **JDK:** Java 17 (for Gradle build environment)
- **Android Device / Emulator:** Running Android 10 (API Level 29) or higher

### Building & Running
1. Clone the repository:
   ```bash
   git clone https://github.com/hamraj37/eXPlayer.git
   cd eXPlayer
   ```
2. Open the project in Android Studio.
3. Sync project with Gradle files.
4. Run the app on an emulator or connected device:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Unit Tests
To run unit tests:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 License

```text
Copyright (c) 2026 eX Player

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, goddess or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
