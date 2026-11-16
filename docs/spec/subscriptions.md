---
type: Feature
title: Subscriptions
stage: 3
mockups: [Subscriptions.png]
---

# Subscriptions (`data/subscriptions/`, `ui/subscriptions/`)

## Model
`@Serializable data class Subscription(id: String, name: String, provider: String, costMinor: Long, cycle: BillingCycle /* MONTHLY, YEARLY */, renewsOn: String /* ISO date */, active: Boolean, note: String = "")`. Money in minor units (paise), never Float/Double.

## Storage
`SubscriptionDataStore`: one JSON string key holding the list. Corrupt JSON → log, keep the raw value under a backup key, emit an empty list, never crash and never overwrite the backup. Device-only: `allowBackup="false"` plus `data_extraction_rules.xml`; no accounts or sync. `SubscriptionRepository`: `subscriptions: Flow<List<Subscription>>`, `upsert`, `delete`, `toggleActive`.

## `SubscriptionCalculator` (pure Kotlin, in `domain/`)
Monthly equivalent = yearly ÷ 12, half-up to the paisa · monthly and yearly totals of active only · active count · next renewal (roll past dates forward by cycle until ≥ today) · days until it.

## Currency
Default INR (`₹`, Indian grouping ₹1,23,456, done in `domain/Money` because the JVM's `NumberFormat` cannot group in lakhs); configurable in [settings](settings.md).

## UI
- Four stat cards + rows: name/provider, cost + equivalent, billing, renewal date, Active/Paused pill (OK toggles).
- OK on a row or "Add subscription" → `GlassDialog`: Name, Provider; cost stepper (±₹10 press, ±₹100 hold); Monthly/Yearly segmented; renewal « ‹ date › » (month/day); Active switch; Note; Delete / Cancel / Save.
- Totals update instantly from the Flow. The Home card shows the same numbers; OK on it opens this screen.
- An "Export CSV" button on this screen starts the export below.

## CSV export (contract with `ks-expense-manager`, do not change without agreement)
User-started `ACTION_SEND`, MIME `text/csv`, CSV in `EXTRA_TEXT`. Header and columns:

| Column | Format |
|---|---|
| `name` | free text, RFC 4180 quoted when needed |
| `amount_minor` | integer in the currency's minor unit (= `costMinor`) |
| `currency` | ISO 4217, upper case (from Settings currency) |
| `cycle` | `weekly` \| `monthly` \| `yearly` (this app emits `monthly`/`yearly`) |
| `next_due` | ISO-8601 `yyyy-MM-dd` (next renewal after roll-forward) |

Rows end with CRLF. Export active and paused rows. A `SubscriptionCsvTest` locks the format.
