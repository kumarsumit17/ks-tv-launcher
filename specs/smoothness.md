---
type: Playbook
title: Smoothness pass
description: "Measured frame-time pass on a `perf` build, before the update work (Stage 6c part 1)."
tags: [smoothness, process]
stage: 6c
---

# Smoothness pass

The owner's TV (KTC Lumio Vision, Android 14, 32-bit ARM, about 2.3 GB RAM) drops frames in KS. Baseline on the real release build 0.5.0, six pinned-row moves: 110 frames, 65 % janky, median 69 ms. This pass finds where the time goes and reduces it. It is part of Stage 6c but is its own change: measured, tested, reviewed and committed separately, before the update work ([verification](verification.md)).

## Measure first
- Never judge smoothness on the debug build: it is several times slower than release and says nothing about what users see.
- Add a `perf` build type: `initWith(release)` (minified, not debuggable), `applicationIdSuffix = ".dev"`, signed with the debug key, label "KS TV Dev". It installs beside the real app like the debug build ([dev-workflow](dev-workflow.md)) and is never released.
- Fixed scenarios, each run three times after a cold start and a 5 s settle, with `adb shell dumpsys gfxinfo <package> reset` before and `dumpsys gfxinfo <package>` after:
  1. Six moves along the pinned row and six back (dynamic wallpaper on).
  2. The same with wallpaper mode Plain.
  3. Control Center open and close, three times.
  4. All apps drawer: open, twelve moves through the grid, close.
  5. Settings: open, scroll to the bottom and back, close.
- Record total frames, janky %, and the 50th, 90th and 95th percentile frame times in `docs/MANUAL_TEST.md` (date, version, build type). Reading the real app's numbers with `dumpsys` is allowed; it changes no data.
- Keep the scenario commands in `scripts/perf.sh` so the same run can be repeated after every later stage.

## Find the cause before changing anything
- Use the measurements, `dumpsys gfxinfo framestats`, a short Perfetto or `atrace` capture, and Compose compiler reports (unstable classes, non-skippable composables) to name what costs the frames. Write the findings in the report before the fixes.
- Likely places to check, not a to-do list: recomposition of Home when focus or the hero changes; the wallpaper crossfade and full-screen overlays (overdraw); the focus glow, shadow and border drawing; Haze blur surfaces; image decode sizes and hardware bitmaps; lazy row and grid item keys and content types; work on the main thread at focus changes; missing baseline profile coverage for KS's own code.

## Fix
- One cause at a time, re-measuring after each, and keep only changes that measurably help.
- Stay inside [performance](performance.md) and the design: no change that makes the app look different from the approved mockups without the owner's agreement. If the look must give way (for example a simpler focus glow), stop and ask with before and after screenshots.
- Allowed without asking: fewer recompositions, drawing in the layer or draw phase, caching drawn shapes, smaller decoded images, a baseline profile for KS code, and choosing lighter defaults **for new installs only** on devices where `ActivityManager.isLowRamDevice` is true or the TV is 32-bit. Never change a setting the user has already saved.

## Targets
- Scenario 1 on the `perf` build: at most 25 % janky frames and a median frame time of 33 ms or less; no scenario worse than its baseline.
- If the targets cannot be met on this TV, say so with the numbers, list what was tried and what remains, and let the owner decide. Do not chase them with visual cuts.

## Done when
- Before and after tables are in `docs/MANUAL_TEST.md`, the findings and each kept change are in `docs/kb/log.md`, unit tests and `kb.py check` pass, and the owner has looked at the result on the TV.

## 4K emulators
The 4K TV-size emulators on the Mac are too slow to measure KS (about 100–200 ms frames even with the UI set to 1920×1080, while the 1080p emulator meets the targets). Measure 4K smoothness on a real 4K TV with a `perf` build; it is a hand check and never blocks a release (`docs/MANUAL_TEST.md`, per-platform step 6).
