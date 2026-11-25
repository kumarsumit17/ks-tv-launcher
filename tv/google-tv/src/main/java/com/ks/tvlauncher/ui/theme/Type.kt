package com.ks.tvlauncher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Typography
import com.ks.tvlauncher.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val inter = GoogleFont("Inter")

/**
 * Inter, downloaded on demand through Google Play services and cached by the system.
 * Loading is asynchronous and never blocks the first frame; until it arrives, or if the
 * provider is missing, text renders in the platform sans-serif.
 */
val InterFamily = FontFamily(
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

/**
 * Type scale for the 960x540 dp canvas (1080p at xhdpi), from specs/design-system.md.
 * Nothing is smaller than 9sp, the minimum readable size from 3 m.
 */
object KsType {
    val HeroTitle = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1).sp,
    )
    val Greeting = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.5).sp,
    )
    val ScreenTitle = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp, lineHeight = 30.sp,
    )
    val Clock = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    val Subtitle = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val Label = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    val SectionLabel = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, letterSpacing = 0.8.sp,
    )
    val Body = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 17.sp,
    )
    val Caption = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp)
    val CaptionSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
}

/** Maps the scale onto TV Material so any default-styled text also uses Inter. */
internal val KsTypography: Typography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = InterFamily),
        displayMedium = KsType.HeroTitle,
        displaySmall = KsType.Greeting,
        headlineLarge = base.headlineLarge.copy(fontFamily = InterFamily),
        headlineMedium = KsType.ScreenTitle,
        headlineSmall = KsType.Clock,
        titleLarge = base.titleLarge.copy(fontFamily = InterFamily),
        titleMedium = KsType.Subtitle,
        titleSmall = KsType.Label,
        bodyLarge = KsType.Body,
        bodyMedium = KsType.Body,
        bodySmall = KsType.Caption,
        labelLarge = KsType.Label,
        labelMedium = KsType.SectionLabel,
        labelSmall = KsType.CaptionSmall,
    )
}
