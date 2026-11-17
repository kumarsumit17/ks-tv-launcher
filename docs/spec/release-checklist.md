---
type: Process
title: Release checklist (finish Stage 5, move the TV to the release build)
stage: 6
---

# Release checklist

Stage 6 starts with this checklist. It closes Stage 5 and moves the owner's TV from the debug build to the signed release, which [app-updates](app-updates.md) needs. Steps marked **Owner** are done by the owner; Claude Code prepares, checks and reports, and never does them on its own.

1. **Stage 5 reviewed and committed.** Follow [verification](verification.md): hand-off to Claude Cowork, then commit and push only after approval.
2. **Final Stage 5 release `v0.5.0`.** Clean tree, `main` pushed, unit tests and `kb.py check` pass, then tag and push the tag. Watch the Release workflow; confirm the GitHub release has `app-release.apk` and the signing certificate SHA-256 starts with `2aa25f08`. Never reuse a version number.
2a. **Set up the dev app** ([dev-workflow](dev-workflow.md)): debug builds become `com.ks.tvlauncher.dev` / "KS TV Dev", create the `dev` branch. Goes through [verification](verification.md) like any change, and lands before the tag in step 2 if possible; otherwise release it as the next patch version.
3. **Owner: switch the TV to the release build, once.**
   - Before uninstalling, note what must be re-entered: profile names, own TV name, subscriptions, pinned apps and their order, weather mode and city, wallpaper. Claude Code may list the current values from the TV screens to help; it must not run the uninstall without the owner saying so.
   - `adb uninstall com.ks.tvlauncher`, then `adb install app-release.apk`. This clears KS data on the TV.
   - Re-pin "KS TV" in the owner's launcher favourites and re-enter the noted details.
   - From now on only release builds go on this TV, so later releases update in place and keep data. Claude Code's TV tests use the separate KS TV Dev app ([dev-workflow](dev-workflow.md)).
4. **Full on-device check** with `docs/MANUAL_TEST.md`, including what Stage 4 left untested: Memory card on/off, glass intensity, Reduce motion, currency, pinned order, Screensaver, weather with Wi-Fi off. Record the results in `docs/MANUAL_TEST.md` (date, build version, pass or the fault found). Faults become fixes that go through [verification](verification.md).
5. **Owner: back up the signing key.** Keep the keystore file and its passwords in a password manager, outside the repo. Without them no update can install over the existing app. Claude Code only confirms that no keystore or `keystore.properties` is tracked by git, and records "key backup confirmed by owner on <date>" in [log](log.md) once the owner says it is done.

Stage 6c ([app-updates](app-updates.md)) must not start until steps 3 and 5 are confirmed by the owner.
