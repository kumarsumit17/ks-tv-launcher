---
type: Reference
title: Tech stack
description: "Fixed libraries and SDK levels."
tags: [tech, stack, decision]
stage: 1
---

# Tech stack (decided, do not substitute)

| Need | Use |
|---|---|
| UI | Jetpack Compose + `androidx.tv:tv-material` (TV Material 3). No phone `androidx.compose.material3` components. |
| Lists | Compose Foundation `LazyRow`, `LazyColumn`, `LazyVerticalGrid` (not the deprecated tv-foundation lists) |
| State | ViewModel + `StateFlow` with immutable UI state data classes |
| Storage | Preferences DataStore + `kotlinx-serialization-json` |
| Images | Coil 3 (`io.coil-kt.coil3:coil-compose`) with a custom fetcher/keyer for app banners and icons |
| Frosted blur | Haze (`dev.chrisbanes.haze`) on API 31+; plain translucent glass below |
| Accent colours | `androidx.palette` |
| DI | Manual `AppContainer` created in `KsApplication`. **No Hilt.** |
| Network | `HttpURLConnection` on `Dispatchers.IO` + kotlinx-serialization (no Retrofit/Ktor) |
| Background work | WorkManager, Stage 5 only |
| Fonts | Inter, bundled as a cut-down variable font in `res/font/` ([design-system](design-system.md)); no font provider |
| Icons | Hand-written `ImageVector`s (stroke 1.8dp, round caps). No `material-icons-extended`. |

SDK: `minSdk 26`; `compileSdk`/`targetSdk` = latest stable platform installed. JVM target 17.
