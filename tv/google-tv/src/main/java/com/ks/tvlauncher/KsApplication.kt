package com.ks.tvlauncher

import android.app.Application
import android.content.Context
import com.ks.tvlauncher.data.language.AppLanguage
import androidx.work.Configuration

/**
 * Also provides WorkManager's configuration, so WorkManager starts on demand (the first
 * reminder schedule or a due check) instead of at every app start.
 */
class KsApplication : Application(), Configuration.Provider {

    /** The chosen language on Android 8 to 12, so notifications use it too (specs/languages.md). */
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLanguage.wrap(base))
    }

    /** Created on first use. No work happens in `onCreate`. */
    val container: AppContainer by lazy { AppContainer(this) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
