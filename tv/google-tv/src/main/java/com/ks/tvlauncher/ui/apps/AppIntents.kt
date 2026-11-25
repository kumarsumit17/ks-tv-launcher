package com.ks.tvlauncher.ui.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** Intents for acting on another app, and a start call that reports failure instead of throwing. */
object AppIntents {

    /** The system's "App info" page for the package. */
    fun appInfo(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(packageName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** The system uninstall dialog. The user confirms there; nothing is removed silently. */
    fun uninstall(packageName: String): Intent =
        Intent(Intent.ACTION_DELETE, packageUri(packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Starts the activity; false if there is no intent or nothing can handle it. */
    fun start(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun packageUri(packageName: String): Uri = Uri.fromParts("package", packageName, null)
}
