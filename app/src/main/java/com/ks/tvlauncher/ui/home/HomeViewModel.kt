package com.ks.tvlauncher.ui.home

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
import com.ks.tvlauncher.data.apps.Wallpaper
import com.ks.tvlauncher.data.apps.WallpaperSource
import com.ks.tvlauncher.data.profile.Profile
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.system.DeviceInfo
import com.ks.tvlauncher.data.system.DeviceInfoRepository
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.domain.DeviceNames
import com.ks.tvlauncher.domain.Greeting
import com.ks.tvlauncher.domain.GreetingPeriod
import com.ks.tvlauncher.domain.PinnedApps
import com.ks.tvlauncher.domain.ProfileNames
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
    /** False until the first app scan and the pinned list have both loaded. */
    val appsLoaded: Boolean = false,
    /** Pinned apps that are installed, in the user's order. */
    val pinned: List<AppInfo> = emptyList(),
    val installedCount: Int = 0,
    /** The app the hero shows: the last focused pinned tile, else the first pinned app. */
    val hero: AppInfo? = null,
    val message: HomeMessage? = null,
)

class HomeViewModel(
    private val appContext: Context,
    settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    deviceInfoRepository: DeviceInfoRepository,
    private val appsRepository: AppsRepository,
    private val pinnedAppsRepository: PinnedAppsRepository,
    private val wallpaperSource: WallpaperSource,
) : ViewModel() {

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
    val uiState: StateFlow<HomeUiState> = combine(shell, appsPart, focusedPackage, message) { base, apps, focused, msg ->
        base.copy(
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
            initialValue = buildShell(ZonedDateTime.now(), LauncherSettings(), Profile(), null),
        )

    /**
     * The dynamic wallpaper for the hero app, or null for the plain background.
     * Waits 250 ms after focus settles, so tiles passed while scrolling load nothing, and a
     * newer focus cancels a load still in flight. Off when the user disables the setting.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val wallpaper: StateFlow<Wallpaper?> = uiState
        .map { state -> state.hero.takeIf { state.visual.dynamicWallpaper } }
        .distinctUntilChanged()
        .debounce(WALLPAPER_DEBOUNCE_MS)
        .mapLatest { app -> app?.let { wallpaperSource.load(it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
            glassIntensity = settings.glassIntensity,
            dynamicWallpaper = settings.dynamicWallpaper,
        ),
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
                    settingsRepository = container.settingsRepository,
                    profileRepository = container.profileRepository,
                    deviceInfoRepository = container.deviceInfoRepository,
                    appsRepository = container.appsRepository,
                    pinnedAppsRepository = container.pinnedAppsRepository,
                    wallpaperSource = container.wallpaperSource,
                )
            }
        }
    }
}
