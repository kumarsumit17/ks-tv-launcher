package com.ks.tvlauncher.ui.home

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ks.tvlauncher.KsApplication
import com.ks.tvlauncher.data.profile.Profile
import com.ks.tvlauncher.data.profile.ProfileRepository
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.data.settings.SettingsRepository
import com.ks.tvlauncher.data.system.DeviceInfo
import com.ks.tvlauncher.data.system.DeviceInfoRepository
import com.ks.tvlauncher.domain.DeviceNames
import com.ks.tvlauncher.domain.Greeting
import com.ks.tvlauncher.domain.GreetingPeriod
import com.ks.tvlauncher.domain.ProfileNames
import com.ks.tvlauncher.ui.theme.VisualSettings
import com.ks.tvlauncher.util.DateTimeFormats
import com.ks.tvlauncher.util.minuteTicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

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
)

class HomeViewModel(
    private val appContext: Context,
    settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    deviceInfoRepository: DeviceInfoRepository,
) : ViewModel() {

    /**
     * One state object for Home, rebuilt at most once a minute or when a setting changes.
     * Upstream sources (clock broadcast, device-name observer, DataStore) are only active
     * while Home is collecting, and stop 5 s after it goes to the background.
     */
    val uiState: StateFlow<HomeUiState> = combine(
        minuteTicker(appContext),
        settingsRepository.settings,
        profileRepository.profile,
        deviceInfoRepository.deviceInfo,
        ::buildState,
    )
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            // Correct clock and greeting on the very first frame, before storage has been read.
            initialValue = buildState(ZonedDateTime.now(), LauncherSettings(), Profile(), null),
        )

    private val homeKeyPresses = Channel<Unit>(Channel.CONFLATED)

    /** Fires when the Home key is pressed while Home is already showing. */
    val homeKeyEvents: Flow<Unit> = homeKeyPresses.receiveAsFlow()

    fun onHomeKey() {
        homeKeyPresses.trySend(Unit)
    }

    fun switchProfile() {
        viewModelScope.launch { profileRepository.switchToNext() }
    }

    private fun buildState(
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
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as KsApplication
                val container = app.container
                HomeViewModel(
                    appContext = app,
                    settingsRepository = container.settingsRepository,
                    profileRepository = container.profileRepository,
                    deviceInfoRepository = container.deviceInfoRepository,
                )
            }
        }
    }
}
