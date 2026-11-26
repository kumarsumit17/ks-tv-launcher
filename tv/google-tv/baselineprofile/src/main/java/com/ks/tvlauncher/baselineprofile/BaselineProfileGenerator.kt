package com.ks.tvlauncher.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Generates the start-up and Home profile shipped in the app (src/release/generated/baselineProfiles). */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startupAndHome() = rule.collect(packageName = TARGET_PACKAGE, includeInStartupProfile = true) {
        openHome()
        browseHome()
    }
}
