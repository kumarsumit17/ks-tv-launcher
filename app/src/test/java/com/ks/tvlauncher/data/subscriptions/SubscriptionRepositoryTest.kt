package com.ks.tvlauncher.data.subscriptions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ks.tvlauncher.domain.BillingCycle
import com.ks.tvlauncher.domain.Subscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** Round-trips through a real DataStore file in a temp folder. */
class SubscriptionRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private class Store(val dataStore: DataStore<Preferences>, private val job: Job) {
        suspend fun close() = job.cancelAndJoin()
    }

    private fun open(): Store {
        val job = Job()
        val file = File(folder.root, "subs.preferences_pb")
        return Store(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }, job)
    }

    private val logged = mutableListOf<String>()
    private fun repo(store: Store) = SubscriptionDataStore(store.dataStore) { msg, _ -> logged += msg }

    private val netflix = Subscription("n", "Netflix", "Standard", 49_900, BillingCycle.MONTHLY, "2026-11-19", true, "family")
    private val prime = Subscription("p", "Prime Video", "Amazon", 149_900, BillingCycle.YEARLY, "2027-03-02", true)

    @Test
    fun emptyByDefault() = runTest {
        val store = open()
        assertEquals(emptyList<Subscription>(), SubscriptionRepository(repo(store)).subscriptions.first())
        store.close()
    }

    @Test
    fun upsertDeleteToggleSurviveReopen() = runTest {
        var store = open()
        var repository = SubscriptionRepository(repo(store))
        repository.upsert(netflix)
        repository.upsert(prime)
        repository.upsert(netflix.copy(costMinor = 64_900))
        repository.toggleActive("p")
        store.close()

        store = open()
        repository = SubscriptionRepository(repo(store))
        assertEquals(listOf(netflix.copy(costMinor = 64_900), prime.copy(active = false)), repository.subscriptions.first())
        repository.delete("n")
        assertEquals(listOf(prime.copy(active = false)), repository.subscriptions.first())
        store.close()
    }

    @Test
    fun corruptJsonReadsEmptyKeepsBackupAndNeverOverwritesIt() = runTest {
        val store = open()
        store.dataStore.edit { it[SubscriptionDataStore.KEY] = "{not json" }
        val dataStore = repo(store)
        val repository = SubscriptionRepository(dataStore)

        assertEquals(emptyList<Subscription>(), repository.subscriptions.first())
        assertTrue(logged.isNotEmpty())
        assertNull(dataStore.backup.first())

        // The first write keeps the raw text before replacing it.
        repository.upsert(netflix)
        assertEquals("{not json", dataStore.backup.first())
        assertEquals(listOf(netflix), repository.subscriptions.first())

        // A second corruption never replaces the first backup.
        store.dataStore.edit { it[SubscriptionDataStore.KEY] = "[broken again" }
        repository.upsert(prime)
        assertEquals("{not json", dataStore.backup.first())
        assertEquals(listOf(prime), repository.subscriptions.first())
        store.close()
    }
}
