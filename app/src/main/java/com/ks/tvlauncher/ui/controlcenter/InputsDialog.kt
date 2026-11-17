package com.ks.tvlauncher.ui.controlcenter

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.InputConnection
import com.ks.tvlauncher.domain.TvInput
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassDialog
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Glass list of the TV's inputs, opened from the Control Center Inputs tile. OK on a row calls
 * [onSelect]; Back calls [onDismiss] and focus returns to [returnFocusTo] (the tile). Drawn
 * inside the panel's focus group.
 */
@Composable
fun InputsDialog(
    inputs: List<TvInput>,
    onSelect: (TvInput) -> Unit,
    onDismiss: () -> Unit,
    returnFocusTo: FocusRequester,
) {
    val firstRow = remember { FocusRequester() }
    GlassDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.cc_inputs),
        maxWidth = 460.dp,
        returnFocusTo = returnFocusTo,
        spacing = 8.dp,
    ) {
        LaunchedEffect(Unit) {
            withFrameNanos { }
            withFrameNanos { }
            firstRow.requestFocusSafely()
        }
        inputs.forEachIndexed { index, input ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0) Modifier.focusRequester(firstRow) else Modifier),
                onClick = { onSelect(input) },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(KsIcons.Plug, contentDescription = null, modifier = Modifier.size(16.dp), tint = KsColors.TextPrimary)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        input.label,
                        style = KsType.Label,
                        color = KsColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(connectionText(input.connection)),
                        style = KsType.Caption,
                        color = if (input.connection == InputConnection.CONNECTED) KsColors.TextPrimary else KsColors.TextSecondary,
                    )
                }
            }
        }
    }
}

private fun connectionText(connection: InputConnection): Int = when (connection) {
    InputConnection.CONNECTED -> R.string.cc_input_connected
    InputConnection.STANDBY -> R.string.cc_input_standby
    InputConnection.DISCONNECTED -> R.string.cc_input_disconnected
}
