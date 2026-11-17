# ks-tv-launcher

Part of KS-Suite. Ad-free, Apple TV–style Android TV / Google TV launcher, run as an app (never the default Home). Package `com.ks.tvlauncher`. Kotlin + Compose for TV.

## How to read the spec (saves tokens)
- The spec is an OKF bundle in `docs/spec/`. Start at `docs/spec/index.md`; it lists the files each stage needs.
- Read only those files. Don't open other spec files or the mockups unless a task needs them.
- Before Grep/Glob/Read on code, run `python3 scripts/kb.py query <symbol|file|module>`, then read `docs/kb/modules/<module>.md`. Open a source file only to change it, or when the query finds nothing.
- Every Markdown file you add to `docs/spec/` or `docs/kb/` is an OKF concept: YAML frontmatter with a non-empty `type`, one concept per file, relative links. See `docs/spec/knowledge-graph.md`.
- Don't re-read a file you already read in this session unless it changed.
- The spec wins over older notes. There is no Hilt, Retrofit or OpenWeatherMap in this project.

## Hard rules
- D-pad first: everything focusable and reachable with arrows; never long-press only (always a second path).
- KS is an app, not the Home launcher: `MAIN` + `LEANBACK_LAUNCHER` only, never `HOME`/`DEFAULT` (see `docs/spec/app-mode.md`).
- Never blur behind a scrolling row.
- Subscription data stays on the device (no backup, no sync). Never silently wipe corrupt data.
- The subscription CSV export is a cross-app contract with `ks-expense-manager`: see `docs/spec/subscriptions.md` before touching it.
- From Stage 6: test only with the KS TV Dev app (`com.ks.tvlauncher.dev`). Never uninstall, clear or debug-install over the real app `com.ks.tvlauncher` (`docs/spec/dev-workflow.md`).
- AGP 9 has built-in Kotlin: don't apply `org.jetbrains.kotlin.android`.

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

## After completion (2026-11-17)
- The staged build is finished. For any new work, read `docs/spec/future-development.md` first and follow its cycle.

## Always
- One stage per request; finish with `docs/spec/definition-of-done.md` (includes `kb.py build` + `kb.py check`).
- Never claim a build or test passed unless the command succeeded.
- Never commit or push a stage or a fix until it has been tested on the TV with sample data, the owner has confirmed, and Claude Cowork has reviewed it (`docs/spec/verification.md`). Stop and hand off for review; commit and push only after approval.
