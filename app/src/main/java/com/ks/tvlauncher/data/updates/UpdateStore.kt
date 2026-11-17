package com.ks.tvlauncher.data.updates

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.ReleaseInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Update bookkeeping in the shared `ks_launcher` DataStore: the pending release, the day
 * GitHub said to wait until and which version was notified. A damaged value reads as
 * "nothing pending". (`update_scheduled_for`, written by 0.7.x, is no longer read.)
 */
class UpdateStore(private val store: DataStore<Preferences>) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: Flow<Preferences> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    /** The newer release found by the last check, or null. */
    val available: Flow<ReleaseInfo?> = prefs.map { decode(it[AVAILABLE]) }.distinctUntilChanged()

    suspend fun setAvailable(release: ReleaseInfo?) {
        store.edit {
            if (release == null) it.remove(AVAILABLE) else it[AVAILABLE] = json.encodeToString(ReleaseInfo.serializer(), release)
        }
    }

    /** Epoch day before which no automatic check runs (after HTTP 403/429). */
    suspend fun waitUntilDay(): Long = prefs.first()[WAIT_UNTIL_DAY] ?: 0L

    suspend fun setWaitUntilDay(day: Long) {
        store.edit { it[WAIT_UNTIL_DAY] = day }
    }

    /** The versionCode whose notification was posted, so each version is notified once. */
    suspend fun notifiedCode(): Int = prefs.first()[NOTIFIED_CODE] ?: 0

    suspend fun setNotifiedCode(code: Int) {
        store.edit { it[NOTIFIED_CODE] = code }
    }

    private fun decode(raw: String?): ReleaseInfo? {
        if (raw.isNullOrEmpty()) return null
        return try {
            json.decodeFromString(ReleaseInfo.serializer(), raw)
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private companion object {
        val AVAILABLE = stringPreferencesKey("update_available")
        val WAIT_UNTIL_DAY = longPreferencesKey("update_wait_until_day")
        val NOTIFIED_CODE = intPreferencesKey("update_notified_code")
    }
}
