package com.ks.tvlauncher.ui.controlcenter

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.system.BluetoothStatus
import com.ks.tvlauncher.data.system.NetworkKind
import com.ks.tvlauncher.data.system.NetworkStatus
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.glass
import com.ks.tvlauncher.ui.components.modalFocus
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsDimens
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType
import com.ks.tvlauncher.ui.theme.LocalMotion
import kotlin.math.roundToInt

/** The Launcher rows' current values, from Home's state. */
class ControlCenterValues(
    val time: String,
    val dynamicWallpaper: Boolean,
    val clock24Hour: Boolean,
    /** "Pune · °C", or "Set city". */
    val weather: String,
    /** "₹ INR". */
    val subscriptions: String,
)

/** What the Launcher rows do. Each closes or covers the panel as Home decides. */
class ControlCenterActions(
    val onDismiss: () -> Unit,
    val onSetDynamicWallpaper: (Boolean) -> Unit,
    val onSetClock24Hour: (Boolean) -> Unit,
    val onOpenWeather: () -> Unit,
    val onOpenSubscriptions: () -> Unit,
    val onOpenMemory: () -> Unit,
)

private val PanelWidth = 400.dp

/**
 * Glass side panel that slides in from the right over Home. Status tiles open their Android
 * settings page (an app cannot switch radios or power); the rows change KS settings or open
 * other KS screens. Modal; Back calls [ControlCenterActions.onDismiss]. Home returns focus to
 * where it was once the panel has gone. [memoryFocus] is the Memory row, so the Memory panel
 * opened from here can give focus back to it.
 */
@Composable
fun ControlCenterPanel(
    network: NetworkStatus?,
    bluetooth: BluetoothStatus?,
    values: ControlCenterValues,
    actions: ControlCenterActions,
    memoryFocus: FocusRequester,
    coveredByDialog: Boolean,
    /** A dialog opened from the panel; drawn inside its focus group so focus can reach it. */
    overlay: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val motion = LocalMotion.current
    val appear = remember { Animatable(if (motion.enabled) 0f else 1f) }
    val firstTile = remember { FocusRequester() }

    BackHandler(enabled = !coveredByDialog, onBack = actions.onDismiss)
    LaunchedEffect(Unit) { appear.animateTo(1f, motion.panel()) }
    LaunchedEffect(Unit) {
        withFrameNanos { }
        firstTile.requestFocusSafely()
    }

    val view = LocalView.current
    val displayMode = remember(view) {
        view.display?.mode?.let { Triple(it.physicalWidth, it.physicalHeight, it.refreshRate.roundToInt()) }
    }

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind { drawRect(KsColors.Scrim, alpha = appear.value) }
            .modalFocus(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            Modifier
                .padding(vertical = KsDimens.OverscanVertical, horizontal = KsDimens.OverscanHorizontal / 2)
                .width(PanelWidth)
                .fillMaxHeight()
                .graphicsLayer {
                    translationX = (1f - appear.value) * (PanelWidth.toPx() + 48.dp.toPx())
                }
                .glass(shape = KsShapes.Panel, frosted = true, fill = KsColors.PanelFill)
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.cc_title), style = KsType.ScreenTitle, color = KsColors.TextPrimary, modifier = Modifier.weight(1f))
                Text(values.time, style = KsType.Label, color = KsColors.TextSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusTile(
                    icon = KsIcons.Wifi,
                    title = stringResource(if (network?.kind == NetworkKind.ETHERNET) R.string.cc_network else R.string.cc_wifi),
                    subtitle = networkText(network),
                    onClick = { SystemIntents.openSettings(context, Settings.ACTION_WIFI_SETTINGS) },
                    modifier = Modifier.weight(1f).focusRequester(firstTile),
                )
                StatusTile(
                    icon = KsIcons.Bluetooth,
                    title = stringResource(R.string.cc_bluetooth),
                    subtitle = bluetoothText(bluetooth),
                    onClick = { SystemIntents.openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS) },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusTile(
                    icon = KsIcons.Monitor,
                    title = stringResource(R.string.cc_display),
                    subtitle = displayMode?.let { stringResource(R.string.cc_display_mode, it.first, it.second, it.third) }.orEmpty(),
                    onClick = { SystemIntents.openSettings(context, Settings.ACTION_DISPLAY_SETTINGS) },
                    modifier = Modifier.weight(1f),
                )
                StatusTile(
                    icon = KsIcons.Moon,
                    title = stringResource(R.string.cc_screensaver),
                    subtitle = stringResource(R.string.cc_start_now),
                    onClick = { SystemIntents.startScreensaver(context) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(stringResource(R.string.cc_launcher).uppercase(), style = KsType.SectionLabel, color = KsColors.TextTertiary)
            ValueRow(
                label = stringResource(R.string.cc_wallpaper),
                value = stringResource(if (values.dynamicWallpaper) R.string.cc_wallpaper_dynamic else R.string.cc_wallpaper_plain),
                onClick = { actions.onSetDynamicWallpaper(!values.dynamicWallpaper) },
            )
            Row(
                Modifier.fillMaxWidth().glass(shape = KsShapes.Card).padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.cc_clock), style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.weight(1f))
                GlassChip(text = stringResource(R.string.cc_12_hour), selected = !values.clock24Hour, onClick = { actions.onSetClock24Hour(false) })
                Spacer(Modifier.width(6.dp))
                GlassChip(text = stringResource(R.string.cc_24_hour), selected = values.clock24Hour, onClick = { actions.onSetClock24Hour(true) })
            }
            ValueRow(stringResource(R.string.cc_weather), values.weather, onClick = actions.onOpenWeather)
            ValueRow(stringResource(R.string.cc_subscriptions), values.subscriptions, onClick = actions.onOpenSubscriptions)
            ValueRow(
                label = stringResource(R.string.cc_memory),
                value = "",
                onClick = actions.onOpenMemory,
                modifier = Modifier.focusRequester(memoryFocus),
            )
            ValueRow(
                label = stringResource(R.string.cc_android_settings),
                value = stringResource(R.string.cc_android_settings_value),
                trailing = KsIcons.ArrowUpRight,
                onClick = { SystemIntents.openSettings(context, Settings.ACTION_SETTINGS) },
            )
            Spacer(Modifier.height(2.dp))
            Text(stringResource(R.string.cc_back_hint), style = KsType.Caption, color = KsColors.TextSecondary)
            Text(stringResource(R.string.cc_limits), style = KsType.Caption, color = KsColors.TextSecondary)
        }
        overlay()
    }
}

@Composable
private fun networkText(network: NetworkStatus?): String = when (network?.kind) {
    null -> ""
    NetworkKind.WIFI -> network.ssid?.let { stringResource(R.string.cc_ssid_connected, it) } ?: stringResource(R.string.cc_connected)
    NetworkKind.ETHERNET -> stringResource(R.string.cc_ethernet)
    NetworkKind.OTHER -> stringResource(R.string.cc_connected)
    NetworkKind.OFFLINE -> stringResource(R.string.cc_offline)
}

@Composable
private fun bluetoothText(bluetooth: BluetoothStatus?): String = when {
    bluetooth == null -> ""
    !bluetooth.available -> stringResource(R.string.cc_not_available)
    !bluetooth.enabled -> stringResource(R.string.cc_off)
    bluetooth.pairedCount != null -> pluralStringResource(R.plurals.cc_paired, bluetooth.pairedCount, bluetooth.pairedCount)
    else -> stringResource(R.string.cc_on)
}

@Composable
private fun StatusTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = KsColors.TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(title, style = KsType.Label, color = KsColors.TextPrimary, maxLines = 1)
        Text(subtitle, style = KsType.Caption, color = KsColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: ImageVector = KsIcons.ChevronRight,
) {
    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1)
            if (value.isNotEmpty()) {
                Text(value, style = KsType.Caption, color = KsColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.width(4.dp))
            }
            Icon(trailing, contentDescription = null, modifier = Modifier.size(12.dp), tint = KsColors.TextSecondary)
        }
    }
}

/** Collects the status flows and shows the panel. */
@Composable
fun ControlCenter(
    viewModel: ControlCenterViewModel,
    values: ControlCenterValues,
    actions: ControlCenterActions,
    memoryFocus: FocusRequester,
    coveredByDialog: Boolean,
    overlay: @Composable () -> Unit = {},
) {
    val network by viewModel.network.collectAsStateWithLifecycle()
    val bluetooth by viewModel.bluetooth.collectAsStateWithLifecycle()
    ControlCenterPanel(network, bluetooth, values, actions, memoryFocus, coveredByDialog, overlay)
}
