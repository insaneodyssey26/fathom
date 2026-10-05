package com.masum.fathom.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.masum.fathom.core.notification.DailyVersePicker
import com.masum.fathom.core.notification.DailyVerseNotifier
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.update.UpdateNotifier
import java.time.LocalDate

class NotificationTestReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settings = SanctuaryPreferences(context).settings.value
        when (intent.getStringExtra("kind")) {
            "update" -> UpdateNotifier.post(context, intent.getIntExtra("code", 99), force = true)
            else -> {
                val day = LocalDate.now().plusDays(intent.getIntExtra("offset", 0).toLong())
                DailyVerseNotifier.post(context, DailyVersePicker.pickFor(context, day, settings.verseSource, settings.userTradition), settings)
            }
        }
    }
}
