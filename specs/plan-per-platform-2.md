---
type: Plan
title: Per-platform optimization plan, round 2
description: The second round of Google TV optimizations for KS TV Launcher after round 1: an instant Home, smoother Settings and All apps, low-memory TVs, pictures from USB drives, and the hand checks on the owner's TV.
tags: [plan, platforms, google-tv, performance, startup, memory]
---

# Per-platform optimization plan, round 2

**Status: started on 2026-11-27 at the owner's request ("start the next per-platform plan").** Steps without an owner decision are built straight away under [build, test, release](verification.md); decisions 2 and 3 are asked when their step starts. Round 1: [plan-per-platform](plan-per-platform.md) (complete).

## 1. Audit after round 1 (google-tv-v0.11.1, 2026-11-27)

| Area | Today | Gap |
|---|---|---|
| Home ready | Home shows "Loading your apps…" until `LauncherApps` has listed every app (`AppsRepository.apps` starts as null on each cold start) | On a slow TV the app scan decides when Home is usable; the last list is not kept between launches |
| Smoothness at 1080p | Pinned row: 17 ms median, 22 % janky (within target). Control Center 21 ms / 29 %. **All apps 29 ms / 41 %. Settings 36 ms / 43 %** (`docs/MANUAL_TEST.md`, step 6) | All apps and Settings are the heaviest screens |
| Low-memory TVs | Same blur, image cache (15 % of the heap) and wallpaper colour work on every TV; `isLowRamDevice` is not read | The owner's TV has about 2.3 GB RAM and a 32-bit CPU |
| Own picture as wallpaper | Only through the system file picker, which the owner's TV hides ([custom-wallpaper](custom-wallpaper.md), [future-development](future-development.md) open item) | No route on TVs without a picker |
| Measured only on emulators | Cold start, 4K smoothness, TalkBack speech, Hindi wording, update from inside KS | Need the owner's TV; hand checks never block a release |

## 2. The plan

### 2.1 Instant Home
- Keep the last app list (package, label, banner-or-icon kind, category) in a small file in KS's own storage, written after each scan.
- At a cold start Home shows that list at once, with artwork loaded as tiles appear, and the fresh scan replaces it when it arrives. `reportFullyDrawn` then comes with the cached list.
- A cached app that has since been uninstalled disappears when the scan arrives; opening it before then uses the existing "no longer installed" path.
- The cache holds nothing personal beyond what KS already stores (names of installed apps), stays on the device and is never backed up ([app-config](app-config.md)).
- Target: Home ready within 50 ms of the first frame on the emulator, measured with `StartupBenchmark`; the baseline profile is regenerated.

### 2.2 Smoother All apps and Settings
- Profile both screens on the `perf` build (Compose compiler reports, `scripts/perf.sh` scenarios 4 and 5) and fix what the measurements show: unstable parameters, items without keys or content types, work in composition.
- Target at 1080p: both at most 30 % janky with a median of at most 25 ms, and no other scenario worse.

### 2.3 Low-memory TVs (decision 2)
- On a TV that reports `isLowRamDevice`, or has under 3 GB of RAM: glass intensity defaults to Low (no blur) until the user picks one, the image memory cache is 8 % of the heap instead of 15 %, and the dynamic wallpaper's colours come from a smaller bitmap.
- A choice the user already made is never changed.

### 2.4 Pictures from a USB drive (decision 3)
- On a TV with no file picker, Settings › Wallpaper offers "Pictures on this TV and USB drives": a D-pad grid of images found through `MediaStore`, copied into KS like a picked file.
- Needs `READ_MEDIA_IMAGES` (Android 13+) or `READ_EXTERNAL_STORAGE` (12 and lower), asked only when the user opens that grid. Closes the open item in [future-development](future-development.md).

### 2.5 Hand checks on the owner's TV
One list, in `docs/MANUAL_TEST.md`, done when the TV is reachable; none blocks a release: install `google-tv-v0.9.0` or later by hand once (account rename), update from inside KS after that, cold start and Home ready with `am start -W`, 4K smoothness with a `perf` build, TalkBack speech on Home and Settings, the Hindi wording, the system-setting checks from [future-development](future-development.md).

## 3. Decisions for the owner
1. This plan as a whole (started at the owner's request; steps 1 and 2 need no decision).
2. **Low-memory defaults** (2.3): yes, or keep every TV the same.
3. **Pictures from USB drives** (2.4): add the photo permission and the grid, or leave own pictures to TVs with a file picker.

## 4. Build order and testing
Each step is built, tested on every TV-size emulator (`scripts/tv_sizes.sh`, English and Hindi) and released on its own, after moving ks-utility to its newest tag.

| Step | What | Tested on |
|---|---|---|
| 1 | Instant Home (2.1), baseline profile regenerated | Unit tests for the cache; `StartupBenchmark`; every TV size |
| 2 | All apps and Settings smoothness (2.2) | `scripts/perf.sh` on the 1080p emulator; every TV size |
| 3 | Low-memory defaults (2.3), if decision 2 is yes | An emulator with `ro.config.low_ram`-like memory; every TV size |
| 4 | Pictures from USB drives (2.4), if decision 3 is yes | An emulator with pictures pushed to its storage; every TV size |
| 5 | Hand checks (2.5) | The owner's TV |
