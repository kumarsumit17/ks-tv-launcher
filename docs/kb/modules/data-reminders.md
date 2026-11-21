---
type: Module
title: data.reminders
package: com.ks.tvlauncher.data.reminders
spec: [../../../specs/reminders.md, ../../../specs/performance.md]
---

# data.reminders

## Purpose
Renewal reminders: `ReminderScheduler` (unique daily WorkManager job + a one-off check after each change), `RenewalReminderWorker` (runs `RenewalReminders.plan`), `ReminderNotifier` (channel, post, cancel) and `ReminderStore` (notified `id@date` keys in the shared DataStore).

## Rules
- WorkManager starts on demand: `KsApplication` is the `Configuration.Provider` and the manifest removes `WorkManagerInitializer`. Never touch WorkManager at app start.
- Off by default. Settings writes the switch before calling the scheduler, so the check reads the new value; switching off cancels the daily job and withdraws every renewal notification (`cancelAllReminders`: renewal channel only, never the update notification).
- `SubscriptionRepository` calls `onChanged` after every write; that re-checks only while reminders are on. Paused, deleted or re-dated subscriptions lose their notification.
- Cancel before posting (same notification id per subscription). While notifications are blocked, nothing new is marked notified, so the reminder comes once they are allowed.
- Never promise exact timing in the UI: WorkManager may defer the work.
- `ReminderScheduler.ensureDaily()` re-registers the daily job with KEEP and runs no immediate check. It is called by `AppContainer.ensureDailyWork` while reminders are on, so the job returns after a force-stop.

<!-- kb:generated:start -->

## Files (generated)
- `data/reminders/ReminderNotifier.kt` — `ReminderNotifier`
- `data/reminders/ReminderScheduler.kt` — `ReminderScheduler`
- `data/reminders/ReminderStore.kt` — `ReminderStore`
- `data/reminders/RenewalReminderWorker.kt` — `RenewalReminderWorker`

## Depends on (generated)
- [data.settings](data-settings.md) via `SettingsRepository`
- [domain](domain.md) via `BillingCycle`, `DueReminder`, `Money`, `RenewalReminders`
- [root](root.md) via `KsApplication`, `MainActivity`
- [util](util.md) via `DateTimeFormats`

## Used by (generated)
- [data.updates](data-updates.md) via `ReminderNotifier`
- [root](root.md) via `ReminderScheduler`, `ReminderStore`
- [ui.settings](ui-settings.md) via `ReminderNotifier`, `ReminderScheduler`

## Spec
- [reminders](../../../specs/reminders.md), [performance](../../../specs/performance.md)

<!-- kb:generated:end -->
