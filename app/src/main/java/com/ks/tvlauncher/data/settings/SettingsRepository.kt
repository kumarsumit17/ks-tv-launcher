package com.ks.tvlauncher.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

    private companion object {
        val USE_24_HOUR = booleanPreferencesKey("clock_24_hour")
        val SHOW_DATE = booleanPreferencesKey("clock_show_date")
        val DYNAMIC_WALLPAPER = booleanPreferencesKey("dynamic_wallpaper")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val GLASS_INTENSITY = stringPreferencesKey("glass_intensity")
    }
}
