---
type: Rules
title: Performance and lightweight design
stage: all
---

# Performance and lightweight design

KS - TV Launcher must stay a lightweight, high-performance launcher. Responsiveness comes before decoration, but never at the cost of core Android TV behaviour.

## Requirements
- **Startup**: keep cold start short. No work in `Application.onCreate`; create dependencies lazily.
- **Background**: no persistent service, no manifest (startup) receivers, no wake locks. Weather refreshes periodically and is configurable ([weather](weather.md)); reminders use scheduled work ([reminders](reminders.md)).
  - **WorkManager's own manifest entries are accepted.** The library merges in its own permissions (`WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `ACCESS_NETWORK_STATE`), services and receivers so it can run scheduled work. That is the library, not KS: KS code never acquires a wake lock, never starts or declares a service, and declares no receiver of its own in the manifest.
- **No polling** for clock, app or system status. Use system broadcasts, observers and callbacks, registered only while the screen is visible.
- **Compose**: lazy rows and grids; avoid needless recomposition; animate in the draw or layer phase; keep UI state immutable.
- **Images**: load asynchronously, cache icons and artwork, and do not load large wallpapers until they are needed. Release what is no longer used.
- **Effects**: light animations; blur only where [design-system](design-system.md) allows.
- **Network**: no unnecessary requests; cache the last result.
- **Coroutines and Flow**: stop upstream work when nothing collects; avoid leaks and needless allocations.
- **D-pad**: focus feedback is immediate.

## User switches ([settings](settings.md))
- Dynamic wallpaper on/off.
- Reduce motion (no animations).
- Transition speed (shorter animations mean fewer frames on a slow TV; default 0.5×).
- Glass intensity; Low means no blur.

## Checks
- Test on real Google TV hardware.
- Watch memory, CPU and startup time during the Stage 5 performance pass.

## How Stage 1 meets this
- `KsApplication.onCreate` does nothing; `AppContainer` is lazy; one DataStore file.
- Clock: `ACTION_TIME_TICK` broadcast through `minuteTicker`, registered at runtime, removed 5 s after Home stops.
- TV name: a content observer, not a timer.
- `animateDpadFocus`: one animation, read only in layer and draw lambdas, so frames redraw without recomposing.
- With motion off, animation specs snap, so no animation frames run.
- Blur: only the weather pill and dialogs; nothing is registered with Haze when blur is off or below API 31.
- Only the libraries Stage 1 uses are in the build. Coil and Palette arrive in Stage 2, WorkManager in Stage 5.
