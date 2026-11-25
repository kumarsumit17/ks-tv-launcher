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

    /** [names] with [raw] added at the end; unchanged if it is invalid, a duplicate or the list is full. */
    fun add(names: List<String>, raw: String): List<String> {
        val name = sanitize(raw) ?: return names
        if (name in names || names.size >= MAX_NAMES) return names
        return names + name
    }

    /** True when [raw] could be added to [names]. */
    fun canAdd(names: List<String>, raw: String): Boolean {
        val name = sanitize(raw) ?: return false
        return name !in names && names.size < MAX_NAMES
    }

    /** [old] replaced by [raw] in place; unchanged if [raw] is invalid or already used by another name. */
    fun rename(names: List<String>, old: String, raw: String): List<String> {
        val name = sanitize(raw) ?: return names
        val index = names.indexOf(old)
        if (index < 0 || (name != old && name in names)) return names
        return names.toMutableList().also { it[index] = name }
    }

    fun remove(names: List<String>, name: String): List<String> = names - name
}
