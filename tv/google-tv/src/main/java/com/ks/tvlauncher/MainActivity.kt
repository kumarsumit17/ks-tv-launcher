package com.ks.tvlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ks.tvlauncher.ui.apps.LocalAppImageLoader
import com.ks.tvlauncher.ui.home.HomeActions
import com.ks.tvlauncher.ui.home.HomeScreen
import com.ks.tvlauncher.ui.home.HomeViewModel
import com.ks.tvlauncher.ui.theme.KsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The single activity. KS runs in app mode (specs/app-mode.md): it is opened from the
 * TV's own launcher and never acts as the Home app.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as KsApplication).container
        val imageLoader = container.imageLoader
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_OPEN_UPDATES, false)) viewModel.onOpenUpdates()
        // About 5 s after Home is first shown, off the main thread: re-register the daily
        // reminder and update-check work (both dropped by a force-stop), then tidy up after an
        // update. Never at app start (specs/performance.md).
        lifecycleScope.launch {
            withResumed { }
            delay(DAILY_WORK_DELAY_MS)
            withContext(Dispatchers.Default) {
                container.ensureDailyWork()
                container.updateManager.cleanUpAfterStart()
            }
        }
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
            val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
            val updateVersion by viewModel.updateVersion.collectAsStateWithLifecycle()
            // "Home ready" for am start -W, Macrobenchmark and the baseline profile: the app
            // grid has its apps (specs/performance.md).
            ReportDrawnWhen { state.appsLoaded }
            val actions = remember {
                HomeActions(
                    onSwitchProfile = viewModel::switchProfile,
                    onTileFocused = viewModel::onTileFocused,
                    launchIntent = viewModel::launchIntent,
                    onLaunchFailed = viewModel::onLaunchFailed,
                    onPin = viewModel::pin,
                    onUnpin = viewModel::unpin,
                    onMove = viewModel::move,
                    onDismissMessage = viewModel::dismissMessage,
                    onSetWallpaperMode = viewModel::setWallpaperMode,
                    onSetClock24Hour = viewModel::setUse24Hour,
                    onSetStatsBreakdown = viewModel::setStatsBreakdown,
                    usageAccessGranted = viewModel::usageAccessGranted,
                )
            }
            KsTheme(visual = state.visual) {
                CompositionLocalProvider(LocalAppImageLoader provides imageLoader) {
                    // Back with nothing open returns to the TV's launcher and keeps KS warm for
                    // a fast reopen. Overlays register their own handlers and close first.
                    BackHandler(enabled = true) { moveTaskToBack(true) }
                    HomeScreen(
                        state = state,
                        wallpaper = wallpaper,
                        subscriptions = subscriptions,
                        deviceStats = viewModel.deviceStats,
                        reopenEvents = viewModel.reopenEvents,
                        updateVersion = updateVersion,
                        openUpdatesEvents = viewModel.openUpdatesEvents,
                        actions = actions,
                    )
                }
            }
        }
    }

    /** KS opened again from the launcher while already running (singleTask). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // The update notification opens Settings › About; any other reopen returns to Home.
        if (intent.getBooleanExtra(EXTRA_OPEN_UPDATES, false)) viewModel.onOpenUpdates() else viewModel.onReopened()
    }

    companion object {
        /** Set by the update notification. */
        const val EXTRA_OPEN_UPDATES = "open_updates"
        private const val DAILY_WORK_DELAY_MS = 5_000L
    }
}
