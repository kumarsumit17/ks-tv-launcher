package com.ks.tvlauncher.data.subscriptions

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * The subscription list as one JSON string key in the app's DataStore. Device-only: the app
 * has backup and device transfer turned off.
 *
 * Corrupt JSON is never silently wiped: it is logged, the list reads as empty, and before the
 * first write replaces it the raw text is copied to [BACKUP_KEY]. An existing backup is never
 * overwritten.
 */
class SubscriptionDataStore(
    private val store: DataStore<Preferences>,
    private val log: (String, Throwable) -> Unit = { msg, e -> Log.w(TAG, msg, e) },
) {
    private val json = Json { ignoreUnknownKeys = true }

    val subscriptions: Flow<List<Subscription>> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> decode(prefs[KEY], report = true) ?: emptyList() }
        .distinctUntilChanged()

    /** The raw text kept from a corrupt list, if any. */
    val backup: Flow<String?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[BACKUP_KEY] }
        .distinctUntilChanged()

    /** Applies [change] to the stored list in one transaction. */
    suspend fun update(change: (List<Subscription>) -> List<Subscription>) {
        store.edit { prefs ->
            val raw = prefs[KEY]
            val current = decode(raw, report = false)
            if (current == null && raw != null) keepBackup(prefs, raw)
            val before = current ?: emptyList()
            val next = change(before)
            if (next != before || raw == null || current == null) prefs[KEY] = json.encodeToString(SERIALIZER, next)
        }
    }

    private fun keepBackup(prefs: MutablePreferences, raw: String) {
        if (prefs[BACKUP_KEY] == null) prefs[BACKUP_KEY] = raw
    }

    /** Null when [raw] is present but unreadable. */
    private fun decode(raw: String?, report: Boolean): List<Subscription>? {
        if (raw == null) return emptyList()
        return try {
            json.decodeFromString(SERIALIZER, raw)
        } catch (e: SerializationException) {
            if (report) log("Unreadable subscription data; showing an empty list and keeping the raw value", e)
            null
        } catch (e: IllegalArgumentException) {
            if (report) log("Unreadable subscription data; showing an empty list and keeping the raw value", e)
            null
        }
    }

    companion object {
        private const val TAG = "KsSubscriptions"
        internal val KEY = stringPreferencesKey("subscriptions")
        internal val BACKUP_KEY = stringPreferencesKey("subscriptions_corrupt_backup")
        private val SERIALIZER = ListSerializer(Subscription.serializer())
    }
}
