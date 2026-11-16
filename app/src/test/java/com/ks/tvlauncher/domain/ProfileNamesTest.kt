package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileNamesTest {

    @Test
    fun namesAreTrimmed() {
        assertEquals("Goldi", ProfileNames.sanitize("  Goldi  "))
    }

    @Test
    fun emptyAndBlankNamesAreRejected() {
        assertNull(ProfileNames.sanitize(""))
        assertNull(ProfileNames.sanitize("   "))
    }

    @Test
    fun lengthLimitIsTwentyCharacters() {
        assertEquals("a".repeat(20), ProfileNames.sanitize("a".repeat(20)))
        assertNull(ProfileNames.sanitize("a".repeat(21)))
    }

    @Test
    fun listKeepsOrderDropsDuplicatesAndInvalidNames() {
        assertEquals(
            listOf("Sumit", "Goldi"),
            ProfileNames.normalize(listOf(" Sumit", "", "Goldi", "Sumit", "x".repeat(30))),
        )
    }

    @Test
    fun listIsCappedAtEightNames() {
        val ten = (1..10).map { "Name $it" }
        assertEquals(ten.take(8), ProfileNames.normalize(ten))
    }

    @Test
    fun nextMovesForwardAndWrapsRound() {
        val names = listOf("Sumit", "Goldi", "Asha")
        assertEquals("Goldi", ProfileNames.next(names, "Sumit"))
        assertEquals("Asha", ProfileNames.next(names, "Goldi"))
        assertEquals("Sumit", ProfileNames.next(names, "Asha"))
    }

    @Test
    fun nextFallsBackToFirstNameWhenActiveIsUnknown() {
        assertEquals("Sumit", ProfileNames.next(listOf("Sumit", "Goldi"), null))
        assertEquals("Sumit", ProfileNames.next(listOf("Sumit", "Goldi"), "Someone else"))
    }

    @Test
    fun noSwitchWithFewerThanTwoNames() {
        assertNull(ProfileNames.next(emptyList(), null))
        assertNull(ProfileNames.next(listOf("Sumit"), "Sumit"))
    }
}
