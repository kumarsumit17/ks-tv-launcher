package com.ks.tvlauncher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.system.DeviceInfoRepository

/**
 * Manual dependency container, created once by [KsApplication].
 *
 * Everything is lazy: nothing is constructed, and no file is opened, until the first
 * screen asks for it. That keeps `Application.onCreate` empty and cold start short.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** One preferences file for all launcher settings, so there is a single reader and writer. */
    private val preferences: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile("ks_launcher") }
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(preferences) }
    val profileRepository: ProfileRepository by lazy { ProfileRepository(preferences) }
    val deviceInfoRepository: DeviceInfoRepository by lazy { DeviceInfoRepository(appContext) }
}
