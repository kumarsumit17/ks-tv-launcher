package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppListCodecTest {
    private val apps = listOf(
        AppInfo("com.a", "com.a.Main", "Alpha", 12L, AppCategory.VIDEO, hasBanner = true),
        AppInfo("com.b", "com.b.Tv", "बीटा", 0L, null, hasBanner = false),
    )

    @Test
    fun roundTrips() {
        assertEquals(apps, AppListCodec.decode(AppListCodec.encode(apps)))
        assertEquals(emptyList<AppInfo>(), AppListCodec.decode(AppListCodec.encode(emptyList())))
    }

    @Test
    fun unreadableMeansNoCache() {
        listOf(null, "", "  ", "not json", "[]", """{"format":2,"apps":[]}""").forEach { assertNull(it, AppListCodec.decode(it)) }
    }

    @Test
    fun skipsBrokenEntriesAndUnknownCategories() {
        val text = """{"format":1,"apps":[
            {"packageName":"","activityName":"x","label":"Empty"},
            {"packageName":"com.c","activityName":"com.c.M","label":"","category":"SPORTS","extra":1}
        ]}"""
        assertEquals(listOf(AppInfo("com.c", "com.c.M", "com.c", 0L, null, false)), AppListCodec.decode(text))
    }
}
