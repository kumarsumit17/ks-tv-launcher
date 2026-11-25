package com.ks.tvlauncher.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
        assertFalse(defaults.remindersEnabled)
        assertEquals(3, defaults.reminderLeadDays)

        repo.setShowMemoryCard(false)
        repo.setManualCity(pune)
        repo.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        repo.setGlassIntensity(GlassIntensity.LOW)
        repo.setCurrency("usd")
        repo.setRemindersEnabled(true)
        repo.setReminderLeadDays(30)
        val saved = repo.settings.first()
        assertFalse(saved.showMemoryCard)
        assertEquals(pune, saved.weatherCity)
        assertEquals(WeatherCityMode.MANUAL, saved.weatherCityMode)
        assertEquals(TemperatureUnit.FAHRENHEIT, saved.temperatureUnit)
        assertEquals(GlassIntensity.LOW, saved.glassIntensity)
        assertEquals("USD", saved.currency)
        assertEquals(true, saved.remindersEnabled)
        // Clamped to the 14-day maximum.
        assertEquals(14, saved.reminderLeadDays)

        // Back to automatic keeps the manual city for next time.
        repo.setWeatherCityMode(WeatherCityMode.AUTO)
        assertEquals(WeatherCityMode.AUTO, repo.settings.first().weatherCityMode)
        assertEquals(pune, repo.settings.first().weatherCity)

        // A damaged city reads as "no city" instead of failing.
        store.edit { it[stringPreferencesKey("weather_city")] = "{broken" }
        assertNull(repo.settings.first().weatherCity)
        job.cancelAndJoin()
    }

    private fun newStore(job: Job, name: String) =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(folder.root, name) }

    @Test
    fun wallpaperDefaultsAndRoundTrip() = runTest {
        val job = Job()
        val repo = SettingsRepository(newStore(job, "wallpaper.preferences_pb"))

        val defaults = repo.settings.first()
        assertEquals(WallpaperMode.DYNAMIC, defaults.wallpaperMode)
        assertEquals(WallpaperImage.BuiltIn(0), defaults.wallpaperImage)
        assertEquals(WallpaperDim.MEDIUM, defaults.wallpaperDim)
        assertFalse(defaults.wallpaperPictureFailed)

        // Picking an image switches to CUSTOM in the same write.
        repo.setWallpaperImage(WallpaperImage.BuiltIn(3))
        repo.setWallpaperDim(WallpaperDim.HIGH)
        val custom = repo.settings.first()
        assertEquals(WallpaperMode.CUSTOM, custom.wallpaperMode)
        assertEquals(WallpaperImage.BuiltIn(3), custom.wallpaperImage)
        assertEquals(WallpaperDim.HIGH, custom.wallpaperDim)

        repo.setWallpaperMode(WallpaperMode.PLAIN)
        assertEquals(WallpaperMode.PLAIN, repo.settings.first().wallpaperMode)
        // The image choice is kept for the next time CUSTOM is chosen.
        assertEquals(WallpaperImage.BuiltIn(3), repo.settings.first().wallpaperImage)
        job.cancelAndJoin()
    }

    @Test
    fun oldDynamicSwitchOffMigratesToPlain() = runTest {
        val job = Job()
        val store = newStore(job, "legacy.preferences_pb")
        store.edit { it[booleanPreferencesKey("dynamic_wallpaper")] = false }
        val repo = SettingsRepository(store)
        assertEquals(WallpaperMode.PLAIN, repo.settings.first().wallpaperMode)

        // Choosing a mode replaces the old key, so it can never win again.
        repo.setWallpaperMode(WallpaperMode.DYNAMIC)
        assertEquals(WallpaperMode.DYNAMIC, repo.settings.first().wallpaperMode)
        assertNull(store.data.first()[booleanPreferencesKey("dynamic_wallpaper")])
        job.cancelAndJoin()
    }

    @Test
    fun unreadablePictureFallsBackToPlainOnlyForThatPicture() = runTest {
        val job = Job()
        val repo = SettingsRepository(newStore(job, "failed.preferences_pb"))
        val old = WallpaperImage.Picture(1L)
        val new = WallpaperImage.Picture(2L)

        repo.setWallpaperImage(new)
        // A failure reported for an older copy changes nothing.
        repo.onWallpaperPictureFailed(old)
        assertEquals(WallpaperMode.CUSTOM, repo.settings.first().wallpaperMode)

        repo.onWallpaperPictureFailed(new)
        val failed = repo.settings.first()
        assertEquals(WallpaperMode.PLAIN, failed.wallpaperMode)
        assertTrue(failed.wallpaperPictureFailed)
        assertEquals(new, failed.wallpaperImage)

        // Any new choice clears the note.
        repo.setWallpaperMode(WallpaperMode.DYNAMIC)
        assertFalse(repo.settings.first().wallpaperPictureFailed)
        job.cancelAndJoin()
    }

    @Test
    fun removingThePictureLeavesCustom() = runTest {
        val job = Job()
        val repo = SettingsRepository(newStore(job, "removed.preferences_pb"))
        repo.setWallpaperImage(WallpaperImage.Picture(5L))
        repo.onWallpaperPictureRemoved()
        val removed = repo.settings.first()
        assertEquals(WallpaperMode.PLAIN, removed.wallpaperMode)
        assertEquals(WallpaperImage.BuiltIn(0), removed.wallpaperImage)

        // With a built-in chosen, removing the stored copy changes nothing.
        repo.setWallpaperImage(WallpaperImage.BuiltIn(2))
        repo.onWallpaperPictureRemoved()
        assertEquals(WallpaperMode.CUSTOM, repo.settings.first().wallpaperMode)
        assertEquals(WallpaperImage.BuiltIn(2), repo.settings.first().wallpaperImage)
        job.cancelAndJoin()
    }

    @Test
    fun animationSpeedDefaultsToHalfAndSnapsToSteps() = runTest {
        val job = Job()
        val store = newStore(job, "speed.preferences_pb")
        val repo = SettingsRepository(store)
        assertEquals(50, repo.settings.first().animationSpeed)

        repo.setAnimationSpeed(125)
        assertEquals(125, repo.settings.first().animationSpeed)

        // An odd stored value reads as the nearest step instead of failing.
        store.edit { it[androidx.datastore.preferences.core.intPreferencesKey("animation_speed_percent")] = 990 }
        assertEquals(150, repo.settings.first().animationSpeed)
        job.cancelAndJoin()
    }
}
