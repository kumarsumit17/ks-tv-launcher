package com.ks.tvlauncher.ui.theme

import androidx.compose.ui.graphics.Color

/** Colour tokens from docs/spec/design-system.md. */
object KsColors {
    val BackgroundTop = Color(0xFF0C1426)
    val BackgroundBottom = Color(0xFF05070D)

    val GlassFill = Color.White.copy(alpha = 0.12f)
    val GlassBorder = Color.White.copy(alpha = 0.18f)

    /** Dense fill for dialogs and side panels: readable on its own, and lets blur show through. */
    val PanelFill = Color(0xD9161D2E)
    val Scrim = Color(0x99000000)

    val TextPrimary = Color(0xFFF4F6FB)
    val TextSecondary = Color(0xFFB8C1D3)
    val TextTertiary = Color(0xFFAEB8CB)

    val Warning = Color(0xFFFFD9A3)
    val WarningContainer = Color(0xFFFFC46E).copy(alpha = 0.14f)
    val Success = Color(0xFFA8F0C8)
    val SuccessContainer = Color(0xFFA8F0C8).copy(alpha = 0.14f)
    val Danger = Color(0xFFFFB3B3)
    val DangerContainer = Color(0xFFFFB3B3).copy(alpha = 0.14f)

    val PrimaryButton = Color(0xFFF4F6FB)
    val OnPrimaryButton = Color(0xFF0A0F1C)

    val Accent = Color(0xFF7FB2FF)
    val NameGradientStart = Color(0xFFFFE1B0)
    val NameGradientEnd = Color(0xFFFFB9C8)
}
