package com.ks.tvlauncher.data.updates

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.ks.tvlauncher.data.apps.PinnedAppsRepository
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.reminders.ReminderStore
import com.ks.tvlauncher.data.settings.GlassIntensity
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.subscriptions.SubscriptionDataStore
import com.ks.tvlauncher.data.weather.CityLocator
import com.ks.tvlauncher.data.weather.CityResolver
import com.ks.tvlauncher.domain.BillingCycle
import com.ks.tvlauncher.domain.Subscription
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.WallpaperImage
import com.ks.tvlauncher.domain.WallpaperMode
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

/**
 * In-app updates keep data only if the new release reads what the previous one wrote
 * (specs/app-updates.md). Loads `stored/ks_launcher-v0.6.1.json` into a DataStore and
 * reads it back through every repository of this build.
 */
class StoredDataCompatibilityTest {

    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun v061DataReadsUnchanged() = runTest {
        val job = Job()
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) {
            File(folder.root, "ks_launcher.preferences_pb")
        }
        val fixture = Json.parseToJsonElement(
            requireNotNull(javaClass.getResource("/stored/ks_launcher-v0.6.1.json")).readText(),
        ).jsonObject
        store.edit { prefs ->
            fixture.getValue("booleans").jsonObject.forEach { (k, v) -> prefs[booleanPreferencesKey(k)] = v.jsonPrimitive.boolean }
            fixture.getValue("ints").jsonObject.forEach { (k, v) -> prefs[intPreferencesKey(k)] = v.jsonPrimitive.int }
            fixture.getValue("strings").jsonObject.forEach { (k, v) -> prefs[stringPreferencesKey(k)] = v.jsonPrimitive.content }
            fixture.getValue("stringSets").jsonObject.forEach { (k, v) ->
                prefs[stringSetPreferencesKey(k)] = v.jsonArray.map { it.jsonPrimitive.content }.toSet()
            }
        }

        val settings = SettingsRepository(store).settings.first()
        assertEquals(true, settings.use24Hour)
        assertFalse(settings.showDate)
        assertEquals(WallpaperMode.CUSTOM, settings.wallpaperMode)
        assertEquals(WallpaperImage.BuiltIn(3), settings.wallpaperImage)
        assertEquals(WallpaperDim.HIGH, settings.wallpaperDim)
        assertFalse(settings.wallpaperPictureFailed)
        assertTrue(settings.reduceMotion)
        assertEquals(75, settings.animationSpeed)
        assertEquals(GlassIntensity.LOW, settings.glassIntensity)
        assertEquals("USD", settings.currency)
        assertFalse(settings.showMemoryCard)
        assertEquals(WeatherCityMode.AUTO, settings.weatherCityMode)
        assertEquals(WeatherCity("Pune", "Maharashtra", "India", 18.52, 73.86), settings.weatherCity)
        assertEquals(TemperatureUnit.FAHRENHEIT, settings.temperatureUnit)
        assertTrue(settings.remindersEnabled)
        assertEquals(5, settings.reminderLeadDays)
        // New in 6c, absent from v0.6.1 data: its default.
        assertTrue(settings.autoUpdateCheck)

        val profile = ProfileRepository(store).profile.first()
        assertEquals(listOf("TEST Goldi", "TEST Sumi"), profile.names)
        assertEquals("TEST Sumi", profile.activeName)
        assertTrue(profile.useOwnTvName)
        assertEquals("TEST Living room", profile.ownTvName)
        assertTrue(profile.setupDone)

        assertEquals(
            listOf("com.netflix.ninja", "com.google.android.youtube.tv", "com.amazon.amazonvideo.livingroom"),
            PinnedAppsRepository(store).pinned.first(),
        )

        val subs = SubscriptionDataStore(store, log = { _, e -> throw AssertionError(e) })
        assertEquals(
            listOf(
                Subscription("a1", "TEST Netflix", "Netflix", 49900, BillingCycle.MONTHLY, "2026-11-20", active = true),
                Subscription("b2", "TEST Prime", "Amazon", 149900, BillingCycle.YEARLY, "2026-03-01", active = false, note = "family"),
            ),
            subs.subscriptions.first(),
        )
        assertEquals("[{\"id\":", subs.backup.first())

        assertEquals(setOf("a1@2026-11-20"), ReminderStore(store).notified())

        val noNetwork = object : CityLocator {
            override suspend fun locate(): WeatherCity = throw IOException("not in tests")
        }
        val resolver = CityResolver(store, noNetwork, SettingsRepository(store).settings, now = { 1_763_380_000_001L })
        val city = resolver.city.filterNotNull().first()
        assertEquals(WeatherCity("Mumbai", "Maharashtra", "India", 19.07, 72.88), city.city)
        assertTrue(city.automatic)

        // Nothing pending from an older build.
        assertNull(UpdateStore(store).available.first())
        job.cancelAndJoin()
    }
}
