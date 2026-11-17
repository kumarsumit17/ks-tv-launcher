package com.ks.tvlauncher.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.BuildConfig
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.reminders.ReminderNotifier
import com.ks.tvlauncher.data.settings.GlassIntensity
import com.ks.tvlauncher.domain.AnimationSpeed
import com.ks.tvlauncher.domain.RenewalReminders
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.domain.WeatherCityMode
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.GlassSearchField
import com.ks.tvlauncher.ui.components.GlassSection
import com.ks.tvlauncher.ui.components.GlassStepper
import com.ks.tvlauncher.ui.components.GlassToggle
import com.ks.tvlauncher.ui.components.modalFocus
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.controlcenter.SystemIntents
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsType
import kotlinx.coroutines.delay

/** Where Settings opens. */
enum class SettingsSection { PROFILE, WEATHER, ABOUT }

/** Currencies offered in the picker (ISO 4217). Subscriptions and the CSV use the chosen one. */
private val CURRENCIES = listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED")

// LazyColumn item indexes, so Settings can open scrolled to a section.
private const val ITEM_PROFILE = 0
private const val ITEM_WEATHER = 5
private const val ITEM_ABOUT = 9

private const val DIALOG_SETTLE_MS = 120L

/**
 * Every launcher setting, persisted in DataStore and reachable with the D-pad. Drawn opaque
 * over Home; Back (or the Home button) calls [onClose]. The profile dialog opens on top.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    viewModel: SettingsViewModel,
    startAt: SettingsSection,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val profileFocus = remember { FocusRequester() }
    val cityFocus = remember { FocusRequester() }
    val weatherFocus = remember { FocusRequester() }
    val aboutFocus = remember { FocusRequester() }
    var profileOpen by rememberSaveable { mutableStateOf(false) }
    var cityQuery by rememberSaveable { mutableStateOf("") }
    // The focused control (a result row, a mode button) is about to disappear: move focus to
    // a control that stays, once the new layout is in place.
    var refocusTarget by remember { mutableStateOf<FocusRequester?>(null) }
    LaunchedEffect(refocusTarget) {
        val target = refocusTarget ?: return@LaunchedEffect
        withFrameNanos { }
        withFrameNanos { }
        target.requestFocusSafely()
        refocusTarget = null
    }

    BackHandler(enabled = !profileOpen, onBack = onClose)

    LaunchedEffect(Unit) {
        when (startAt) {
            SettingsSection.PROFILE -> {
                withFrameNanos { }
                profileFocus.requestFocusSafely()
            }
            SettingsSection.WEATHER -> {
                listState.scrollToItem(ITEM_WEATHER)
                withFrameNanos { }
                weatherFocus.requestFocusSafely()
            }
            SettingsSection.ABOUT -> {
                listState.scrollToItem(ITEM_ABOUT)
                withFrameNanos { }
                aboutFocus.requestFocusSafely()
            }
        }
    }
    // After the profile dialog has gone, focus returns to the row that opened it.
    var profileWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(profileOpen) {
        if (profileOpen) {
            profileWasOpen = true
        } else if (profileWasOpen) {
            profileWasOpen = false
            delay(DIALOG_SETTLE_MS)
            listState.scrollToItem(ITEM_PROFILE)
            withFrameNanos { }
            profileFocus.requestFocusSafely()
        }
    }

    val settings = state.settings
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KsColors.BackgroundTop, KsColors.BackgroundBottom)))
            .modalFocus(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = KsDimens.OverscanHorizontal, vertical = KsDimens.OverscanVertical)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassButton(text = stringResource(R.string.settings_back), icon = KsIcons.ChevronLeft, onClick = onClose)
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.settings_title), style = KsType.HeroTitle, color = KsColors.TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    // Left on the first control of a row stays put: it must never jump to the
                    // Home button at the top left (OK there would close Settings). Up still
                    // reaches it.
                    .focusProperties {
                        onExit = { if (requestedFocusDirection == FocusDirection.Left) cancelFocusChange() }
                    }
                    .focusGroup(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            ) {
                item(key = "profile") {
                    GlassSection(stringResource(R.string.settings_profile)) {
                        val names = state.profile.names
                        ValueRow(
                            label = stringResource(R.string.settings_whos_watching),
                            value = if (names.isEmpty()) stringResource(R.string.settings_no_names) else names.joinToString(", "),
                            onClick = { profileOpen = true },
                            modifier = Modifier.focusRequester(profileFocus),
                        )
                    }
                }
                item(key = "home") {
                    GlassSection(stringResource(R.string.settings_home)) {
                        GlassToggle(stringResource(R.string.settings_show_memory), settings.showMemoryCard, viewModel::setShowMemoryCard)
                    }
                }
                item(key = "wallpaper") {
                    GlassSection(stringResource(R.string.settings_wallpaper)) {
                        WallpaperSettings(
                            settings = settings,
                            files = state.wallpaperFiles,
                            viewModel = viewModel,
                            onRefocus = { refocusTarget = it },
                        )
                    }
                }
                item(key = "look") {
                    GlassSection(stringResource(R.string.settings_look)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceRow(stringResource(R.string.settings_glass)) {
                                GlassIntensity.entries.forEach { level ->
                                    GlassChip(
                                        text = stringResource(
                                            when (level) {
                                                GlassIntensity.LOW -> R.string.settings_glass_low
                                                GlassIntensity.MEDIUM -> R.string.settings_glass_medium
                                                GlassIntensity.HIGH -> R.string.settings_glass_high
                                            },
                                        ),
                                        selected = settings.glassIntensity == level,
                                        onClick = { viewModel.setGlassIntensity(level) },
                                    )
                                }
                            }
                            GlassToggle(stringResource(R.string.settings_reduce_motion), settings.reduceMotion, viewModel::setReduceMotion)
                            ChoiceRow(stringResource(R.string.settings_animation_speed)) {
                                GlassStepper(
                                    value = AnimationSpeed.label(settings.animationSpeed),
                                    onStep = { direction, _ ->
                                        val next = AnimationSpeed.step(settings.animationSpeed, direction)
                                        if (next != settings.animationSpeed) viewModel.setAnimationSpeed(next)
                                    },
                                )
                            }
                            Text(
                                stringResource(
                                    if (settings.reduceMotion) R.string.settings_animation_speed_off else R.string.settings_animation_speed_hint,
                                ),
                                style = KsType.Caption,
                                color = KsColors.TextSecondary,
                            )
                        }
                    }
                }
                item(key = "clock") {
                    GlassSection(stringResource(R.string.settings_clock)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChoiceRow(stringResource(R.string.cc_clock)) {
                                GlassChip(stringResource(R.string.settings_clock_auto), settings.use24Hour == null, { viewModel.setUse24Hour(null) })
                                GlassChip(stringResource(R.string.cc_12_hour), settings.use24Hour == false, { viewModel.setUse24Hour(false) })
                                GlassChip(stringResource(R.string.cc_24_hour), settings.use24Hour == true, { viewModel.setUse24Hour(true) })
                            }
                            GlassToggle(stringResource(R.string.settings_show_date), settings.showDate, viewModel::setShowDate)
                        }
                    }
                }
                item(key = "weather") {
                    GlassSection(stringResource(R.string.settings_weather)) {
                        WeatherSettings(
                            state = state,
                            query = cityQuery,
                            onQueryChange = { cityQuery = it },
                            sectionFocus = weatherFocus,
                            fieldFocus = cityFocus,
                            viewModel = viewModel,
                            onCityChosen = { cityQuery = "" },
                            onRefocus = { refocusTarget = it },
                        )
                    }
                }
                item(key = "pinned") {
                    GlassSection(stringResource(R.string.settings_pinned)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.pinned.isEmpty()) {
                                Text(stringResource(R.string.settings_pinned_empty), style = KsType.Body, color = KsColors.TextSecondary)
                            }
                            // Keyed, so focus follows the moved app. The buttons never disable
                            // (a move past either end does nothing): a button that disables
                            // under focus drops focus somewhere unpredictable.
                            state.pinned.forEachIndexed { index, app ->
                                key(app.packageName) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${index + 1}. ${app.label}",
                                        style = KsType.Label,
                                        color = KsColors.TextPrimary,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    GlassButton(
                                        text = stringResource(R.string.menu_move_left),
                                        icon = KsIcons.ChevronLeft,
                                        onClick = { viewModel.movePinned(app.packageName, -1) },
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    GlassButton(
                                        text = stringResource(R.string.menu_move_right),
                                        icon = KsIcons.ChevronRight,
                                        onClick = { viewModel.movePinned(app.packageName, 1) },
                                    )
                                }
                                }
                            }
                        }
                    }
                }
                item(key = "currency") {
                    GlassSection(stringResource(R.string.settings_currency)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CURRENCIES.forEach { code ->
                                GlassChip(code, settings.currency == code, { viewModel.setCurrency(code) })
                            }
                        }
                    }
                }
                item(key = "reminders") {
                    GlassSection(stringResource(R.string.settings_reminders)) {
                        ReminderSettings(
                            enabled = settings.remindersEnabled,
                            leadDays = settings.reminderLeadDays,
                            viewModel = viewModel,
                        )
                    }
                }
                item(key = "about") {
                    GlassSection(stringResource(R.string.settings_about)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                                style = KsType.Label,
                                color = KsColors.TextPrimary,
                            )
                            state.device?.let {
                                Text(
                                    stringResource(R.string.settings_device, it.model, it.androidRelease, it.sdkInt),
                                    style = KsType.Body,
                                    color = KsColors.TextSecondary,
                                )
                            }
                            if (BuildConfig.IN_APP_UPDATES) {
                                UpdateSettings(settings.autoUpdateCheck, viewModel, checkFocus = aboutFocus)
                            } else {
                                // A test build is signed with another key: no release can update it.
                                Text(stringResource(R.string.settings_update_debug), style = KsType.Caption, color = KsColors.TextSecondary)
                            }
                            ValueRow(
                                label = stringResource(R.string.cc_android_settings),
                                value = stringResource(R.string.cc_android_settings_value),
                                trailing = KsIcons.ArrowUpRight,
                                onClick = { SystemIntents.openSettings(context, Settings.ACTION_SETTINGS) },
                                modifier = if (BuildConfig.IN_APP_UPDATES) Modifier else Modifier.focusRequester(aboutFocus),
                            )
                        }
                    }
                }
            }
        }

        if (profileOpen) {
            ProfileDialog(
                profile = state.profile,
                device = state.device,
                onSave = { names, active, useOwn, own ->
                    viewModel.saveProfile(names, active, useOwn, own)
                    profileOpen = false
                },
                onDismiss = { profileOpen = false },
                // Focus return is handled above, once the dialog has gone.
                returnFocusTo = null,
            )
        }
    }
}

@Composable
private fun WeatherSettings(
    state: SettingsUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    sectionFocus: FocusRequester,
    fieldFocus: FocusRequester,
    viewModel: SettingsViewModel,
    onCityChosen: () -> Unit,
    onRefocus: (FocusRequester) -> Unit,
) {
    val settings = state.settings
    val manual = settings.weatherCityMode == WeatherCityMode.MANUAL
    // "Change city" in automatic mode reveals the search field.
    var changing by rememberSaveable { mutableStateOf(false) }
    val showSearch = manual || changing
    val resolved = state.weatherCity

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = when {
                resolved != null -> stringResource(
                    R.string.settings_city_line,
                    resolved.city.description,
                    stringResource(if (resolved.automatic) R.string.weather_mode_auto else R.string.weather_mode_manual),
                )
                manual -> stringResource(R.string.settings_city_none_manual)
                else -> stringResource(R.string.settings_city_none_auto)
            },
            style = KsType.Label,
            color = KsColors.TextPrimary,
        )
        Text(stringResource(R.string.settings_city_privacy), style = KsType.Caption, color = KsColors.TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (manual) {
                GlassButton(
                    text = stringResource(R.string.settings_use_automatic),
                    onClick = {
                        changing = false
                        viewModel.useAutomaticCity()
                        onRefocus(sectionFocus)
                    },
                    modifier = Modifier.focusRequester(sectionFocus),
                )
            } else {
                // Never disabled while detecting: a button that disables under focus drops focus.
                GlassButton(
                    text = stringResource(R.string.settings_detect_again),
                    onClick = viewModel::detectAgain,
                    modifier = Modifier.focusRequester(sectionFocus),
                )
                if (!changing) {
                    GlassButton(
                        text = stringResource(R.string.settings_change_city),
                        onClick = {
                            changing = true
                            onRefocus(fieldFocus)
                        },
                    )
                }
            }
        }
        when (state.detect) {
            DetectState.IDLE -> Unit
            DetectState.DETECTING -> Text(stringResource(R.string.settings_detecting), style = KsType.Caption, color = KsColors.TextSecondary)
            DetectState.FAILED -> Text(stringResource(R.string.settings_detect_failed), style = KsType.Caption, color = KsColors.Warning)
        }
        ChoiceRow(stringResource(R.string.settings_units)) {
            GlassChip(stringResource(R.string.unit_celsius), settings.temperatureUnit == TemperatureUnit.CELSIUS, {
                viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
            })
            GlassChip(stringResource(R.string.unit_fahrenheit), settings.temperatureUnit == TemperatureUnit.FAHRENHEIT, {
                viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
            })
        }
        if (showSearch) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassSearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = stringResource(R.string.settings_city_hint),
                    imeAction = ImeAction.Done,
                    maxLength = 60,
                    modifier = Modifier.weight(1f).focusRequester(fieldFocus),
                )
                Spacer(Modifier.width(8.dp))
                GlassButton(
                    text = stringResource(R.string.settings_city_search),
                    onClick = { viewModel.searchCity(query) },
                )
            }
            when (val search = state.citySearch) {
                CitySearch.Idle -> Unit
                CitySearch.Searching -> Text(stringResource(R.string.settings_city_searching), style = KsType.Caption, color = KsColors.TextSecondary)
                CitySearch.Failed -> Text(stringResource(R.string.settings_city_failed), style = KsType.Caption, color = KsColors.Warning)
                is CitySearch.Results -> {
                    if (search.cities.isEmpty()) {
                        Text(stringResource(R.string.settings_city_no_match), style = KsType.Caption, color = KsColors.TextSecondary)
                    }
                    search.cities.forEach { place ->
                        ValueRow(
                            label = place.description,
                            value = "",
                            onClick = {
                                viewModel.selectCity(place)
                                onCityChosen()
                                onRefocus(sectionFocus)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reminders on/off and the lead time. Turning them on asks for the notification permission on
 * Android 13+; if notifications are blocked, a line says so with a way to Android settings.
 */
@Composable
private fun ReminderSettings(enabled: Boolean, leadDays: Int, viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val notifier = remember { ReminderNotifier(context) }
    var canNotify by remember { mutableStateOf(notifier.canNotify()) }
    // Set when the user declined the permission, so the blocked line shows even while off.
    var declined by rememberSaveable { mutableStateOf(false) }
    val toggleFocus = remember { FocusRequester() }
    // Back from Android settings with notifications allowed, the focused "Open notification
    // settings" row disappears: hand focus to the toggle instead of letting it escape.
    var openedSettings by remember { mutableStateOf(false) }
    var refocusToggle by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        val now = notifier.canNotify()
        if (now && !canNotify && enabled) viewModel.recheckReminders()
        canNotify = now
        if (openedSettings) {
            openedSettings = false
            refocusToggle = now
        }
        onPauseOrDispose { }
    }
    LaunchedEffect(refocusToggle) {
        if (!refocusToggle) return@LaunchedEffect
        withFrameNanos { }
        withFrameNanos { }
        toggleFocus.requestFocusSafely()
        refocusToggle = false
    }
    val showBlocked = !canNotify && (enabled || declined)
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        canNotify = notifier.canNotify()
        declined = !granted
        if (granted) viewModel.setRemindersEnabled(true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassToggle(stringResource(R.string.settings_reminders_toggle), enabled, modifier = Modifier.focusRequester(toggleFocus), onCheckedChange = { on ->
            val needsAsk = on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            if (needsAsk) {
                askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setRemindersEnabled(on)
            }
        })
        if (enabled) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_reminder_lead), style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.width(140.dp))
                // Never disables at the ends (1 and 14 days): a step past either end does nothing.
                GlassStepper(
                    value = pluralStringResource(R.plurals.settings_reminder_days, leadDays, leadDays),
                    onStep = { direction, _ ->
                        val next = RenewalReminders.clampLeadDays(leadDays + direction)
                        if (next != leadDays) viewModel.setReminderLeadDays(next)
                    },
                )
            }
        }
        Text(stringResource(R.string.settings_reminders_note), style = KsType.Caption, color = KsColors.TextSecondary)
        if (showBlocked) {
            Text(stringResource(R.string.settings_reminders_blocked), style = KsType.Caption, color = KsColors.Warning)
            ValueRow(
                label = stringResource(R.string.settings_reminders_open),
                value = "",
                trailing = KsIcons.ArrowUpRight,
                onClick = { openedSettings = SystemIntents.openAppNotificationSettings(context) },
            )
        }
    }
}

/** A label and a row of chips. */
@Composable
internal fun ChoiceRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.width(140.dp))
        content()
    }
}

/** A focusable row: label on the left, current value and a chevron on the right. */
@Composable
internal fun ValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: androidx.compose.ui.graphics.vector.ImageVector = KsIcons.ChevronRight,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (value.isNotEmpty()) {
                Text(value, style = KsType.Caption, color = KsColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(4.dp))
            }
            Icon(trailing, contentDescription = null, modifier = Modifier.size(12.dp), tint = KsColors.TextSecondary)
        }
    }
}
