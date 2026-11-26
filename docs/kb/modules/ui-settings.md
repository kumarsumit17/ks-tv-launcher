---
type: Module
title: ui.settings
package: com.ks.tvlauncher.ui.settings
spec: [../../../specs/settings.md, ../../../specs/greeting-widget.md, ../../../specs/weather.md, ../../../specs/custom-wallpaper.md]
---

# ui.settings

## Purpose
The KS Settings screen (every persisted setting, opened from the gear or at Weather from the pill / Control Center), `SettingsViewModel` (incl. city search) and the "Who's watching?" `ProfileDialog`.

## Rules
- Left never leaves the settings list: `focusProperties { onExit }` cancels a Left exit, so Left on the first control of a row cannot reach the Home button (OK there closed Settings). Up still reaches it.
- About › updates: `UpdateRows` is the one layout (automatic-check switch, "Check for updates", one action button whose label follows the flow: Download and install → Cancel → Install, so focus never moves while it runs). Release uses it through `UpdateSettings`; debug and perf through `UpdatePreview`, inert (every press shows "Updates are off in KS TV Dev", nothing is checked or saved), so focus can be tested on the dev app.
- Down from the switch and Up from "Android settings" point at "Check for updates" explicitly (`focusProperties`): the narrow left button lost Compose's focus search to the full-width rows around it and was unreachable in 0.7.0.
- Every setting is written straight to DataStore through the view model; there is no Save step on the screen.
- The profile dialog edits a local copy and writes once on Save via `ProfileRepository.save`; Cancel changes nothing. Both mark first-run setup done.
- Rename/Remove: hold OK on a name chip, or the "Rename or remove ‹name›" button (second path, no long-press-only action).
- Screen is opaque over Home with `modalFocus`; the profile dialog is drawn inside it and focus returns to the Profile row after it has gone.
- Weather section: city line with "· automatic"/"· manual", privacy line, Detect again / Change city (AUTO) or Use my current city (MANUAL), units, then the search field last, so Down from the field never lands on a mode button.
- A control that disappears or disables under focus must hand focus on first (`refocusTarget`, `focusChip`, `FOCUS_FIELD`): on the TV, focus otherwise escaped the modal dialog.
- Wallpaper section (Stage 6b, `WallpaperSettings`): mode chips, six built-in swatches plus "Your picture", "Choose a picture" only when `canPickFiles`, "Remove image" (hands focus to "Choose a picture" or the Dynamic chip before it disappears), and Dim only in Custom. File state (`WallpaperFiles`) is read when the section opens, not when Home starts. Section indexes: Weather is item 5, About is item 9 (`SettingsSection.ABOUT`, opened by the Home update pill and the update notification).
- Renewal reminders (Stage 5): turning the toggle on asks for `POST_NOTIFICATIONS` on API 33+ and only turns on if granted. The lead-time stepper shows only while on and never disables at 1 or 14. A blocked line with "Open notification settings" shows when notifications are off; it is re-checked on resume. Back from that page with notifications allowed, the row disappears and focus goes to the toggle (`openedSettings` → `refocusToggle`); a focus listener on the row does not work because the row reports losing focus as it is removed.

<!-- kb:generated:start -->

## Files (generated)
- `ui/settings/ProfileDialog.kt` — `ProfileDialog`
- `ui/settings/SettingsScreen.kt` — `ChoiceRow`, `SettingsScreen`, `SettingsSection`, `ValueRow`
- `ui/settings/SettingsViewModel.kt` — `CitySearch`, `DetectState`, `SettingsUiState`, `SettingsViewModel`, `WallpaperFiles`
- `ui/settings/UpdateSettings.kt` — `UpdatePreview`, `UpdateSettings`
- `ui/settings/WallpaperSettings.kt` — `WallpaperSettings`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppsRepository`, `PinnedAppsRepository`
- [data.language](data-language.md) via `AppLanguage`
- [data.profile](data-profile.md) via `Profile`, `ProfileRepository`
- [data.reminders](data-reminders.md) via `ReminderNotifier`, `ReminderScheduler`
- [data.settings](data-settings.md) via `GlassIntensity`, `LauncherSettings`, `SettingsRepository`
- [data.system](data-system.md) via `DeviceInfo`, `DeviceInfoRepository`
- [data.updates](data-updates.md) via `UpdateFailure`, `UpdateManager`, `UpdateScheduler`, `UpdateState`
- [data.wallpaper](data-wallpaper.md) via `WallpaperPictureStore`
- [data.weather](data-weather.md) via `CityResolver`, `ResolvedCity`, `WeatherRepository`
- [domain](domain.md) via `AnimationSpeed`, `AppInfo`, `ProfileNames`, `ReleaseInfo`, `RenewalReminders`, `TemperatureUnit` +6
- [root](root.md) via `KsApplication`
- [ui.components](ui-components.md) via `BuiltInWallpaper`, `DpadFocusable`, `GlassButton`, `GlassCard`, `GlassChip`, `GlassDialog` +8
- [ui.controlcenter](ui-controlcenter.md) via `SystemIntents`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsShapes`, `KsType`, `forText`

## Used by (generated)
- [ui.home](ui-home.md) via `ProfileDialog`, `SettingsScreen`, `SettingsSection`, `SettingsViewModel`

## Spec
- [settings](../../../specs/settings.md), [greeting-widget](../../../specs/greeting-widget.md), [weather](../../../specs/weather.md), [custom-wallpaper](../../../specs/custom-wallpaper.md)

<!-- kb:generated:end -->
