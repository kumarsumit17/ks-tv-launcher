package com.ks.tvlauncher.domain

import java.util.Locale

/** How full RAM or storage is, from the percentage used. */
enum class UsageLevel { NORMAL, GETTING_FULL, ALMOST_FULL }

/**
 * Byte sizes the way Android Settings shows them: decimal units (1 GB = 1,000,000,000 bytes),
 * one decimal place, MB below 1 GB.
 */
object ByteFormatter {
    private const val MB = 1_000_000.0
    private const val GB = 1_000_000_000.0

    /** At or above this percentage the level is [UsageLevel.GETTING_FULL]. */
    const val GETTING_FULL_PERCENT = 75.0

    /** Above this percentage the level is [UsageLevel.ALMOST_FULL]. */
    const val ALMOST_FULL_PERCENT = 90.0

    /** "1.3 GB", "512.0 MB". */
    fun format(bytes: Long, locale: Locale = Locale.getDefault()): String =
        if (bytes >= GB) "${number(bytes / GB, locale)} GB" else "${number(bytes / MB, locale)} MB"

    /** Just the number in [total]'s unit: "1.3" for 1.3 GB when the total is in GB. */
    fun formatIn(bytes: Long, total: Long, locale: Locale = Locale.getDefault()): String =
        number(bytes / divisor(total), locale)

    /** "1.3 / 2.0 GB": both numbers in the unit the total uses. */
    fun usedOfTotal(used: Long, total: Long, locale: Locale = Locale.getDefault()): String =
        "${formatIn(used, total, locale)} / ${formatIn(total, total, locale)} ${unit(total)}"

    /** "GB" when [total] is at least 1 GB, else "MB". */
    fun unit(total: Long): String = if (total >= GB) "GB" else "MB"

    /** Percentage used, 0–100. A zero or negative total counts as 0 %. */
    fun percentUsed(used: Long, total: Long): Double =
        if (total <= 0L) 0.0 else (used.coerceIn(0L, total).toDouble() / total) * 100.0

    /** Under 75 % normal; 75 % up to and including 90 % getting full; over 90 % almost full. */
    fun level(percent: Double): UsageLevel = when {
        percent > ALMOST_FULL_PERCENT -> UsageLevel.ALMOST_FULL
        percent >= GETTING_FULL_PERCENT -> UsageLevel.GETTING_FULL
        else -> UsageLevel.NORMAL
    }

    private fun divisor(total: Long): Double = if (total >= GB) GB else MB

    private fun number(value: Double, locale: Locale): String = String.format(locale, "%.1f", value)
}
