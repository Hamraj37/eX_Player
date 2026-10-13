# Project Plan

Create a video player app like MX Player using XML activities and Java language, with a liquid glass (glassmorphism / frosted glass translucent overlays) theme, supporting every video format (MP4, MKV, AVI, MOV, FLV, TS, WEBM, M3U8, etc.) using Jetpack Media3 / ExoPlayer in Java, with gesture controls for volume, brightness, seeking, speed control, picture-in-picture, background play, subtitle support, and a liquid glass UI design using XML layouts and drawables.

## Project Brief

# Project Brief: Liquid Glass Video Player

## Features
- **Universal Media Playback & Subtitles**: High-performance playback supporting wide video formats (MP4, MKV, AVI, MOV, FLV, TS, WEBM, M3U8) with embedded and external subtitle integration (SRT, ASS, VTT) using Jetpack Media3 ExoPlayer.
- **Gesture-Based Player Controls**: Screen touch gestures for vertical brightness control (left side), volume adjustment (right side), horizontal swipe seeking, and double-tap gesture controls.
- **Liquid Glass UI & Media Library**: Frosted glass translucent overlay aesthetic built with XML drawables and layouts, featuring automated local storage video indexing.
- **Background Playback & Picture-in-Picture (PiP)**: Support for background audio playback via MediaSession and Android Picture-in-Picture mode for seamless multitasking.

## High-Level Tech Stack
- **Language**: Java
- **UI Framework**: Android View System with XML Layouts, Activities, and custom Glassmorphism Drawables
- **Media Engine**: AndroidX Media3 ExoPlayer (`androidx.media3:media3-exoplayer`, `androidx.media3:media3-ui`)
- **Architecture & Core Components**: MVVM with Android Architecture Components (`ViewModel`, `LiveData`, `ViewBinding`)

## Implementation Steps

### Task_1_VideoLibrary: Implement local video storage scanner and Liquid Glass media library UI in Java and XML Activity with RecyclerView, ViewModel, and LiveData.
- **Status:** COMPLETED
- **Updates:** Task 1 completed successfully. Local video scanner, VideoItem model, VideoRepository, VideoViewModel, VideoAdapter, item_video_glass.xml, Liquid Glass XML drawables, search filter, permission handler, and MainActivity created and tested. Build assembleDebug succeeds.
- **Acceptance Criteria:**
  - Storage permission handled and local videos indexed
  - RecyclerView displays video list with thumbnails and titles in liquid glass theme
  - Clicking video item navigates to player screen with video Uri

### Task_2_PlayerCoreAndGestures: Implement Player Activity using Media3 ExoPlayer with gesture controls, custom translucent liquid glass overlay controls, subtitle selector, aspect ratio, and speed options.
- **Status:** COMPLETED
- **Updates:** Task 2 completed successfully. PlayerActivity with Media3 ExoPlayer, layout_glass_player_controls.xml, MX Player style gesture controls (brightness left, volume right, seek horizontal, double tap 10s seek), liquid glass UI overlays, Subtitle selector dialog, Audio track selector dialog, Aspect ratio toggle, Playback speed popup, orientation lock, and playlist navigation implemented and verified with assembleDebug.
- **Acceptance Criteria:**
  - Media3 ExoPlayer loads and plays local video formats
  - Gesture controls for brightness (left side), volume (right side), and horizontal seeking working
  - Custom liquid glass overlay player controls display playback controls, subtitle selector, aspect ratio toggle, and playback speed adjustment

### Task_3_PipAndBackgroundPlayback: Implement Picture-in-Picture (PiP) mode and background audio playback using MediaSession.
- **Status:** COMPLETED
- **Updates:** Task 3 completed successfully. Picture-in-Picture (PiP) mode and Background Playback using Media3 MediaSession, BackgroundPlaybackService, PictureInPictureParams with remote actions, auto-PiP, and background play toggle implemented and verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - App smoothly transitions to Picture-in-Picture mode when entering background during playback
  - Background audio playback supported with MediaSession controls

### Task_4_RunAndVerify: Final build, UI polish, app icon integration, and stability verification. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Task 4 completed successfully. App deployed, launched, and verified on standard phone emulator by critic_agent. 100% stability, no crashes, no ANRs, all features (Java, XML activities/layouts, Liquid Glass UI theme, Media3 ExoPlayer universal playback, gesture controls for volume/brightness/seek, subtitle selector dialog, audio selector, aspect ratio toggle, playback speed, PiP mode, background play, and search filter) verified working.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - App launches without crashes and all video player features work seamlessly
- **Duration:** N/A

