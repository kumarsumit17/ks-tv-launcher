package com.ks.tvlauncher.data.apps

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Round-trips through a real DataStore file in a temp folder. */
class PinnedAppsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val netflix = "com.netflix.ninja"
    private val youtube = "com.google.android.youtube.tv"
    private val kodi = "org.xbmc.kodi"

    private class Store(val dataStore: DataStore<Preferences>, private val job: Job) {
        /** DataStore allows one live instance per file, so close this one before reopening. */
        suspend fun close() = job.cancelAndJoin()
    }

    private fun open(file: File): Store {
        val job = Job()
        return Store(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }, job)
    }

    private fun file() = File(folder.root, "pins.preferences_pb")

    @Test
    fun pinnedIsNullBeforeFirstRun() = runTest {
        val store = open(file())
        assertNull(PinnedAppsRepository(store.dataStore).pinned.first())
        store.close()
    }

    @Test
    fun firstRunWritesTheInstalledDefaultsOnce() = runTest {
        val store = open(file())
        val repository = PinnedAppsRepository(store.dataStore)

        repository.initializeIfNeeded(setOf(kodi, netflix, "com.example.other"))
        assertEquals(listOf(netflix, kodi), repository.pinned.first())

        // A later call must not bring back defaults the user has since removed.
        repository.unpin(netflix)
        repository.initializeIfNeeded(setOf(kodi, netflix, youtube))
        assertEquals(listOf(kodi), repository.pinned.first())
        store.close()
    }

    @Test
    fun unpinningEverythingStaysEmptyRatherThanReturningToDefaults() = runTest {
        val store = open(file())
        val repository = PinnedAppsRepository(store.dataStore)
        repository.initializeIfNeeded(setOf(netflix))
        repository.unpin(netflix)
        repository.initializeIfNeeded(setOf(netflix))
        assertEquals(emptyList<String>(), repository.pinned.first())
        store.close()
    }

    @Test
    fun orderSurvivesReopeningTheFile() = runTest {
        val file = file()
        val first = open(file)
        PinnedAppsRepository(first.dataStore).apply {
            initializeIfNeeded(emptySet())
            pin(netflix)
            pin(youtube)
            pin(kodi)
            move(kodi, -2)
        }
        first.close()

        val second = open(file)
        assertEquals(listOf(kodi, netflix, youtube), PinnedAppsRepository(second.dataStore).pinned.first())
        second.close()
    }

    @Test
    fun pruneDropsUninstalledPackagesAndPersistsIt() = runTest {
        val file = file()
        val first = open(file)
        PinnedAppsRepository(first.dataStore).apply {
            initializeIfNeeded(emptySet())
            pin(netflix)
            pin(youtube)
            pin(kodi)
            prune(setOf(netflix, kodi))
            assertEquals(listOf(netflix, kodi), pinned.first())
        }
        first.close()

        val second = open(file)
        assertEquals(listOf(netflix, kodi), PinnedAppsRepository(second.dataStore).pinned.first())
        second.close()
    }

    @Test
    fun pinAndMoveKeepTheListFreeOfDuplicates() = runTest {
        val store = open(file())
        val repository = PinnedAppsRepository(store.dataStore)
        repository.initializeIfNeeded(emptySet())
        repository.pin(netflix)
        repository.pin(netflix)
        repository.pin(kodi)
        repository.move(netflix, 1)
        assertEquals(listOf(kodi, netflix), repository.pinned.first())
        store.close()
    }

    @Test
    fun unreadableStoredValueReadsAsNothingPinnedInsteadOfCrashing() = runTest {
        val store = open(file())
        store.dataStore.edit { it[stringPreferencesKey("pinned_apps")] = "{not json" }
        val repository = PinnedAppsRepository(store.dataStore)
        assertEquals(emptyList<String>(), repository.pinned.first())

        // And the list can be used normally afterwards.
        repository.pin(kodi)
        assertEquals(listOf(kodi), repository.pinned.first())
        store.close()
    }
}
