package com.ks.tvlauncher.ui.settings

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
import com.ks.tvlauncher.data.weather.CityResolver
import com.ks.tvlauncher.data.weather.ResolvedCity
import com.ks.tvlauncher.data.weather.WeatherRepository
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WeatherCity
import com.ks.tvlauncher.domain.WeatherCityMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

/** The Settings city search. */
sealed interface CitySearch {
    data object Idle : CitySearch
    data object Searching : CitySearch
    data class Results(val cities: List<WeatherCity>) : CitySearch
    data object Failed : CitySearch
}

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
) : ViewModel() {

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
        combine(citySearch, cityResolver.city, detectState, ::Triple),
    ) { settings, profile, device, pinned, (search, city, detect) ->
        SettingsUiState(true, settings, profile, device, pinned, search, city, detect)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private fun write(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    fun setShowMemoryCard(value: Boolean) = write { settingsRepository.setShowMemoryCard(value) }
    fun setDynamicWallpaper(value: Boolean) = write { settingsRepository.setDynamicWallpaper(value) }
    fun setGlassIntensity(value: GlassIntensity) = write { settingsRepository.setGlassIntensity(value) }
    fun setReduceMotion(value: Boolean) = write { settingsRepository.setReduceMotion(value) }
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
                )
            }
        }
    }
}
