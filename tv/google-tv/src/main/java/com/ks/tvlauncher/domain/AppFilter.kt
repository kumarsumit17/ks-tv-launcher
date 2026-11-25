package com.ks.tvlauncher.domain

/** Search, category filter and sort for the All apps drawer. */
object AppFilter {

    /** The categories that have at least one app, in display order. Empty ones are hidden. */
    fun availableCategories(apps: List<AppInfo>): List<AppCategory> =
        AppCategory.entries.filter { category -> apps.any { it.category == category } }

    /**
     * Apps matching [query] (case-insensitive, anywhere in the label) and [category]
     * (null means All), sorted by label A–Z, or Z–A when [ascending] is false.
     */
    fun apply(
        apps: List<AppInfo>,
        query: String,
        category: AppCategory?,
        ascending: Boolean,
        labelOrder: Comparator<String> = String.CASE_INSENSITIVE_ORDER,
    ): List<AppInfo> {
        val needle = query.trim()
        val order = compareBy<AppInfo, String>(labelOrder) { it.label }.thenBy { it.packageName }
        return apps
            .asSequence()
            .filter { category == null || it.category == category }
            .filter { needle.isEmpty() || it.label.contains(needle, ignoreCase = true) }
            .sortedWith(if (ascending) order else order.reversed())
            .toList()
    }
}
