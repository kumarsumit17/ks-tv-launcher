package com.ks.tvlauncher.data.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ks.tvlauncher.MainActivity
import com.ks.tvlauncher.R
import com.ks.tvlauncher.domain.BillingCycle
import com.ks.tvlauncher.domain.DueReminder
import com.ks.tvlauncher.domain.Money
import com.ks.tvlauncher.domain.RenewalReminders
import com.ks.tvlauncher.util.DateTimeFormats

/** Posts and withdraws renewal notifications on one low-key channel. */
class ReminderNotifier(context: Context) {
    private val context = context.applicationContext
    private val manager = NotificationManagerCompat.from(this.context)

    /** False when the user has blocked KS notifications (or not allowed them on Android 13+). */
    fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return manager.areNotificationsEnabled()
    }

    fun cancel(subscriptionId: String) = manager.cancel(RenewalReminders.notificationId(subscriptionId))

    fun cancelAll() = manager.cancelAll()

    fun post(reminder: DueReminder, currency: String) {
        if (!canNotify()) return
        ensureChannel()
        val sub = reminder.subscription
        val res = context.resources
        val title = when (reminder.daysUntil) {
            0L -> res.getString(R.string.reminder_title_today, sub.name)
            1L -> res.getString(R.string.reminder_title_tomorrow, sub.name)
            else -> res.getQuantityString(R.plurals.reminder_title_days, reminder.daysUntil.toInt(), sub.name, reminder.daysUntil.toInt())
        }
        val cycle = res.getString(if (sub.cycle == BillingCycle.MONTHLY) R.string.subs_monthly else R.string.subs_yearly)
        val text = res.getString(
            R.string.reminder_text,
            Money.format(sub.costMinor, currency),
            cycle,
            DateTimeFormats.mediumDate(reminder.date),
        )
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(RenewalReminders.notificationId(sub.id), notification)
        } catch (e: SecurityException) {
            // Permission withdrawn between the check and the post: skip quietly.
        }
    }

    private fun ensureChannel() {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(context.getString(R.string.reminder_channel))
                .setDescription(context.getString(R.string.reminder_channel_description))
                .build(),
        )
    }

    private companion object {
        const val CHANNEL_ID = "renewals"
    }
}
