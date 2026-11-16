package com.ks.tvlauncher

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * App mode (docs/spec/app-mode.md): KS is listed among TV apps and must never be offered as
 * the Home app. Fails if LEANBACK_LAUNCHER is removed or HOME is added to the manifest.
 */
@RunWith(AndroidJUnit4::class)
class HomeIntentTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun resolvesToMain(vararg categories: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
        categories.forEach(intent::addCategory)
        @Suppress("DEPRECATION")
        return context.packageManager.queryIntentActivities(intent, 0)
            .any { it.activityInfo.name == MainActivity::class.java.name }
    }

    @Test
    fun appearsInLeanbackLauncher() {
        assertTrue(resolvesToMain(Intent.CATEGORY_LEANBACK_LAUNCHER))
    }

    @Test
    fun isNeverAHomeApp() {
        assertFalse(resolvesToMain(Intent.CATEGORY_HOME))
        assertFalse(resolvesToMain(Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT))
    }

    @Test
    fun tileLabelIsShort() {
        val label = context.packageManager
            .getActivityInfo(android.content.ComponentName(context, MainActivity::class.java), 0)
            .loadLabel(context.packageManager)
        assertTrue(label.toString() == "KS TV")
    }
}
