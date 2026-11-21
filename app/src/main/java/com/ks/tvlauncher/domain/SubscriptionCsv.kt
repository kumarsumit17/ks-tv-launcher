package com.ks.tvlauncher.domain

import java.time.LocalDate

/**
 * CSV export. This format is a contract with ks-expense-manager (specs/subscriptions.md):
 * do not change it without agreement. `SubscriptionCsvTest` locks it.
 */
object SubscriptionCsv {
    const val MIME_TYPE = "text/csv"
    const val HEADER = "name,amount_minor,currency,cycle,next_due"
    private const val EOL = "\r\n"

    /** Active and paused rows, each ending with CRLF; `next_due` is the rolled-forward renewal. */
    fun export(subs: List<Subscription>, currencyCode: String, today: LocalDate): String = buildString {
        append(HEADER).append(EOL)
        val currency = currencyCode.uppercase()
        for (sub in subs) {
            val nextDue = SubscriptionCalculator.nextRenewal(sub, today)?.toString() ?: sub.renewsOn
            append(quote(sub.name)).append(',')
            append(sub.costMinor).append(',')
            append(currency).append(',')
            append(cycle(sub.cycle)).append(',')
            append(nextDue).append(EOL)
        }
    }

    private fun cycle(cycle: BillingCycle): String = when (cycle) {
        BillingCycle.MONTHLY -> "monthly"
        BillingCycle.YEARLY -> "yearly"
    }

    /** RFC 4180: quote a field that holds a comma, quote, CR or LF; double inner quotes. */
    fun quote(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\r' || it == '\n' }) {
            "\"" + field.replace("\"", "\"\"") + "\""
        } else {
            field
        }
}
