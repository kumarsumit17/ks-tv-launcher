package com.ks.tvlauncher.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import android.provider.Settings
import com.ks.tvlauncher.data.system.DeviceStats
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.TemperatureUnit
import com.ks.tvlauncher.ui.controlcenter.ControlCenter
import com.ks.tvlauncher.ui.controlcenter.ControlCenterActions
import com.ks.tvlauncher.ui.controlcenter.ControlCenterCard
import com.ks.tvlauncher.ui.controlcenter.ControlCenterValues
import com.ks.tvlauncher.ui.controlcenter.ControlCenterViewModel
import com.ks.tvlauncher.ui.controlcenter.SystemIntents
import com.ks.tvlauncher.ui.settings.ProfileDialog
import com.ks.tvlauncher.ui.settings.SettingsScreen
import com.ks.tvlauncher.ui.settings.SettingsSection
import com.ks.tvlauncher.ui.settings.SettingsViewModel
import com.ks.tvlauncher.ui.widgets.MemoryStorageCard
import com.ks.tvlauncher.ui.widgets.MemoryStoragePanel
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.apps.Wallpaper
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.ui.apps.AppIntents
import com.ks.tvlauncher.ui.apps.AppMenu
import com.ks.tvlauncher.ui.apps.AppMenuState
import com.ks.tvlauncher.ui.apps.AppsDrawer
import com.ks.tvlauncher.ui.apps.AppsDrawerViewModel
import com.ks.tvlauncher.ui.components.FocusMemory
import com.ks.tvlauncher.ui.components.FrostSource
import com.ks.tvlauncher.ui.components.GlassSnackbar
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.subscriptions.SubscriptionsScreen
import com.ks.tvlauncher.ui.subscriptions.SubscriptionsUiState
import com.ks.tvlauncher.ui.subscriptions.SubscriptionsViewModel
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.widgets.GreetingWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

private const val SNACKBAR_MS = 3_000L
private const val MENU_SETTLE_MS = 120L

/** What Home can ask the view model to do. Grouped so the screen takes one parameter for them. */
class HomeActions(
    val onSwitchProfile: () -> Unit,
    val onTileFocused: (String) -> Unit,
    val launchIntent: (String) -> Intent?,
    val onLaunchFailed: (String) -> Unit,
    val onPin: (String) -> Unit,
    val onUnpin: (String) -> Unit,
    val onMove: (String, Int) -> Unit,
    val onDismissMessage: () -> Unit,
    val onSetDynamicWallpaper: (Boolean) -> Unit,
    val onSetClock24Hour: (Boolean) -> Unit,
    /** True while the Memory & storage panel is open, for the Apps / Other split. */
    val onSetStatsBreakdown: (Boolean) -> Unit,
    val usageAccessGranted: () -> Boolean,
)

/** Where the Memory & storage panel was opened from, so focus can go back there. */
private enum class MemoryOpener { CARD, CONTROL_CENTER }

// Keys for the Home controls whose focus is restored after Control Center or Settings.
private const val KEY_ROW = "row"
private const val KEY_WEATHER = "weather"
private const val KEY_GEAR = "gear"
private const val KEY_CHIP = "chip"
private const val KEY_HERO_OPEN = "heroOpen"
private const val KEY_HERO_UNPIN = "heroUnpin"
private const val KEY_ALL_APPS = "allApps"
private const val KEY_SUBS = "subs"
private const val KEY_MEMORY = "memory"
private const val KEY_CC = "cc"

/** Remembers [key] as the Home control focused last. */
private fun Modifier.trackFocus(memory: FocusMemory, key: String): Modifier =
    focusRequester(memory.requester(key)).onFocusChanged { if (it.isFocused) memory.lastKey = key }

/**
 * The main screen: top bar, greeting, hero, pinned row and the bottom strip, with the All apps
 * drawer and the app menu drawn over it.
 *
 * Focus order, top to bottom: top bar, greeting chip, hero buttons, pinned row, bottom strip.
 * Opening an overlay moves focus into it; closing it returns focus to the control that opened it.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    wallpaper: Wallpaper?,
    subscriptions: SubscriptionsUiState,
    deviceStats: StateFlow<DeviceStats?>,
    reopenEvents: Flow<Unit>,
    actions: HomeActions,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val rowState = rememberLazyListState()
    val rowMemory = remember { FocusMemory() }
    val gridMemory = remember { FocusMemory() }
    val heroFocus = remember { FocusRequester() }
    val allAppsFocus = remember { FocusRequester() }
    val drawerFallbackFocus = remember { FocusRequester() }
    val subscriptionsFocus = remember { FocusRequester() }

    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var subscriptionsOpen by rememberSaveable { mutableStateOf(false) }
    var settingsAt by rememberSaveable { mutableStateOf<SettingsSection?>(null) }
    var controlCenterOpen by rememberSaveable { mutableStateOf(false) }
    var memoryOpener by rememberSaveable { mutableStateOf<MemoryOpener?>(null) }
    var firstRunProfileOpen by rememberSaveable { mutableStateOf(false) }
    var firstRunProfileShown by rememberSaveable { mutableStateOf(false) }
    val homeMemory = remember { FocusMemory() }
    // Builds no flows until Settings or the profile dialog collects them.
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val ccMemoryFocus = remember { FocusRequester() }
    // The app whose menu is open, from either the pinned row or the drawer.
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }
    val anyOverlay = drawerOpen || subscriptionsOpen || settingsAt != null || controlCenterOpen ||
        memoryOpener != null || firstRunProfileOpen || menuApp != null
    var initialFocusDone by remember { mutableStateOf(false) }
    val hasPinned = state.pinned.isNotEmpty()
    // Read through updated state so long-running effects always see the current pins.
    val pinnedKeys by rememberUpdatedState(state.pinned.map { it.packageName })

    /** The pinned tile focused last (else the first one); the All apps card if none are pinned. */
    fun rowTarget(): FocusRequester = rowMemory.target(pinnedKeys, fallback = allAppsFocus)

    // The Home control focused when Control Center, Settings or the first-run dialog opened.
    // Captured at open time: while an overlay closes, focus briefly lands on another Home
    // control, which would otherwise overwrite homeMemory.lastKey (seen on the TV).
    var restoreKey by rememberSaveable { mutableStateOf<String?>(null) }

    /** Back to the Home control focused before an overlay opened; the pinned row by default. */
    fun restoreHomeFocus() {
        val key = restoreKey ?: homeMemory.lastKey
        val target = when {
            key == null || key == KEY_ROW -> rowTarget()
            (key == KEY_HERO_OPEN || key == KEY_HERO_UNPIN) && !hasPinned -> allAppsFocus
            key == KEY_MEMORY && !state.showMemoryCard -> allAppsFocus
            key == KEY_CHIP && state.nextName == null -> rowTarget()
            else -> homeMemory.requester(key)
        }
        target.requestFocusSafely()
    }

    fun openControlCenter() {
        if (!anyOverlay) controlCenterOpen = true
    }

    fun open(app: AppInfo) {
        if (!AppIntents.start(context, actions.launchIntent(app.packageName))) actions.onLaunchFailed(app.label)
    }

    // Initial focus: the first pinned tile once the apps have loaded, else the All apps card.
    LaunchedEffect(state.appsLoaded) {
        if (state.appsLoaded && !initialFocusDone) {
            withFrameNanos { }
            if (!anyOverlay) rowTarget().requestFocusSafely()
            initialFocusDone = true
        }
    }
    // The last pinned app was unpinned from the hero: its buttons are gone, so move focus on.
    LaunchedEffect(hasPinned) {
        if (initialFocusDone && !hasPinned && !anyOverlay) {
            withFrameNanos { }
            allAppsFocus.requestFocusSafely()
        }
    }
    // KS reopened from the TV's launcher while it was in the background: close overlays, keep
    // the row where it was, and return focus to the last focused tile.
    LaunchedEffect(reopenEvents) {
        reopenEvents.collect {
            menuApp = null
            drawerOpen = false
            subscriptionsOpen = false
            settingsAt = null
            controlCenterOpen = false
            memoryOpener = null
            if (firstRunProfileOpen) {
                firstRunProfileOpen = false
                settingsViewModel.dismissFirstRunProfile()
            }
            homeMemory.lastKey = KEY_ROW
            withFrameNanos { }
            rowTarget().requestFocusSafely()
        }
    }
    // When the app menu closes, put focus back on the tile it was opened from. This runs after
    // the menu has left the screen (asking while it is still closing was seen to fail on a
    // real TV), and after a short pause so a pin, unpin or move has reached the list.
    var menuWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(menuApp) {
        if (menuApp != null) {
            menuWasOpen = true
        } else if (menuWasOpen) {
            menuWasOpen = false
            delay(MENU_SETTLE_MS)
            if (drawerOpen) {
                gridMemory.last(drawerFallbackFocus).requestFocusSafely()
            } else {
                rowTarget().requestFocusSafely()
            }
        }
    }
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(SNACKBAR_MS)
            actions.onDismissMessage()
        }
    }

    val menu: @Composable () -> Unit = {
        menuApp?.let { app ->
            AppMenu(
                state = AppMenuState(
                    app = app,
                    pinned = state.pinned.any { it.packageName == app.packageName },
                    canMoveLeft = HomeViewModel.canMove(state.pinned, app.packageName, -1),
                    canMoveRight = HomeViewModel.canMove(state.pinned, app.packageName, 1),
                ),
                onDismiss = { menuApp = null },
                onOpen = {
                    menuApp = null
                    open(app)
                },
                onPin = {
                    actions.onPin(app.packageName)
                    menuApp = null
                },
                onUnpin = {
                    actions.onUnpin(app.packageName)
                    menuApp = null
                },
                onMove = { delta -> actions.onMove(app.packageName, delta) },
                onAppInfo = {
                    menuApp = null
                    AppIntents.start(context, AppIntents.appInfo(app.packageName))
                },
                onUninstall = {
                    menuApp = null
                    AppIntents.start(context, AppIntents.uninstall(app.packageName))
                },
                // Focus return is handled above, once the menu has gone.
                returnFocusTo = null,
            )
        }
    }

    // First run: once Home has settled, ask who's watching.
    LaunchedEffect(initialFocusDone, state.profileSetupNeeded) {
        if (initialFocusDone && state.profileSetupNeeded && !firstRunProfileShown && !anyOverlay) {
            firstRunProfileShown = true
            firstRunProfileOpen = true
        }
    }
    // Control Center, Settings or the first-run dialog closed: return to where focus was,
    // unless another overlay (Subscriptions, Settings) took over.
    var focusOverlayWasOpen by remember { mutableStateOf(false) }
    val focusOverlayOpen = controlCenterOpen || settingsAt != null || firstRunProfileOpen
    LaunchedEffect(focusOverlayOpen) {
        if (focusOverlayOpen) {
            if (!focusOverlayWasOpen) restoreKey = homeMemory.lastKey
            focusOverlayWasOpen = true
        } else if (focusOverlayWasOpen) {
            focusOverlayWasOpen = false
            delay(MENU_SETTLE_MS)
            if (!anyOverlay) restoreHomeFocus()
            restoreKey = null
        }
    }
    // The Apps / Other split is read only while the panel is open.
    LaunchedEffect(memoryOpener != null) { actions.onSetStatsBreakdown(memoryOpener != null) }
    val stats: DeviceStats? = if (state.showMemoryCard || memoryOpener != null) {
        deviceStats.collectAsStateWithLifecycle().value
    } else {
        null
    }

    val memoryPanel: @Composable () -> Unit = {
        memoryOpener?.let { opener ->
            val usageGranted = remember(stats) { actions.usageAccessGranted() }
            MemoryStoragePanel(
                stats = stats,
                usageAccessGranted = usageGranted,
                onManageApps = { SystemIntents.openSettings(context, Settings.ACTION_APPLICATION_SETTINGS) },
                onFreeUpSpace = { SystemIntents.openSettings(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS) },
                onEnableUsageAccess = { SystemIntents.openSettings(context, Settings.ACTION_USAGE_ACCESS_SETTINGS) },
                onDismiss = { memoryOpener = null },
                returnFocusTo = if (opener == MemoryOpener.CARD) homeMemory.requester(KEY_MEMORY) else ccMemoryFocus,
            )
        }
    }

    FrostSource(
        modifier = modifier
            .fillMaxSize()
            // MENU opens Control Center when nothing else is open, and closes it again.
            .onPreviewKeyEvent { event ->
                if (event.key != Key.Menu) return@onPreviewKeyEvent false
                when {
                    controlCenterOpen && memoryOpener == null -> {
                        if (event.type == KeyEventType.KeyUp) controlCenterOpen = false
                        true
                    }
                    !anyOverlay -> {
                        if (event.type == KeyEventType.KeyUp) controlCenterOpen = true
                        true
                    }
                    else -> false
                }
            },
        background = { HomeBackground(wallpaper) },
    ) {
        Column(Modifier.fillMaxSize().padding(vertical = KsDimens.OverscanVertical)) {
            HomeTopBar(
                time = state.time,
                shortDate = state.shortDate,
                weather = state.weather,
                temperatureUnit = state.temperatureUnit,
                onOpenWeather = { settingsAt = SettingsSection.WEATHER },
                onOpenSettings = { settingsAt = SettingsSection.PROFILE },
                onLongPress = ::openControlCenter,
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
                weatherModifier = Modifier.trackFocus(homeMemory, KEY_WEATHER),
                gearModifier = Modifier.trackFocus(homeMemory, KEY_GEAR),
            )
            Spacer(Modifier.height(12.dp))
            GreetingWidget(
                greeting = state.greeting,
                name = state.name,
                longDate = state.longDate,
                tvName = state.tvName,
                androidVersion = state.androidVersion,
                nextName = state.nextName,
                onSwitchProfile = actions.onSwitchProfile,
                onLongPress = ::openControlCenter,
                chipModifier = Modifier.trackFocus(homeMemory, KEY_CHIP),
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal).width(500.dp),
            )
            Spacer(Modifier.weight(1f))
            HomeHero(
                app = state.hero,
                openFocus = heroFocus,
                downFocus = ::rowTarget,
                onOpen = ::open,
                onUnpin = { actions.onUnpin(it.packageName) },
                onLongPress = ::openControlCenter,
                unpinFocus = homeMemory.requester(KEY_HERO_UNPIN),
                onButtonFocused = { isOpen -> homeMemory.lastKey = if (isOpen) KEY_HERO_OPEN else KEY_HERO_UNPIN },
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
            )
            Spacer(Modifier.height(8.dp))
            if (hasPinned) {
                PinnedRow(
                    apps = state.pinned,
                    listState = rowState,
                    focusMemory = rowMemory,
                    upFocus = heroFocus,
                    downFocus = allAppsFocus,
                    onOpen = ::open,
                    onOpenMenu = { menuApp = it },
                    onTileFocused = {
                        homeMemory.lastKey = KEY_ROW
                        actions.onTileFocused(it.packageName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
            }
            // With pinned apps, Up from any card returns to the tile that was focused last.
            val upToRow = Modifier.focusProperties { up = if (hasPinned) rowTarget() else FocusRequester.Default }
            Row(
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AllAppsCard(
                    installedCount = state.installedCount,
                    onClick = { drawerOpen = true },
                    onLongClick = ::openControlCenter,
                    modifier = Modifier
                        .focusRequester(allAppsFocus)
                        .onFocusChanged { if (it.isFocused) homeMemory.lastKey = KEY_ALL_APPS }
                        .then(upToRow),
                )
                SubscriptionsCard(
                    state = subscriptions,
                    onClick = { subscriptionsOpen = true },
                    onLongClick = ::openControlCenter,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(subscriptionsFocus)
                        .onFocusChanged { if (it.isFocused) homeMemory.lastKey = KEY_SUBS }
                        .then(upToRow),
                )
                if (state.showMemoryCard) {
                    MemoryStorageCard(
                        stats = stats,
                        onClick = { memoryOpener = MemoryOpener.CARD },
                        onLongClick = ::openControlCenter,
                        modifier = Modifier.width(230.dp).trackFocus(homeMemory, KEY_MEMORY).then(upToRow),
                    )
                }
                ControlCenterCard(
                    onClick = ::openControlCenter,
                    modifier = Modifier.trackFocus(homeMemory, KEY_CC).then(upToRow),
                )
            }
        }

        if (drawerOpen) {
            val drawerViewModel: AppsDrawerViewModel = viewModel(factory = AppsDrawerViewModel.Factory)
            val drawerState by drawerViewModel.uiState.collectAsStateWithLifecycle()
            AppsDrawer(
                state = drawerState,
                focusMemory = gridMemory,
                fallbackFocus = drawerFallbackFocus,
                returnFocusTo = allAppsFocus,
                onClose = {
                    menuApp = null
                    drawerOpen = false
                    drawerViewModel.reset()
                },
                onQueryChange = drawerViewModel::setQuery,
                onCategoryChange = drawerViewModel::setCategory,
                onToggleSort = drawerViewModel::toggleSort,
                onToggleEdit = drawerViewModel::toggleEditMode,
                onOpenApp = ::open,
                onOpenMenu = { menuApp = it },
                // Inside the drawer the menu belongs to the drawer's focus group.
                overlay = { menu() },
            )
        } else {
            menu()
        }

        if (controlCenterOpen) {
            val ccViewModel: ControlCenterViewModel = viewModel(factory = ControlCenterViewModel.Factory)
            ControlCenter(
                viewModel = ccViewModel,
                values = ControlCenterValues(
                    time = state.time,
                    dynamicWallpaper = state.visual.dynamicWallpaper,
                    clock24Hour = state.clock24Hour,
                    weather = stringResource(
                        R.string.cc_weather_value,
                        state.weatherCity?.let { resolved ->
                            if (resolved.automatic) stringResource(R.string.cc_weather_auto, resolved.city.name) else resolved.city.name
                        } ?: stringResource(R.string.weather_set_city),
                        stringResource(if (state.temperatureUnit == TemperatureUnit.CELSIUS) R.string.unit_celsius else R.string.unit_fahrenheit),
                    ),
                    subscriptions = Money.currency(state.currency).getSymbol(Money.localeFor(state.currency)) + " " + state.currency,
                ),
                actions = ControlCenterActions(
                    onDismiss = { controlCenterOpen = false },
                    onSetDynamicWallpaper = actions.onSetDynamicWallpaper,
                    onSetClock24Hour = actions.onSetClock24Hour,
                    onOpenWeather = {
                        controlCenterOpen = false
                        settingsAt = SettingsSection.WEATHER
                    },
                    onOpenSubscriptions = {
                        controlCenterOpen = false
                        subscriptionsOpen = true
                    },
                    onOpenMemory = { memoryOpener = MemoryOpener.CONTROL_CENTER },
                ),
                memoryFocus = ccMemoryFocus,
                coveredByDialog = memoryOpener != null,
                overlay = { if (memoryOpener == MemoryOpener.CONTROL_CENTER) memoryPanel() },
            )
        }

        // From the card the panel sits over Home; from Control Center it is drawn inside it.
        if (memoryOpener == MemoryOpener.CARD) memoryPanel()

        if (subscriptionsOpen) {
            val subsViewModel: SubscriptionsViewModel = viewModel(factory = SubscriptionsViewModel.Factory)
            val subsState by subsViewModel.uiState.collectAsStateWithLifecycle()
            SubscriptionsScreen(
                state = subsState,
                onClose = { subscriptionsOpen = false },
                onSave = subsViewModel::save,
                onDelete = subsViewModel::delete,
                onToggleActive = subsViewModel::toggleActive,
                exportCsv = subsViewModel::exportCsv,
            )
        }
        // Back from Subscriptions: focus returns to the Home card that opened it.
        var subscriptionsWasOpen by remember { mutableStateOf(false) }
        LaunchedEffect(subscriptionsOpen) {
            if (subscriptionsOpen) {
                subscriptionsWasOpen = true
            } else if (subscriptionsWasOpen) {
                subscriptionsWasOpen = false
                withFrameNanos { }
                subscriptionsFocus.requestFocusSafely()
            }
        }

        settingsAt?.let { section ->
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                state = settingsState,
                viewModel = settingsViewModel,
                startAt = section,
                onClose = { settingsAt = null },
            )
        }

        if (firstRunProfileOpen) {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            if (settingsState.loaded) {
                ProfileDialog(
                    profile = settingsState.profile,
                    device = settingsState.device,
                    onSave = { names, active, useOwn, own ->
                        settingsViewModel.saveProfile(names, active, useOwn, own)
                        firstRunProfileOpen = false
                    },
                    onDismiss = {
                        settingsViewModel.dismissFirstRunProfile()
                        firstRunProfileOpen = false
                    },
                    returnFocusTo = null,
                )
            }
        }

        state.message?.let { message ->
            Box(Modifier.fillMaxSize().padding(bottom = 36.dp), contentAlignment = Alignment.BottomCenter) {
                GlassSnackbar(
                    message = when (message) {
                        is HomeMessage.AppMissing -> stringResource(R.string.app_missing, message.label)
                    },
                )
            }
        }
    }
}
