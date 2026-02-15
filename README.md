# Attenote

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="Attenote app icon" width="120" />
</p>

<p align="center"><strong>Attendance and teaching notes, finally in one focused app.</strong></p>

Attenote is an Android app built for teachers who want a fast, reliable way to run daily attendance and capture lesson notes without juggling spreadsheets, chat groups, and paper registers.

## Overview

Attenote helps you stay in control of classroom operations from one clean workflow:

- Organize classes and student rosters.
- Take attendance quickly with clear class/session context.
- Capture lesson notes and media from the same workspace.
- Review summaries and key trends across days.
- Keep your data structured for backup and restore.

The app is designed around practical teaching time, with an interface focused on speed, clarity, and low friction during live sessions.

## Why Attenote

- Built for real class flow: attendance + notes together, not separate tools.
- Teacher-first UX: simple structure, focused screens, minimal cognitive load.
- Data that stays useful: structured records for follow-up, reporting, and continuity.
- Mobile-native reliability: optimized for day-to-day Android use.

## Core Capabilities

- Class and schedule management
- Student management
- Attendance tracking
- Lesson notes with attachments
- Daily summary and insights
- Backup and restore support

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room
- Koin
- DataStore
- Coroutines / Flow
- WorkManager

## Quick Start

```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.uteacher.attenote -c android.intent.category.LAUNCHER 1
```

## Package

`com.uteacher.attenote`
