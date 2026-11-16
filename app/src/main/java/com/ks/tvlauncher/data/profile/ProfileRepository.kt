package com.ks.tvlauncher.data.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ks.tvlauncher.domain.ProfileNames
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.IOException

data class Profile(
    val names: List<String> = emptyList(),
    val activeName: String? = null,
    val useOwnTvName: Boolean = false,
    val ownTvName: String = "",
)

/** Who's watching, and the optional custom TV name. Used on screen only; never leaves the device. */
class ProfileRepository(private val store: DataStore<Preferences>) {

    val profile: Flow<Profile> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map(::read)
        .distinctUntilChanged()

    /** Moves to the next saved name, wrapping round. Does nothing with fewer than two names. */
    suspend fun switchToNext() {
        store.edit { prefs ->
            val current = read(prefs)
            ProfileNames.next(current.names, current.activeName)?.let { prefs[ACTIVE_NAME] = it }
        }
    }

    private fun read(prefs: Preferences): Profile {
        val names = ProfileNames.normalize(decodeNames(prefs[NAMES]))
        return Profile(
            names = names,
            activeName = prefs[ACTIVE_NAME]?.takeIf { it in names },
            useOwnTvName = prefs[USE_OWN_TV_NAME] ?: false,
            ownTvName = prefs[OWN_TV_NAME].orEmpty(),
        )
    }

    private fun decodeNames(raw: String?): List<String> {
        if (raw.isNullOrEmpty()) return emptyList()
        return try {
            Json.decodeFromString(NAMES_SERIALIZER, raw)
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private companion object {
        /** JSON array of strings, so the order is kept. */
        val NAMES = stringPreferencesKey("profile_names")
        val ACTIVE_NAME = stringPreferencesKey("profile_active_name")
        val USE_OWN_TV_NAME = booleanPreferencesKey("tv_name_use_own")
        val OWN_TV_NAME = stringPreferencesKey("tv_name_own")
        val NAMES_SERIALIZER = ListSerializer(String.serializer())
    }
}
