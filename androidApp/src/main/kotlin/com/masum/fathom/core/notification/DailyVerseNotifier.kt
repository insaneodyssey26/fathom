package com.masum.fathom.core.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.masum.fathom.MainActivity
import com.masum.fathom.R
import com.masum.fathom.core.sanctuary.SanctuarySettings
import com.masum.fathom.core.widget.DailyWisdomWidgetProvider
import com.masum.fathom.core.widget.WidgetArt
import com.masum.fathom.ui.theme.NocturneColors
import com.masum.fathom.ui.theme.toneFor

object DailyVerseNotifier {

    const val ACTION_SAVE = "com.masum.fathom.notification.SAVE_VERSE"
    const val EXTRA_PASSAGE_ID = "passage_id"
    const val EXTRA_TRADITION = "tradition_key"
    const val EXTRA_REFERENCE = "reference"
    const val EXTRA_TRANSLATION = "translation"

    @SuppressLint("MissingPermission")
    fun post(context: Context, verse: DailyVerse, settings: SanctuarySettings, saved: Boolean = false) {
        if (!NotificationChannels.areEnabled(context)) return
        NotificationChannels.register(context)

        val hue = toneFor(verse.traditionId, NocturneColors).hue.toArgb()
        val requestBase = verse.id.hashCode()

        val open = PendingIntent.getActivity(
            context,
            requestBase,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(DailyWisdomWidgetProvider.EXTRA_PASSAGE_ID, verse.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val share = PendingIntent.getActivity(
            context,
            requestBase + 1,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "“${verse.translation}”\n— ${verse.reference}\n\nShared from Fathom")
                },
                "Share verse"
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicVersion = NotificationCompat.Builder(context, NotificationChannels.DAILY_VERSE)
            .setSmallIcon(R.drawable.ic_stat_fathom)
            .setColor(hue)
            .setContentTitle("Today's verse")
            .setContentText("Open Fathom to read it.")
            .build()

        val builder = NotificationCompat.Builder(context, NotificationChannels.DAILY_VERSE)
            .setSmallIcon(R.drawable.ic_stat_fathom)
            .setLargeIcon(WidgetArt.emblem(context, verse.traditionId, hue, 128))
            .setColor(hue)
            .setContentTitle(verse.reference)
            .setContentText(verse.translation)
            .setSubText(if (saved) "${verse.traditionId.displayName} · Saved" else verse.traditionId.displayName)
            .setStyle(NotificationCompat.BigTextStyle().bigText(verse.translation))
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(if (settings.showVerseOnLockScreen) NotificationCompat.VISIBILITY_PUBLIC else NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(open)

        if (!saved) {
            val save = PendingIntent.getBroadcast(
                context,
                requestBase + 2,
                Intent(context, NotificationActionReceiver::class.java).apply {
                    action = ACTION_SAVE
                    putExtra(EXTRA_PASSAGE_ID, verse.id)
                    putExtra(EXTRA_TRADITION, verse.traditionId.key)
                    putExtra(EXTRA_REFERENCE, verse.reference)
                    putExtra(EXTRA_TRANSLATION, verse.translation)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Save", save)
        }
        builder.addAction(0, "Share", share)

        NotificationManagerCompat.from(context).notify(NotificationChannels.DAILY_VERSE_ID, builder.build())
    }
}
