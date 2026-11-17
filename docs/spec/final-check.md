---
type: Process
title: Final release check
stage: 6c
---

# Final release check

The last part of Stage 6c, after the [smoothness](smoothness.md) pass and [app-updates](app-updates.md) are released. It closes the two gaps left by the [release-checklist](release-checklist.md): the Stage 1 to 3 checklists were never run on a release build, and reminders were never checked across a TV restart. No new features. Docs and fixes only.

## 1. Stage 1 to 3 checklists on the release build
- Run every item of the Stage 1, Stage 2 and Stage 3 sections of `docs/MANUAL_TEST.md` on the **real, released KS TV** (`com.ks.tvlauncher`) on the owner's TV, at the latest released version.
- Items that changed since they were written (for example app mode replaced the Home-key checks) are run in their current form; note the difference.
- Protect the owner's data ([dev-workflow](dev-workflow.md), [verification](verification.md)):
  - never uninstall or clear the real app;
  - note each setting before changing it, change one at a time, restore it before the next;
  - checks that need data entry (subscriptions, names) use `TEST ` items, removed afterwards; never edit or delete the owner's own names, pins or subscriptions;
  - pin and unpin checks use one app that the owner has not pinned, then restore the original order.
- Record each item as Pass, Fault or Not possible on this TV, with the date and version. A fault becomes a fix that goes through [verification](verification.md) and its own release.

## 2. Reminders after a TV restart
- With the owner's agreement to restart the TV (it interrupts whatever is playing): turn reminders on in the real app, add "TEST Reboot" renewing tomorrow, and confirm its notification appears.
- Dismiss that notification, then restart the TV (`adb reboot`, or the owner uses the remote). Wait for the TV and reconnect.
- Without opening KS, check that the daily reminder job is still registered (`adb shell dumpsys jobscheduler` filtered to the package) and that KS has no other background work running.
- Open KS and confirm reminders are still on and the settings unchanged. The already-notified renewal must not be notified again.
- Add "TEST Reboot 2" renewing tomorrow and confirm a new notification, which proves checks still run after the restart. WorkManager timing is approximate, so the daily run itself is not waited for; the registered job is the evidence.
- Remove both `TEST ` items and restore the reminder setting and lead time to what the owner had.
- Record the result in `docs/MANUAL_TEST.md`.

## Done when
- Both parts are recorded with date and version, the owner's data is confirmed unchanged against a baseline taken at the start, `kb.py check` passes, and the owner and Claude Cowork have reviewed the record. Then note in [log](log.md) that the project as specified is complete.
