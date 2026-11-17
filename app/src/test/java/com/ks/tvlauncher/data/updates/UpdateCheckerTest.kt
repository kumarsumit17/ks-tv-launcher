package com.ks.tvlauncher.data.updates

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ks.tvlauncher.domain.ReleaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class UpdateCheckerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private fun release(code: Int) = ReleaseInfo("0.${code / 100 % 100}.${code % 100}", code, "notes", "https://example.invalid/a.apk", 10)

    private class FakeSource : UpdateSource {
        var answer: () -> ReleaseInfo = { throw IOException("offline") }
        var calls = 0
        override suspend fun latest(): ReleaseInfo {
            calls++
            return answer()
        }
    }

    private fun withChecker(block: suspend (UpdateChecker, UpdateStore, FakeSource, MutableList<ReleaseInfo>, LongArray) -> Unit) = runTest {
        val job = Job()
        val store = UpdateStore(
            PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(folder.root, "u.preferences_pb") },
        )
        val source = FakeSource()
        val posted = mutableListOf<ReleaseInfo>()
        val day = longArrayOf(20_000)
        val checker = UpdateChecker(source, store, installedCode = 601, today = { day[0] }, notify = { posted += it; true })
        block(checker, store, source, posted, day)
        job.cancelAndJoin()
    }

    @Test
    fun newerReleaseIsStoredAndNotifiedOnce() = withChecker { checker, store, source, posted, _ ->
        source.answer = { release(602) }
        assertEquals(CheckResult.Available(release(602)), checker.check(automatic = true))
        assertEquals(release(602), store.available.first())
        checker.check(automatic = true)
        assertEquals(1, posted.size)
    }

    @Test
    fun manualCheckDoesNotNotify() = withChecker { checker, _, source, posted, _ ->
        source.answer = { release(700) }
        checker.check(automatic = false)
        assertTrue(posted.isEmpty())
    }

    @Test
    fun sameOrLowerVersionIsUpToDateAndClearsPending() = withChecker { checker, store, source, _, _ ->
        store.setAvailable(release(602))
        source.answer = { release(601) }
        assertEquals(CheckResult.UpToDate, checker.check(automatic = true))
        assertNull(store.available.first())
        source.answer = { release(500) }
        assertEquals(CheckResult.UpToDate, checker.check(automatic = false))
    }

    @Test
    fun rateLimitWaitsUntilTheNextDay() = withChecker { checker, _, source, _, day ->
        source.answer = { throw RateLimitedException(429) }
        assertEquals(CheckResult.Waiting, checker.check(automatic = true))
        source.answer = { release(602) }
        assertEquals(CheckResult.Waiting, checker.check(automatic = false))
        assertEquals(1, source.calls)
        day[0] += 1
        assertEquals(CheckResult.Available(release(602)), checker.check(automatic = true))
    }

    @Test
    fun networkFailureKeepsPendingRelease() = withChecker { checker, store, _, _, _ ->
        store.setAvailable(release(602))
        assertEquals(CheckResult.Failed, checker.check(automatic = true))
        assertEquals(release(602), store.available.first())
    }

    @Test
    fun blockedNotificationIsTriedAgain() = runTest {
        val job = Job()
        val store = UpdateStore(
            PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(folder.root, "b.preferences_pb") },
        )
        var allowed = false
        var posts = 0
        val source = FakeSource().apply { answer = { release(602) } }
        val checker = UpdateChecker(source, store, 601, { 1L }, notify = { if (allowed) posts++; allowed })
        checker.check(automatic = true)
        assertEquals(0, store.notifiedCode())
        allowed = true
        checker.check(automatic = true)
        checker.check(automatic = true)
        assertEquals(1, posts)
        job.cancelAndJoin()
    }

    @Test
    fun installedReleaseIsForgotten() = withChecker { checker, store, _, _, _ ->
        store.setAvailable(release(601))
        assertTrue(checker.clearIfInstalled(store.available.first()))
        assertNull(store.available.first())
        store.setAvailable(release(602))
        assertFalse(checker.clearIfInstalled(store.available.first()))
    }
}
