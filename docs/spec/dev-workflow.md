---
type: Process
title: Dev app, branches and release cycle
stage: 6
---

# Dev app, branches and release cycle

Applies from the [release-checklist](release-checklist.md) onward. Two apps can sit on the owner's TV:

| | KS TV (real) | KS TV Dev (test copy) |
|---|---|---|
| Package | `com.ks.tvlauncher` | `com.ks.tvlauncher.dev` |
| Build | release, signed by the release workflow | debug |
| Data | the owner's real data | its own, `TEST ` data only |
| Who gets it | the owner, friends, customers | the owner's TV only, during testing |

## Build setup
- Debug build type: `applicationIdSuffix = ".dev"`, activity label "KS TV Dev" (`app_name_short` overridden in `src/debug/res`), a visibly different banner tint. `namespace` stays `com.ks.tvlauncher`.
- Nothing may hardcode the package name; use `context.packageName` / `BuildConfig.APPLICATION_ID` (own-app exclusion in the app list, intents, tests).
- The release APK contains no dev code paths, label or suffix. Anyone given `app-release.apk` sees only "KS TV".
- A `perf` build type ([smoothness](smoothness.md)) is release code installed as KS TV Dev with the debug key, for measuring only; it is never released.
- In-app updates stay off in the dev app ([app-updates](app-updates.md)). Settings › About still shows the update rows there, inert and laid out as in release, so their D-pad paths can be tested; any press says "Updates are off in KS TV Dev". The release APK carries no dev wording (the message is overridden in `src/debug/res`).

## Branches
- Work happens on `dev`. `main` only receives reviewed work and is what gets tagged.
- Tag pushes come from `main` only. Pushing a tag publishes the GitHub release.

## Cycle for every stage or fix
1. Build on `dev`; install **KS TV Dev** on the TV (`./gradlew installDebug`) and test with `TEST ` data ([verification](verification.md)). Open it with `adb shell am start -n com.ks.tvlauncher.dev/com.ks.tvlauncher.MainActivity`.
2. Owner confirms, Claude Cowork reviews, commit and push to `dev`.
3. Merge `dev` into `main`, push, tag `vX.Y.Z` (owner's decision), wait for the Release workflow, confirm the signing fingerprint.
4. **Put the release on the TV**: through the in-app update once Stage 6c exists; otherwise `adb install -r app-release.apk` (same key, so data is kept). Never uninstall first.
5. **Verify the owner's data survived**, on the TV, and record it in `docs/MANUAL_TEST.md` (date, version): profile names and active name, TV name, pinned apps and order, subscriptions and totals, weather mode and city, wallpaper, settings. If anything is missing, stop: do not start new work until the cause is found.
6. **Remove the test copy**: `adb uninstall com.ks.tvlauncher.dev`. It is reinstalled at step 1 of the next cycle.

## Hard rules
- Never run `adb uninstall com.ks.tvlauncher` or `pm clear com.ks.tvlauncher` (the real app), except the one-time switch in the release checklist, and then only when the owner says so.
- Never install a debug build under the real package name.
- `adb uninstall` and `pm clear` are allowed on `com.ks.tvlauncher.dev` only.
