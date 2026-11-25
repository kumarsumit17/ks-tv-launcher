package com.ks.tvlauncher.data.reminders

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * Which renewals have been notified already (`id@yyyy-MM-dd` keys), so each renewal is
 * notified once. Lives in the shared `ks_launcher` DataStore; device-only like the rest.
 */
class ReminderStore(private val store: DataStore<Preferences>) {

    suspend fun notified(): Set<String> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it[NOTIFIED] ?: emptySet() }
        .first()

    suspend fun setNotified(keys: Set<String>) {
        store.edit { if (keys.isEmpty()) it.remove(NOTIFIED) else it[NOTIFIED] = keys }
    }

    private companion object {
        val NOTIFIED = stringSetPreferencesKey("reminders_notified")
    }
}
