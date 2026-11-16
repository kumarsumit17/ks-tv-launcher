package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/** A short, display-only notice. It takes no focus; the caller decides how long it stays. */
@Composable
fun GlassSnackbar(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        style = KsType.Label,
        color = KsColors.TextPrimary,
        modifier = modifier
            .semantics { liveRegion = LiveRegionMode.Polite }
            .glass(shape = KsShapes.Pill, fill = KsColors.PanelFill)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}
