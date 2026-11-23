# ks-tv-launcher

Part of KS-Suite. Ad-free, Apple TV–style Android TV / Google TV launcher, run as an app (never the default Home). Package `com.ks.tvlauncher`. Kotlin + Compose for TV.

## How to read the spec (saves tokens)
- The spec is an OKF bundle in `specs/`. Start at `specs/index.md`; it lists the files each stage needs.
- Read only those files. Don't open other spec files or the mockups unless a task needs them.
- Before Grep/Glob/Read on code, run `python3 scripts/kb.py query <symbol|file|module>`, then read `docs/kb/modules/<module>.md`. Open a source file only to change it, or when the query finds nothing.
- Every Markdown file you add to `specs/` or `docs/kb/` is an OKF concept: YAML frontmatter with a non-empty `type`, one concept per file, relative links. See `specs/knowledge-graph.md`.
- Don't re-read a file you already read in this session unless it changed.
- The spec wins over older notes. There is no Hilt, Retrofit or OpenWeatherMap in this project.

## Specs
- All specs for this project live in `specs/` in the repository root, as an Open Knowledge Format (OKF) v0.2 bundle. Never create spec files anywhere else. Start at `specs/index.md`.
- One topic per file. Every file except `index.md` and `log.md` starts with YAML frontmatter: `type` (required: `Specification`, `Policy`, `Plan`, `Playbook` or `Reference`), `title`, `description` (one sentence), `tags`.
- When a spec is added: list it in `specs/index.md` with its description. When a spec is added or changed: add a line under today's date (`# YYYY-MM-DD`, newest first) in `specs/log.md`.
- Link between specs with relative markdown links (`[Dependencies](dependencies.md)`), so they also work on GitHub.
- `docs/kb/` is the generated code graph, not a spec bundle: `scripts/kb.py build` writes it, `kb.py check` validates both.

## Hard rules
- D-pad first: everything focusable and reachable with arrows; never long-press only (always a second path).
- KS is an app, not the Home launcher: `MAIN` + `LEANBACK_LAUNCHER` only, never `HOME`/`DEFAULT` (see `specs/app-mode.md`).
- Never blur behind a scrolling row.
- Subscription data stays on the device (no backup, no sync). Never silently wipe corrupt data.
- The subscription CSV export is a cross-app contract with `ks-expense-manager`: see `specs/subscriptions.md` before touching it.
- From Stage 6: test only with the KS TV Dev app (`com.ks.tvlauncher.dev`). Never uninstall, clear or debug-install over the real app `com.ks.tvlauncher` (`specs/dev-workflow.md`).
- AGP 9 has built-in Kotlin: don't apply `org.jetbrains.kotlin.android`.

## Platform folders
- Every KS-Suite app repo has the same top-level platform folders: `tv/google-tv/`, `desktop/mac/`, `desktop/windows/`, `mobile/android/` and `mobile/ios/`. All five exist in every app repo, whether or not the app supports that platform yet.
- This app targets `tv/google-tv`. The other folders are empty on purpose (a `.gitkeep` only): they mark where that platform's code would go, not missing work. Do not add code for a platform that is not a target without the owner asking.
- Three layers, narrowest first: a platform folder holds code for one platform of this app; `shared/` in the repository root holds code used by two or more platforms of this app, written once and never copied between platform folders; `ks-utility` holds generic code used by several apps. This app has one target, so it has no `shared/` folder.
- The app's code is still in `app/` with the Gradle build in the repository root. Moving it into `tv/google-tv/` is a separate, planned change: it touches the Gradle settings, the release workflow paths and `scripts/kb.py`, so it is done as its own stage and tested on the TV.
- Never rename, merge or remove these folders, and never add another top-level platform folder; the layout is a suite-wide rule.

## Shared code (`ks-utility`)
- Generic utility code shared across the KS-Suite apps lives in the `ks-utility` repository. It is a dependency of this app: a git submodule at `ks-utility/`, pinned to a commit, included with `includeBuild("ks-utility/kotlin")` and used as `libs.ks.utility` (`com.ks:utility`). After cloning, run `git submodule update --init`.
- Before writing a generic helper (one that knows nothing about this app), look in `ks-utility/kotlin` and use it from there. If it is missing and another app would need it too, add it in the `ks-utility` repository, not here. Application-specific logic stays in this repo.
- Never edit files under `ks-utility/` from this repo. Change them in the `ks-utility` repository, then move the pin: `git -C ks-utility fetch && git -C ks-utility checkout <commit>`, and commit the changed `ks-utility` entry.
- Keep the Kotlin version in `ks-utility/kotlin/gradle/libs.versions.toml` in step with `kotlin` in this repo's catalog.
- CI checks out with `submodules: true` (`.github/workflows/release.yml`).

## Commands
```bash
./gradlew assembleDebug            # build
./gradlew testDebugUnitTest        # unit tests
./gradlew installDebug             # install on TV / emulator
./gradlew lintDebug                # lint
python3 scripts/kb.py query X      # find code via the knowledge graph (use before grep)
python3 scripts/kb.py build        # refresh docs/kb after code changes
python3 scripts/kb.py check        # must print OK before every commit
adb shell am start -n com.ks.tvlauncher/.MainActivity   # open KS on the TV
```
Test on the Android TV emulator (1080p, API 34) or a real TV; phone emulators hide focus bugs.

Emulator from the KS-Suite workspace (suite spec `specs/testing.md`): `../scripts/emulators.sh create` once, then `../scripts/emulators.sh start tv` boots `ks-tv-36` (Android TV, 1080p, API 36; the Mac's arrow keys, Enter and Esc are the D-pad, OK and Back). Then:
```bash
export ANDROID_SERIAL=$(../scripts/emulators.sh serial tv)
./gradlew installDebug connectedDebugAndroidTest       # installs KS TV Dev only
adb shell am start -n com.ks.tvlauncher.dev/com.ks.tvlauncher.MainActivity
```

## After completion (2026-11-17)
- The staged build is finished. For any new work, read `specs/future-development.md` first and follow its cycle.

## Always
- One stage per request; finish with `specs/definition-of-done.md` (includes `kb.py build` + `kb.py check`).
- Never claim a build or test passed unless the command succeeded.
- Never commit or push a stage or a fix until it has been tested on the TV with sample data, the owner has confirmed, and Claude Cowork has reviewed it (`specs/verification.md`). Stop and hand off for review; commit and push only after approval.
