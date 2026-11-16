package com.ks.tvlauncher

import android.app.Application

class KsApplication : Application() {
    /** Created on first use. No work happens in `onCreate`. */
    val container: AppContainer by lazy { AppContainer(this) }
}
