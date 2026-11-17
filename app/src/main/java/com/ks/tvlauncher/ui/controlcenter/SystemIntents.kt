package com.ks.tvlauncher.ui.controlcenter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.ks.tvlauncher.ui.apps.AppIntents

/** Android settings pages and the screensaver. Each falls back to the main settings page. */
object SystemIntents {

    /** Opens [action]; if the TV has no such page, the main Android settings. False if neither opens. */
    fun openSettings(context: Context, action: String): Boolean =
        AppIntents.start(context, Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) ||
            AppIntents.start(context, Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))

    /**
     * KS's notification settings; where the TV has no such page, KS's app details, then the
     * main settings page.
     */
    fun openAppNotificationSettings(context: Context): Boolean {
        val notifications = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return AppIntents.start(context, notifications) || AppIntents.start(context, details) ||
            openSettings(context, Settings.ACTION_SETTINGS)
    }

    /**
     * Starts the system screensaver (dream) through SystemUI's Somnambulator activity where the
     * TV has it; otherwise opens the screensaver settings page.
     */
    fun startScreensaver(context: Context): Boolean {
        val dream = Intent(Intent.ACTION_MAIN)
            .setComponent(ComponentName("com.android.systemui", "com.android.systemui.Somnambulator"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return AppIntents.start(context, dream) || openSettings(context, Settings.ACTION_DREAM_SETTINGS)
    }
}
