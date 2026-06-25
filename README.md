# EzTechv2

EzTechv2 is an Android learning app for Python programming. It combines tutorial content, video lessons, coding problems, an in-app Python IDE, gamification, leaderboard, bookmarks, and user profile management.

## Features

- Email/password authentication with Firebase Authentication.
- Firestore-backed user profiles, learning content, video lessons, tutorials, coding problems, progress, bookmarks, and leaderboard data.
- Home dashboard with learning progress and quick actions.
- Learn module with video tutorials and article-style tutorial lessons.
- Problems module with ordered Python practice problems, search, filters, code drafts, custom input, submission history, and accepted submission tracking.
- In-app Python IDE powered by Chaquopy.
- Gamification with EXP, levels, streaks, badges, and leaderboard ranking.
- Profile and settings screens with display name editing, avatar URL, dark/light/system theme preference, notifications toggle, and logout.
- Offline-friendly fallback and lightweight in-memory caching for key Firestore reads.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Coroutines and Flow
- Firebase Authentication
- Cloud Firestore
- DataStore Preferences
- Chaquopy Python runtime
- Coil image loading
- YouTube Player library

## Project Structure

```text
app/                 Main Android app and navigation shell
core/common/         Shared Resource and common utilities
core/domain/         Domain models, repositories, and use cases
core/data/           Firebase, local data, Python execution, repository implementations
core/ui/             Shared UI components and theme
feature/auth/        Login, register, password reset
feature/home/        Home dashboard
feature/learn/       Tutorial/video learning screens
feature/problems/    Problem list, detail, solve flow
feature/ide/         Python IDE
feature/leaderboard/ Leaderboard and ranking UI
feature/profile/     Profile, badges, edit profile, settings
```

## Firebase Setup

The app uses Firebase Authentication and Cloud Firestore. Firebase Storage is not required.

1. Create or select a Firebase project.
2. Enable Email/Password sign-in in Firebase Authentication.
3. Enable Cloud Firestore.
4. Add the Android app package `com.eztech.app`.
5. Place `google-services.json` in `app/google-services.json`.
6. Deploy Firestore rules when rules change:

```powershell
firebase deploy --only firestore
```

During a demo, this command is not required if the Firestore rules have already been deployed. The APK only needs internet access and a valid Firebase configuration.

## Build

From the project root:

```powershell
.\gradlew.bat :app:assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Unsigned release APK:

```powershell
.\gradlew.bat :app:assembleRelease
```

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Tests

```powershell
.\gradlew.bat :core:domain:test :core:data:testDebugUnitTest :feature:problems:testDebugUnitTest :feature:profile:testDebugUnitTest
```

## Data Notes

The app currently supports Firestore-driven Python learning content and ordered coding problems. Adding more problems, for example 600 total, is supported as long as the documents follow the same schema and include an order/index field for stable sorting.

For larger datasets, Firestore pagination can be added later to reduce initial reads. For a private demo app, hundreds of problem documents are fine.

## Demo Checklist

- Install the debug APK.
- Register or log in.
- Verify Home, Learn, Video, Tutorial, Problems, Solve, IDE, Rank, Profile, and Settings.
- Confirm Firestore shows user documents and progress updates.
- Confirm problems appear in the intended order.
