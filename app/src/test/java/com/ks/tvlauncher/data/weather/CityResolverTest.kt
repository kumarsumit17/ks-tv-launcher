package com.ks.tvlauncher.data.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

// runBlocking, not runTest: DataStore does real I/O, and runTest's virtual-time timeouts would fire first.
/** Automatic vs manual city, the 24 h re-detection, and what happens when detection fails. */
class CityResolverTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val pune = WeatherCity("Pune", "Maharashtra", "India", 18.52, 73.86)
    private val mumbai = WeatherCity("Mumbai", "Maharashtra", "India", 19.07, 72.88)
    private val delhi = WeatherCity("Delhi", "Delhi", "India", 28.61, 77.21)

    private class FakeLocator(var next: () -> WeatherCity) : CityLocator {
        var calls = 0
        override suspend fun locate(): WeatherCity {
            calls++
            return next()
        }
    }

    private val offline: () -> WeatherCity = { throw IOException("offline") }

    private fun store(job: Job): DataStore<Preferences> {
        val file = File(folder.root, "city.preferences_pb")
        return PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
    }

    private fun auto() = LauncherSettings()

    /** Waits (real time, DataStore runs on IO) for the first non-null city. */
    private suspend fun CityResolver.firstCity(): ResolvedCity = withTimeout(5_000) { city.filterNotNull().first() }

    @Test
    fun automaticByDefaultDetectsOnFirstLaunch() = runBlocking {
        val job = Job()
        val locator = FakeLocator { pune }
        val resolver = CityResolver(store(job), locator, flowOf(auto()), now = { 0L })
        assertEquals(ResolvedCity(pune, automatic = true), resolver.firstCity())
        assertEquals(1, locator.calls)
        job.cancelAndJoin()
    }

    @Test
    fun cachedCityIsReusedWithin24HoursAndRedetectedAfter() = runBlocking {
        val job = Job()
        val dataStore = store(job)
        val locator = FakeLocator { pune }
        CityResolver(dataStore, locator, flowOf(auto()), now = { 0L }).firstCity()

        locator.next = { mumbai }
        val sameDay = CityResolver(dataStore, locator, flowOf(auto()), now = { CityResolver.DETECT_INTERVAL_MS - 1 })
        assertEquals(pune, sameDay.firstCity().city)
        assertTrue(sameDay.detectAgain()) // Detect again forces a lookup even inside 24 h
        assertEquals(mumbai, withTimeout(5_000) { sameDay.city.first { it?.city == mumbai } }!!.city)
        assertEquals(2, locator.calls)

        locator.next = { delhi }
        val nextDay = CityResolver(dataStore, locator, flowOf(auto()), now = { 2 * CityResolver.DETECT_INTERVAL_MS })
        assertEquals(delhi, withTimeout(5_000) { nextDay.city.first { it?.city == delhi } }!!.city)
        assertEquals(3, locator.calls)
        job.cancelAndJoin()
    }

    @Test
    fun failedDetectionKeepsTheLastDetectedCity() = runBlocking {
        val job = Job()
        val dataStore = store(job)
        val locator = FakeLocator { pune }
        CityResolver(dataStore, locator, flowOf(auto()), now = { 0L }).firstCity()

        locator.next = offline
        val later = CityResolver(dataStore, locator, flowOf(auto()), now = { 3 * CityResolver.DETECT_INTERVAL_MS })
        assertFalse(later.detectAgain())
        assertEquals(ResolvedCity(pune, automatic = true), later.firstCity())
        job.cancelAndJoin()
    }

    @Test
    fun failedDetectionWithNothingCachedGivesNoCity() = runBlocking {
        val job = Job()
        val locator = FakeLocator(offline)
        val resolver = CityResolver(store(job), locator, flowOf(auto()))
        assertFalse(resolver.detectAgain())
        assertNull(resolver.city.first())
        job.cancelAndJoin()
    }

    @Test
    fun manualCityWinsAndAutomaticComesBack() = runBlocking {
        val job = Job()
        val locator = FakeLocator { pune }
        val settings = MutableStateFlow(auto())
        val resolver = CityResolver(store(job), locator, settings, now = { 0L })
        assertEquals(ResolvedCity(pune, automatic = true), resolver.firstCity())

        settings.value = LauncherSettings(weatherCityMode = WeatherCityMode.MANUAL, weatherCity = mumbai)
        assertEquals(ResolvedCity(mumbai, automatic = false), withTimeout(5_000) { resolver.city.first { it?.automatic == false } })

        settings.value = LauncherSettings(weatherCityMode = WeatherCityMode.AUTO, weatherCity = mumbai)
        assertEquals(ResolvedCity(pune, automatic = true), withTimeout(5_000) { resolver.city.first { it?.automatic == true } })
        // Switching back reused the fresh cache: no second lookup.
        assertEquals(1, locator.calls)
        job.cancelAndJoin()
    }

    @Test
    fun manualModeNeverCallsTheLocator() = runBlocking {
        val job = Job()
        val locator = FakeLocator { pune }
        val manual = LauncherSettings(weatherCityMode = WeatherCityMode.MANUAL, weatherCity = mumbai)
        val resolver = CityResolver(store(job), locator, flowOf(manual))
        assertEquals(ResolvedCity(mumbai, automatic = false), resolver.firstCity())
        assertEquals(0, locator.calls)
        assertTrue(resolver.detectAgain()) // explicit Detect again still works and caches
        job.cancelAndJoin()
    }
}
