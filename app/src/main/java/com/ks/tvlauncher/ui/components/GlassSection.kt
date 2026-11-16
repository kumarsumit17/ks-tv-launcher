package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * A labelled group of controls. The content is one focus group, so the D-pad treats the
 * section as a unit when moving between sections.
 */
@Composable
fun GlassSection(
    label: String,
    modifier: Modifier = Modifier,
    labelPadding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label.uppercase(),
            style = KsType.SectionLabel,
            color = KsColors.TextTertiary,
            modifier = Modifier.padding(horizontal = labelPadding),
        )
        Column(modifier = Modifier.focusGroup(), content = content)
    }
}
