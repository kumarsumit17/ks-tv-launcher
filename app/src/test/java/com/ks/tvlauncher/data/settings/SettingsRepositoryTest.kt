package com.ks.tvlauncher.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun stageFourSettingsRoundTrip() = runTest {
        val job = Job()
        val file = File(folder.root, "settings.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
        val repo = SettingsRepository(store)
        val pune = WeatherCity("Pune", "Maharashtra", "India", 18.52, 73.86)

        val defaults = repo.settings.first()
        assertEquals(true, defaults.showMemoryCard)
        assertNull(defaults.weatherCity)
        assertEquals(WeatherCityMode.AUTO, defaults.weatherCityMode)
        assertEquals(TemperatureUnit.CELSIUS, defaults.temperatureUnit)

        repo.setShowMemoryCard(false)
        repo.setManualCity(pune)
        repo.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        repo.setGlassIntensity(GlassIntensity.LOW)
        repo.setCurrency("usd")
        val saved = repo.settings.first()
        assertFalse(saved.showMemoryCard)
        assertEquals(pune, saved.weatherCity)
        assertEquals(WeatherCityMode.MANUAL, saved.weatherCityMode)
        assertEquals(TemperatureUnit.FAHRENHEIT, saved.temperatureUnit)
        assertEquals(GlassIntensity.LOW, saved.glassIntensity)
        assertEquals("USD", saved.currency)

        // Back to automatic keeps the manual city for next time.
        repo.setWeatherCityMode(WeatherCityMode.AUTO)
        assertEquals(WeatherCityMode.AUTO, repo.settings.first().weatherCityMode)
        assertEquals(pune, repo.settings.first().weatherCity)

        // A damaged city reads as "no city" instead of failing.
        store.edit { it[stringPreferencesKey("weather_city")] = "{broken" }
        assertNull(repo.settings.first().weatherCity)
        job.cancelAndJoin()
    }
}
