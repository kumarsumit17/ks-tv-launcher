package com.ks.tvlauncher.domain

/** Rules for the "who's watching" names: trimmed, 1–20 characters, at most 8, no duplicates. */
object ProfileNames {
    const val MAX_NAMES = 8
    const val MAX_LENGTH = 20

    /** Returns the cleaned name, or null if it is not a valid name. */
    fun sanitize(raw: String): String? =
        raw.trim().takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }

    /** Cleans a stored or edited list: drops invalid and duplicate names, keeps order, caps the count. */
    fun normalize(names: List<String>): List<String> =
        names.mapNotNull(::sanitize).distinct().take(MAX_NAMES)

    /**
     * The name the "Switch to …" chip offers: the one after [active], wrapping to the first.
     * Null when there are fewer than two names. If [active] is not in the list, the first name.
     */
    fun next(names: List<String>, active: String?): String? {
        if (names.size < 2) return null
        val index = names.indexOf(active)
        return names[(index + 1) % names.size]
    }
}
