# Fathom

Fathom is an offline-first sacred wisdom and philosophical text reader. It brings together passages from diverse traditions—including Hinduism, Stoicism, Buddhism, and Taoism—into a quiet, distraction-free reading experience.

---

## Overview

The application is designed around three principles:

- **Calm Editorial Aesthetic:** Focused purely on text, commentary, and typography, removing typical social feed distractions.
- **Offline-First Persistence:** Wisdom corpora and user data reside in local SQLite databases, enabling instant loading and full offline capability.
- **Multiplatform Architecture:** Shared business logic, database queries, and feed generation in pure Kotlin Multiplatform, paired with 100% native platform UI.

---

## Project Structure

```
├── sharedLogic/     # Pure Kotlin Multiplatform core
│   ├── commonMain/  # Domain models, repositories, feed engine, and SQLDelight databases
│   ├── androidMain/ # Android platform implementations (cryptographic verifiers, drivers)
│   └── iosMain/     # iOS platform implementations (Apple CoreCrypto bindings)
├── androidApp/      # Native Android application (Jetpack Compose, custom theme & animations)
└── iosApp/          # Native iOS application entry point (SwiftUI)
```

---

## Technical Stack

- **Core:** Kotlin Multiplatform (Kotlin 2.1+)
- **Android UI:** Jetpack Compose with a bespoke design system (`androidApp/.../ui/design`): variable Fraunces / Newsreader / Geist / Geist Mono, AGSL atmosphere shader, `androidx.graphics:graphics-shapes` tradition emblems, Backdrop frosted blur, adaptive docked bar / rail
- **Persistence:** SQLDelight 2.0 (Dual databases: Starter Content and User Metadata)
- **Concurrency:** Kotlin Coroutines & StateFlow (Unidirectional Data Flow)
- **Security:** SHA-256 cryptographic verification for dynamic content packs

---

## Design System

- **Tone:** each tradition owns a hue (`ui/theme/Tone.kt`) that tints the atmosphere, accents and emblems of whatever the reader is viewing.
- **Atmosphere:** `ui/design/Atmosphere.kt` renders an aurora and film grain with AGSL on Android 13+, with a gradient fallback below.
- **Glass:** `GlassLayer` captures a screen's background in a local backdrop and draws glass chrome as its sibling. Glass must never sit inside the content it samples.
- **Adaptive shell:** `ui/shell/FathomApp.kt` switches between a docked bottom bar and a docked rail at 600dp width or below 480dp height.
- **Typography roles:** display and headings use Fraunces, scripture and commentary use Newsreader, interface text uses Geist, citations use Geist Mono.

---

## Building and Testing

### Prerequisites
- JDK 17 or higher
- Android SDK (API level 35)

### Commands
- Build Android debug APK:
  ```bash
  ./gradlew :androidApp:assembleDebug
  ```
- Run shared core unit tests:
  ```bash
  ./gradlew :sharedLogic:testAndroidHostTest
  ```