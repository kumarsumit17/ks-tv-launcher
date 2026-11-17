package com.ks.tvlauncher

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import coil3.ImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import com.ks.tvlauncher.data.apps.AppArtworkFetcher
import com.ks.tvlauncher.data.apps.AppArtworkKeyer
import com.ks.tvlauncher.data.apps.AppsRepository
import com.ks.tvlauncher.data.apps.PinnedAppsRepository
import com.ks.tvlauncher.data.apps.WallpaperSource
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.subscriptions.SubscriptionDataStore
import com.ks.tvlauncher.data.subscriptions.SubscriptionRepository
import com.ks.tvlauncher.data.system.DeviceInfoRepository
import com.ks.tvlauncher.data.system.DeviceStatsRepository
import com.ks.tvlauncher.data.system.SystemStatusRepository
import com.ks.tvlauncher.data.weather.CityResolver
import com.ks.tvlauncher.data.weather.IpWhoIsLocator
import com.ks.tvlauncher.data.weather.OpenMeteoProvider
import com.ks.tvlauncher.data.weather.WeatherRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container, created once by [KsApplication].
 *
 * Everything is lazy: nothing is constructed, and no file is opened, until the first
 * screen asks for it. That keeps `Application.onCreate` empty and cold start short.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Lives as long as the process; only shares flows, which stop when nobody collects. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** One preferences file for all launcher settings, so there is a single reader and writer. */
    private val preferences: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile("ks_launcher") }
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(preferences) }
    val profileRepository: ProfileRepository by lazy { ProfileRepository(preferences) }
    val deviceInfoRepository: DeviceInfoRepository by lazy { DeviceInfoRepository(appContext) }
    val deviceStatsRepository: DeviceStatsRepository by lazy { DeviceStatsRepository(appContext) }
    val systemStatusRepository: SystemStatusRepository by lazy { SystemStatusRepository(appContext) }
    val cityResolver: CityResolver by lazy {
        CityResolver(preferences, IpWhoIsLocator(), settingsRepository.settings)
    }
    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(preferences, OpenMeteoProvider(), cityResolver.city.map { it?.city })
    }
    val appsRepository: AppsRepository by lazy { AppsRepository(appContext, appScope) }
    val pinnedAppsRepository: PinnedAppsRepository by lazy { PinnedAppsRepository(preferences) }
    val subscriptionRepository: SubscriptionRepository by lazy {
        SubscriptionRepository(SubscriptionDataStore(preferences))
    }

    /**
     * Loads app banners and icons. A small memory cache (15 % of the app's heap budget) and no
     * disk cache: the artwork already lives on the device.
     */
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(appContext)
            .components {
                add(AppArtworkKeyer())
                add(AppArtworkFetcher.Factory())
            }
            .memoryCache { MemoryCache.Builder().maxSizePercent(appContext, 0.15).build() }
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
    }

    val wallpaperSource: WallpaperSource by lazy { WallpaperSource(appContext, imageLoader) }
}
