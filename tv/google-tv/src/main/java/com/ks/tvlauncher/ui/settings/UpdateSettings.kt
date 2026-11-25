package com.ks.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
 * About › updates (release builds): automatic checks on/off, "Check for updates", and one
 * action button whose label follows the flow (Download and install → Cancel → Install), so
 * focus never has to move while it runs. Both buttons stay in place; neither disables.
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

    val action = release?.let { rel ->
        when (state) {
            is UpdateState.Downloading, UpdateState.Verifying ->
                stringResource(R.string.settings_update_cancel) to viewModel::cancelUpdate
            UpdateState.NeedsPermission ->
                stringResource(R.string.settings_update_install, rel.versionName) to viewModel::installUpdate
            UpdateState.Installing ->
                stringResource(R.string.settings_update_installing) to {}
            else -> stringResource(R.string.settings_update_download, rel.versionName) to viewModel::startUpdate
        }
    }
    UpdateRows(
        autoCheck = autoCheck,
        onAutoCheckChange = viewModel::setAutoUpdateCheck,
        onCheck = viewModel::checkForUpdates,
        action = action,
        status = statusText(state),
        checkFocus = checkFocus,
    ) {
        release?.let { rel ->
            Text(stringResource(R.string.settings_update_available, rel.versionName), style = KsType.Label, color = KsColors.TextPrimary)
            Text(stringResource(R.string.settings_update_explain), style = KsType.Caption, color = KsColors.TextSecondary)
            if (rel.notes.isNotBlank()) {
                Text(rel.notes, style = KsType.Caption, color = KsColors.TextSecondary)
            }
        }
    }
}

/**
 * KS TV Dev and perf builds: the same rows, focusable and laid out as in release (the action
 * button included, as a pending update shows it), so the D-pad paths can be tested on the dev
 * app. Nothing is checked, downloaded or saved: every press only says updates are off.
 */
@Composable
internal fun UpdatePreview(autoCheck: Boolean, checkFocus: FocusRequester) {
    var pressed by remember { mutableStateOf(false) }
    val off = { pressed = true }
    UpdateRows(
        autoCheck = autoCheck,
        onAutoCheckChange = { off() },
        onCheck = off,
        action = stringResource(R.string.settings_update_download_preview) to off,
        status = if (pressed) stringResource(R.string.settings_update_preview_off) to true else null,
        checkFocus = checkFocus,
    ) {
        Text(stringResource(R.string.settings_update_debug), style = KsType.Caption, color = KsColors.TextSecondary)
    }
}

/**
 * The shared layout. Down from the switch and Up from "Android settings" (set by the caller)
 * go to "Check for updates" explicitly: the narrow left button otherwise loses the focus search
 * to the full-width rows above and below it (seen on the TV in 0.7.0). Right from it reaches
 * the action button; Up from either button reaches the switch, Down reaches "Android settings".
 */
@Composable
private fun UpdateRows(
    autoCheck: Boolean,
    onAutoCheckChange: (Boolean) -> Unit,
    onCheck: () -> Unit,
    action: Pair<String, () -> Unit>?,
    status: Pair<String, Boolean>?,
    checkFocus: FocusRequester,
    details: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlassToggle(
            stringResource(R.string.settings_update_auto),
            autoCheck,
            onAutoCheckChange,
            modifier = Modifier.focusProperties { down = checkFocus },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            GlassButton(
                text = stringResource(R.string.settings_update_check),
                onClick = onCheck,
                modifier = Modifier.focusRequester(checkFocus),
            )
            action?.let { (label, onClick) -> GlassButton(text = label, onClick = onClick, primary = true) }
        }
        status?.let { (text, warn) ->
            Text(text, style = KsType.Caption, color = if (warn) KsColors.Warning else KsColors.TextSecondary)
        }
        details()
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
