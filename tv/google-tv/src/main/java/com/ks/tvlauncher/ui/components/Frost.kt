package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import com.ks.tvlauncher.ui.theme.LocalGlassStyle

/** The layer that frosted surfaces blur. Null outside a [FrostSource] or when blur is off. */
private val LocalFrostState = staticCompositionLocalOf<HazeState?> { null }

/**
 * Hosts a screen whose [background] can be blurred by frosted surfaces in [content].
 *
 * Blur budget (specs/design-system.md): only still surfaces are frosted — the weather
 * pill, dialogs and side panels. Tiles and cards in rows and grids never are. When the glass
 * style has blur off (below API 31, or Low intensity) the background is not even registered
 * as a source, so Haze does no work at all.
 */
@Composable
fun FrostSource(
    modifier: Modifier = Modifier,
    background: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val blurEnabled = LocalGlassStyle.current.blurEnabled
    val state = rememberHazeState()
    Box(modifier) {
        Box(Modifier.fillMaxSize().then(if (blurEnabled) Modifier.hazeSource(state) else Modifier)) {
            background()
        }
        CompositionLocalProvider(LocalFrostState provides if (blurEnabled) state else null, content = content)
    }
}

/** Blurs what is behind this surface, when blur is available and switched on. Otherwise a no-op. */
@Composable
internal fun Modifier.frost(enabled: Boolean): Modifier {
    if (!enabled) return this
    val state = LocalFrostState.current ?: return this
    val radius = LocalGlassStyle.current.blurRadius
    val input = remember(state) { HazeInput.Sources(state) }
    val style = remember(radius) {
        HazeBlurStyle {
            blurRadius(radius)
            // Noise costs an extra shader pass and is invisible from the sofa.
            noiseFactor(0f)
        }
    }
    return this.hazeBlur(input, style, HazePerformanceMode.Performance)
}
