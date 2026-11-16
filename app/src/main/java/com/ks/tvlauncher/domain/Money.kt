package com.ks.tvlauncher.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/** Formats minor-unit amounts. INR uses Indian grouping (₹1,23,456). */
object Money {
    const val DEFAULT_CURRENCY = "INR"

    /** A known ISO 4217 code, else INR. */
    fun currency(code: String): Currency = try {
        Currency.getInstance(code.uppercase())
    } catch (e: IllegalArgumentException) {
        Currency.getInstance(DEFAULT_CURRENCY)
    }

    /** Minor units per major unit: 100 for INR, 1 for JPY. */
    fun minorPerMajor(code: String): Long {
        val digits = currency(code).defaultFractionDigits.coerceAtLeast(0)
        var factor = 1L
        repeat(digits) { factor *= 10 }
        return factor
    }

    /** "₹1,499" for whole amounts, "₹124.92" otherwise. */
    fun format(minor: Long, code: String, locale: Locale = localeFor(code)): String {
        val currency = currency(code)
        val digits = currency.defaultFractionDigits.coerceAtLeast(0)
        if (currency.currencyCode == DEFAULT_CURRENCY) return formatInr(minor)
        val format = NumberFormat.getCurrencyInstance(locale).apply { this.currency = currency }
        val whole = minor % minorPerMajor(code) == 0L
        format.minimumFractionDigits = if (whole) 0 else digits
        format.maximumFractionDigits = if (whole) 0 else digits
        return format.format(BigDecimal.valueOf(minor, digits))
    }

    /**
     * Indian grouping (last three digits, then pairs) done here rather than by NumberFormat:
     * the JVM's java.text supports one grouping size only, so tests and devices would differ.
     */
    private fun formatInr(minor: Long): String {
        val negative = minor < 0
        val abs = kotlin.math.abs(minor)
        val rupees = (abs / 100).toString()
        val paise = abs % 100
        val grouped = if (rupees.length <= 3) {
            rupees
        } else {
            val head = rupees.dropLast(3)
            head.reversed().chunked(2).joinToString(",").reversed() + "," + rupees.takeLast(3)
        }
        val fraction = if (paise == 0L) "" else "." + paise.toString().padStart(2, '0')
        return (if (negative) "-" else "") + "₹" + grouped + fraction
    }

    fun localeFor(code: String): Locale =
        if (currency(code).currencyCode == DEFAULT_CURRENCY) Locale.forLanguageTag("en-IN") else Locale.getDefault()
}
