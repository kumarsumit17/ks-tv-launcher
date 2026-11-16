package com.ks.tvlauncher

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ks.tvlauncher.ui.home.HomeScreen
import com.ks.tvlauncher.ui.home.HomeViewModel
import com.ks.tvlauncher.ui.theme.KsTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels { HomeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            KsTheme(visual = state.visual) {
                // A launcher must not exit: Back on Home does nothing.
                BackHandler(enabled = true) {}
                HomeScreen(
                    state = state,
                    homeKeyEvents = viewModel.homeKeyEvents,
                    onSwitchProfile = viewModel::switchProfile,
                    onOpenSettings = ::openAndroidSettings,
                )
            }
        }
    }

    /** The Home key while Home is already in front arrives here (singleTask). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) viewModel.onHomeKey()
    }

    private fun openAndroidSettings() {
        try {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            // No settings app on this device: nothing to open.
        }
    }
}
