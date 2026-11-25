package com.ks.tvlauncher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.ui.theme.LocalMotion

/**
 * Modal glass panel, drawn over the current screen. Place it last inside the screen's
 * [FrostSource] so it sits on top and can blur what is behind it.
 *
 * - Opening moves focus into the panel; the D-pad cannot leave it while it is open.
 * - If focus still ends up outside (the focused control disappeared, as a Move button does at
 *   the end of the row), the panel takes it back within a frame. The control that removed
 *   itself may send focus somewhere nearer first. One GlassDialog never opens another on top.
 * - Back calls [onDismiss].
 * - On close, focus returns to [returnFocusTo], the control that opened it.
 */
@Composable
fun GlassDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    maxWidth: Dp = 420.dp,
    returnFocusTo: FocusRequester? = null,
    /** Gap between the panel's children. */
    spacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val motion = LocalMotion.current
    val appear = remember { Animatable(if (motion.enabled) 0f else 1f) }
    val panelFocus = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    BackHandler(onBack = onDismiss)
    LaunchedEffect(Unit) { appear.animateTo(1f, motion.panel()) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        panelFocus.requestFocus()
    }
    LaunchedEffect(hasFocus) {
        if (hasFocus || !hadFocus) return@LaunchedEffect
        // Give the content one frame to place focus on a nearer control itself.
        withFrameNanos { }
        if (!hasFocus) {
            try {
                panelFocus.requestFocus()
            } catch (e: IllegalStateException) {
                // Not attached any more: the dialog is closing.
            }
        }
    }
    DisposableEffect(returnFocusTo) {
        onDispose {
            try {
                returnFocusTo?.requestFocus()
            } catch (e: IllegalStateException) {
                // The opener has left the screen; there is nothing to return focus to.
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(KsColors.Scrim, alpha = appear.value) }
            // Modal: focus may move inside the dialog but never out of it.
            .focusProperties { onExit = { cancelFocusChange() } }
            .onFocusChanged {
                hasFocus = it.hasFocus
                if (it.hasFocus) hadFocus = true
            }
            .focusGroup(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = modifier
                .graphicsLayer {
                    val p = appear.value
                    alpha = p
                    val scale = 0.96f + 0.04f * p
                    scaleX = scale
                    scaleY = scale
                }
                .widthIn(max = maxWidth)
                .focusRequester(panelFocus)
                .focusGroup()
                .glass(shape = KsShapes.Panel, frosted = true, fill = KsColors.PanelFill)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(spacing),
        ) {
            if (title != null) {
                Text(title, style = KsType.ScreenTitle, color = KsColors.TextPrimary)
            }
            content()
        }
    }
}
