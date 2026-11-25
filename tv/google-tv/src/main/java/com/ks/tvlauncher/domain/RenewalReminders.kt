package com.ks.tvlauncher.domain

import java.time.LocalDate

/** A renewal that is inside the reminder window. */
data class DueReminder(val subscription: Subscription, val date: LocalDate, val daysUntil: Long) {
    /** Identifies this renewal: the same subscription renewing on another date is a new reminder. */
    val key: String get() = RenewalReminders.key(subscription.id, date)
}

/** What one daily check should do. */
data class ReminderPlan(
    /** Renewals to notify now (not notified before). */
    val toNotify: List<DueReminder>,
    /** Subscription ids whose posted notification no longer applies (paused, deleted, date changed). */
    val toCancel: Set<String>,
    /** The notified keys to store after this check. */
    val notified: Set<String>,
)

/**
 * Pure rules for renewal reminders. A daily check notifies each active renewal once, when it
 * is [leadDays] days away or closer, and withdraws notifications that no longer apply.
 */
object RenewalReminders {
    const val MIN_LEAD_DAYS = 1
    const val MAX_LEAD_DAYS = 14
    const val DEFAULT_LEAD_DAYS = 3

    fun clampLeadDays(days: Int): Int = days.coerceIn(MIN_LEAD_DAYS, MAX_LEAD_DAYS)

    fun key(subscriptionId: String, date: LocalDate): String = "$subscriptionId@$date"

    private fun idOf(key: String): String = key.substringBeforeLast('@')

    /** Active renewals from today to [leadDays] days ahead, soonest first. */
    fun due(subs: List<Subscription>, today: LocalDate, leadDays: Int): List<DueReminder> {
        val lead = clampLeadDays(leadDays).toLong()
        return subs.asSequence()
            .filter { it.active }
            .mapNotNull { sub ->
                val date = SubscriptionCalculator.nextRenewal(sub, today) ?: return@mapNotNull null
                val days = SubscriptionCalculator.daysUntil(date, today)
                if (days in 0..lead) DueReminder(sub, date, days) else null
            }
            .sortedWith(compareBy<DueReminder> { it.date }.thenBy { it.subscription.name.lowercase() })
            .toList()
    }

    /**
     * One check. [notified] holds the keys notified earlier. A key stays only while its
     * subscription is active and still renews on that date. Every dropped key (paused, deleted,
     * date moved, or the renewal has passed) withdraws that notification. Callers cancel before
     * they post, so a new reminder for the same subscription is not withdrawn.
     */
    fun plan(subs: List<Subscription>, today: LocalDate, leadDays: Int, notified: Set<String>): ReminderPlan {
        val current = subs.asSequence()
            .filter { it.active }
            .mapNotNull { sub -> SubscriptionCalculator.nextRenewal(sub, today)?.let { key(sub.id, it) } }
            .toSet()
        val kept = notified.filterTo(HashSet()) { it in current }
        val toCancel = notified.asSequence().filterNot { it in current }.map(::idOf).toSet()
        val toNotify = due(subs, today, leadDays).filterNot { it.key in kept }
        return ReminderPlan(toNotify, toCancel, kept + toNotify.map { it.key })
    }

    /** A stable notification id for a subscription, so a newer reminder replaces an older one. */
    fun notificationId(subscriptionId: String): Int = subscriptionId.hashCode()
}
