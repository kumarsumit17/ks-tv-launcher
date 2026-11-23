package com.ks.tvlauncher.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.system.DeviceStats
import com.ks.tvlauncher.domain.ByteFormatter
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Bottom-strip card: RAM and storage as label, bar and "used / total". OK opens the panel;
 * holding OK runs [onLongClick] (Control Center). [stats] null while the first read runs.
 */
@Composable
fun MemoryStorageCard(
    stats: DeviceStats?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) {
        if (stats == null) {
            Text(stringResource(R.string.mem_card_title), style = KsType.Label, color = KsColors.TextPrimary)
            Text(stringResource(R.string.mem_reading), style = KsType.Caption, color = KsColors.TextSecondary)
        } else {
            UsageLine(stringResource(R.string.mem_ram), stats.ramUsed, stats.ramTotal)
            Spacer(Modifier.height(4.dp))
            UsageLine(stringResource(R.string.mem_storage), stats.storageUsed, stats.storageTotal)
        }
    }
}

@Composable
private fun UsageLine(label: String, used: Long, total: Long) {
    val percent = ByteFormatter.percentUsed(used, total)
    val level = ByteFormatter.level(percent)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // From the caption's own size, so the column grows exactly as the text does at any TV
        // text size (Android scales small sp by more than large sp) and both bars stay aligned.
        val labelWidth = with(LocalDensity.current) { KsType.Caption.fontSize.toDp() } * LABEL_EMS
        Text(label, style = KsType.Caption, color = KsColors.TextPrimary, modifier = Modifier.width(labelWidth), maxLines = 1)
        UsageBar((percent / 100.0).toFloat(), level, Modifier.weight(1f).height(5.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = KsColors.TextPrimary)) {
                    append(ByteFormatter.formatIn(used, total))
                }
                append(" / ${ByteFormatter.formatIn(total, total)} ${ByteFormatter.unit(total)}")
            },
            style = KsType.Caption,
            color = KsColors.TextSecondary,
            maxLines = 1,
        )
    }
}

/** Room for "Storage" (and its Hindi), in caption ems. */
private const val LABEL_EMS = 4.6f
