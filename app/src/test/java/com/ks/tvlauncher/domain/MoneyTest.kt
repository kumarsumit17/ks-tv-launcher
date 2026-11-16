package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyTest {
    @Test fun inrUsesIndianGroupingAndHidesWholeFractions() {
        assertEquals("₹499", Money.format(49_900, "INR"))
        assertEquals("₹14,663", Money.format(1_466_300, "INR"))
        assertEquals("₹1,23,456", Money.format(12_345_600, "INR"))
        assertEquals("₹124.92", Money.format(12_492, "INR"))
        assertEquals("₹12,34,56,789.05", Money.format(123_456_789_05, "INR"))
        assertEquals("₹0", Money.format(0, "INR"))
    }

    @Test fun minorUnits() {
        assertEquals(100L, Money.minorPerMajor("INR"))
        assertEquals(1L, Money.minorPerMajor("JPY"))
        assertEquals(100L, Money.minorPerMajor("nonsense"))
    }
}
