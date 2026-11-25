package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFilterTest {

    private fun app(label: String, category: AppCategory? = null) = AppInfo(
        packageName = "pkg.${label.lowercase().replace(' ', '.')}",
        activityName = "Main",
        label = label,
        versionCode = 1,
        category = category,
        hasBanner = true,
    )

    private val netflix = app("Netflix", AppCategory.VIDEO)
    private val spotify = app("Spotify", AppCategory.MUSIC)
    private val files = app("Files", AppCategory.TOOLS)
    private val kodi = app("kodi", AppCategory.VIDEO)
    private val other = app("Zed")
    private val all = listOf(spotify, netflix, other, kodi, files)

    private fun labels(apps: List<AppInfo>) = apps.map { it.label }

    @Test
    fun sortsByLabelIgnoringCase() {
        assertEquals(listOf("Files", "kodi", "Netflix", "Spotify", "Zed"), labels(AppFilter.apply(all, "", null, true)))
    }

    @Test
    fun descendingReversesTheOrder() {
        assertEquals(listOf("Zed", "Spotify", "Netflix", "kodi", "Files"), labels(AppFilter.apply(all, "", null, false)))
    }

    @Test
    fun searchMatchesAnywhereInTheLabelIgnoringCaseAndSpaces() {
        assertEquals(listOf("Netflix"), labels(AppFilter.apply(all, "  FLIX ", null, true)))
        assertEquals(listOf("Files", "Netflix"), labels(AppFilter.apply(all, "i", AppCategory.TOOLS, true) + AppFilter.apply(all, "fl", AppCategory.VIDEO, true)))
    }

    @Test
    fun categoryFilterKeepsOnlyThatCategory() {
        assertEquals(listOf("kodi", "Netflix"), labels(AppFilter.apply(all, "", AppCategory.VIDEO, true)))
    }

    @Test
    fun noMatchGivesAnEmptyList() {
        assertEquals(emptyList<String>(), labels(AppFilter.apply(all, "xyz", null, true)))
        assertEquals(emptyList<String>(), labels(AppFilter.apply(all, "", AppCategory.GAMES, true)))
    }

    @Test
    fun emptyCategoriesAreHiddenAndOrderIsFixed() {
        assertEquals(
            listOf(AppCategory.VIDEO, AppCategory.MUSIC, AppCategory.TOOLS),
            AppFilter.availableCategories(all),
        )
        assertEquals(emptyList<AppCategory>(), AppFilter.availableCategories(listOf(other)))
    }
}
