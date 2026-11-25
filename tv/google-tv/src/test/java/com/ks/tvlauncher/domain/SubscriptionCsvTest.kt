package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Locks the CSV contract with ks-expense-manager. */
class SubscriptionCsvTest {

    private val today = LocalDate.of(2026, 11, 16)

    @Test fun headerOnlyWhenEmpty() {
        assertEquals("name,amount_minor,currency,cycle,next_due\r\n", SubscriptionCsv.export(emptyList(), "INR", today))
    }

    @Test fun rowsUseMinorUnitsCrlfAndRollForward() {
        val subs = listOf(
            Subscription("1", "Netflix", "Standard plan", 49_900, BillingCycle.MONTHLY, "2026-10-19", active = true),
            Subscription("2", "Prime Video", "Amazon", 149_900, BillingCycle.YEARLY, "2027-03-02", active = true),
            Subscription("3", "Spotify", "", 11_900, BillingCycle.MONTHLY, "2026-11-22", active = false),
        )
        val expected = "name,amount_minor,currency,cycle,next_due\r\n" +
            "Netflix,49900,INR,monthly,2026-11-19\r\n" +
            "Prime Video,149900,INR,yearly,2027-03-02\r\n" +
            "Spotify,11900,INR,monthly,2026-11-22\r\n"
        assertEquals(expected, SubscriptionCsv.export(subs, "inr", today))
    }

    @Test fun quotesFieldsPerRfc4180() {
        assertEquals("plain", SubscriptionCsv.quote("plain"))
        assertEquals("\"a,b\"", SubscriptionCsv.quote("a,b"))
        assertEquals("\"say \"\"hi\"\"\"", SubscriptionCsv.quote("say \"hi\""))
        assertEquals("\"line\nbreak\"", SubscriptionCsv.quote("line\nbreak"))
        assertEquals("\"cr\rhere\"", SubscriptionCsv.quote("cr\rhere"))
        val csv = SubscriptionCsv.export(
            listOf(Subscription("1", "Disney+, Hotstar", "", 29_900, BillingCycle.MONTHLY, "2026-11-28", true)),
            "INR",
            today,
        )
        assertEquals("\"Disney+, Hotstar\",29900,INR,monthly,2026-11-28", csv.split("\r\n")[1])
    }
}
