---
type: Playbook
title: Final release check
description: "Stage 1–3 checklists (read-only on the real app, data entry on the `perf` build) and reminders after a TV restart (Stage 6c part 3, last)."
tags: [final, check, process]
stage: 6c
---

# Final release check

The last part of Stage 6c, after the [smoothness](smoothness.md) pass and [app-updates](app-updates.md) are released. It closes the two gaps left by the [release-checklist](release-checklist.md): the Stage 1 to 3 checklists were never run on a release build, and reminders were never checked across a TV restart. No new features. Docs and fixes only.

## Two builds, and the real app is never written to
- **Real KS TV** (`com.ks.tvlauncher`, the latest release on the owner's TV): read-only checks only. That means D-pad navigation, screens and dialogs opening and closing, apps launching, and confirming the owner's data is unchanged against the baseline. Nothing is saved, toggled, pinned, moved or deleted there, and no setting is changed, not even to restore it afterwards. A dialog opened only to look is closed with Back or Cancel.
- **`perf` build** ([smoothness](smoothness.md): release code installed as KS TV Dev, `com.ks.tvlauncher.dev`, debug key, separate data): every check that needs data entry or a change. That covers subscriptions, names, pins and order, settings, first-run states and reminders, always with `TEST ` items. Built with the released version (`-PversionName`/`-PversionCode`) so it runs the same code. Removed with `adb uninstall com.ks.tvlauncher.dev` when the check is finished ([dev-workflow](dev-workflow.md)).
- Don't force-stop the real app. Android then cancels its WorkManager jobs, and KS re-registers them only at the next TV restart or reminder/update setting change. Cold-start and force-stop persistence checks run on the `perf` build.
- Never uninstall or clear the real app. Never edit or delete the owner's names, pins or subscriptions.
- `docs/MANUAL_TEST.md` names the build each check ran on.

## 1. Stage 1 to 3 checklists on the release code
- Run every item of the Stage 1, Stage 2 and Stage 3 sections of `docs/MANUAL_TEST.md`. Read-only items run on the real KS TV; items that need data entry or a change run on the `perf` build.
- Items that changed since they were written (for example app mode replaced the Home-key checks) are run in their current form; note the difference.
- Items that change Android's own settings (time, time zone, 12/24-hour, device name, animator scale) are done by the owner with the remote, or recorded as not run.
- Record each item as Pass, Fault or Not possible on this TV, with the date, version and build. A fault becomes a fix that goes through [verification](verification.md) and its own release.

## 2. Reminders after a TV restart (on the `perf` build)
- With the owner's agreement to restart the TV (it interrupts whatever is playing): turn reminders on in KS TV Dev (`perf`), add "TEST Reboot" renewing tomorrow, and confirm its notification appears. The real app's reminder setting is not touched.
- Dismiss that notification (or let the restart clear it), then restart the TV (`adb reboot`, or the owner uses the remote). Wait for the TV and reconnect.
- Without opening either app, check that KS TV Dev's daily reminder job is still registered (`adb shell dumpsys jobscheduler` filtered to the package) and that it has no other background work running. Also note the real app's jobs, read-only.
- Open KS TV Dev and confirm reminders are still on and its settings unchanged. The already-notified renewal must not be notified again.
- Add "TEST Reboot 2" renewing tomorrow and confirm a new notification, which proves checks still run after the restart. WorkManager timing is approximate, so the daily run itself is not waited for; the registered job is the evidence.
- Record the result in `docs/MANUAL_TEST.md`, then uninstall KS TV Dev (its `TEST ` items go with it).

## Done when
- Both parts are recorded with date, version and build, the owner's data in the real app is confirmed unchanged against a baseline taken at the start, KS TV Dev is removed, `kb.py check` passes, and the owner and Claude Cowork have reviewed the record. Then note in [log](log.md) that the project as specified is complete.
