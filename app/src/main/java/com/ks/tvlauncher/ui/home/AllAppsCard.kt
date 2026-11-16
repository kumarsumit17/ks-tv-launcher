package com.ks.tvlauncher.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/** Bottom-strip card: "All apps" with the installed count. OK opens the drawer. */
@Composable
fun AllAppsCard(installedCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(KsIcons.Grid, contentDescription = null, modifier = Modifier.size(16.dp), tint = KsColors.TextPrimary)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(stringResource(R.string.all_apps), style = KsType.Label, color = KsColors.TextPrimary)
                Text(
                    text = pluralStringResource(R.plurals.installed_count, installedCount, installedCount),
                    style = KsType.Caption,
                    color = KsColors.TextSecondary,
                )
            }
        }
    }
}
