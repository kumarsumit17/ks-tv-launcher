package com.ks.tvlauncher.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Cold start to first frame (timeToInitialDisplay) and to Home ready (timeToFullDisplay),
 * without and with the baseline profile (specs/performance.md).
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun coldStartNoProfile() = coldStart(CompilationMode.None())

    @Test
    fun coldStartWithProfile() = coldStart(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun coldStart(mode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait()
        // Keep tracing until the frame after reportFullyDrawn is rendered, or the metric has no end.
        device.waitForIdle()
        Thread.sleep(SETTLE_MS)
    }

    private companion object {
        const val SETTLE_MS = 1_000L
    }
}
