package com.ks.tvlauncher.ui.theme

import android.app.UiModeManager
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * The system accessibility settings KS follows (specs/accessibility.md), read live: changing them
 * in Android settings while KS is open takes effect without a restart.
 */
@Immutable
data class SystemAccessibility(
    /** False when "Remove animations" (or the developer option) set the animator scale to 0. */
    val animations: Boolean = true,
    /** "High contrast text" is on, or the system contrast is raised (Android 14+). */
    val highContrast: Boolean = false,
)

/** The secure setting behind Accessibility › "High contrast text" (readable by apps). */
private const val HIGH_TEXT_CONTRAST = "high_text_contrast_enabled"

/** Reads the settings now. Never throws: a setting KS may not read counts as off. */
fun readSystemAccessibility(context: Context): SystemAccessibility {
    val resolver = context.contentResolver
    val animations = systemAnimationsEnabled(context)
    val highText = runCatching { Settings.Secure.getInt(resolver, HIGH_TEXT_CONTRAST, 0) == 1 }.getOrDefault(false)
    val contrast = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        runCatching { context.getSystemService(UiModeManager::class.java).contrast > 0f }.getOrDefault(false)
    } else {
        false
    }
    return SystemAccessibility(animations = animations, highContrast = highText || contrast)
}

/** [SystemAccessibility], updated while the screen is shown (no polling: a content observer). */
@Composable
fun rememberSystemAccessibility(): SystemAccessibility {
    val context = LocalContext.current
    var state by remember(context) { mutableStateOf(readSystemAccessibility(context)) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                state = readSystemAccessibility(context)
            }
        }
        val resolver = context.contentResolver
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        runCatching { resolver.registerContentObserver(Settings.Secure.getUriFor(HIGH_TEXT_CONTRAST), false, observer) }
        val contrastListener = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            UiModeManager.ContrastChangeListener { state = readSystemAccessibility(context) }.also { listener ->
                runCatching { context.getSystemService(UiModeManager::class.java).addContrastChangeListener(context.mainExecutor, listener) }
            }
        } else {
            null
        }
        onDispose {
            resolver.unregisterContentObserver(observer)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && contrastListener != null) {
                runCatching { context.getSystemService(UiModeManager::class.java).removeContrastChangeListener(contrastListener) }
            }
        }
    }
    return state
}
