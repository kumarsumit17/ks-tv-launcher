package com.ks.tvlauncher.data.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.WeatherCity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/** What the weather pill shows. */
sealed interface WeatherState {
    /** No city chosen: the pill offers "Set city". */
    data object NoCity : WeatherState

    /** First fetch for this city, nothing cached yet. */
    data object Loading : WeatherState

    /** [stale] when the last refresh failed and this is the cached reading. */
    data class Ready(val city: WeatherCity, val weather: CurrentWeather, val stale: Boolean) : WeatherState

    /** The fetch failed and there is nothing cached. */
    data object Unavailable : WeatherState
}

/**
 * Weather for the chosen city, refreshed every 30 minutes while someone collects [state]
 * (Home is visible). The last reading is cached in the shared DataStore, so reopening KS shows
 * it at once and does not refetch while it is fresh.
 */
class WeatherRepository(
    private val store: DataStore<Preferences>,
    private val provider: WeatherProvider,
    private val city: Flow<WeatherCity?>,
    private val now: () -> Long = System::currentTimeMillis,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: Flow<WeatherState> = city
        .distinctUntilChanged()
        .flatMapLatest { chosen -> if (chosen == null) flowOf(WeatherState.NoCity) else refreshing(chosen) }

    /** Up to five places for the Settings city search. */
    suspend fun searchCity(name: String): List<WeatherCity> = provider.searchCity(name)

    private fun refreshing(city: WeatherCity): Flow<WeatherState> = flow {
        var cached = readCache(city)
        if (cached != null) emit(WeatherState.Ready(city, cached.weather, stale = false)) else emit(WeatherState.Loading)
        var wait = cached?.let { (REFRESH_MS - (now() - it.fetchedAtMillis)).coerceIn(0L, REFRESH_MS) } ?: 0L
        while (true) {
            if (wait > 0) delay(wait)
            wait = try {
                val fresh = CachedWeather(city.key, provider.current(city), now())
                writeCache(fresh)
                cached = fresh
                emit(WeatherState.Ready(city, fresh.weather, stale = false))
                REFRESH_MS
            } catch (e: IOException) {
                emit(cached?.let { WeatherState.Ready(city, it.weather, stale = true) } ?: WeatherState.Unavailable)
                RETRY_MS
            }
        }
    }

    private suspend fun readCache(city: WeatherCity): CachedWeather? {
        val raw = store.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { it[CACHE] }
            .first() ?: return null
        val cached = try {
            Json.decodeFromString(CachedWeather.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
        return cached?.takeIf { it.cityKey == city.key }
    }

    private suspend fun writeCache(value: CachedWeather) {
        try {
            store.edit { it[CACHE] = Json.encodeToString(CachedWeather.serializer(), value) }
        } catch (e: IOException) {
            // The reading is still shown; it just will not survive a restart.
        }
    }

    @Serializable
    private data class CachedWeather(val cityKey: String, val weather: CurrentWeather, val fetchedAtMillis: Long)

    companion object {
        const val REFRESH_MS = 30 * 60 * 1000L

        /** After a failed fetch, try again sooner than the normal refresh. */
        const val RETRY_MS = 5 * 60 * 1000L

        private val CACHE = stringPreferencesKey("weather_cache")
    }
}
