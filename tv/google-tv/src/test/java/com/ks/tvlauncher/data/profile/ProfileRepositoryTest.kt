package com.ks.tvlauncher.data.profile

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ProfileRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun saveCleansNamesAndMarksSetupDone() = runTest {
        val job = Job()
        val file = File(folder.root, "profile.preferences_pb")
        val repo = ProfileRepository(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file })

        assertFalse(repo.profile.first().setupDone)
        repo.save(listOf(" Sumit ", "Goldi", "Sumit", ""), "Goldi", useOwnTvName = true, ownTvName = "  Living Room  ")
        val saved = repo.profile.first()
        assertEquals(listOf("Sumit", "Goldi"), saved.names)
        assertEquals("Goldi", saved.activeName)
        assertTrue(saved.useOwnTvName)
        assertEquals("Living Room", saved.ownTvName)
        assertTrue(saved.setupDone)

        repo.switchToNext()
        assertEquals("Sumit", repo.profile.first().activeName)

        // An active name that is not in the list is dropped.
        repo.save(listOf("Ria"), "Goldi", useOwnTvName = false, ownTvName = "")
        assertEquals(null, repo.profile.first().activeName)
        job.cancelAndJoin()
    }

    @Test
    fun dismissingFirstRunMarksSetupDone() = runTest {
        val job = Job()
        val file = File(folder.root, "profile2.preferences_pb")
        val repo = ProfileRepository(PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file })
        repo.markSetupDone()
        assertTrue(repo.profile.first().setupDone)
        assertTrue(repo.profile.first().names.isEmpty())
        job.cancelAndJoin()
    }
}
