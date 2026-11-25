package com.ks.tvlauncher.data.apps

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.PinnedApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.IOException

/** The ordered list of pinned package names, stored as a JSON array in the shared DataStore. */
class PinnedAppsRepository(private val store: DataStore<Preferences>) {

    /**
     * The pinned packages in order. Null only before the first-run defaults have been written,
     * so "never set up" is different from "the user unpinned everything".
     */
    val pinned: Flow<List<String>?> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { read(it) }
        .distinctUntilChanged()

    /** On first run only, pins the installed default apps. Later calls change nothing. */
    suspend fun initializeIfNeeded(installed: Set<String>) {
        store.edit { prefs ->
            if (prefs[KEY] == null) write(prefs, PinnedApps.defaults(installed))
        }
    }

    suspend fun pin(packageName: String) = update { PinnedApps.pin(it, packageName) }

    suspend fun unpin(packageName: String) = update { PinnedApps.unpin(it, packageName) }

    suspend fun move(packageName: String, delta: Int) = update { PinnedApps.move(it, packageName, delta) }

    /** Silently drops packages that are no longer installed. Writes only if something changed. */
    suspend fun prune(installed: Set<String>) = update { PinnedApps.prune(it, installed) }

    private suspend fun update(change: (List<String>) -> List<String>) {
        store.edit { prefs ->
            val current = read(prefs) ?: emptyList()
            val next = PinnedApps.normalize(change(current))
            if (next != current || prefs[KEY] == null) write(prefs, next)
        }
    }

    private fun read(prefs: Preferences): List<String>? {
        val raw = prefs[KEY] ?: return null
        return try {
            PinnedApps.normalize(Json.decodeFromString(SERIALIZER, raw))
        } catch (e: SerializationException) {
            // Unreadable: behave as "nothing pinned" rather than crash Home.
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private fun write(prefs: MutablePreferences, pinned: List<String>) {
        prefs[KEY] = Json.encodeToString(SERIALIZER, pinned)
    }

    private companion object {
        val KEY = stringPreferencesKey("pinned_apps")
        val SERIALIZER = ListSerializer(String.serializer())
    }
}
