package com.ks.tvlauncher.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.system.DeviceStats
import com.ks.tvlauncher.domain.ByteFormatter
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassDialog
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType
import kotlin.math.roundToInt

/**
 * Expanded RAM and storage panel. "Manage apps" opens Android's app settings (an app cannot
 * list other running apps); "Free up space" opens storage settings. The Apps / Other split
 * appears only once the user has allowed Usage access, which [onEnableUsageAccess] offers.
 */
@Composable
fun MemoryStoragePanel(
    stats: DeviceStats?,
    usageAccessGranted: Boolean,
    onManageApps: () -> Unit,
    onFreeUpSpace: () -> Unit,
    onEnableUsageAccess: () -> Unit,
    onDismiss: () -> Unit,
    returnFocusTo: FocusRequester?,
) {
    val firstButton = remember { FocusRequester() }
    GlassDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.mem_card_title),
        maxWidth = 760.dp,
        returnFocusTo = returnFocusTo,
    ) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            withFrameNanos { }
            firstButton.requestFocusSafely()
        }
        if (stats == null) {
            Text(stringResource(R.string.mem_reading), style = KsType.Body, color = KsColors.TextSecondary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Gauge(
                label = stringResource(R.string.mem_ram),
                used = stats?.ramUsed ?: 0L,
                total = stats?.ramTotal ?: 0L,
                detail = stats?.let { stringResource(R.string.mem_free_for_apps, ByteFormatter.format(it.ramAvailable)) }.orEmpty(),
                button = stringResource(R.string.mem_manage_apps),
                primary = false,
                onClick = onManageApps,
                buttonModifier = Modifier.focusRequester(firstButton),
                modifier = Modifier.weight(1f),
            )
            val breakdown = stats?.breakdown
            Gauge(
                label = stringResource(R.string.mem_storage),
                used = stats?.storageUsed ?: 0L,
                total = stats?.storageTotal ?: 0L,
                detail = when {
                    stats == null -> ""
                    breakdown != null -> stringResource(
                        R.string.mem_breakdown,
                        ByteFormatter.format(stats.storageAvailable),
                        ByteFormatter.format(breakdown.appsBytes),
                        ByteFormatter.format(breakdown.otherBytes),
                    )
                    else -> stringResource(R.string.mem_free, ByteFormatter.format(stats.storageAvailable))
                },
                button = stringResource(R.string.mem_free_up),
                primary = true,
                onClick = onFreeUpSpace,
                modifier = Modifier.weight(1f),
            )
        }
        if (!usageAccessGranted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.mem_usage_explain),
                    style = KsType.Caption,
                    color = KsColors.TextSecondary,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                GlassButton(text = stringResource(R.string.mem_usage_offer), onClick = onEnableUsageAccess)
            }
        }
    }
}

@Composable
private fun Gauge(
    label: String,
    used: Long,
    total: Long,
    detail: String,
    button: String,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
) {
    val percent = ByteFormatter.percentUsed(used, total)
    val level = ByteFormatter.level(percent)
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(116.dp), contentAlignment = Alignment.Center) {
            UsageRing((percent / 100.0).toFloat(), level, Modifier.size(116.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.mem_percent, percent.roundToInt()), style = KsType.ScreenTitle, color = KsColors.TextPrimary)
                Text(label.uppercase(), style = KsType.CaptionSmall, color = KsColors.TextSecondary)
            }
        }
        Spacer(Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (total > 0L) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = KsColors.TextPrimary, fontSize = KsType.ScreenTitle.fontSize)) {
                            append(ByteFormatter.format(used))
                        }
                        append(" ")
                        append(stringResource(R.string.mem_used_of, ByteFormatter.format(total)))
                    },
                    style = KsType.Subtitle,
                    color = KsColors.TextSecondary,
                    maxLines = 1,
                )
            }
            if (detail.isNotEmpty()) Text(detail, style = KsType.Body, color = KsColors.TextSecondary, maxLines = 2)
            if (total > 0L) GlassPill(text = usageTag(level), tone = usageTone(level))
            Spacer(Modifier.height(2.dp))
            GlassButton(
                text = button,
                onClick = onClick,
                icon = KsIcons.ChevronRight,
                primary = primary,
                modifier = buttonModifier,
            )
        }
    }
}
