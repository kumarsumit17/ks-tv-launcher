package com.ks.tvlauncher.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/** The next renewal among active subscriptions. */
data class NextRenewal(val subscription: Subscription, val date: LocalDate, val daysUntil: Long)

/** The numbers the Subscriptions screen and the Home card show. */
data class SubscriptionSummary(
    val activeCount: Int,
    val totalCount: Int,
    val monthlyMinor: Long,
    val yearlyMinor: Long,
    val next: NextRenewal?,
)

/** Pure subscription maths. All money is in minor units; only active subscriptions count in totals. */
object SubscriptionCalculator {

    /** Monthly cost; a yearly price is divided by 12, rounded half-up to the minor unit. */
    fun monthlyEquivalent(sub: Subscription): Long = when (sub.cycle) {
        BillingCycle.MONTHLY -> sub.costMinor
        BillingCycle.YEARLY -> BigDecimal.valueOf(sub.costMinor)
            .divide(BigDecimal.valueOf(12), 0, RoundingMode.HALF_UP)
            .longValueExact()
    }

    /** Yearly cost; a monthly price times 12. */
    fun yearlyEquivalent(sub: Subscription): Long = when (sub.cycle) {
        BillingCycle.MONTHLY -> sub.costMinor * 12
        BillingCycle.YEARLY -> sub.costMinor
    }

    fun monthlyTotal(subs: List<Subscription>): Long = subs.filter { it.active }.sumOf(::monthlyEquivalent)

    fun yearlyTotal(subs: List<Subscription>): Long = subs.filter { it.active }.sumOf(::yearlyEquivalent)

    fun activeCount(subs: List<Subscription>): Int = subs.count { it.active }

    /** Null when [iso] is not a valid `yyyy-MM-dd` date. */
    fun parseDate(iso: String): LocalDate? = try {
        LocalDate.parse(iso)
    } catch (e: DateTimeParseException) {
        null
    }

    /**
     * The first renewal on or after [today]: past dates move forward by whole cycles. Steps are
     * counted from the stored date, so a renewal on the 31st stays on the month's last day
     * instead of drifting to the 28th. Null for an unreadable date.
     */
    fun nextRenewal(sub: Subscription, today: LocalDate): LocalDate? {
        val start = parseDate(sub.renewsOn) ?: return null
        if (!start.isBefore(today)) return start
        val unit = when (sub.cycle) {
            BillingCycle.MONTHLY -> ChronoUnit.MONTHS
            BillingCycle.YEARLY -> ChronoUnit.YEARS
        }
        var steps = unit.between(start, today)
        var date = start.plus(steps, unit)
        while (date.isBefore(today)) {
            steps++
            date = start.plus(steps, unit)
        }
        return date
    }

    fun daysUntil(date: LocalDate, today: LocalDate): Long = ChronoUnit.DAYS.between(today, date)

    /** The soonest renewal among active subscriptions; ties go to the earlier name. */
    fun nextActiveRenewal(subs: List<Subscription>, today: LocalDate): NextRenewal? = subs
        .asSequence()
        .filter { it.active }
        .mapNotNull { sub -> nextRenewal(sub, today)?.let { sub to it } }
        .minWithOrNull(compareBy<Pair<Subscription, LocalDate>> { it.second }.thenBy { it.first.name.lowercase() })
        ?.let { (sub, date) -> NextRenewal(sub, date, daysUntil(date, today)) }

    fun summary(subs: List<Subscription>, today: LocalDate): SubscriptionSummary = SubscriptionSummary(
        activeCount = activeCount(subs),
        totalCount = subs.size,
        monthlyMinor = monthlyTotal(subs),
        yearlyMinor = yearlyTotal(subs),
        next = nextActiveRenewal(subs, today),
    )
}
