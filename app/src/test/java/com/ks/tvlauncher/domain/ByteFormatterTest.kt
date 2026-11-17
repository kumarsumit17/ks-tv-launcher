package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ByteFormatterTest {
    private val en = Locale.US

    @Test
    fun decimalGigabytesWithOneDecimal() {
        assertEquals("1.3 GB", ByteFormatter.format(1_300_000_000L, en))
        assertEquals("2.0 GB", ByteFormatter.format(2_000_000_000L, en))
        assertEquals("1.0 GB", ByteFormatter.format(1_000_000_000L, en))
    }

    @Test
    fun megabytesBelowOneGigabyte() {
        assertEquals("999.9 MB", ByteFormatter.format(999_900_000L, en))
        assertEquals("512.0 MB", ByteFormatter.format(512_000_000L, en))
        assertEquals("0.0 MB", ByteFormatter.format(0L, en))
    }

    @Test
    fun usedOfTotalUsesTheTotalsUnit() {
        assertEquals("1.3 / 2.0 GB", ByteFormatter.usedOfTotal(1_300_000_000L, 2_000_000_000L, en))
        assertEquals("0.5 / 8.0 GB", ByteFormatter.usedOfTotal(500_000_000L, 8_000_000_000L, en))
        assertEquals("300.0 / 900.0 MB", ByteFormatter.usedOfTotal(300_000_000L, 900_000_000L, en))
    }

    @Test
    fun decimalSeparatorFollowsLocale() {
        assertEquals("1,3 GB", ByteFormatter.format(1_300_000_000L, Locale.GERMANY))
    }

    @Test
    fun percentUsed() {
        assertEquals(65.0, ByteFormatter.percentUsed(1_300_000_000L, 2_000_000_000L), 1e-9)
        assertEquals(0.0, ByteFormatter.percentUsed(5L, 0L), 1e-9)
        assertEquals(100.0, ByteFormatter.percentUsed(15L, 10L), 1e-9)
    }

    @Test
    fun thresholdsAtExactlySeventyFiveAndNinety() {
        assertEquals(UsageLevel.NORMAL, ByteFormatter.level(74.99))
        assertEquals(UsageLevel.GETTING_FULL, ByteFormatter.level(75.0))
        assertEquals(UsageLevel.GETTING_FULL, ByteFormatter.level(ByteFormatter.percentUsed(75L, 100L)))
        assertEquals(UsageLevel.GETTING_FULL, ByteFormatter.level(90.0))
        assertEquals(UsageLevel.GETTING_FULL, ByteFormatter.level(ByteFormatter.percentUsed(9L, 10L)))
        assertEquals(UsageLevel.ALMOST_FULL, ByteFormatter.level(90.01))
        assertEquals(UsageLevel.ALMOST_FULL, ByteFormatter.level(100.0))
    }
}
