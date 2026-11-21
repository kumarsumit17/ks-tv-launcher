---
type: Module
title: data.updates
package: com.ks.tvlauncher.data.updates
spec: [../../../specs/app-updates.md, ../../../specs/reminders.md, ../../../specs/performance.md]
---

# data.updates

## Purpose
In-app updates (release builds only): `GitHubUpdateSource` (releases/latest), `UpdateChecker` (one check, shared by the daily `UpdateCheckWorker` and "Check for updates"), `UpdateStore` (pending release, 403/429 wait day, notified version), `UpdateNotifier` ("App updates" channel), `UpdateScheduler` and `UpdateManager` (download → verify → `PackageInstaller`, the About flow's state).

## Rules
- Off unless `BuildConfig.IN_APP_UPDATES` (true for release only; debug and perf are KS TV Dev, signed differently, and never check).
- Independent of reminders: own WorkManager name (`app_update_check_daily`), own channel (`app_updates`) and notification tag (`app_update`). Reminders cancel only renewal-channel notifications (`cancelAllReminders`); never call `cancelAll()`.
- WorkManager is touched only by `ensureDaily` (KEEP, while the switch is on), called from `AppContainer.ensureDailyWork` about 5 s after Home is first shown, or by the Settings switch. `update_scheduled_for` from 0.7.x is no longer read.
- HTTP 403/429 stores tomorrow as `update_wait_until_day`; no request (manual or daily) until then.
- Verify before any prompt, in order: size, package = `context.packageName`, signer SHA-256 set = installed app's (`apkContentsSigners`), versionCode higher. Any failure deletes `cacheDir/updates/update.apk` and installs nothing.
- The install status receiver is registered at runtime (`RECEIVER_NOT_EXPORTED`) for one install; the manifest declares no receiver. Every install sets `USER_ACTION_REQUIRED` (API 31+); KS never tries to skip Android's prompts.

<!-- kb:generated:start -->

## Files (generated)
- `data/updates/UpdateChecker.kt` — `CheckResult`, `UpdateChecker`
- `data/updates/UpdateManager.kt` — `UpdateFailure`, `UpdateManager`, `UpdateState`
- `data/updates/UpdateNotifier.kt` — `UpdateNotifier`
- `data/updates/UpdateScheduler.kt` — `UpdateCheckWorker`, `UpdateScheduler`
- `data/updates/UpdateSource.kt` — `GitHubUpdateSource`, `RateLimitedException`, `UpdateSource`
- `data/updates/UpdateStore.kt` — `UpdateStore`

Tests: `data/updates/GitHubReleaseParsingTest.kt`, `data/updates/StoredDataCompatibilityTest.kt`, `data/updates/UpdateCheckerTest.kt`

## Depends on (generated)
- [data.reminders](data-reminders.md) via `ReminderNotifier`
- [domain](domain.md) via `AppUpdates`, `ReleaseInfo`, `UpdateVerdict`
- [root](root.md) via `KsApplication`, `MainActivity`

## Used by (generated)
- [root](root.md) via `GitHubUpdateSource`, `UpdateChecker`, `UpdateManager`, `UpdateNotifier`, `UpdateScheduler`, `UpdateStore`
- [ui.home](ui-home.md) via `UpdateStore`
- [ui.settings](ui-settings.md) via `UpdateFailure`, `UpdateManager`, `UpdateScheduler`, `UpdateState`

## Spec
- [app-updates](../../../specs/app-updates.md), [reminders](../../../specs/reminders.md), [performance](../../../specs/performance.md)

<!-- kb:generated:end -->
