# Code knowledge log

One entry per stage: what changed and why.

## Stage 1
- Foundation, design system, Home shell and greeting widget: 11 modules. Graph and module files seeded from the old code map.

## Stage 2
- App mode: manifest is `MAIN` + `LEANBACK_LAUNCHER` only, the activity label is "KS TV", Back moves the task to the background, and reopening closes overlays and refocuses the last tile.
- New modules `data.apps` (discovery via `LauncherApps.Callback`, pinned list in DataStore, Coil artwork fetcher and keyer, wallpaper source) and `ui.apps` (All apps drawer, app menu, artwork image, app intents).
- Home now shows real pinned apps, a hero with Open and Unpin, the All apps card and the dynamic wallpaper; `EmptyAppTile` is gone.
- `MotionPolicy` returns `FiniteAnimationSpec` so `Crossfade` can use it.
- Added Coil 3 and Palette (first used here) and the `REQUEST_DELETE_PACKAGES` permission, which the Uninstall menu item needs.
- After a first run on the Lumio TV (Android 14): Up/Down rules moved from containers to the items, `focusRestorer` replaced by `FocusMemory`, the wallpaper made smaller, more blurred and much darker, the focus glow smoothed, and full-width buttons now fill their focus ring.
- Same TV session: focus now returns to the acted-on tile after the app menu closes (done from `HomeScreen` once the menu has gone, not from the dialog while it is closing). Verified on the TV: row and drawer focus, menu, pin/unpin/move, search and keyboard, Back chain, background and hot reopen, app launch.

## Stage 3
- New modules `data.subscriptions` (JSON list in the shared DataStore, corrupt data kept under a backup key) and `ui.subscriptions` (screen, add/edit dialog, shared `subscriptionsState`).
- `domain` gains `Subscription`, `SubscriptionCalculator`, `SubscriptionCsv` (ks-expense-manager contract) and `Money` (INR grouped by hand because the JVM's `NumberFormat` has no lakh grouping).
- Home's bottom strip now has the Subscriptions card next to All apps; `LauncherSettings.currency` added (picker in Stage 4).
- `GlassSearchField` takes `icon`, `imeAction` and `maxLength` so dialogs can use it as a plain text field. The serialization compiler plugin is now applied for `@Serializable` models.

## Stage 4
- New modules `data.weather` (Open-Meteo provider, cached `WeatherState` flow), `ui.controlcenter` (side panel, card, status VM, `SystemIntents`) and `ui.settings` (Settings screen, `SettingsViewModel` with city search, `ProfileDialog`).
- `data.system` gains `DeviceStatsRepository` (RAM/storage every 10 s while collected, optional Apps/Other via Usage access) and `SystemStatusRepository` (network callback, Bluetooth state receiver).
- `domain` gains `ByteFormatter`, `WeatherCodes`, `WeatherCity`; `ProfileNames` gains add/rename/remove. Settings gain Memory card, weather city, °C/°F; profile gains `save` and `setupDone`.
- Home: focusable weather pill, gear → KS Settings, bottom strip with Memory & storage and Control Center cards, MENU / hold-OK open Control Center, first-run profile dialog, focus restored to the last Home control after overlays.
- Glass components (`GlassButton`, `GlassIconButton`, `GlassPill`, `GlassChip`) take an optional `onLongClick`. New icons for weather groups, Wi-Fi, Bluetooth, display, moon, memory.
- Manifest: `BLUETOOTH` (maxSdk 30), `PACKAGE_USAGE_STATS` (user-granted only), Bluetooth feature not required.
- Spec change during Stage 4: the weather city is automatic by default. Added `CityLocator`/`IpWhoIsLocator` (ipwho.is over HTTPS, checked from the owner's network before coding) and `CityResolver` (AUTO/MANUAL, 24 h detection cache, keep-last-on-failure); `LauncherSettings.weatherCityMode`; Settings gains Detect again, Change city and Use my current city.
- TV test fixes: focus snapshot when overlays open (`restoreKey`), focus handed to a chip or the field when the focused profile control disappears, Down from the rename field goes to Rename, profile dialog spacing 8dp so it fits the safe area, Settings pinned rows keyed with never-disabled move buttons.

## Stage 5
- New module `data.reminders`: `ReminderScheduler` (unique daily WorkManager job, one-off check after changes), `RenewalReminderWorker`, `ReminderNotifier`, `ReminderStore`. `domain.RenewalReminders` holds the pure plan (due window, notify once, cancel on pause/delete/date change) with `RenewalRemindersTest`.
- WorkManager 2.12.0 added; it starts on demand (`KsApplication` is the `Configuration.Provider`, default initializer removed in the manifest). `POST_NOTIFICATIONS` added, asked only when the user turns reminders on (API 33+).
- Settings: `remindersEnabled` (default off) and `reminderLeadDays` (1-14, default 3) with a Renewal reminders section. `SubscriptionRepository` takes an `onChanged` hook wired to the scheduler.
- Fixes from the Stage 4 TV test: Home shows a loading state until the first app scan (hero + All apps card); `GlassSearchField` leaves editing on one Back (`onPreInterceptKeyBeforeSoftKeyboard`).
- Found in the Stage 5 TV test: the Subscriptions card flashed "0 · Track what you pay for" on cold start (now "Loading…"); focus escaped to Settings' Home button when the "Open notification settings" row disappeared on return (now handed to the toggle).
- Performance pass on the TV (API 34, 32-bit ARM, 2.4 GB): debug cold start about 6.3 s before and after this stage, about 2.6 s once AOT-compiled; 75 MB PSS on Home; 0 % CPU when idle. Release builds include profileinstaller (Compose baseline profiles).
- `SETUP.md` written; README links it and lists WorkManager.

## Stage 6: release checklist
- Debug builds are the separate app KS TV Dev: `applicationIdSuffix = ".dev"` in `app/build.gradle.kts`, label "KS TV Dev" and an amber banner and icon from `src/debug/res`. The namespace and code are unchanged; nothing in the code hardcodes the package (the app list hides `context.packageName`, WorkManager's startup provider uses `${applicationId}`).
- Noted for 6c: `ReminderNotifier.cancelAll()` uses `NotificationManager.cancelAll()`, which would also clear an update notification; [app-updates](../spec/app-updates.md) now requires reminders off to cancel only reminder notifications.

## Stage 6a: Inputs
- `domain.TvInputs` (pure filter, label and order rules, `TvInputsTest`) and `data.system.TvInputRepository` (`TvInputManager` list + `TvInputCallback` while collected, passthrough switch intent). `ControlCenterViewModel` exposes `inputs`.
- Control Center: Inputs tile and `InputsDialog` (glass list with Connected / Standby / Not connected; OK switches, failure shows a snackbar), with its own plug icon (`KsIcons.Plug`) so it is not confused with Display. Status tiles are now a list laid out two per row so hidden tiles leave no gap.
- Fix from the release check: the Screensaver tile is hidden when neither SystemUI's Somnambulator nor the dream settings page resolves, and `startScreensaver` no longer falls back to the main settings page. Manifest `<queries>` gains `com.android.systemui` and the `DREAM_SETTINGS` action so the check can see them.

## Stage 6b: Wallpaper
- `domain.Wallpapers` (mode, dim, image encoding, migration, `fitWithin`) with `WallpapersTest`; `LauncherSettings` swaps `dynamicWallpaper` for `wallpaperMode`, `wallpaperImage`, `wallpaperDim` and `wallpaperPictureFailed` (migration and fallback covered in `SettingsRepositoryTest`).
- New module `data.wallpaper`: `WallpaperPictureStore` (import once to `filesDir/wallpaper.jpg`, Coil load at screen size, remove, document-picker check that ignores the Android TV stub package).
- `ui.components.BuiltInWallpaper`: six wallpapers drawn with gradients. `HomeBackground` draws `HomeWallpaper` (Plain / Dynamic / BuiltIn / Picture) with the dim overlay; `VisualSettings` loses `dynamicWallpaper`.
- Settings gains a Wallpaper section (`WallpaperSettings`); Control Center's Wallpaper row cycles the three modes. Manifest `<queries>` gains `OPEN_DOCUMENT image/*`.

## Fix after the 6b TV test: transition speed and Reduce motion
- `domain.AnimationSpeed` with `AnimationSpeedTest`; `LauncherSettings.animationSpeed` (`animation_speed_percent`, default 50); `MotionPolicy` scales every tween; Settings › Look and motion gains a Transition speed stepper.
- `LocalMotion` changed from `staticCompositionLocalOf` to `compositionLocalOf`, so toggling Reduce motion or the speed no longer recomposes the whole screen.

## Stage 6c part 1: Smoothness pass
- Findings (`perf` build on the owner's TV, `dumpsys gfxinfo framestats`): the UI thread was not the problem (recomposition p50 2.4 ms, layout 0.1 ms, record 1.3 ms). The time was GPU: about 13 ms issuing draw commands and 38 ms on the GPU per frame. Home's background stacks five to six full-screen layers (navy gradient, wallpaper, tint, darkening, two readability gradients) and all of them were redrawn on every frame, even while only a tile's focus animated. Blur was not the cause: glass Low and a build without the wallpaper blur measured the same. Compose compiler reports: strong skipping makes every composable skippable, so unstable classes were not a factor.
- Kept 1: `HomeBackground` draws its whole stack into one offscreen layer (`CompositingStrategy.Offscreen`), so a frame where only the foreground changes composites one cached texture. Scenario 1: 66 % → 42 % janky, median 69 → 44 ms; Control Center 86 % → 20 %.
- Kept 2: everything except the user's picture (gradients, glows, the 32×18 dynamic bitmap) is drawn into that layer at a quarter of the screen size and scaled up (`SmoothLayer`), so a wallpaper crossfade frame is about 16× cheaper and looks the same. The picture keeps a full-resolution cached layer; switching between the two kinds still crossfades. The wallpaper blur radius is divided by the same scale. Scenario 1: 42 % → 8 % janky, median 44 → 32 ms.
- Tried and dropped: folding the dynamic tint and darkening into a colour matrix (no measurable gain after kept 2); switching the wallpaper blur off (no gain).
- Build: `perf` build type (`initWith(release)`, `.dev` suffix, debug key, KS TV Dev resources from `src/debug/res`, `profileable` from `src/perf`) and `-PcomposeReports` for compiler reports. `scripts/perf.sh` holds the scenarios.

## Stage 6c part 2: In-app updates
- New module `data.updates` and `domain.AppUpdates` (tag → versionCode like `release.yml`, `isNewer`, ordered `verify`, `ReleaseInfo`). GitHub `releases/latest` behind `UpdateSource`; daily `UpdateCheckWorker` on an unmetered network; pending release in the shared DataStore; 403/429 waits until the next day.
- About gains the update flow (`UpdateSettings`); Home's top bar shows an "Update available" pill (not frosted, to keep the blur budget) that opens Settings › About, as does the notification (`MainActivity.EXTRA_OPEN_UPDATES`).
- `BuildConfig.IN_APP_UPDATES`: release true, debug and perf false. Manifest adds `REQUEST_INSTALL_PACKAGES`. The install status receiver is runtime-registered.
- Rider: `ReminderNotifier.cancelAll()` called `NotificationManager.cancelAll()`, which would also have withdrawn an update notification; now `cancelAllReminders()` cancels only the renewal channel.
- Rider: Settings' list cancels Left focus exits, so Left from "−" of Transition speed (or any first control) no longer jumps to the Home button.
- Stored-data fixture `app/src/test/resources/stored/ks_launcher-v0.6.1.json` and `StoredDataCompatibilityTest` read every v0.6.1 value through this build's repositories.

## Fix after v0.7.0: About update rows reachable, dev preview
- `UpdateRows` (shared layout), `UpdateSettings` (release) and `UpdatePreview` (debug/perf, inert). Explicit `focusProperties`: switch Down → Check, "Android settings" Up → Check. Preview message overridden in `src/debug/res` so the release APK has no dev wording.

