package com.ks.tvlauncher.data.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.WeatherCity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class WeatherRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val pune = WeatherCity("Pune", "Maharashtra", "India", 18.52, 73.86)

    private class FakeProvider(var result: () -> CurrentWeather) : WeatherProvider {
        var calls = 0
        override suspend fun searchCity(name: String): List<WeatherCity> = emptyList()
        override suspend fun current(city: WeatherCity): CurrentWeather {
            calls++
            return result()
        }
    }

    private fun store(job: Job): DataStore<Preferences> {
        val file = File(folder.root, "weather.preferences_pb")
        return PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
    }

    @Test
    fun noCity() = runTest {
        val job = Job()
        val repo = WeatherRepository(store(job), FakeProvider { CurrentWeather(1.0, 0) }, flowOf(null))
        assertEquals(WeatherState.NoCity, repo.state.first())
        job.cancelAndJoin()
    }

    @Test
    fun firstFetchLoadsThenReadyAndIsCached() = runTest {
        val job = Job()
        val dataStore = store(job)
        val provider = FakeProvider { CurrentWeather(27.4, 3) }
        val repo = WeatherRepository(dataStore, provider, flowOf(pune), now = { 1_000L })
        assertEquals(
            listOf(WeatherState.Loading, WeatherState.Ready(pune, CurrentWeather(27.4, 3), stale = false)),
            repo.state.take(2).toList(),
        )
        // A fresh cache is shown at once and not refetched.
        val again = WeatherRepository(dataStore, provider, flowOf(pune), now = { 2_000L })
        assertEquals(WeatherState.Ready(pune, CurrentWeather(27.4, 3), stale = false), again.state.first())
        assertEquals(1, provider.calls)
        job.cancelAndJoin()
    }

    @Test
    fun failureWithCacheIsStaleAndWithoutIsUnavailable() = runTest {
        val job = Job()
        val dataStore = store(job)
        val failing = FakeProvider { throw IOException("offline") }
        assertEquals(
            listOf(WeatherState.Loading, WeatherState.Unavailable),
            WeatherRepository(dataStore, failing, flowOf(pune)).state.take(2).toList(),
        )

        WeatherRepository(dataStore, FakeProvider { CurrentWeather(20.0, 61) }, flowOf(pune), now = { 0L })
            .state.take(2).toList()
        // Cache older than 30 minutes: shown, then refetched, which fails, so it is marked stale.
        val later = WeatherRepository.REFRESH_MS + 1
        assertEquals(
            listOf(
                WeatherState.Ready(pune, CurrentWeather(20.0, 61), stale = false),
                WeatherState.Ready(pune, CurrentWeather(20.0, 61), stale = true),
            ),
            WeatherRepository(dataStore, failing, flowOf(pune), now = { later }).state.take(2).toList(),
        )
        job.cancelAndJoin()
    }

    @Test
    fun corruptCacheIsIgnored() = runTest {
        val job = Job()
        val dataStore = store(job)
        dataStore.edit { it[stringPreferencesKey("weather_cache")] = "{not json" }
        val repo = WeatherRepository(dataStore, FakeProvider { CurrentWeather(5.0, 71) }, flowOf(pune))
        assertEquals(
            listOf(WeatherState.Loading, WeatherState.Ready(pune, CurrentWeather(5.0, 71), stale = false)),
            repo.state.take(2).toList(),
        )
        job.cancelAndJoin()
    }
}
