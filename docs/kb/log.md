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
