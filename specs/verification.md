---
type: Playbook
title: Build, test, release
description: "Every feature or fix is built, tested on every TV-size emulator with sample data, then committed, pushed and released, without waiting for anyone."
tags: [verification, process, release]
stage: all
---

# Build, test, release

Applies to every feature **and every fix**, however small. Order is fixed: build → test → commit and push → release. There is no review or confirmation step in between: when the tests pass, the work is committed, pushed and released straight away (owner's rule, 2026-11-25). Hand checks by the owner and paid signing never block a release.

## 1. Build
- Spec first ([future-development](future-development.md)), then the code on `dev`.
- `./gradlew assembleDebug testDebugUnitTest lintDebug` pass; `python3 scripts/kb.py build`, then `kb.py check` prints OK ([definition-of-done](definition-of-done.md)).

## 2. Test with sample data
- On **every TV-size emulator** (32, 43, 50, 55, 65, 75, 85 and 100 inch; `../scripts/emulators.sh sizes`): `connectedDebugAndroidTest`, install **KS TV Dev** (`com.ks.tvlauncher.dev`), open it, and check the changed screens with the D-pad. Never touch the real app `com.ks.tvlauncher` ([dev-workflow](dev-workflow.md)).
  - `scripts/tv_sizes.sh OUT_DIR` does this for every size in one run: connected tests, install, screenshots of Home, All apps, Subscriptions, Control Center and Settings (with `LANGS="en hi"` also Home and Settings in Hindi), the label audit ([accessibility](accessibility.md)) and a crash count. Look at the screenshots of the changed screens, at least on 32 and 100 inch.
- Exercise the changed feature with **sample test data** entered through the app itself, with names starting `TEST ` (for example a subscription "TEST Netflix", ₹499 monthly; "TEST Prime", ₹1,499 yearly; a profile name "TEST Goldi"). Cover the normal path, an edge case and the empty state.
- Drive it with `adb shell input keyevent` (DPAD, ENTER, BACK) and check results with `adb exec-out screencap -p`; look at the screenshot only as far as the check needs ([token-budget](token-budget.md)).
- The owner's TV (`adb connect 192.168.1.5:5555`) is used as well when it is reachable, with KS TV Dev only. If it is not reachable, the emulators are enough; it never blocks.
- Protect the owner's real data: never run `pm clear` or uninstall on `com.ks.tvlauncher`. Sample data never ships in the code ([ground-rules](ground-rules.md)).
- Record what was tested and the result in the report, and add new checks to `docs/MANUAL_TEST.md`.
- If a test fails, fix it and test again. Nothing is committed or released while a test fails.

## 3. Commit and push
`git commit` on `dev` (one commit per feature or fix, Conventional Commits), then `git push`.

## 4. Release
- Fast-forward `main` to `dev`, push it, and tag the next version from `main`: a feature raises MINOR (`v0.9.0`), a fix raises PATCH (`v0.8.1`). Never reuse or move a tag.
- Wait for the Release workflow and confirm the signer SHA-256 starts with `2aa25f08`. If the workflow fails, fix it on `dev` and release a new version; the failed tag is never reused.
- Then follow [dev-workflow](dev-workflow.md) steps 4 to 6 when the owner updates the TV from inside KS: the data check and removing KS TV Dev. These are hand steps and never hold back the next feature, unless the data check finds lost data: then that is fixed first.
