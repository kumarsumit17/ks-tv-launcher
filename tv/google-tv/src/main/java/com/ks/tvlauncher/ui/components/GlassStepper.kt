package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Minus / value / plus control for the remote.
 *
 * A single press of OK calls [onStep] with `large = false`. While OK is held, every key
 * repeat calls it with `large = true`, so callers can step faster (for example ±10 on a
 * press and ±100 while held). `direction` is -1 or +1.
 */
@Composable
fun GlassStepper(
    value: String,
    onStep: (direction: Int, large: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton(KsIcons.Minus, stringResource(R.string.stepper_decrease)) { large -> onStep(-1, large) }
        Text(
            text = value,
            style = KsType.Clock,
            color = KsColors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.defaultMinSize(minWidth = 72.dp),
        )
        StepButton(KsIcons.Plus, stringResource(R.string.stepper_increase)) { large -> onStep(+1, large) }
    }
}

@Composable
private fun StepButton(icon: ImageVector, contentDescription: String, onStep: (large: Boolean) -> Unit) {
    // Plain holder, not state: it only gates key handling and must not trigger recomposition.
    val held = remember { BooleanArray(1) }
    DpadFocusable(
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.key != Key.DirectionCenter && event.key != Key.Enter && event.key != Key.NumPadEnter) {
                return@onPreviewKeyEvent false
            }
            when (event.type) {
                KeyEventType.KeyDown ->
                    if (event.nativeKeyEvent.repeatCount > 0) {
                        held[0] = true
                        onStep(true)
                        true
                    } else {
                        false
                    }
                // Swallow the release after a hold so it does not also count as a click.
                KeyEventType.KeyUp -> held[0].also { held[0] = false }
                else -> false
            }
        },
        onClick = { onStep(false) },
        shape = KsShapes.Pill,
        focusedScale = 1.1f,
    ) {
        Box(Modifier.glass(shape = KsShapes.Pill).size(32.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(15.dp), tint = KsColors.TextPrimary)
        }
    }
}
