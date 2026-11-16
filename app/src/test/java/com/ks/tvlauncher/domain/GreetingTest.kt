package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class GreetingTest {

    private fun at(hour: Int, minute: Int) = GreetingPeriod.at(LocalTime.of(hour, minute))

    @Test
    fun morningStartsAtFive() {
        assertEquals(GreetingPeriod.EVENING, at(4, 59))
        assertEquals(GreetingPeriod.MORNING, at(5, 0))
    }

    @Test
    fun afternoonStartsAtNoon() {
        assertEquals(GreetingPeriod.MORNING, at(11, 59))
        assertEquals(GreetingPeriod.AFTERNOON, at(12, 0))
    }

    @Test
    fun eveningStartsAtFivePm() {
        assertEquals(GreetingPeriod.AFTERNOON, at(16, 59))
        assertEquals(GreetingPeriod.EVENING, at(17, 0))
    }

    @Test
    fun eveningRunsThroughMidnight() {
        assertEquals(GreetingPeriod.EVENING, at(23, 59))
        assertEquals(GreetingPeriod.EVENING, at(0, 0))
    }

    @Test
    fun noSavedNameMeansGreetingOnly() {
        assertNull(Greeting.displayName(null))
        assertNull(Greeting.displayName(""))
        assertNull(Greeting.displayName("   "))
    }

    @Test
    fun savedNameIsTrimmed() {
        assertEquals("Sumit", Greeting.displayName("  Sumit "))
    }
}
