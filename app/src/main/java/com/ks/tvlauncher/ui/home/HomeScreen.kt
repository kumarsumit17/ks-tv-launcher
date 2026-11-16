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
    val onOpenSettings: () -> Unit,
    val onTileFocused: (String) -> Unit,
    val launchIntent: (String) -> Intent?,
    val onLaunchFailed: (String) -> Unit,
    val onPin: (String) -> Unit,
    val onUnpin: (String) -> Unit,
    val onMove: (String, Int) -> Unit,
    val onDismissMessage: () -> Unit,
)

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
    // The app whose menu is open, from either the pinned row or the drawer.
    var menuApp by remember { mutableStateOf<AppInfo?>(null) }
    var initialFocusDone by remember { mutableStateOf(false) }
    val hasPinned = state.pinned.isNotEmpty()
    // Read through updated state so long-running effects always see the current pins.
    val pinnedKeys by rememberUpdatedState(state.pinned.map { it.packageName })

    /** The pinned tile focused last (else the first one); the All apps card if none are pinned. */
    fun rowTarget(): FocusRequester = rowMemory.target(pinnedKeys, fallback = allAppsFocus)

    fun open(app: AppInfo) {
        if (!AppIntents.start(context, actions.launchIntent(app.packageName))) actions.onLaunchFailed(app.label)
    }

    // Initial focus: the first pinned tile once the apps have loaded, else the All apps card.
    LaunchedEffect(state.appsLoaded) {
        if (state.appsLoaded && !initialFocusDone) {
            withFrameNanos { }
            rowTarget().requestFocusSafely()
            initialFocusDone = true
        }
    }
    // The last pinned app was unpinned from the hero: its buttons are gone, so move focus on.
    LaunchedEffect(hasPinned) {
        if (initialFocusDone && !hasPinned && !drawerOpen && !subscriptionsOpen) {
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

    FrostSource(modifier = modifier.fillMaxSize(), background = { HomeBackground(wallpaper) }) {
        Column(Modifier.fillMaxSize().padding(vertical = KsDimens.OverscanVertical)) {
            HomeTopBar(
                time = state.time,
                shortDate = state.shortDate,
                onOpenSettings = actions.onOpenSettings,
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
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
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal).width(500.dp),
            )
            Spacer(Modifier.weight(1f))
            HomeHero(
                app = state.hero,
                openFocus = heroFocus,
                downFocus = ::rowTarget,
                onOpen = ::open,
                onUnpin = { actions.onUnpin(it.packageName) },
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
                    onTileFocused = { actions.onTileFocused(it.packageName) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
            }
            Row(
                modifier = Modifier.padding(horizontal = KsDimens.OverscanHorizontal),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AllAppsCard(
                    installedCount = state.installedCount,
                    onClick = { drawerOpen = true },
                    modifier = Modifier
                        .focusRequester(allAppsFocus)
                        // With pinned apps, Up returns to the tile that was focused last.
                        .focusProperties { up = if (hasPinned) rowTarget() else FocusRequester.Default },
                )
                SubscriptionsCard(
                    state = subscriptions,
                    onClick = { subscriptionsOpen = true },
                    modifier = Modifier
                        .width(460.dp)
                        .focusRequester(subscriptionsFocus)
                        .focusProperties { up = if (hasPinned) rowTarget() else FocusRequester.Default },
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
