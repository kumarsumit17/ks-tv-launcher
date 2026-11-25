package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The profile dialog's add / rename / remove rules. */
class ProfileNamesEditTest {

    @Test
    fun addTrimsAndAppends() {
        assertEquals(listOf("Sumit", "Goldi"), ProfileNames.add(listOf("Sumit"), "  Goldi "))
    }

    @Test
    fun addRejectsInvalidDuplicateAndNinth() {
        val eight = (1..8).map { "N$it" }
        assertEquals(listOf("Sumit"), ProfileNames.add(listOf("Sumit"), "   "))
        assertEquals(listOf("Sumit"), ProfileNames.add(listOf("Sumit"), "Sumit"))
        assertEquals(listOf("Sumit"), ProfileNames.add(listOf("Sumit"), "x".repeat(21)))
        assertEquals(eight, ProfileNames.add(eight, "Ninth"))
        assertFalse(ProfileNames.canAdd(eight, "Ninth"))
        assertTrue(ProfileNames.canAdd(listOf("Sumit"), "Goldi"))
        assertFalse(ProfileNames.canAdd(listOf("Sumit"), " Sumit "))
    }

    @Test
    fun renameKeepsPositionAndRejectsClashes() {
        val names = listOf("Sumit", "Goldi", "Ria")
        assertEquals(listOf("Sumit", "Gold", "Ria"), ProfileNames.rename(names, "Goldi", " Gold "))
        assertEquals(names, ProfileNames.rename(names, "Goldi", "Ria"))
        assertEquals(names, ProfileNames.rename(names, "Goldi", ""))
        assertEquals(names, ProfileNames.rename(names, "Nobody", "X"))
        assertEquals(names, ProfileNames.rename(names, "Goldi", "Goldi"))
    }

    @Test
    fun removeDropsOnlyThatName() {
        assertEquals(listOf("Sumit", "Ria"), ProfileNames.remove(listOf("Sumit", "Goldi", "Ria"), "Goldi"))
        assertEquals(listOf("Sumit"), ProfileNames.remove(listOf("Sumit"), "Nobody"))
    }
}
