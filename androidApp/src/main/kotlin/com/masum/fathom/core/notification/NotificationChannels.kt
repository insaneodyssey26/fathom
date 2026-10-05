package com.masum.fathom.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object NotificationChannels {
    const val DAILY_VERSE = "fathom_daily_verse"
    const val APP_UPDATES = "fathom_app_updates"
    private const val LEGACY_DAILY = "fathom_daily_contemplation"

    const val DAILY_VERSE_ID = 1001
    const val APP_UPDATE_ID = 1002

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        manager.deleteNotificationChannel(LEGACY_DAILY)

        manager.createNotificationChannel(
            NotificationChannel(DAILY_VERSE, "Daily verse", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "One verse a day at the time you choose."
                setShowBadge(false)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(APP_UPDATES, "App updates", NotificationManager.IMPORTANCE_LOW).apply {
                description = "A quiet note when a new version of Fathom is available on Google Play."
                setShowBadge(false)
            }
        )
    }

    fun areEnabled(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()
}
