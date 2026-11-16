package com.ks.tvlauncher.data.apps

import android.content.pm.ApplicationInfo
import com.ks.tvlauncher.domain.AppCategory
import com.ks.tvlauncher.domain.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppArtworkKeyerTest {

    private fun app(versionCode: Long, hasBanner: Boolean = true) = AppInfo(
        packageName = "com.example.app",
        activityName = "com.example.app.TvActivity",
        label = "Example",
        versionCode = versionCode,
        category = null,
        hasBanner = hasBanner,
    )

    @Test
    fun artworkModelCarriesPackageVersionAndKind() {
        assertEquals(
            AppArtwork("com.example.app", "com.example.app.TvActivity", 7, banner = true),
            app(7).artwork(),
        )
    }

    @Test
    fun anAppUpdateOrAKindChangeGivesADifferentCacheModel() {
        assertNotEquals(app(7).artwork(), app(8).artwork())
        assertNotEquals(app(7, hasBanner = true).artwork(), app(7, hasBanner = false).artwork())
        assertEquals(app(7).artwork(), app(7).artwork())
    }

    @Test
    fun systemCategoriesMapToTheDrawerFilter() {
        assertEquals(AppCategory.VIDEO, AppsRepository.categoryOf(ApplicationInfo.CATEGORY_VIDEO))
        assertEquals(AppCategory.MUSIC, AppsRepository.categoryOf(ApplicationInfo.CATEGORY_AUDIO))
        assertEquals(AppCategory.GAMES, AppsRepository.categoryOf(ApplicationInfo.CATEGORY_GAME))
        assertEquals(AppCategory.TOOLS, AppsRepository.categoryOf(ApplicationInfo.CATEGORY_PRODUCTIVITY))
        assertNull(AppsRepository.categoryOf(ApplicationInfo.CATEGORY_UNDEFINED))
        assertNull(AppsRepository.categoryOf(ApplicationInfo.CATEGORY_SOCIAL))
    }
}
