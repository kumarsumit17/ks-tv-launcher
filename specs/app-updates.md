---
type: Specification
title: In-app updates
description: "Check, download, verify and install new releases, keeping data (Stage 6)."
tags: [app, updates, feature]
stage: 6
---

# In-app updates

KS finds, downloads and installs its own new releases on the TV, keeping every saved detail (names, TV name, pinned apps, subscriptions, settings, wallpaper).

## Why data survives
An update is an in-place install of the same package signed with the same key, so Android keeps the app's data. Three things protect that:
- **Same key forever.** Releases are signed only by the release workflow. Losing the keystore ends in-place updates.
- **Stored data stays readable.** Never rename, remove or change the meaning of a DataStore key or a serialized field. New fields get defaults (`ignoreUnknownKeys` is already on). A change that cannot be compatible needs a migration plus a unit test that loads data written by the previous release. Keep one sample of each stored value under `app/src/test/resources/stored/` as the compatibility fixture.
- **Never downgrade**: only install a higher `versionCode`.

## Where updates come from
- `GET https://api.github.com/repos/kumarsumit17/ks-tv-launcher/releases/latest` (public, no token; only the shared request headers from ks-utility's `GitHubRepo.requestHeaders`). Use `tag_name`, `body` and the asset named `app-release.apk` (`browser_download_url`, `size`, `digest`). The asset is used only when the suite's [app-update rules](https://github.com/kumarsumit17/ks-utility/blob/main/specs/app-update.md) accept it (`AppUpdate.parseRelease`, ks-utility `v0.3.0`): its URL starts with `https://github.com/kumarsumit17/ks-tv-launcher/releases/download/`, it has a size, and GitHub lists a `sha256:` digest for it. Behind an `UpdateSource` interface.
- The tag `vMAJOR.MINOR.PATCH` maps to `versionCode = MAJOR*10000 + MINOR*100 + PATCH`, the same rule as `.github/workflows/release.yml`. An update exists when that is greater than `BuildConfig.VERSION_CODE`. Pure function with unit tests, including malformed tags.

## When KS checks
- Automatically once a day with WorkManager (unmetered network), plus "Check for updates" in [settings](settings.md) › About. Honour HTTP 403/429 by waiting until the next day. Setting "Check for updates automatically", on by default.
- While that setting is on, the daily check is re-registered about 5 s after Home is first shown (unique work, KEEP), which also starts it on a fresh install and brings it back after a force-stop ([performance](performance.md)). Release builds only; debug and `perf` builds never schedule it.
- **Release builds only.** A debug build is signed differently and can never be updated by a release, so in debug the About row says so and nothing is checked. KS TV Dev still shows the same update rows, focusable and laid out as in release, as an inert preview for focus testing ([dev-workflow](dev-workflow.md)).

## Telling the user
- A notification "KS TV update available: vX.Y.Z" (the Stage 5 notification channel pattern and permission, [reminders](reminders.md)), on its own "App updates" channel and its own WorkManager work.
- Reminders and updates are independent: turning renewal reminders off cancels only reminder work and reminder notifications, never the update check or an update notification. Likewise, "Check for updates automatically" off never touches reminders.
- Because TV notifications are easy to miss, also a glass "Update available" pill in the Home top bar while an update is pending; OK opens Settings › About.
- About shows the installed version, the available version and the release notes text.

## Download and verify (before any install prompt)
- Download to `cacheDir/updates/` over HTTPS with progress; resume is not required; delete the file on failure and after a successful update.
- Check, in order: size matches the asset; its SHA-256 (ks-utility `Sha256`) equals GitHub's digest; `PackageManager.getPackageArchiveInfo` reports package `com.ks.tvlauncher`; its signing certificate SHA-256 equals the installed app's own; its `versionCode` is higher. Any failure: delete the file, show "Update could not be verified", install nothing.

## Install
- `PackageInstaller` session with a status receiver; permission `REQUEST_INSTALL_PACKAGES`.
- Android asks the user to allow KS to install apps the first time (`Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES` for this package) and to confirm each install. KS never tries to skip those prompts. Explain them in one line before starting.
- After success Android restarts the app into the new version; on failure or cancel, say what happened and keep the old version working.
- Flow, all D-pad reachable: Update available → Download (progress, Cancel) → Verify → Install → system confirm.

## Tests
- Unit: tag-to-code mapping, comparison, release JSON parsing, verification decisions (wrong checksum, wrong package, wrong signer, lower or equal version), stored-data compatibility fixtures.
- TV ([verification](verification.md)): needs two real signed releases, so this feature is verified by installing release N on the TV, publishing N+1, and updating from inside KS, then checking names, TV name, pins, subscriptions and wallpaper are unchanged.
