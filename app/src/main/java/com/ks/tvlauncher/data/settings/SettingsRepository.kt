package com.ks.tvlauncher.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.RenewalReminders
import com.ks.tvlauncher.domain.TemperatureUnit
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
    val dynamicWallpaper: Boolean = true,
    val reduceMotion: Boolean = false,
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
                dynamicWallpaper = prefs[DYNAMIC_WALLPAPER] ?: true,
                reduceMotion = prefs[REDUCE_MOTION] ?: false,
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

    suspend fun setDynamicWallpaper(value: Boolean) {
        store.edit { it[DYNAMIC_WALLPAPER] = value }
    }

    suspend fun setReduceMotion(value: Boolean) {
        store.edit { it[REDUCE_MOTION] = value }
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
        val DYNAMIC_WALLPAPER = booleanPreferencesKey("dynamic_wallpaper")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
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
