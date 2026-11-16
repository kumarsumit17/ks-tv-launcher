package com.ks.tvlauncher.util

import android.content.Context
import android.text.format.DateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.Locale

/** Locale-aware clock and date text. Patterns come from the platform, so word order follows the TV's region. */
object DateTimeFormats {

    /** "5:25 pm" or "17:25". [use24Hour] null follows the TV's own setting. */
    fun time(context: Context, now: ZonedDateTime, use24Hour: Boolean?, locale: Locale = Locale.getDefault()): String {
        val twentyFour = use24Hour ?: DateFormat.is24HourFormat(context)
        return format(now, if (twentyFour) "Hm" else "hma", locale) {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        }
    }

    /** "Mon, 16 Nov" */
    fun shortDate(now: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        format(now, "EEEdMMM", locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }

    /** "Monday, 16 November" */
    fun longDate(now: ZonedDateTime, locale: Locale = Locale.getDefault()): String =
        format(now, "EEEEdMMMM", locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale) }

    /** "19 Nov 2026" */
    fun mediumDate(date: LocalDate, locale: Locale = Locale.getDefault()): String =
        format(date, "dMMMy", locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }

    private inline fun format(
        now: TemporalAccessor,
        skeleton: String,
        locale: Locale,
        fallback: () -> DateTimeFormatter,
    ): String = try {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, skeleton), locale).format(now)
    } catch (e: IllegalArgumentException) {
        // A locale pattern java.time cannot parse: use its own localized style instead.
        fallback().format(now)
    }
}
