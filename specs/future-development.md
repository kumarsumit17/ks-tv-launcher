---
type: Policy
title: Future development rules
description: "Rules for any change after completion, the signing key, open items."
tags: [future, development, rules]
stage: after completion
---

# Future development rules

The project as specified was completed on **2026-11-17 at 16:00 IST (UTC+05:30)** with release `v0.7.3`, installed on the owner's TV from inside KS. Everything after that follows these rules.

## The cycle for any change
1. Write or update the spec first (one OKF concept per file, linked from [index](index.md), noted in [log](log.md)).
2. Build on `dev` and test with **KS TV Dev** only (`com.ks.tvlauncher.dev`; the `perf` build for anything about speed or release-only behaviour). Use `TEST ` data and remove it ([verification](verification.md), [dev-workflow](dev-workflow.md)).
3. The owner confirms; Claude Cowork reviews the uncommitted work; only then commit and push.
4. Merge `dev` into `main`, tag a **new** version from `main` (never reuse or move a tag), and let the Release workflow publish it. Confirm the signer SHA-256 starts with `2aa25f08`.
5. The owner updates **from inside KS** (Settings › About › Check for updates). ADB install is a fallback only, always `adb install -r`, never an uninstall.
6. After the update, confirm the owner's data is unchanged and record it in `docs/MANUAL_TEST.md`. Remove KS TV Dev.

## Never
- Never uninstall, clear, force-stop or write test data into the real app `com.ks.tvlauncher`.
- Never rename, remove or change the meaning of a stored key or serialized field without a migration and a compatibility test ([app-updates](app-updates.md)).
- Never add `HOME` or `DEFAULT` to the manifest ([app-mode](app-mode.md)).
- Never commit a keystore, password or `keystore.properties`.

## The signing key
- Every update depends on the release key (`ks-release`, SHA-256 `2aa25f08…6448`). The owner keeps the keystore file and its password backed up outside the repo and outside this Mac. If it is lost, no update can install over the existing app.
- If the key or the GitHub signing secrets ever change, stop: that is a new app as far as Android is concerned.

## Open items (optional, not scheduled)
- **System-setting checks** not yet run on the TV: changing the TV's time, time zone and 12/24-hour setting, renaming the TV, and Animator duration scale set to "Animation off". The owner changes the setting with the remote; results go in `docs/MANUAL_TEST.md`.
- **Own photo as wallpaper on a TV with no file picker**: the owner's TV hides "Choose a picture". It needs another route (for example a picture read from a USB drive, or a file-manager app that provides a picker). The picker import path is also untested on any device ([custom-wallpaper](custom-wallpaper.md)).
- **CSV import in `ks-expense-manager`**: KS exports the subscriptions CSV; reading it belongs to that app. The format is a contract ([subscriptions](subscriptions.md)).
- **Release-only checks still unproven**: the update-check job returning after a force-stop on a release build, and the Home update pill's Left behaviour.
- **Graphify**: consider it only if the code grows past about 100 source files ([token-budget](token-budget.md)).

Each open item that is taken up becomes its own spec change and goes through the cycle above.
