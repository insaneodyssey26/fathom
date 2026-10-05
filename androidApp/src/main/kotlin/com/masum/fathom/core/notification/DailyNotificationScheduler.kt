package com.masum.fathom.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.update.UpdateCheckScheduler
import java.time.LocalDate
import java.util.Calendar

class DailyWisdomReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val settings = SanctuaryPreferences(context).settings.value
        if (!settings.dailyNotificationEnabled) return

        val verse = DailyVersePicker.pickFor(context, LocalDate.now(), settings.verseSource, settings.userTradition)
        DailyVerseNotifier.post(context, verse, settings)
        DailyNotificationScheduler.scheduleDailyReminder(context, settings.notificationHour, settings.notificationMinute)
    }
}

class ScheduleRestoreReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        NotificationChannels.register(context)
        val settings = SanctuaryPreferences(context).settings.value
        if (settings.dailyNotificationEnabled) {
            DailyNotificationScheduler.scheduleDailyReminder(context, settings.notificationHour, settings.notificationMinute)
        }
        UpdateCheckScheduler.sync(context, settings.updateAlertsEnabled)
    }
}

object DailyNotificationScheduler {

    private const val REQUEST_CODE = 101

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        Intent(context, DailyWisdomReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun nextTriggerMillis(hour: Int, minute: Int, now: Long = System.currentTimeMillis()): Long =
        Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now + 1_000L) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        runCatching {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTriggerMillis(hour, minute), pendingIntent(context))
        }
    }

    fun cancelDailyReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(pendingIntent(context))
    }
}
