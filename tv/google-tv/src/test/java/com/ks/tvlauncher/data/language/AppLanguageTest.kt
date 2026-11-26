package com.ks.tvlauncher.data.language

import org.junit.Assert.assertEquals
import org.junit.Test

/** The suite's language rule: anything unknown in the saved setting counts as "Same as the system". */
class AppLanguageTest {
    @Test
    fun parsesSavedChoices() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.parse(null))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.parse(""))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.parse("  "))
        assertEquals("en", AppLanguage.parse("en"))
        assertEquals("hi", AppLanguage.parse("hi"))
        assertEquals("hi", AppLanguage.parse("hi-IN"))
        assertEquals("en", AppLanguage.parse("en_GB"))
        assertEquals("hi", AppLanguage.parse("HI"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.parse("fr"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.parse("nonsense"))
    }

    @Test
    fun speaksEnglishAndHindi() {
        assertEquals(listOf("en", "hi"), AppLanguage.SUPPORTED)
    }
}
