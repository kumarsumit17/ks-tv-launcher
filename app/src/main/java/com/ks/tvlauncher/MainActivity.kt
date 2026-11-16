package com.ks.tvlauncher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ks.tvlauncher.ui.apps.AppIntents
import com.ks.tvlauncher.ui.apps.LocalAppImageLoader
import com.ks.tvlauncher.ui.home.HomeActions
import com.ks.tvlauncher.ui.home.HomeScreen
import com.ks.tvlauncher.ui.home.HomeViewModel
import com.ks.tvlauncher.ui.theme.KsTheme

/**
 * The single activity. KS runs in app mode (docs/spec/app-mode.md): it is opened from the
 * TV's own launcher and never acts as the Home app.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageLoader = (application as KsApplication).container.imageLoader
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val wallpaper by viewModel.wallpaper.collectAsStateWithLifecycle()
            val actions = remember {
                HomeActions(
                    onSwitchProfile = viewModel::switchProfile,
                    onOpenSettings = ::openAndroidSettings,
                    onTileFocused = viewModel::onTileFocused,
                    launchIntent = viewModel::launchIntent,
                    onLaunchFailed = viewModel::onLaunchFailed,
                    onPin = viewModel::pin,
                    onUnpin = viewModel::unpin,
                    onMove = viewModel::move,
                    onDismissMessage = viewModel::dismissMessage,
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
                        reopenEvents = viewModel.reopenEvents,
                        actions = actions,
                    )
                }
            }
        }
    }

    /** KS opened again from the launcher while already running (singleTask). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.onReopened()
    }

    private fun openAndroidSettings() {
        AppIntents.start(this, Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
