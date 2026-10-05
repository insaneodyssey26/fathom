package com.masum.fathom.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.masum.fathom.core.database.SqlDelightUserDataRepository
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DailyVerseNotifier.ACTION_SAVE) return
        val passageId = intent.getStringExtra(DailyVerseNotifier.EXTRA_PASSAGE_ID) ?: return
        val tradition = TraditionId.fromKeyOrNull(intent.getStringExtra(DailyVerseNotifier.EXTRA_TRADITION).orEmpty()) ?: return
        val reference = intent.getStringExtra(DailyVerseNotifier.EXTRA_REFERENCE).orEmpty()
        val translation = intent.getStringExtra(DailyVerseNotifier.EXTRA_TRANSLATION).orEmpty()

        val pending = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val driver = AndroidSqliteDriver(FathomUserDatabase.Schema, appContext, "app_user_data.db")
                val repository = SqlDelightUserDataRepository(FathomUserDatabase(driver))
                repository.saveBookmark(
                    Bookmark(
                        id = "bm_$passageId",
                        passageId = passageId,
                        createdAtEpochMs = System.currentTimeMillis()
                    )
                )
                driver.close()
                DailyVerseNotifier.post(
                    appContext,
                    DailyVerse(passageId, tradition, reference, translation),
                    SanctuaryPreferences(appContext).settings.value,
                    saved = true
                )
            } finally {
                pending.finish()
            }
        }
    }
}
