package com.ks.tvlauncher.ui.controlcenter

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.ks.tvlauncher.ui.apps.AppIntents

/** Android settings pages and the screensaver. Settings pages fall back to the main settings page. */
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
     * TV has it; otherwise opens the screensaver settings page. Never falls back to the main
     * settings page: where neither exists, Control Center hides the tile
     * ([screensaverAvailable]).
     */
    fun startScreensaver(context: Context): Boolean =
        AppIntents.start(context, dreamIntent()) || AppIntents.start(context, dreamSettingsIntent())

    /** True if the TV has a screensaver to start or a screensaver settings page to open. */
    fun screensaverAvailable(context: Context): Boolean {
        val pm = context.packageManager
        return listOf(dreamIntent(), dreamSettingsIntent()).any { intent ->
            try {
                pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.exported == true
            } catch (e: RuntimeException) {
                false
            }
        }
    }

    private fun dreamIntent(): Intent = Intent(Intent.ACTION_MAIN)
        .setComponent(ComponentName("com.android.systemui", "com.android.systemui.Somnambulator"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun dreamSettingsIntent(): Intent =
        Intent(Settings.ACTION_DREAM_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
