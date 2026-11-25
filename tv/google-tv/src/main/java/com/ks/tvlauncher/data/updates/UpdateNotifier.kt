package com.ks.tvlauncher.data.updates

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ks.tvlauncher.MainActivity
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.reminders.ReminderNotifier
import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo

/**
 * "KS TV update available" on its own "App updates" channel, under its own tag, so renewal
 * reminders never withdraw it and it never withdraws them.
 */
class UpdateNotifier(context: Context) {
    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)

    /** Same rule as reminders: the permission on Android 13+ and notifications not blocked. */
    fun canNotify(): Boolean = ReminderNotifier(context).canNotify()

    fun post(release: ReleaseInfo) {
        if (!canNotify()) return
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.update_channel))
                .setDescription(context.getString(R.string.update_channel_description))
                .build(),
        )
        val open = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_OPEN_UPDATES, true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.update_notification_title, release.versionName))
            .setContentText(context.getString(R.string.update_notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(TAG, AppUpdates.NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission withdrawn between the check and the post: the Home pill still shows it.
        }
    }

    fun cancel() = manager.cancel(TAG, AppUpdates.NOTIFICATION_ID)

    companion object {
        const val CHANNEL_ID = "app_updates"
        const val TAG = "app_update"
        private const val REQUEST_CODE = 1
    }
}
