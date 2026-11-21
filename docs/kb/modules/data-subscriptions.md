---
type: Module
title: data.subscriptions
package: com.ks.tvlauncher.data.subscriptions
spec: [../../../specs/subscriptions.md]
---

# data.subscriptions

## Purpose
Subscriptions on the device: `SubscriptionDataStore` (the list as one JSON key in the shared DataStore, corrupt-data handling) and `SubscriptionRepository` (`subscriptions`, `upsert`, `delete`, `toggleActive`).

## Rules
- Device-only: no backup (manifest + `data_extraction_rules.xml`), no accounts, no sync.
- Corrupt JSON: log, read as empty, and copy the raw text to `subscriptions_corrupt_backup` inside the first write that replaces it. Never overwrite an existing backup; never crash.
- `SubscriptionRepository(onChanged)` runs after every write; the container wires it to `ReminderScheduler.onChanged` (Stage 5).
- Every change is one `store.edit` transaction; the shared `ks_launcher` file, no second DataStore.

<!-- kb:generated:start -->

## Files (generated)
- `data/subscriptions/SubscriptionDataStore.kt` — `SubscriptionDataStore`
- `data/subscriptions/SubscriptionRepository.kt` — `SubscriptionRepository`

Tests: `data/subscriptions/SubscriptionRepositoryTest.kt`

## Depends on (generated)
- [domain](domain.md) via `Subscription`

## Used by (generated)
- [root](root.md) via `SubscriptionDataStore`, `SubscriptionRepository`
- [ui.home](ui-home.md) via `SubscriptionRepository`
- [ui.subscriptions](ui-subscriptions.md) via `SubscriptionRepository`

## Spec
- [subscriptions](../../../specs/subscriptions.md)

<!-- kb:generated:end -->
