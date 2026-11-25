package com.ks.tvlauncher.data.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/** The city weather uses, and whether it was detected ([automatic]) or picked by the user. */
data class ResolvedCity(val city: WeatherCity, val automatic: Boolean)

/**
 * Chooses the weather city. AUTO (default): the city detected from the network, cached in
 * DataStore and re-detected at most once every 24 h (or on "Detect again"); if detection
 * fails the last detected city is kept, and with none the result is null ("Set city").
 * MANUAL: the city the user picked in Settings.
 */
class CityResolver(
    private val store: DataStore<Preferences>,
    private val locator: CityLocator,
    private val settings: Flow<LauncherSettings>,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val detectLock = Mutex()

    private val detected: Flow<DetectedCity?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { decode(it[DETECTED]) }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val city: Flow<ResolvedCity?> = settings
        .map { it.weatherCityMode to it.weatherCity }
        .distinctUntilChanged()
        .flatMapLatest { (mode, manual) ->
            when (mode) {
                WeatherCityMode.MANUAL -> flowOf(manual?.let { ResolvedCity(it, automatic = false) })
                WeatherCityMode.AUTO -> automatic()
            }
        }
        .distinctUntilChanged()

    /** Forces a new detection. False if it failed (the last detected city is kept). */
    suspend fun detectAgain(): Boolean = detect(force = true)

    private fun automatic(): Flow<ResolvedCity?> = channelFlow {
        // Detection runs beside the cache, so a cached city shows at once.
        launch { detect(force = false) }
        detected.collect { send(it?.let { d -> ResolvedCity(d.city, automatic = true) }) }
    }

    private suspend fun detect(force: Boolean): Boolean = detectLock.withLock {
        val cached = detected.first()
        if (!force && cached != null && now() - cached.detectedAtMillis < DETECT_INTERVAL_MS) return true
        return try {
            val city = locator.locate()
            store.edit { it[DETECTED] = Json.encodeToString(DetectedCity.serializer(), DetectedCity(city, now())) }
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun decode(raw: String?): DetectedCity? {
        if (raw.isNullOrEmpty()) return null
        return try {
            Json.decodeFromString(DetectedCity.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @Serializable
    private data class DetectedCity(val city: WeatherCity, val detectedAtMillis: Long)

    companion object {
        const val DETECT_INTERVAL_MS = 24 * 60 * 60 * 1000L
        private val DETECTED = stringPreferencesKey("weather_detected_city")
    }
}
