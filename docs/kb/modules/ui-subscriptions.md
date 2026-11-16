---
type: Module
title: ui.subscriptions
package: com.ks.tvlauncher.ui.subscriptions
spec: [../../spec/subscriptions.md, ../../spec/focus-navigation.md]
---

# ui.subscriptions

## Purpose
The Subscriptions screen (stat cards, list, Export CSV), the add/edit `SubscriptionDialog`, and `subscriptionsState`, the flow both this screen and the Home card read.

## Rules
- Totals come only from `SubscriptionCalculator` via `subscriptionsState`, so Home and this screen always agree.
- "Today" follows `minuteTicker`, so renewals roll forward at midnight without polling.
- Rows: OK opens the editor; Right reaches the Active/Paused pill, whose OK toggles. No long-press actions.
- After the dialog closes the screen refocuses the row it came from, else Add subscription; the dialog itself gets `returnFocusTo = null`.
- Export is user-started `ACTION_SEND` with `text/csv` in `EXTRA_TEXT`; the format belongs to `SubscriptionCsv` (cross-app contract).
- The screen is drawn opaque over Home (Home stays composed) and uses `modalFocus()`; the list rows are not frosted.

<!-- kb:generated:start -->

## Files (generated)
- `ui/subscriptions/SubscriptionDialog.kt` — `SubscriptionDialog`
- `ui/subscriptions/SubscriptionsScreen.kt` — `SubscriptionsScreen`, `nextRenewalLine`
- `ui/subscriptions/SubscriptionsViewModel.kt` — `SubscriptionRow`, `SubscriptionsUiState`, `SubscriptionsViewModel`, `subscriptionsState`

## Depends on (generated)
- [data.settings](data-settings.md) via `SettingsRepository`
- [data.subscriptions](data-subscriptions.md) via `SubscriptionRepository`
- [domain](domain.md) via `BillingCycle`, `Money`, `NextRenewal`, `Subscription`, `SubscriptionCalculator`, `SubscriptionCsv` +1
- [root](root.md) via `KsApplication`
- [ui.apps](ui-apps.md) via `AppIntents`
- [ui.components](ui-components.md) via `FocusMemory`, `GlassButton`, `GlassCard`, `GlassChip`, `GlassDialog`, `GlassPill` +6
- [ui.icons](ui-icons.md) via `KsIcons`
- [ui.theme](ui-theme.md) via `KsColors`, `KsDimens`, `KsType`
- [util](util.md) via `DateTimeFormats`, `minuteTicker`

## Used by (generated)
- [ui.home](ui-home.md) via `SubscriptionsScreen`, `SubscriptionsUiState`, `SubscriptionsViewModel`, `nextRenewalLine`, `subscriptionsState`

## Spec
- [subscriptions](../../spec/subscriptions.md), [focus-navigation](../../spec/focus-navigation.md)

<!-- kb:generated:end -->
