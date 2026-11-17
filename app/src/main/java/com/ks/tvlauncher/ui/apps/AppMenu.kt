package com.ks.tvlauncher.ui.apps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
 * When a Move button disappears at the start or end of the row, focus goes to the nearest
 * remaining button ([AppMenuFocus]); it never reaches the screen behind the menu.
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
    val unpinFocus = remember { FocusRequester() }
    val moveLeftFocus = remember { FocusRequester() }
    val moveRightFocus = remember { FocusRequester() }
    var lastMove by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.canMoveLeft, state.canMoveRight) {
        val target = when (AppMenuFocus.afterMove(lastMove, state.canMoveLeft, state.canMoveRight)) {
            AppMenuButton.UNPIN -> unpinFocus
            AppMenuButton.MOVE_LEFT -> moveLeftFocus
            AppMenuButton.MOVE_RIGHT -> moveRightFocus
            null -> return@LaunchedEffect
        }
        lastMove = 0
        try {
            target.requestFocus()
        } catch (e: IllegalStateException) {
            // The button is not laid out yet; GlassDialog pulls focus back into the panel.
        }
    }
    val move: (Int) -> Unit = { delta ->
        lastMove = delta
        onMove(delta)
    }
    GlassDialog(
        onDismiss = onDismiss,
        title = state.app.label,
        maxWidth = 260.dp,
        returnFocusTo = returnFocusTo,
    ) {
        GlassButton(stringResource(R.string.menu_open), onClick = onOpen, modifier = row, icon = KsIcons.Play, primary = true)
        if (state.pinned) {
            GlassButton(stringResource(R.string.menu_unpin), onClick = onUnpin, modifier = row.focusRequester(unpinFocus))
            if (state.canMoveLeft) {
                GlassButton(
                    stringResource(R.string.menu_move_left),
                    onClick = { move(-1) },
                    modifier = row.focusRequester(moveLeftFocus),
                    icon = KsIcons.ChevronLeft,
                )
            }
            if (state.canMoveRight) {
                GlassButton(
                    stringResource(R.string.menu_move_right),
                    onClick = { move(1) },
                    modifier = row.focusRequester(moveRightFocus),
                    icon = KsIcons.ChevronRight,
                )
            }
        } else {
            GlassButton(stringResource(R.string.menu_pin), onClick = onPin, modifier = row, icon = KsIcons.Plus)
        }
        GlassButton(stringResource(R.string.menu_app_info), onClick = onAppInfo, modifier = row)
        GlassButton(stringResource(R.string.menu_uninstall), onClick = onUninstall, modifier = row)
    }
}
