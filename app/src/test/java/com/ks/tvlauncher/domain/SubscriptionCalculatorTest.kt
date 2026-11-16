package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class SubscriptionCalculatorTest {

    private val today = LocalDate.of(2026, 11, 16)

    private fun sub(
        id: String,
        cost: Long,
        cycle: BillingCycle = BillingCycle.MONTHLY,
        renewsOn: String = "2026-11-19",
        active: Boolean = true,
    ) = Subscription(id, id, "", cost, cycle, renewsOn, active)

    @Test fun monthlyEquivalentOfYearlyRoundsHalfUp() {
        // 149900 / 12 = 12491.67 -> 12492
        assertEquals(12492L, SubscriptionCalculator.monthlyEquivalent(sub("a", 149_900, BillingCycle.YEARLY)))
        // 18 / 12 = 1.5 -> 2 (half-up)
        assertEquals(2L, SubscriptionCalculator.monthlyEquivalent(sub("b", 18, BillingCycle.YEARLY)))
        // 17 / 12 = 1.42 -> 1
        assertEquals(1L, SubscriptionCalculator.monthlyEquivalent(sub("c", 17, BillingCycle.YEARLY)))
        assertEquals(49_900L, SubscriptionCalculator.monthlyEquivalent(sub("d", 49_900)))
    }

    @Test fun yearlyEquivalent() {
        assertEquals(598_800L, SubscriptionCalculator.yearlyEquivalent(sub("a", 49_900)))
        assertEquals(149_900L, SubscriptionCalculator.yearlyEquivalent(sub("b", 149_900, BillingCycle.YEARLY)))
    }

    @Test fun totalsCountActiveOnly() {
        val subs = listOf(
            sub("netflix", 49_900),
            sub("prime", 149_900, BillingCycle.YEARLY),
            sub("hotstar", 29_900),
            sub("youtube", 29_900),
            sub("spotify", 11_900, active = false),
        )
        assertEquals(4, SubscriptionCalculator.activeCount(subs))
        assertEquals(49_900L + 12_492 + 29_900 + 29_900, SubscriptionCalculator.monthlyTotal(subs))
        assertEquals(598_800L + 149_900 + 358_800 + 358_800, SubscriptionCalculator.yearlyTotal(subs))
    }

    @Test fun renewalTodayOrLaterIsUnchanged() {
        assertEquals(today, SubscriptionCalculator.nextRenewal(sub("a", 1, renewsOn = "2026-11-16"), today))
        assertEquals(LocalDate.of(2027, 3, 2), SubscriptionCalculator.nextRenewal(sub("a", 1, BillingCycle.YEARLY, "2027-03-02"), today))
    }

    @Test fun pastRenewalRollsForwardByCycle() {
        assertEquals(LocalDate.of(2026, 11, 19), SubscriptionCalculator.nextRenewal(sub("a", 1, renewsOn = "2026-08-19"), today))
        assertEquals(LocalDate.of(2026, 12, 10), SubscriptionCalculator.nextRenewal(sub("a", 1, renewsOn = "2026-10-10"), today))
        assertEquals(LocalDate.of(2027, 3, 2), SubscriptionCalculator.nextRenewal(sub("a", 1, BillingCycle.YEARLY, "2024-03-02"), today))
        assertEquals(LocalDate.of(2026, 11, 16), SubscriptionCalculator.nextRenewal(sub("a", 1, BillingCycle.YEARLY, "2025-11-16"), today))
    }

    @Test fun monthEndDoesNotDrift() {
        val d = LocalDate.of(2026, 3, 1)
        // Jan 31 -> Feb 28 -> Mar 31, not Mar 28.
        assertEquals(LocalDate.of(2026, 3, 31), SubscriptionCalculator.nextRenewal(sub("a", 1, renewsOn = "2026-01-31"), d))
        // Leap day yearly: 2028-02-29 seen from 2029 -> 2029-02-28.
        assertEquals(LocalDate.of(2029, 2, 28), SubscriptionCalculator.nextRenewal(sub("a", 1, BillingCycle.YEARLY, "2028-02-29"), LocalDate.of(2029, 1, 1)))
    }

    @Test fun invalidDateGivesNull() {
        assertNull(SubscriptionCalculator.nextRenewal(sub("a", 1, renewsOn = "not a date"), today))
    }

    @Test fun daysUntil() {
        assertEquals(3L, SubscriptionCalculator.daysUntil(LocalDate.of(2026, 11, 19), today))
        assertEquals(0L, SubscriptionCalculator.daysUntil(today, today))
    }

    @Test fun nextActiveRenewalSkipsPaused() {
        val subs = listOf(
            sub("spotify", 1, renewsOn = "2026-11-17", active = false),
            sub("netflix", 1, renewsOn = "2026-11-19"),
            sub("hotstar", 1, renewsOn = "2026-11-28"),
        )
        val next = SubscriptionCalculator.nextActiveRenewal(subs, today)!!
        assertEquals("netflix", next.subscription.id)
        assertEquals(3L, next.daysUntil)
        assertNull(SubscriptionCalculator.nextActiveRenewal(subs.map { it.copy(active = false) }, today))
    }
}
