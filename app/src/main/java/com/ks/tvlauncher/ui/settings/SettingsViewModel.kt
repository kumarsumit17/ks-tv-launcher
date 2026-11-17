package com.ks.tvlauncher.ui.settings

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.apps.AppsRepository
import com.ks.tvlauncher.data.apps.PinnedAppsRepository
import com.ks.tvlauncher.data.profile.Profile
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.reminders.ReminderScheduler
import com.ks.tvlauncher.data.settings.GlassIntensity
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.system.DeviceInfo
import com.ks.tvlauncher.data.system.DeviceInfoRepository
import com.ks.tvlauncher.data.updates.UpdateManager
import com.ks.tvlauncher.data.updates.UpdateScheduler
import com.ks.tvlauncher.data.updates.UpdateState
import com.ks.tvlauncher.domain.ReleaseInfo
import com.ks.tvlauncher.data.wallpaper.WallpaperPictureStore
import com.ks.tvlauncher.data.weather.CityResolver
import com.ks.tvlauncher.data.weather.ResolvedCity
import com.ks.tvlauncher.data.weather.WeatherRepository
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.WallpaperImage
import com.ks.tvlauncher.domain.WallpaperMode
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/** The Settings city search. */
sealed interface CitySearch {
    data object Idle : CitySearch
    data object Searching : CitySearch
    data class Results(val cities: List<WeatherCity>) : CitySearch
    data object Failed : CitySearch
}

/** The Wallpaper section's file state, read when Settings opens it. */
@Immutable
data class WallpaperFiles(
    /** A picture has been copied to app storage. */
    val pictureSaved: Boolean = false,
    /** The TV has a document picker, so "Choose a picture" can be offered. */
    val canPickFiles: Boolean = false,
    val importing: Boolean = false,
    /** The last picked file could not be read. */
    val importFailed: Boolean = false,
)

/** "Detect again" progress. */
enum class DetectState { IDLE, DETECTING, FAILED }

@Immutable
data class SettingsUiState(
    val loaded: Boolean = false,
    val settings: LauncherSettings = LauncherSettings(),
    val profile: Profile = Profile(setupDone = true),
    val device: DeviceInfo? = null,
    /** Installed pinned apps in the user's order. */
    val pinned: List<AppInfo> = emptyList(),
    val citySearch: CitySearch = CitySearch.Idle,
    /** The city weather uses now; null when automatic detection has not succeeded yet. */
    val weatherCity: ResolvedCity? = null,
    val detect: DetectState = DetectState.IDLE,
    val wallpaperFiles: WallpaperFiles = WallpaperFiles(),
)

/** Reads and writes every launcher setting, the profile and the weather city. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    deviceInfoRepository: DeviceInfoRepository,
    appsRepository: AppsRepository,
    private val pinnedAppsRepository: PinnedAppsRepository,
    private val weatherRepository: WeatherRepository,
    private val cityResolver: CityResolver,
    private val reminderScheduler: ReminderScheduler,
    private val wallpaperPictureStore: WallpaperPictureStore,
    private val updateManager: UpdateManager,
    private val updateScheduler: UpdateScheduler,
) : ViewModel() {

    /** About › updates (release builds only). */
    val updateState: StateFlow<UpdateState> get() = updateManager.state
    val updateAvailable: StateFlow<ReleaseInfo?> get() = updateManager.available

    fun checkForUpdates() = updateManager.check()
    fun startUpdate() = updateManager.start()
    fun cancelUpdate() = updateManager.cancel()
    fun installUpdate() = updateManager.install()
    fun onUpdateResumed() = updateManager.onResumed()
    fun resetUpdateMessage() = updateManager.reset()

    /** Writes the switch, then starts or stops only the update work (never reminders). */
    fun setAutoUpdateCheck(value: Boolean) = write {
        settingsRepository.setAutoUpdateCheck(value)
        updateScheduler.setEnabled(value)
    }

    private val wallpaperFiles = MutableStateFlow(WallpaperFiles())

    private val detectState = MutableStateFlow(DetectState.IDLE)

    private val citySearch = MutableStateFlow<CitySearch>(CitySearch.Idle)
    private var searchJob: Job? = null

    private val pinnedApps = combine(appsRepository.apps, pinnedAppsRepository.pinned) { apps, pinned ->
        if (apps == null || pinned == null) {
            emptyList()
        } else {
            val byPackage = apps.associateBy { it.packageName }
            pinned.mapNotNull(byPackage::get)
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        profileRepository.profile,
        deviceInfoRepository.deviceInfo,
        pinnedApps,
        combine(citySearch, cityResolver.city, detectState, wallpaperFiles) { search, city, detect, files ->
            SettingsUiState(citySearch = search, weatherCity = city, detect = detect, wallpaperFiles = files)
        },
    ) { settings, profile, device, pinned, extra ->
        extra.copy(loaded = true, settings = settings, profile = profile, device = device, pinned = pinned)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun setShowMemoryCard(value: Boolean) = write { settingsRepository.setShowMemoryCard(value) }
    fun setWallpaperMode(mode: WallpaperMode) = write { settingsRepository.setWallpaperMode(mode) }
    fun setWallpaperDim(dim: WallpaperDim) = write { settingsRepository.setWallpaperDim(dim) }

    /** A built-in wallpaper picked: it becomes the CUSTOM image. */
    fun chooseBuiltInWallpaper(id: Int) = write { settingsRepository.setWallpaperImage(WallpaperImage.BuiltIn(id)) }

    /** The saved picture picked again; a new version makes Home load it afresh. */
    fun chooseSavedPicture() = write {
        if (wallpaperPictureStore.exists()) {
            settingsRepository.setWallpaperImage(WallpaperImage.Picture(System.currentTimeMillis()))
        } else {
            wallpaperFiles.update { it.copy(pictureSaved = false) }
        }
    }

    /** Reads whether a picture is saved and whether the TV has a file picker (off the main thread). */
    fun refreshWallpaperFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val saved = wallpaperPictureStore.exists()
            val canPick = wallpaperPictureStore.canPickFiles()
            wallpaperFiles.update { it.copy(pictureSaved = saved, canPickFiles = canPick) }
        }
    }

    /** A file came back from the picker: copy it, then show it. The original is never kept. */
    fun importPicture(uri: Uri) {
        if (wallpaperFiles.value.importing) return
        wallpaperFiles.update { it.copy(importing = true, importFailed = false) }
        viewModelScope.launch {
            val ok = wallpaperPictureStore.import(uri)
            if (ok) settingsRepository.setWallpaperImage(WallpaperImage.Picture(System.currentTimeMillis()))
            wallpaperFiles.update {
                it.copy(importing = false, importFailed = !ok, pictureSaved = it.pictureSaved || ok)
            }
        }
    }

    /** "Remove image": deletes the copy; if it was showing, the wallpaper becomes Plain. */
    fun removePicture() = write {
        wallpaperPictureStore.remove()
        settingsRepository.onWallpaperPictureRemoved()
        wallpaperFiles.update { it.copy(pictureSaved = false, importFailed = false) }
    }
    fun setGlassIntensity(value: GlassIntensity) = write { settingsRepository.setGlassIntensity(value) }
    fun setReduceMotion(value: Boolean) = write { settingsRepository.setReduceMotion(value) }
    fun setAnimationSpeed(percent: Int) = write { settingsRepository.setAnimationSpeed(percent) }
    fun setUse24Hour(value: Boolean?) = write { settingsRepository.setUse24Hour(value) }
    fun setShowDate(value: Boolean) = write { settingsRepository.setShowDate(value) }
    fun setTemperatureUnit(value: TemperatureUnit) = write { settingsRepository.setTemperatureUnit(value) }
    fun setCurrency(code: String) = write { settingsRepository.setCurrency(code) }
    /** Writes the switch first, so the check that follows reads the new value. */
    fun setRemindersEnabled(value: Boolean) = write {
        settingsRepository.setRemindersEnabled(value)
        reminderScheduler.setEnabled(value)
    }

    fun setReminderLeadDays(days: Int) = write {
        settingsRepository.setReminderLeadDays(days)
        reminderScheduler.onChanged()
    }

    /** Notifications were allowed again in Android settings: deliver what is due. */
    fun recheckReminders() = write { reminderScheduler.onChanged() }

    fun movePinned(packageName: String, delta: Int) = write { pinnedAppsRepository.move(packageName, delta) }

    /** A search result picked: it becomes the manual city. */
    fun selectCity(city: WeatherCity) {
        searchJob?.cancel()
        citySearch.value = CitySearch.Idle
        write { settingsRepository.setManualCity(city) }
    }

    /** "Use my current city (automatic)". */
    fun useAutomaticCity() {
        searchJob?.cancel()
        citySearch.value = CitySearch.Idle
        write { settingsRepository.setWeatherCityMode(WeatherCityMode.AUTO) }
    }

    /** "Detect again": a fresh network lookup; on failure the last detected city stays. */
    fun detectAgain() {
        if (detectState.value == DetectState.DETECTING) return
        detectState.value = DetectState.DETECTING
        viewModelScope.launch {
            detectState.value = if (cityResolver.detectAgain()) DetectState.IDLE else DetectState.FAILED
        }
    }

    /** Looks [query] up with the weather provider; a newer search replaces one still running. */
    fun searchCity(query: String) {
        if (query.isBlank()) return
        searchJob?.cancel()
        citySearch.value = CitySearch.Searching
        searchJob = viewModelScope.launch {
            citySearch.value = try {
                CitySearch.Results(weatherRepository.searchCity(query))
            } catch (e: IOException) {
                CitySearch.Failed
            }
        }
    }

    fun saveProfile(names: List<String>, activeName: String?, useOwnTvName: Boolean, ownTvName: String) =
        write { profileRepository.save(names, activeName, useOwnTvName, ownTvName) }

    fun dismissFirstRunProfile() = write { profileRepository.markSetupDone() }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = (this[APPLICATION_KEY] as KsApplication).container
                SettingsViewModel(
                    c.settingsRepository,
                    c.profileRepository,
                    c.deviceInfoRepository,
                    c.appsRepository,
                    c.pinnedAppsRepository,
                    c.weatherRepository,
                    c.cityResolver,
                    c.reminderScheduler,
                    c.wallpaperPictureStore,
                    c.updateManager,
                    c.updateScheduler,
                )
            }
        }
    }
}
