package com.jitou.app.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jitou.app.MainActivity
import com.jitou.app.R
import com.jitou.app.data.local.ActiveProposalId
import com.jitou.app.data.local.JitouDatabase
import com.jitou.app.data.local.toDomain
import com.jitou.app.model.HaircutNotificationRules
import com.jitou.app.model.ScheduledHaircutNotification
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object HaircutNotificationScheduler {
    private const val ChannelId = "haircut_reminders"
    private const val ChannelName = "几头提醒"
    private const val AlarmRequestCode = 2209
    private const val NotificationId = 2209
    private const val ExtraScheduledAtMillis = "scheduled_at_millis"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ChannelId,
            ChannelName,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "根据上次剪头日期提醒几时头"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun scheduleNext(context: Context) {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            rescheduleFromDatabase(appContext)
        }
    }

    internal suspend fun rescheduleFromDatabase(
        context: Context,
        now: LocalDateTime = LocalDateTime.now(),
    ) {
        ensureChannel(context)

        val database = JitouDatabase.getInstance(context)
        val records = database.haircutRecordDao().getAll().map { it.toDomain() }
        val proposalStatus = database.appointmentProposalDao()
            .getActive(ActiveProposalId)
            ?.toDomain()
            ?.status
        val next = HaircutNotificationRules.nextNotificationAfter(
            records = records,
            proposalStatus = proposalStatus,
            now = now,
        )

        if (next == null) {
            cancel(context)
        } else {
            schedule(context, next)
        }
    }

    internal suspend fun notifyIfStillDue(
        context: Context,
        scheduledAtMillis: Long,
    ) {
        val database = JitouDatabase.getInstance(context)
        val records = database.haircutRecordDao().getAll().map { it.toDomain() }
        val proposalStatus = database.appointmentProposalDao()
            .getActive(ActiveProposalId)
            ?.toDomain()
            ?.status
        val scheduledAt = scheduledAtMillis.toLocalDateTime()
        val dueNotification = HaircutNotificationRules.notificationAt(
            records = records,
            proposalStatus = proposalStatus,
            at = scheduledAt,
        )

        if (dueNotification != null && context.canPostNotifications()) {
            showNotification(context, dueNotification.message)
        }
    }

    internal fun scheduledAtMillis(intent: Intent): Long =
        intent.getLongExtra(ExtraScheduledAtMillis, 0L).takeIf { it > 0L } ?: System.currentTimeMillis()

    private fun schedule(
        context: Context,
        notification: ScheduledHaircutNotification,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAtMillis = notification.dateTime.toEpochMillis()
        val pendingIntent = alarmPendingIntent(context, triggerAtMillis)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(alarmPendingIntent(context, 0L))
    }

    private fun alarmPendingIntent(
        context: Context,
        scheduledAtMillis: Long,
    ): PendingIntent {
        val intent = Intent(context, HaircutNotificationReceiver::class.java)
            .putExtra(ExtraScheduledAtMillis, scheduledAtMillis)
        return PendingIntent.getBroadcast(
            context,
            AlarmRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun showNotification(
        context: Context,
        message: String,
    ) {
        val openAppIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("几头")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(openAppIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationId, notification)
    }

    private fun Context.canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun Long.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

class HaircutNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val scheduledAtMillis = HaircutNotificationScheduler.scheduledAtMillis(intent)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                HaircutNotificationScheduler.ensureChannel(appContext)
                HaircutNotificationScheduler.notifyIfStillDue(appContext, scheduledAtMillis)
                HaircutNotificationScheduler.rescheduleFromDatabase(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

class HaircutBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    HaircutNotificationScheduler.rescheduleFromDatabase(appContext)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
