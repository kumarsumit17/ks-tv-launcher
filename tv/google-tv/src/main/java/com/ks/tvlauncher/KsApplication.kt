package com.ks.tvlauncher

import android.app.Application
import androidx.work.Configuration

/**
 * Also provides WorkManager's configuration, so WorkManager starts on demand (the first
 * reminder schedule or a due check) instead of at every app start.
 */
class KsApplication : Application(), Configuration.Provider {
    /** Created on first use. No work happens in `onCreate`. */
    val container: AppContainer by lazy { AppContainer(this) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
