---
type: Plan
title: Per-platform optimization plan
description: Approved plan for making the Google TV build of KS TV Launcher the best launcher app on its platform: D-pad and 10-foot UI, startup, size, Google TV rules, accessibility and languages.
tags: [plan, platforms, google-tv, performance, accessibility, languages]
---

# Per-platform optimization plan

**Status: approved by the owner on 2026-11-25, as written.** Decisions 2 to 5 are asked when their step starts. Follows the suite rule that each platform build is its own app tuned to its OS ([KS-Suite platform folders](https://github.com/kumasumit/KS-Suite/blob/main/specs/platform-folders.md)).

KS TV Launcher has one platform, Google TV / Android TV. So there is no `shared/` folder, and releases are tagged `google-tv-vX.Y.Z` from `google-tv-v0.9.0` on, as the suite's [platform tags](https://github.com/kumasumit/KS-Suite/blob/main/specs/versioning-signing.md) require of one-platform apps. This plan covers only the TV optimizations. The rules in [ground-rules](ground-rules.md), [app-mode](app-mode.md) and [future-development](future-development.md) still apply. Each step below goes through the cycle in future-development.

## 1. Audit: what the build does today (v0.8.0, 2026-11-25)

| Area | Today | Gap |
|---|---|---|
| Code location | `app/` with Gradle in the root; `tv/google-tv/` holds only `.gitkeep` | The suite layout wants the code in `tv/google-tv/` (already a planned stage in `CLAUDE.md`) |
| D-pad and 10-foot | D-pad first, explicit focus paths, no long-press-only actions ([focus-navigation](focus-navigation.md)); 960×540 dp canvas scaled to any density (`KsCanvas`), text capped at 1.3×, overscan-safe 48/27 dp, tuned for 55 and 65 inch ([design-system](design-system.md)) | Never checked at 32 and 100 inch as one batch; no key-repeat acceleration on long rows |
| Startup | No work in `Application.onCreate`, WorkManager on demand, daily work 5 s after Home; release cold start 593–616 ms on the owner's TV (`docs/MANUAL_TEST.md`) | **No baseline profile**, so the first starts after an install or update run un-compiled code; no `reportFullyDrawn`, so "Home ready" is not measured; downloadable Google Fonts (`ui-text-google-fonts`) depend on the system font provider at first draw |
| Smoothness | Scenario 1: 8 % janky frames, 32 ms median after the Stage 6c pass ([smoothness](smoothness.md)); blur only where [design-system](design-system.md) allows | 32 ms median is about 30 fps; no measurement on the 4K emulators |
| Packaging and size | R8 and resource shrinking on `release` and `perf`; APK 1.95 MB, AAB 4.6 MB; no native code | Library translations for every locale are kept (no locale filter); size is not reported per release |
| Google TV rules | `LEANBACK_LAUNCHER` only, never `HOME`; leanback required, touchscreen, Wi-Fi and Bluetooth not required; banner; landscape; no `QUERY_ALL_PACKAGES`; no backup | Banner and icon not checked against the current Google TV quality list (sizes, transparency); `resizeableActivity=false` and the orientation lock not re-checked on Android 16/17 large-screen rules for TV |
| Accessibility | 27 `contentDescription`s and semantics in 7 of 105 source files; Reduce motion and Transition speed in [settings](settings.md) | **TalkBack on TV never tested**; icon-only controls may be unlabelled; the system's "Remove animations" (animator scale 0) is an open item; high-contrast text not checked |
| Languages | English only, in `res/values/strings.xml` (about 300 strings) | **No Hindi, no language choice**: the suite requires English and Hindi through ks-utility (`lang.mjs android`), `locales_config.xml`, and the language-file check in the tests |
| Dependencies | ks-utility submodule at v0.9.0; release workflow at ks-utility's `kotlin-app-release.yml@v0.4.1` | Both move together to the newest ks-utility tag in their own commit |

## 2. The plan

### 2.1 Layout: code into `tv/google-tv/`
Move `app/` to `tv/google-tv/` as Gradle project `:tv:google-tv`, keeping the package `com.ks.tvlauncher`, the application ids (`com.ks.tvlauncher`, `.dev`), the signing and the release file names `app-release.apk` and `app-release.aab` (installed copies look for them, [app-updates](app-updates.md)). The settings, the release workflow paths and `scripts/kb.py` change with it. No behaviour change, so a rehearsal release run and one install over a `perf` build prove it.

### 2.2 D-pad and 10-foot UI
- Check every screen on every TV-size emulator (32, 43, 50, 55, 65, 75, 85, 100 inch, `../scripts/emulators.sh sizes`), with screenshots kept per size. Fix anything clipped or too small to read.
- Holding Left or Right on a long row (All apps, Subscriptions) moves faster after about a second, so the end of a 100-app row is reachable without many presses. Focus stays visible all the way.
- Every focusable element shows a visible focus state, and nothing needs a pointer, as in the Google TV app quality guidelines.

### 2.3 Performance and startup (a launcher must open fast)
- **Baseline profile** for start-up and the Home screen (`androidx.baselineprofile` Gradle plugin and a `:baselineprofile` test module, both build-time only; `profileinstaller` already comes with Compose). Generated on the TV emulator, checked in, shipped in the APK.
- `reportFullyDrawn()` once Home shows the app grid, so `am start -W` and Macrobenchmark measure "Home ready", not just the first frame.
- **Targets on the owner's 55 inch TV, release build:** cold start to first frame at most 450 ms (today about 600 ms), Home ready at most 700 ms, the first start after an update no more than 20 % slower than later ones.
- Fonts: bundle the two font families used, if it costs less than about 300 KB, so the first frame never waits for or falls back from the font provider. Decision 2.
- Re-run the [smoothness](smoothness.md) scenarios on the 4K (85 and 100 inch) emulators and the owner's TV; target the median frame at most 20 ms on the owner's TV. Fixes only where the profile shows a cost.

### 2.4 Packaging and size
- Keep only the app's own languages from libraries (`androidResources.localeFilters` with `en` and `hi`).
- Report the APK and AAB size in each release's notes; a release more than 10 % larger than the last one says why.
- No ABI splits are needed (no native code).

### 2.5 Google TV launcher and leanback rules
- Kept, as today: `MAIN` + `LEANBACK_LAUNCHER` only, never `HOME`/`DEFAULT`; leanback required; touchscreen, Wi-Fi and Bluetooth not required; no `QUERY_ALL_PACKAGES`; no backup.
- Check the banner (320×180 dp, text in the image, no transparency) and the icon against the current Google TV quality list, and fix if needed.
- Check on API 36 and 37 that the landscape lock and `resizeableActivity=false` are still honoured on TV, and drop them if the platform ignores them (less to maintain).
- Out of scope unless the owner asks: reading other apps' channels or Watch Next rows (needs TV-provider permissions KS does not hold, and a launcher run as an app gets little from it).

### 2.6 Accessibility (TalkBack on TV)
- A TalkBack pass on the TV emulator: every focused control reads its name and state, icon-only controls get labels, the app grid reads each app's name, toggles and sliders read their value.
- Focus order matches the visual order on every screen.
- Honour the system's "Remove animations" (animator duration scale 0) the same way as Reduce motion; this closes that open item in [future-development](future-development.md).
- High-contrast text (the system setting) keeps every text readable over the wallpaper; the glass panels get a stronger backing when it is on.

### 2.7 English and Hindi (suite rule)
- Interface text moves from `strings.xml` into `lang/en.json` and `lang/hi.json` in the suite format, with the shared catalogue from ks-utility; `lang.mjs android` generates the Android string resources at build time, and `lang.mjs check` runs in the unit tests.
- Language choice through Android's per-app language setting (`locales_config.xml`; on API 33+ the system setting, and a "Language" row in [settings](settings.md) for every API level). Follows the system until the user picks one, and remembers the pick. The implementation choice is decision 3.
- Hindi is written and reviewed by the owner against the ks-utility Hindi style guide. Every screen is checked on the 43 and 100 inch emulators in Hindi (longer text, Devanagari line height).
- Numbers, dates, times and money keep the platform formatters with Latin digits.

## 3. Decisions for the owner

1. **Approve this plan**, with or without changes, and the order in section 4.
2. **Fonts:** bundle the two font families (faster and steadier first frame, about +300 KB at most) or keep downloadable Google Fonts.
3. **Language switch:** (a) the platform's `LocaleManager` on API 33+ and a small KS setting for API 26–32, with no new dependency; or (b) `androidx.appcompat` (`AppCompatDelegate.setApplicationLocales`), the way ks-utility's spec describes, which is one new dependency and about 0.5–1 MB before R8.
4. **New build-time dependencies:** `androidx.baselineprofile` (Gradle plugin) and `androidx.benchmark` Macrobenchmark in a test module. Neither ships in the APK.
5. **Hold-to-scroll faster** on long rows (2.2): yes or no.

## 4. Build order and testing

Each step is its own change on `dev`: built, tested, then committed, pushed and released without waiting for anyone ([verification](verification.md)). Every step is tested on every TV-size emulator (32 to 100 inch) with KS TV Dev only, never the real app ([dev-workflow](dev-workflow.md)). Nothing is pushed or tagged without the owner's OK in words.

| Step | What | Tested on |
|---|---|---|
| 1 | Layout move to `tv/google-tv/` (2.1); ks-utility and release workflow pins to the newest tag | Build, unit and connected tests; a release rehearsal run; `perf` install on the 55 inch emulator |
| 2 | Startup: baseline profile, `reportFullyDrawn`, fonts (decision 2), locale filter, size in release notes (2.3, 2.4) | Macrobenchmark on the TV emulator; `am start -W` on every TV size; owner's TV with the next release |
| 3 | English and Hindi (2.7) | Language-file check in the tests; every screen in both languages on every TV size |
| 4 | Accessibility (2.6) | TalkBack, "Remove animations" and high-contrast text on the 55 inch emulator, then every size |
| 5 | D-pad polish and Google TV rules (2.2, 2.5) | Every TV size with screenshots; the banner on the emulator's launcher |
| 6 | Smoothness re-run (2.3) | 85 and 100 inch emulators, owner's TV |

**Built so far (2026-11-25):**
- Step 1: the code is in `tv/google-tv/` (`:tv:google-tv`); the release workflow builds it with ks-utility's `kotlin-app-release.yml@v0.9.0` and keeps the file names `app-release.apk` and `.aab`. Build, 172 unit tests, lint and the release build pass (APK 1.94 MB, unchanged). On every TV-size emulator (32, 43, 50, 55, 65, 75, 85, 100 inch) the connected tests pass, KS TV Dev installs and opens with no crash, and Home lays out the same. The rehearsal run passed, and step 1 was released as `v0.8.1` (signer `2aa25f08…`, `app-release.apk` 1.95 MB, `app-release.aab` 4.6 MB). Seen in passing, not caused by the move: from All apps, Down then Right reached Control Center rather than Active Subscriptions on the emulators; checked in step 5.

- Step 2 (without the fonts, which wait for decision 2): `reportFullyDrawn` once Home has its apps; a baseline profile (21,628 rules) generated by the test-only `:tv:google-tv-baselineprofile` module and shipped through `profileinstaller`; only English and Hindi kept from libraries' resources (release APK 1.94 → 1.89 MB). Macrobenchmark on the 43 inch emulator, release build, 10 cold starts each: first frame 220 → 196 ms, Home ready 388 → 343 ms (median, without → with the profile). With the suite's platform tags back, step 2 also moves ks-utility and the release workflow to `v0.10.0`, makes the update check read the release list, and ships as `google-tv-v0.9.0`. There is no bridge release: after the GitHub account was renamed to `kumasumit`, copies up to v0.8.1 cannot install any release in-app, so the owner installs `google-tv-v0.9.0` once by hand ([app-updates](app-updates.md)).

Each step is released on its own as soon as its tests pass (a new MINOR version for a step that changes the app, PATCH for a fix); the owner installs it from inside KS on the TV when convenient, and the data check in [dev-workflow](dev-workflow.md) runs then. Paid signing and hand checks never block a release.
