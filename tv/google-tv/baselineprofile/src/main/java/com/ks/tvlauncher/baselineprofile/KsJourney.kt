package com.ks.tvlauncher.baselineprofile

import android.view.KeyEvent
import androidx.benchmark.macro.MacrobenchmarkScope

/** KS TV Dev: the generator and benchmarks never touch the real app (specs/dev-workflow.md). */
const val TARGET_PACKAGE = "com.ks.tvlauncher.dev"

/** Opens KS and waits until Home reports it is fully drawn (the app grid has its apps). */
fun MacrobenchmarkScope.openHome() {
    pressHome()
    startActivityAndWait()
}

/**
 * The common first minute on Home with the remote: close the first-run name dialog if it is
 * open, then move focus through the top bar, the hero, the pinned row and the bottom strip.
 */
fun MacrobenchmarkScope.browseHome() {
    val keys = listOf(
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN,
    )
    for (key in keys) {
        device.pressKeyCode(key)
        device.waitForIdle(500)
    }
}
