package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RenewalRemindersTest {

    private val today = LocalDate.of(2026, 11, 16)

    private fun sub(id: String, renewsOn: String, active: Boolean = true, cycle: BillingCycle = BillingCycle.MONTHLY) =
        Subscription(id, id, "", 49_900, cycle, renewsOn, active)

    @Test fun clampKeepsLeadDaysInRange() {
        assertEquals(1, RenewalReminders.clampLeadDays(0))
        assertEquals(1, RenewalReminders.clampLeadDays(-5))
        assertEquals(7, RenewalReminders.clampLeadDays(7))
        assertEquals(14, RenewalReminders.clampLeadDays(15))
    }

    @Test fun dueIncludesTodayAndLeadBoundaryOnly() {
        val subs = listOf(
            sub("today", "2026-11-16"),
            sub("three", "2026-11-19"),
            sub("four", "2026-11-20"),
            sub("paused", "2026-11-17", active = false),
            sub("broken", "not-a-date"),
        )
        val due = RenewalReminders.due(subs, today, leadDays = 3)
        assertEquals(listOf("today", "three"), due.map { it.subscription.id })
        assertEquals(listOf(0L, 3L), due.map { it.daysUntil })
    }

    @Test fun duePastDateRollsForward() {
        // Renewed on 18 Oct; next is 18 Nov, two days away.
        val due = RenewalReminders.due(listOf(sub("a", "2026-10-18")), today, leadDays = 3)
        assertEquals(LocalDate.of(2026, 11, 18), due.single().date)
        assertEquals(2L, due.single().daysUntil)
    }

    @Test fun dueSortsBySoonestThenName() {
        val subs = listOf(sub("b", "2026-11-18"), sub("a", "2026-11-18"), sub("c", "2026-11-17"))
        assertEquals(listOf("c", "a", "b"), RenewalReminders.due(subs, today, 3).map { it.subscription.id })
    }

    @Test fun planNotifiesOnceThenKeeps() {
        val subs = listOf(sub("a", "2026-11-18"))
        val first = RenewalReminders.plan(subs, today, 3, emptySet())
        assertEquals(listOf("a"), first.toNotify.map { it.subscription.id })
        assertEquals(setOf("a@2026-11-18"), first.notified)

        val second = RenewalReminders.plan(subs, today.plusDays(1), 3, first.notified)
        assertTrue(second.toNotify.isEmpty())
        assertTrue(second.toCancel.isEmpty())
        assertEquals(first.notified, second.notified)
    }

    @Test fun planCancelsPausedAndDeleted() {
        val notified = setOf("a@2026-11-18", "b@2026-11-18")
        val plan = RenewalReminders.plan(listOf(sub("a", "2026-11-18", active = false)), today, 3, notified)
        assertEquals(setOf("a", "b"), plan.toCancel)
        assertTrue(plan.toNotify.isEmpty())
        assertTrue(plan.notified.isEmpty())
    }

    @Test fun planDateChangeCancelsAndRenotifiesInWindow() {
        val notified = setOf("a@2026-11-18")
        val plan = RenewalReminders.plan(listOf(sub("a", "2026-11-17")), today, 3, notified)
        assertEquals(setOf("a"), plan.toCancel)
        assertEquals(LocalDate.of(2026, 11, 17), plan.toNotify.single().date)
        assertEquals(setOf("a@2026-11-17"), plan.notified)
    }

    @Test fun planDateMovedOutOfWindowOnlyCancels() {
        val plan = RenewalReminders.plan(listOf(sub("a", "2026-12-01")), today, 3, setOf("a@2026-11-18"))
        assertEquals(setOf("a"), plan.toCancel)
        assertTrue(plan.toNotify.isEmpty())
        assertTrue(plan.notified.isEmpty())
    }

    @Test fun planAfterRenewalPassedDropsOldKey() {
        // Notified for 18 Nov; on 19 Nov the next renewal is 18 Dec, outside the window.
        val plan = RenewalReminders.plan(listOf(sub("a", "2026-11-18")), LocalDate.of(2026, 11, 19), 3, setOf("a@2026-11-18"))
        assertEquals(setOf("a"), plan.toCancel)
        assertTrue(plan.notified.isEmpty())
    }

    @Test fun keyAndNotificationIdAreStable() {
        assertEquals("x-1@2026-11-18", RenewalReminders.key("x-1", LocalDate.of(2026, 11, 18)))
        assertEquals(RenewalReminders.notificationId("x-1"), RenewalReminders.notificationId("x-1"))
        assertNotEquals(RenewalReminders.notificationId("x-1"), RenewalReminders.notificationId("x-2"))
    }
}
