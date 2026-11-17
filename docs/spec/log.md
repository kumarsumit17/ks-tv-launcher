# Spec log

## 2026-11-16
- Split the single `docs/SPEC.md` into this OKF v0.1 bundle, one concept per file, with a per-stage reading table in `index.md`.
- Added [token-budget](token-budget.md) and `code-map.md` (Graphify-style map to read before searching code).
- Added [greeting-widget](greeting-widget.md) and [memory-storage-widget](memory-storage-widget.md).
- Renamed the RAM panel button from "Running apps" to "Manage apps".
- Merged from the earlier repo notes: CSV export contract with `ks-expense-manager` ([subscriptions](subscriptions.md)), blur budget ([design-system](design-system.md)), no long-press-only actions ([apps](apps.md)), device-only data, key-mapper alternative ([google-tv-setup](google-tv-setup.md)).
- Dropped from the earlier notes (superseded by this spec): Hilt, Retrofit/OkHttp, OpenWeatherMap + API key, the `di/` package, and the 16sp minimum body size (the approved mockups' type scale applies).
- Renamed mockups to short names (Home.png, AllApps.png, Subscriptions.png, ControlCenter.png, Flow.png, Greeting.png, MemoryStorage.png).
- Added [performance](performance.md) from the owner's performance and lightweight-design requirements; linked from `index.md`.
- Stage 1 done: filled in `code-map.md`; added `docs/MANUAL_TEST.md`. Haze 2.x splits blur into `dev.chrisbanes.haze:haze-blur`, which the build uses alongside `haze`.
- Replaced the hand-written code map with an OKF code bundle `docs/kb/` and `scripts/kb.py` (build, query, check); see [knowledge-graph](knowledge-graph.md). [token-budget](token-budget.md), [definition-of-done](definition-of-done.md) and [ground-rules](ground-rules.md) now require `kb.py query` before searching and a passing `kb.py check` before each commit. `code-map.md` is a redirect, to be removed.
- Added [app-mode](app-mode.md): KS runs as an app opened from the owner's launcher (`com.sumit.lumiohome`), never the default Home. Manifest drops HOME/DEFAULT, Back on the main screen moves KS to the background, tile label "KS TV". [google-tv-setup](google-tv-setup.md) no longer covers disabling the stock launcher. Implemented at the start of Stage 2.
- Stage 2: [app-config](app-config.md) now lists the `REQUEST_DELETE_PACKAGES` permission, which `ACTION_DELETE` in the app menu ([apps](apps.md)) needs on API 28+.
- Stage 3: [subscriptions](subscriptions.md) currency line: INR grouping is done in `domain/Money`, not `NumberFormat`, because the JVM's java.text cannot group in lakhs (unit tests and devices would disagree). CSV contract unchanged.

## 2026-11-17
- Added [verification](verification.md): every stage and fix is tested on the TV with `TEST ` sample data, confirmed by the owner, reviewed by Claude Cowork, and only then committed and pushed. [definition-of-done](definition-of-done.md) and `CLAUDE.md` updated to match.
- Stage 4: [app-config](app-config.md) lists the `BLUETOOTH` (maxSdk 30) and `PACKAGE_USAGE_STATS` permissions. [home-screen](home-screen.md) clarified: the gear opens KS Settings (Android settings are a row in Settings and Control Center). [control-center](control-center.md): "long-press OK on Home" applies to Home controls without their own hold action; pinned tiles keep hold-OK for the app menu.
- [weather](weather.md): the city is now detected automatically by default (public-IP lookup via `https://ipwho.is/`, cached, at most once a day) so weather works straight after install; a manual city in [settings](settings.md) overrides it. Replaces "no city until the user sets one".
- Stage 4: confirmed `https://ipwho.is/` answers over HTTPS with `success`, `city`, `region`, `country`, `latitude`, `longitude` from the owner's network, so [weather](weather.md) keeps it (no change of service).
- Added Stage 6 (extras): [inputs](inputs.md), [custom-wallpaper](custom-wallpaper.md) and [app-updates](app-updates.md) (GitHub Releases, verify signer before install, `PackageInstaller`, stored-data compatibility rule). Linked from [stages](stages.md), `index.md`, [settings](settings.md) and [control-center](control-center.md).
- Added [release-checklist](release-checklist.md) as the first part of Stage 6: review and commit Stage 5, release `v0.5.0`, one-time switch of the TV to the release build, full on-device check of the items Stage 4 left untested, and the signing-key backup.
- Added [dev-workflow](dev-workflow.md): debug builds become a separate app (`com.ks.tvlauncher.dev`, "KS TV Dev") so testing never touches the real app; work on `dev`, tag from `main`; after every release, put it on the TV, verify the owner's data survived, then uninstall KS TV Dev. [verification](verification.md), [release-checklist](release-checklist.md) and `CLAUDE.md` updated.
- [performance](performance.md): WorkManager's own merged permissions, services and receivers (`WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`) are accepted; KS code itself holds no wake lock, runs no service and declares no receiver of its own.
- [app-updates](app-updates.md): updates get their own notification channel and work; turning renewal reminders off cancels only reminder work and notifications, never the update check or an update notification (and the reverse).
- Release checklist step 2a: debug builds are now KS TV Dev (`com.ks.tvlauncher.dev`), and a local `dev` branch exists ([dev-workflow](dev-workflow.md)).
- Release checklist: `v0.5.0` released (signer SHA-256 `2aa25f08…`); the owner's TV moved to the release build once (step 3); full on-device check recorded in `docs/MANUAL_TEST.md` with one fault (Screensaver tile opens Android Settings on a TV with no screensaver); KS TV Dev removed.
- Key backup confirmed by owner on 2026-11-17. No keystore or `keystore.properties` is tracked by git or in its history. Steps 3 and 5 of the [release-checklist](release-checklist.md) are confirmed, so Stage 6c ([app-updates](app-updates.md)) may start.
