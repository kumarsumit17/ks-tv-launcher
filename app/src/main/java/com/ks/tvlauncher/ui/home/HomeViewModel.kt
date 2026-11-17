package com.ks.tvlauncher.ui.home

import com.ks.tvlauncher.BuildConfig
import com.ks.tvlauncher.data.updates.UpdateStore
import com.ks.tvlauncher.domain.AppUpdates
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.apps.AppsRepository
import com.ks.tvlauncher.data.apps.PinnedAppsRepository
import com.ks.tvlauncher.data.apps.WallpaperSource
import com.ks.tvlauncher.data.profile.Profile
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.subscriptions.SubscriptionRepository
import com.ks.tvlauncher.data.system.DeviceInfo
import com.ks.tvlauncher.data.system.DeviceInfoRepository
import com.ks.tvlauncher.data.system.DeviceStats
import com.ks.tvlauncher.data.system.DeviceStatsRepository
import com.ks.tvlauncher.data.wallpaper.WallpaperPictureStore
import com.ks.tvlauncher.data.weather.CityResolver
import com.ks.tvlauncher.data.weather.ResolvedCity
import com.ks.tvlauncher.data.weather.WeatherRepository
import com.ks.tvlauncher.data.weather.WeatherState
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.domain.DeviceNames
import com.ks.tvlauncher.domain.Greeting
import com.ks.tvlauncher.domain.GreetingPeriod
import com.ks.tvlauncher.domain.PinnedApps
import com.ks.tvlauncher.domain.ProfileNames
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.WallpaperImage
import com.ks.tvlauncher.domain.WallpaperMode
import com.ks.tvlauncher.ui.subscriptions.SubscriptionsUiState
import com.ks.tvlauncher.ui.subscriptions.subscriptionsState
import com.ks.tvlauncher.ui.theme.VisualSettings
import com.ks.tvlauncher.util.DateTimeFormats
import com.ks.tvlauncher.util.minuteTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

/** A short notice for the snackbar. Text is resolved in the UI so it follows the TV's language. */
sealed interface HomeMessage {
    data class AppMissing(val label: String) : HomeMessage
}

@Immutable
data class HomeUiState(
    val time: String = "",
    /** Null when the user hides the date. */
    val shortDate: String? = null,
    val longDate: String = "",
    val greeting: GreetingPeriod = GreetingPeriod.EVENING,
    /** Null when no name is saved: only the greeting shows. */
    val name: String? = null,
    /** The name the "Switch to …" chip offers; null with fewer than two names. */
    val nextName: String? = null,
    val tvName: String = "",
    val androidVersion: String = "",
    val visual: VisualSettings = VisualSettings(),
    val wallpaperMode: WallpaperMode = WallpaperMode.DYNAMIC,
    val wallpaperDim: WallpaperDim = WallpaperDim.MEDIUM,
    /** False until the first app scan and the pinned list have both loaded. */
    val appsLoaded: Boolean = false,
    /** Pinned apps that are installed, in the user's order. */
    val pinned: List<AppInfo> = emptyList(),
    val installedCount: Int = 0,
    /** The app the hero shows: the last focused pinned tile, else the first pinned app. */
    val hero: AppInfo? = null,
    val message: HomeMessage? = null,
    val showMemoryCard: Boolean = true,
    /** The clock format in use, after "follow the TV" is resolved. */
    val clock24Hour: Boolean = false,
    val currency: String = com.ks.tvlauncher.domain.Money.DEFAULT_CURRENCY,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val weather: WeatherState = WeatherState.Loading,
    /** The weather city and whether it was detected; null when there is none yet. */
    val weatherCity: ResolvedCity? = null,
    /** First run: no names and the profile dialog never saved or dismissed. */
    val profileSetupNeeded: Boolean = false,
)

class HomeViewModel(
    private val appContext: Context,
    private val profileRepository: ProfileRepository,
    deviceInfoRepository: DeviceInfoRepository,
    private val appsRepository: AppsRepository,
    private val pinnedAppsRepository: PinnedAppsRepository,
    private val wallpaperSource: WallpaperSource,
    private val wallpaperPictureStore: WallpaperPictureStore,
    subscriptionRepository: SubscriptionRepository,
    private val settingsRepository: SettingsRepository,
    weatherRepository: WeatherRepository,
    cityResolver: CityResolver,
    private val deviceStatsRepository: DeviceStatsRepository,
    updateStore: UpdateStore,
) : ViewModel() {

    /**
     * The pending update's version for the top-bar pill, or null. Release builds only; a
     * release that is already installed never shows.
     */
    val updateVersion: StateFlow<String?> = (
        if (BuildConfig.IN_APP_UPDATES) {
            updateStore.available.map { release ->
                release?.takeIf { AppUpdates.isNewer(it.versionCode, BuildConfig.VERSION_CODE) }?.versionName
            }
        } else {
            flowOf(null)
        }
        ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val updateOpens = Channel<Unit>(Channel.CONFLATED)

    /** Fires when the update notification was opened: Home shows Settings › About. */
    val openUpdatesEvents: Flow<Unit> = updateOpens.receiveAsFlow()

    fun onOpenUpdates() {
        updateOpens.trySend(Unit)
    }

    /** The Home card's numbers: the same state the Subscriptions screen shows. */
    val subscriptions: StateFlow<SubscriptionsUiState> =
        subscriptionsState(appContext, subscriptionRepository, settingsRepository)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubscriptionsUiState())

    private data class AppsPart(
        val loaded: Boolean = false,
        val pinned: List<AppInfo> = emptyList(),
        val installedCount: Int = 0,
    )

    private val focusedPackage = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<HomeMessage?>(null)

    /** Clock, greeting, profile and device: rebuilt once a minute or when a setting changes. */
    private val shell: Flow<HomeUiState> = combine(
        minuteTicker(appContext),
        settingsRepository.settings,
        profileRepository.profile,
        deviceInfoRepository.deviceInfo,
        ::buildShell,
    )

    /**
     * Installed apps joined with the pinned list. Each new app list also keeps the stored pins
     * honest: first-run defaults are written once, and uninstalled packages are dropped.
     */
    private val appsPart: Flow<AppsPart> = combine(
        appsRepository.apps.onEach { apps ->
            if (apps != null) {
                val installed = apps.mapTo(HashSet()) { it.packageName }
                pinnedAppsRepository.initializeIfNeeded(installed)
                pinnedAppsRepository.prune(installed)
            }
        },
        pinnedAppsRepository.pinned,
    ) { apps, pinned ->
        if (apps == null || pinned == null) {
            AppsPart()
        } else {
            val byPackage = apps.associateBy { it.packageName }
            AppsPart(loaded = true, pinned = pinned.mapNotNull(byPackage::get), installedCount = apps.size)
        }
    }

    /**
     * One state object for Home. Every upstream source (clock broadcast, device-name observer,
     * package callback, DataStore) is active only while Home collects, and stops 5 s after it
     * goes to the background.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        shell,
        appsPart,
        focusedPackage,
        message,
        combine(weatherRepository.state, cityResolver.city, ::Pair),
    ) { base, apps, focused, msg, (weather, city) ->
        base.copy(
            weather = weather,
            weatherCity = city,
            appsLoaded = apps.loaded,
            pinned = apps.pinned,
            installedCount = apps.installedCount,
            hero = apps.pinned.firstOrNull { it.packageName == focused } ?: apps.pinned.firstOrNull(),
            message = msg,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // Correct clock and greeting on the very first frame, before storage has been read.
            // setupDone = true: the first-run dialog waits for the real profile.
            initialValue = buildShell(ZonedDateTime.now(), LauncherSettings(), Profile(setupDone = true), null),
        )

    /**
     * The dynamic wallpaper for the hero app. Waits 250 ms after focus settles, so tiles passed
     * while scrolling load nothing, and a newer focus cancels a load still in flight.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun dynamicWallpaper(): Flow<HomeWallpaper> = uiState
        .map { it.hero }
        .distinctUntilChanged()
        .debounce(WALLPAPER_DEBOUNCE_MS)
        .mapLatest { app -> HomeWallpaper.Dynamic(app?.let { wallpaperSource.load(it) }) }

    /**
     * The user's picture, loaded once at screen size. Missing or unreadable: PLAIN, and the
     * setting falls back to PLAIN with a note for Settings.
     */
    private fun pictureWallpaper(image: WallpaperImage.Picture): Flow<HomeWallpaper> = flow {
        val metrics = appContext.resources.displayMetrics
        val bitmap = wallpaperPictureStore.load(metrics.widthPixels, metrics.heightPixels)
        if (bitmap == null) {
            settingsRepository.onWallpaperPictureFailed(image)
            emit(HomeWallpaper.Plain)
        } else {
            emit(HomeWallpaper.Picture(bitmap))
        }
    }

    /**
     * What Home draws behind everything, from the wallpaper mode. The dim level is applied by
     * the background itself, so changing it never reloads the picture. The picture is dropped
     * 5 s after Home is hidden (`replayExpirationMillis = 0`) and loaded again on return.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val wallpaper: StateFlow<HomeWallpaper> = settingsRepository.settings
        .map { it.wallpaperMode to it.wallpaperImage }
        .distinctUntilChanged()
        .flatMapLatest { (mode, image) ->
            when (mode) {
                WallpaperMode.PLAIN -> flowOf(HomeWallpaper.Plain)
                WallpaperMode.DYNAMIC -> dynamicWallpaper()
                WallpaperMode.CUSTOM -> when (image) {
                    is WallpaperImage.BuiltIn -> flowOf(HomeWallpaper.BuiltIn(image.id))
                    is WallpaperImage.Picture -> pictureWallpaper(image)
                }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000, replayExpirationMillis = 0), HomeWallpaper.Plain)

    private val statsBreakdown = MutableStateFlow(false)

    /**
     * RAM and storage every 10 s, only while the card or the panel collects it; stops at once
     * when Home is hidden. The Apps / Other split is read only while the panel is open.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val deviceStats: StateFlow<DeviceStats?> = statsBreakdown
        .flatMapLatest { deviceStatsRepository.stats(withBreakdown = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), null)

    fun setStatsBreakdown(enabled: Boolean) {
        statsBreakdown.value = enabled
    }

    fun usageAccessGranted(): Boolean = deviceStatsRepository.usageAccessGranted()

    fun setWallpaperMode(mode: WallpaperMode) {
        viewModelScope.launch { settingsRepository.setWallpaperMode(mode) }
    }

    fun setUse24Hour(value: Boolean) {
        viewModelScope.launch { settingsRepository.setUse24Hour(value) }
    }

    private val reopens = Channel<Unit>(Channel.CONFLATED)

    /** Fires when KS is opened again while it was already running in the background. */
    val reopenEvents: Flow<Unit> = reopens.receiveAsFlow()

    fun onReopened() {
        reopens.trySend(Unit)
    }

    fun onTileFocused(packageName: String) {
        focusedPackage.value = packageName
    }

    fun switchProfile() {
        viewModelScope.launch { profileRepository.switchToNext() }
    }

    fun pin(packageName: String) {
        viewModelScope.launch { pinnedAppsRepository.pin(packageName) }
    }

    fun unpin(packageName: String) {
        viewModelScope.launch { pinnedAppsRepository.unpin(packageName) }
    }

    fun move(packageName: String, delta: Int) {
        viewModelScope.launch { pinnedAppsRepository.move(packageName, delta) }
    }

    /** Null when the app cannot be launched, for example because it has been uninstalled. */
    fun launchIntent(packageName: String): Intent? = appsRepository.launchIntent(packageName)

    /** The app has gone: tell the user and re-read the app list. */
    fun onLaunchFailed(label: String) {
        message.value = HomeMessage.AppMissing(label)
        appsRepository.refresh()
    }

    fun dismissMessage() {
        message.value = null
    }

    private fun buildShell(
        now: ZonedDateTime,
        settings: LauncherSettings,
        profile: Profile,
        device: DeviceInfo?,
    ): HomeUiState = HomeUiState(
        time = DateTimeFormats.time(appContext, now, settings.use24Hour),
        shortDate = if (settings.showDate) DateTimeFormats.shortDate(now) else null,
        longDate = DateTimeFormats.longDate(now),
        greeting = GreetingPeriod.at(now.toLocalTime()),
        name = Greeting.displayName(profile.activeName),
        nextName = ProfileNames.next(profile.names, profile.activeName),
        tvName = device?.let {
            DeviceNames.displayName(profile.useOwnTvName, profile.ownTvName, it.systemName, it.model)
        }.orEmpty(),
        androidVersion = device?.androidRelease.orEmpty(),
        visual = VisualSettings(
            reduceMotion = settings.reduceMotion,
            animationSpeed = settings.animationSpeed,
            glassIntensity = settings.glassIntensity,
        ),
        wallpaperMode = settings.wallpaperMode,
        wallpaperDim = settings.wallpaperDim,
        showMemoryCard = settings.showMemoryCard,
        clock24Hour = settings.use24Hour ?: android.text.format.DateFormat.is24HourFormat(appContext),
        currency = settings.currency,
        temperatureUnit = settings.temperatureUnit,
        profileSetupNeeded = !profile.setupDone && profile.names.isEmpty(),
    )

    companion object {
        private const val WALLPAPER_DEBOUNCE_MS = 250L

        /** Whether [packageName] can move one place left (-1) or right (+1) in [pinned]. */
        fun canMove(pinned: List<AppInfo>, packageName: String, delta: Int): Boolean =
            PinnedApps.canMove(pinned.map { it.packageName }, packageName, delta)

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as KsApplication
                val container = app.container
                HomeViewModel(
                    appContext = app,
                    profileRepository = container.profileRepository,
                    deviceInfoRepository = container.deviceInfoRepository,
                    appsRepository = container.appsRepository,
                    pinnedAppsRepository = container.pinnedAppsRepository,
                    wallpaperSource = container.wallpaperSource,
                    wallpaperPictureStore = container.wallpaperPictureStore,
                    subscriptionRepository = container.subscriptionRepository,
                    settingsRepository = container.settingsRepository,
                    weatherRepository = container.weatherRepository,
                    cityResolver = container.cityResolver,
                    deviceStatsRepository = container.deviceStatsRepository,
                    updateStore = container.updateStore,
                )
            }
        }
    }
}
