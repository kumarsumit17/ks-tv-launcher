---
type: Module
title: ui.settings
package: com.ks.tvlauncher.ui.settings
spec: [../../spec/settings.md, ../../spec/greeting-widget.md, ../../spec/weather.md]
---

# ui.settings

## Purpose
The KS Settings screen (every persisted setting, opened from the gear or at Weather from the pill / Control Center), `SettingsViewModel` (incl. city search) and the "Who's watching?" `ProfileDialog`.

## Rules
- Every setting is written straight to DataStore through the view model; there is no Save step on the screen.
- The profile dialog edits a local copy and writes once on Save via `ProfileRepository.save`; Cancel changes nothing. Both mark first-run setup done.
- Rename/Remove: hold OK on a name chip, or the "Rename or remove ‹name›" button (second path, no long-press-only action).
- Screen is opaque over Home with `modalFocus`; the profile dialog is drawn inside it and focus returns to the Profile row after it has gone.
- Weather section: city line with "· automatic"/"· manual", privacy line, Detect again / Change city (AUTO) or Use my current city (MANUAL), units, then the search field last, so Down from the field never lands on a mode button.
- A control that disappears or disables under focus must hand focus on first (`refocusTarget`, `focusChip`, `FOCUS_FIELD`): on the TV, focus otherwise escaped the modal dialog.
- Renewal reminders (Stage 5): turning the toggle on asks for `POST_NOTIFICATIONS` on API 33+ and only turns on if granted. The lead-time stepper shows only while on and never disables at 1 or 14. A blocked line with "Open notification settings" shows when notifications are off; it is re-checked on resume. Back from that page with notifications allowed, the row disappears and focus goes to the toggle (`openedSettings` → `refocusToggle`); a focus listener on the row does not work because the row reports losing focus as it is removed.

<!-- kb:generated:start -->

## Files (generated)
- `ui/settings/ProfileDialog.kt` — `ProfileDialog`
- `ui/settings/SettingsScreen.kt` — `SettingsScreen`, `SettingsSection`, `ValueRow`
- `ui/settings/SettingsViewModel.kt` — `CitySearch`, `DetectState`, `SettingsUiState`, `SettingsViewModel`

## Depends on (generated)
- [data.apps](data-apps.md) via `AppsRepository`, `PinnedAppsRepository`
- [data.profile](data-profile.md) via `Profile`, `ProfileRepository`
- [data.reminders](data-reminders.md) via `ReminderNotifier`, `ReminderScheduler`
- [data.settings](data-settings.md) via `GlassIntensity`, `LauncherSettings`, `SettingsRepository`
- [data.system](data-system.md) via `DeviceInfo`, `DeviceInfoRepository`
- [data.weather](data-weather.md) via `CityResolver`, `ResolvedCity`, `WeatherRepository`
- [domain](domain.md) via `AppInfo`, `ProfileNames`, `RenewalReminders`, `TemperatureUnit`, `WeatherCity`, `WeatherCityMode`
- [root](root.md) via `KsApplication`
- [ui.components](ui-components.md) via `GlassButton`, `GlassCard`, `GlassChip`, `GlassDialog`, `GlassPill`, `GlassSearchField` +5
- [ui.controlcenter](ui-controlcenter.md) via `SystemIntents`
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsType`

## Used by (generated)
- [ui.home](ui-home.md) via `ProfileDialog`, `SettingsScreen`, `SettingsSection`, `SettingsViewModel`

## Spec
- [settings](../../spec/settings.md), [greeting-widget](../../spec/greeting-widget.md), [weather](../../spec/weather.md)

<!-- kb:generated:end -->
