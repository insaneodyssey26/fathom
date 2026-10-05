package com.masum.fathom.core.update

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.install.model.UpdateAvailability
import com.masum.fathom.R
import com.masum.fathom.core.notification.NotificationChannels
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import java.util.concurrent.TimeUnit

object UpdateNotifier {

    private const val PREFS = "fathom_update_alerts"
    private const val KEY_LAST_NOTIFIED = "last_notified_version_code"

    fun lastNotified(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_LAST_NOTIFIED, 0)

    @SuppressLint("MissingPermission")
    fun post(context: Context, availableVersionCode: Int, force: Boolean = false): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!force && prefs.getInt(KEY_LAST_NOTIFIED, 0) >= availableVersionCode) return false
        if (!NotificationChannels.areEnabled(context)) return false
        NotificationChannels.register(context)

        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val target = if (market.resolveActivity(context.packageManager) != null) market else web
        val open = PendingIntent.getActivity(
            context,
            availableVersionCode,
            target,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.APP_UPDATES)
            .setSmallIcon(R.drawable.ic_stat_fathom)
            .setContentTitle("Fathom has an update")
            .setContentText("A newer version is ready on Google Play.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("A newer version of Fathom is ready on Google Play. It will keep your bookmarks, reflections and history."))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(open)
            .addAction(0, "Update", open)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationChannels.APP_UPDATE_ID, notification)
        prefs.edit().putInt(KEY_LAST_NOTIFIED, availableVersionCode).apply()
        return true
    }

    fun clear(context: Context) {
        NotificationManagerCompat.from(context).cancel(NotificationChannels.APP_UPDATE_ID)
    }
}

class UpdateCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SanctuaryPreferences(applicationContext).settings.value
        if (!settings.updateAlertsEnabled) return Result.success()

        return try {
            val info = AppUpdateManagerFactory.create(applicationContext).requestAppUpdateInfo()
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                UpdateNotifier.post(applicationContext, info.availableVersionCode())
            } else {
                UpdateNotifier.clear(applicationContext)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}

object UpdateCheckScheduler {

    private const val WORK_NAME = "fathom_update_check"

    fun sync(context: Context, enabled: Boolean) {
        val manager = WorkManager.getInstance(context)
        if (!enabled) {
            manager.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(12, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInitialDelay(30, TimeUnit.MINUTES)
            .build()
        manager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
