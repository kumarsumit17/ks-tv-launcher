package com.ks.tvlauncher.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.AnimationSpeed
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.RenewalReminders
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.WallpaperImage
import com.ks.tvlauncher.domain.WallpaperMode
import com.ks.tvlauncher.domain.Wallpapers
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/** Low: no blur, cheapest. Medium: light blur on still surfaces. High: stronger blur. */
enum class GlassIntensity { LOW, MEDIUM, HIGH }

data class LauncherSettings(
    /** Null follows the TV's own 12/24-hour setting. */
    val use24Hour: Boolean? = null,
    val showDate: Boolean = true,
    /** DYNAMIC (default), CUSTOM or PLAIN; replaces the old dynamic on/off switch. */
    val wallpaperMode: WallpaperMode = WallpaperMode.DYNAMIC,
    /** What CUSTOM shows: a built-in wallpaper or the user's picture. */
    val wallpaperImage: WallpaperImage = Wallpapers.DEFAULT_IMAGE,
    val wallpaperDim: WallpaperDim = WallpaperDim.MEDIUM,
    /** The picture could not be read, so the mode fell back to PLAIN; Settings says so. */
    val wallpaperPictureFailed: Boolean = false,
    val reduceMotion: Boolean = false,
    /** Animation durations in percent of the design-system ones; 50 (0.5×) by default. */
    val animationSpeed: Int = AnimationSpeed.DEFAULT,
    val glassIntensity: GlassIntensity = GlassIntensity.MEDIUM,
    /** ISO 4217 code for subscription amounts and the CSV export. Default INR. */
    val currency: String = Money.DEFAULT_CURRENCY,
    val showMemoryCard: Boolean = true,
    /** AUTO (default): detected from the network. MANUAL: [weatherCity]. */
    val weatherCityMode: WeatherCityMode = WeatherCityMode.AUTO,
    /** The city the user picked in Settings; used only in MANUAL mode. */
    val weatherCity: WeatherCity? = null,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    /** Renewal reminders are off until the user turns them on (and allows notifications). */
    val remindersEnabled: Boolean = false,
    /** Days before a renewal to notify, 1-14. */
    val reminderLeadDays: Int = RenewalReminders.DEFAULT_LEAD_DAYS,
)

/** Launcher preferences, stored on the device only. */
class SettingsRepository(private val store: DataStore<Preferences>) {

    val settings: Flow<LauncherSettings> = store.data
        // A read error must never take Home down: fall back to defaults.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            LauncherSettings(
                use24Hour = prefs[USE_24_HOUR],
                showDate = prefs[SHOW_DATE] ?: true,
                wallpaperMode = Wallpapers.mode(prefs[WALLPAPER_MODE], prefs[DYNAMIC_WALLPAPER]),
                wallpaperImage = Wallpapers.decode(prefs[WALLPAPER_IMAGE]),
                wallpaperDim = Wallpapers.dim(prefs[WALLPAPER_DIM]),
                wallpaperPictureFailed = prefs[WALLPAPER_PICTURE_FAILED] ?: false,
                reduceMotion = prefs[REDUCE_MOTION] ?: false,
                animationSpeed = AnimationSpeed.normalise(prefs[ANIMATION_SPEED]),
                glassIntensity = prefs[GLASS_INTENSITY]
                    ?.let { saved -> GlassIntensity.entries.firstOrNull { it.name == saved } }
                    ?: GlassIntensity.MEDIUM,
                currency = prefs[CURRENCY] ?: Money.DEFAULT_CURRENCY,
                showMemoryCard = prefs[SHOW_MEMORY_CARD] ?: true,
                weatherCityMode = prefs[WEATHER_CITY_MODE]
                    ?.let { saved -> WeatherCityMode.entries.firstOrNull { it.name == saved } }
                    ?: WeatherCityMode.AUTO,
                weatherCity = decodeCity(prefs[WEATHER_CITY]),
                temperatureUnit = prefs[TEMPERATURE_UNIT]
                    ?.let { saved -> TemperatureUnit.entries.firstOrNull { it.name == saved } }
                    ?: TemperatureUnit.CELSIUS,
                remindersEnabled = prefs[REMINDERS_ENABLED] ?: false,
                reminderLeadDays = RenewalReminders.clampLeadDays(
                    prefs[REMINDER_LEAD_DAYS] ?: RenewalReminders.DEFAULT_LEAD_DAYS,
                ),
            )
        }
        .distinctUntilChanged()

    suspend fun setUse24Hour(value: Boolean?) {
        store.edit { if (value == null) it.remove(USE_24_HOUR) else it[USE_24_HOUR] = value }
    }

    suspend fun setShowDate(value: Boolean) {
        store.edit { it[SHOW_DATE] = value }
    }

    /** Also drops the old dynamic on/off key, now replaced by the mode, and any failure note. */
    suspend fun setWallpaperMode(mode: WallpaperMode) {
        store.edit {
            it[WALLPAPER_MODE] = mode.name
            it.remove(DYNAMIC_WALLPAPER)
            it.remove(WALLPAPER_PICTURE_FAILED)
        }
    }

    /** Picking an image always switches to CUSTOM, in one write. */
    suspend fun setWallpaperImage(image: WallpaperImage) {
        store.edit {
            it[WALLPAPER_IMAGE] = Wallpapers.encode(image)
            it[WALLPAPER_MODE] = WallpaperMode.CUSTOM.name
            it.remove(DYNAMIC_WALLPAPER)
            it.remove(WALLPAPER_PICTURE_FAILED)
        }
    }

    suspend fun setWallpaperDim(dim: WallpaperDim) {
        store.edit { it[WALLPAPER_DIM] = dim.name }
    }

    /**
     * The picture is missing or unreadable: show PLAIN and note it for Settings. Only if the
     * stored choice is still that picture, so a newer pick is never overwritten.
     */
    suspend fun onWallpaperPictureFailed(image: WallpaperImage.Picture) {
        store.edit {
            if (Wallpapers.decode(it[WALLPAPER_IMAGE]) == image && it[WALLPAPER_MODE] == WallpaperMode.CUSTOM.name) {
                it[WALLPAPER_MODE] = WallpaperMode.PLAIN.name
                it[WALLPAPER_PICTURE_FAILED] = true
            }
        }
    }

    /** "Remove image": back to the first built-in; CUSTOM showing the picture becomes PLAIN. */
    suspend fun onWallpaperPictureRemoved() {
        store.edit {
            if (Wallpapers.decode(it[WALLPAPER_IMAGE]) is WallpaperImage.Picture) {
                it[WALLPAPER_IMAGE] = Wallpapers.encode(Wallpapers.DEFAULT_IMAGE)
                if (Wallpapers.mode(it[WALLPAPER_MODE], it[DYNAMIC_WALLPAPER]) == WallpaperMode.CUSTOM) {
                    it[WALLPAPER_MODE] = WallpaperMode.PLAIN.name
                }
            }
            it.remove(WALLPAPER_PICTURE_FAILED)
        }
    }

    suspend fun setReduceMotion(value: Boolean) {
        store.edit { it[REDUCE_MOTION] = value }
    }

    suspend fun setAnimationSpeed(percent: Int) {
        store.edit { it[ANIMATION_SPEED] = AnimationSpeed.normalise(percent) }
    }

    suspend fun setGlassIntensity(value: GlassIntensity) {
        store.edit { it[GLASS_INTENSITY] = value.name }
    }

    suspend fun setCurrency(code: String) {
        store.edit { it[CURRENCY] = code.uppercase() }
    }

    suspend fun setShowMemoryCard(value: Boolean) {
        store.edit { it[SHOW_MEMORY_CARD] = value }
    }

    /** A city picked in Settings: stores it and switches to MANUAL in one write. */
    suspend fun setManualCity(city: WeatherCity) {
        store.edit {
            it[WEATHER_CITY] = Json.encodeToString(WeatherCity.serializer(), city)
            it[WEATHER_CITY_MODE] = WeatherCityMode.MANUAL.name
        }
    }

    /** AUTO returns to the detected city; the manual city is kept for next time. */
    suspend fun setWeatherCityMode(mode: WeatherCityMode) {
        store.edit { it[WEATHER_CITY_MODE] = mode.name }
    }

    suspend fun setTemperatureUnit(value: TemperatureUnit) {
        store.edit { it[TEMPERATURE_UNIT] = value.name }
    }

    suspend fun setRemindersEnabled(value: Boolean) {
        store.edit { it[REMINDERS_ENABLED] = value }
    }

    suspend fun setReminderLeadDays(days: Int) {
        store.edit { it[REMINDER_LEAD_DAYS] = RenewalReminders.clampLeadDays(days) }
    }

    /** A damaged value counts as "no city", so the user can simply pick one again. */
    private fun decodeCity(raw: String?): WeatherCity? {
        if (raw.isNullOrEmpty()) return null
        return try {
            Json.decodeFromString(WeatherCity.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        val USE_24_HOUR = booleanPreferencesKey("clock_24_hour")
        val SHOW_DATE = booleanPreferencesKey("clock_show_date")
        /** Before Stage 6b; read only to migrate "off" to PLAIN. */
        val DYNAMIC_WALLPAPER = booleanPreferencesKey("dynamic_wallpaper")
        val WALLPAPER_MODE = stringPreferencesKey("wallpaper_mode")
        val WALLPAPER_IMAGE = stringPreferencesKey("wallpaper_image")
        val WALLPAPER_DIM = stringPreferencesKey("wallpaper_dim")
        val WALLPAPER_PICTURE_FAILED = booleanPreferencesKey("wallpaper_picture_failed")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val ANIMATION_SPEED = intPreferencesKey("animation_speed_percent")
        val GLASS_INTENSITY = stringPreferencesKey("glass_intensity")
        val CURRENCY = stringPreferencesKey("currency")
        val SHOW_MEMORY_CARD = booleanPreferencesKey("show_memory_card")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_CITY_MODE = stringPreferencesKey("weather_city_mode")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_LEAD_DAYS = intPreferencesKey("reminder_lead_days")
    }
}
