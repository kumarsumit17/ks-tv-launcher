package com.ks.tvlauncher

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Fails if the HOME / LEANBACK_LAUNCHER intent filters are removed from the manifest. */
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
    fun handlesHomeIntent() {
        assertTrue(resolvesToMain(Intent.CATEGORY_HOME, Intent.CATEGORY_DEFAULT))
    }

    @Test
    fun appearsInLeanbackLauncher() {
        assertTrue(resolvesToMain(Intent.CATEGORY_LEANBACK_LAUNCHER))
    }
}
