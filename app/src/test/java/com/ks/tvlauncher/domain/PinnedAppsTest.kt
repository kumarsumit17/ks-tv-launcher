package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinnedAppsTest {

    private val netflix = "com.netflix.ninja"
    private val youtube = "com.google.android.youtube.tv"
    private val kodi = "org.xbmc.kodi"

    @Test
    fun defaultsAreTheInstalledOnesInSpecOrder() {
        val installed = setOf(kodi, "com.example.other", youtube, netflix)
        assertEquals(listOf(netflix, youtube, kodi), PinnedApps.defaults(installed))
    }

    @Test
    fun defaultsPreferTheTvEditionAndFallBackToThePhoneOne() {
        assertEquals(
            listOf("com.spotify.tv.android"),
            PinnedApps.defaults(setOf("com.spotify.tv.android", "com.spotify.music")),
        )
        assertEquals(listOf("com.spotify.music"), PinnedApps.defaults(setOf("com.spotify.music")))
    }

    @Test
    fun defaultsAreEmptyWhenNoneAreInstalled() {
        assertEquals(emptyList<String>(), PinnedApps.defaults(setOf("com.example.other")))
    }

    @Test
    fun pruneDropsUninstalledPackagesAndKeepsOrder() {
        assertEquals(listOf(netflix, kodi), PinnedApps.prune(listOf(netflix, youtube, kodi), setOf(kodi, netflix)))
    }

    @Test
    fun pinAppendsAndIgnoresDuplicates() {
        assertEquals(listOf(netflix, kodi), PinnedApps.pin(listOf(netflix), kodi))
        assertEquals(listOf(netflix, kodi), PinnedApps.pin(listOf(netflix, kodi), netflix))
    }

    @Test
    fun unpinRemovesOnlyThatPackage() {
        assertEquals(listOf(netflix, kodi), PinnedApps.unpin(listOf(netflix, youtube, kodi), youtube))
        assertEquals(listOf(netflix), PinnedApps.unpin(listOf(netflix), "not.pinned"))
    }

    @Test
    fun moveShiftsOnePlaceLeftOrRight() {
        val pinned = listOf(netflix, youtube, kodi)
        assertEquals(listOf(youtube, netflix, kodi), PinnedApps.move(pinned, youtube, -1))
        assertEquals(listOf(netflix, kodi, youtube), PinnedApps.move(pinned, youtube, 1))
    }

    @Test
    fun moveStopsAtTheEnds() {
        val pinned = listOf(netflix, youtube, kodi)
        assertEquals(pinned, PinnedApps.move(pinned, netflix, -1))
        assertEquals(pinned, PinnedApps.move(pinned, kodi, 1))
        assertEquals(pinned, PinnedApps.move(pinned, "not.pinned", 1))
    }

    @Test
    fun canMoveMatchesTheEnds() {
        val pinned = listOf(netflix, youtube, kodi)
        assertFalse(PinnedApps.canMove(pinned, netflix, -1))
        assertTrue(PinnedApps.canMove(pinned, netflix, 1))
        assertTrue(PinnedApps.canMove(pinned, kodi, -1))
        assertFalse(PinnedApps.canMove(pinned, kodi, 1))
        assertFalse(PinnedApps.canMove(pinned, "not.pinned", 1))
    }

    @Test
    fun normalizeDropsBlanksAndDuplicates() {
        assertEquals(listOf(netflix, kodi), PinnedApps.normalize(listOf(netflix, " ", kodi, netflix, "")))
    }
}
