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
    /** False until the profile dialog has been saved or cancelled once (first run). */
    val setupDone: Boolean = false,
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

    /**
     * Saves the profile dialog in one write. Names are cleaned with [ProfileNames.normalize];
     * an active name that is not in the list is dropped. Also marks first-run setup as done.
     */
    suspend fun save(names: List<String>, activeName: String?, useOwnTvName: Boolean, ownTvName: String) {
        val clean = ProfileNames.normalize(names)
        store.edit { prefs ->
            prefs[NAMES] = Json.encodeToString(NAMES_SERIALIZER, clean)
            val active = activeName?.takeIf { it in clean }
            if (active == null) prefs.remove(ACTIVE_NAME) else prefs[ACTIVE_NAME] = active
            prefs[USE_OWN_TV_NAME] = useOwnTvName
            prefs[OWN_TV_NAME] = ownTvName.trim().take(ProfileNames.MAX_LENGTH)
            prefs[SETUP_DONE] = true
        }
    }

    /** The first-run dialog was dismissed without saving: do not open it again by itself. */
    suspend fun markSetupDone() {
        store.edit { it[SETUP_DONE] = true }
    }

    private fun read(prefs: Preferences): Profile {
        val names = ProfileNames.normalize(decodeNames(prefs[NAMES]))
        return Profile(
            names = names,
            activeName = prefs[ACTIVE_NAME]?.takeIf { it in names },
            useOwnTvName = prefs[USE_OWN_TV_NAME] ?: false,
            ownTvName = prefs[OWN_TV_NAME].orEmpty(),
            setupDone = prefs[SETUP_DONE] ?: false,
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
        val SETUP_DONE = booleanPreferencesKey("profile_setup_done")
        val NAMES_SERIALIZER = ListSerializer(String.serializer())
    }
}
