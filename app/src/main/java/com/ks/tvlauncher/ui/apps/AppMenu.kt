package com.ks.tvlauncher.ui.apps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.AppInfo
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassDialog
import com.ks.tvlauncher.ui.icons.KsIcons

@Immutable
data class AppMenuState(
    val app: AppInfo,
    val pinned: Boolean,
    val canMoveLeft: Boolean,
    val canMoveRight: Boolean,
)

/**
 * The small glass menu for one app: Open, Pin/Unpin, Move left/right (pinned apps only),
 * App info and Uninstall. Opened by holding OK on a tile, or by OK while the drawer's Edit
 * toggle is on, so holding OK is never the only way in.
 *
 * Move keeps the menu open so an app can be moved several places; every other action closes it.
 */
@Composable
fun AppMenu(
    state: AppMenuState,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onMove: (delta: Int) -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit,
    returnFocusTo: FocusRequester?,
) {
    val row = Modifier.fillMaxWidth()
    GlassDialog(
        onDismiss = onDismiss,
        title = state.app.label,
        maxWidth = 260.dp,
        returnFocusTo = returnFocusTo,
    ) {
        GlassButton(stringResource(R.string.menu_open), onClick = onOpen, modifier = row, icon = KsIcons.Play, primary = true)
        if (state.pinned) {
            GlassButton(stringResource(R.string.menu_unpin), onClick = onUnpin, modifier = row)
            if (state.canMoveLeft) {
                GlassButton(stringResource(R.string.menu_move_left), onClick = { onMove(-1) }, modifier = row, icon = KsIcons.ChevronLeft)
            }
            if (state.canMoveRight) {
                GlassButton(stringResource(R.string.menu_move_right), onClick = { onMove(1) }, modifier = row, icon = KsIcons.ChevronRight)
            }
        } else {
            GlassButton(stringResource(R.string.menu_pin), onClick = onPin, modifier = row, icon = KsIcons.Plus)
        }
        GlassButton(stringResource(R.string.menu_app_info), onClick = onAppInfo, modifier = row)
        GlassButton(stringResource(R.string.menu_uninstall), onClick = onUninstall, modifier = row)
    }
}
