package com.ks.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.updates.UpdateFailure
import com.ks.tvlauncher.data.updates.UpdateState
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassToggle
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * About › updates (release builds only): automatic checks on/off, "Check for updates", and
 * one action button whose label follows the flow (Download and install → Cancel → Install),
 * so focus never has to move while it runs. Both buttons stay in place; neither disables.
 */
@Composable
internal fun UpdateSettings(autoCheck: Boolean, viewModel: SettingsViewModel, checkFocus: FocusRequester) {
    val state by viewModel.updateState.collectAsStateWithLifecycle()
    val release by viewModel.updateAvailable.collectAsStateWithLifecycle()
    // Back from Android's "Install unknown apps" page: continue if it is now allowed.
    LifecycleResumeEffect(Unit) {
        viewModel.onUpdateResumed()
        onPauseOrDispose { }
    }
    // A finished message ("up to date", "could not be verified") is not shown again next time.
    DisposableEffect(Unit) { onDispose { viewModel.resetUpdateMessage() } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassToggle(stringResource(R.string.settings_update_auto), autoCheck, viewModel::setAutoUpdateCheck)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(
                text = stringResource(R.string.settings_update_check),
                onClick = viewModel::checkForUpdates,
                modifier = Modifier.focusRequester(checkFocus),
            )
            release?.let { rel ->
                val (label, action) = when (state) {
                    is UpdateState.Downloading, UpdateState.Verifying ->
                        stringResource(R.string.settings_update_cancel) to viewModel::cancelUpdate
                    UpdateState.NeedsPermission ->
                        stringResource(R.string.settings_update_install, rel.versionName) to viewModel::installUpdate
                    UpdateState.Installing ->
                        stringResource(R.string.settings_update_installing) to {}
                    else -> stringResource(R.string.settings_update_download, rel.versionName) to viewModel::startUpdate
                }
                GlassButton(text = label, onClick = action, primary = true)
            }
        }
        statusText(state)?.let { (text, warn) ->
            Text(text, style = KsType.Caption, color = if (warn) KsColors.Warning else KsColors.TextSecondary)
        }
        release?.let { rel ->
            Text(stringResource(R.string.settings_update_available, rel.versionName), style = KsType.Label, color = KsColors.TextPrimary)
            Text(stringResource(R.string.settings_update_explain), style = KsType.Caption, color = KsColors.TextSecondary)
            if (rel.notes.isNotBlank()) {
                Text(rel.notes, style = KsType.Caption, color = KsColors.TextSecondary)
            }
        }
    }
}

/** The line under the buttons, and whether it is a warning. */
@Composable
private fun statusText(state: UpdateState): Pair<String, Boolean>? = when (state) {
    UpdateState.Idle -> null
    UpdateState.Checking -> stringResource(R.string.settings_update_checking) to false
    UpdateState.UpToDate -> stringResource(R.string.settings_update_up_to_date) to false
    UpdateState.Waiting -> stringResource(R.string.settings_update_waiting) to true
    UpdateState.CheckFailed -> stringResource(R.string.settings_update_check_failed) to true
    is UpdateState.Downloading -> stringResource(R.string.settings_update_downloading, state.percent) to false
    UpdateState.Verifying -> stringResource(R.string.settings_update_verifying) to false
    UpdateState.NeedsPermission -> stringResource(R.string.settings_update_permission) to true
    UpdateState.Installing -> stringResource(R.string.settings_update_confirm) to false
    is UpdateState.Failed -> stringResource(
        when (state.reason) {
            UpdateFailure.DOWNLOAD -> R.string.settings_update_download_failed
            UpdateFailure.NOT_VERIFIED -> R.string.settings_update_not_verified
            UpdateFailure.INSTALL -> R.string.settings_update_install_failed
            UpdateFailure.CANCELLED -> R.string.settings_update_install_cancelled
        },
    ) to true
}
