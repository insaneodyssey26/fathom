package com.masum.fathom.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.widget.RemoteViews
import com.masum.fathom.MainActivity
import com.masum.fathom.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class WidgetPassage(
    val id: String,
    val tradition: String,
    val reference: String,
    val translation: String
)

class DailyWisdomWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.masum.fathom.ACTION_REFRESH_WIDGET"
        const val EXTRA_PASSAGE_ID = "extra_passage_id"

        private val fallbackPassage = WidgetPassage(
            id = "meditations_5_16",
            tradition = "STOICISM",
            reference = "Meditations 5.16",
            translation = "The soul becomes dyed with the color of its thoughts."
        )

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DailyWisdomWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, DailyWisdomWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            val passage = loadRandomPassage(context)
            for (widgetId in appWidgetIds) {
                renderWidget(context, appWidgetManager, widgetId, passage)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        if (action == ACTION_REFRESH_WIDGET ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DailyWisdomWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                scope.launch {
                    val passage = loadRandomPassage(context)
                    for (widgetId in appWidgetIds) {
                        renderWidget(context, appWidgetManager, widgetId, passage)
                    }
                }
            }
        } else if (action == Intent.ACTION_USER_PRESENT) {
            val prefs = context.getSharedPreferences("fathom_widget_prefs", Context.MODE_PRIVATE)
            val lastUnlockTime = prefs.getLong("last_unlock_widget_refresh", 0L)
            val currentTime = System.currentTimeMillis()
            val thirtyMinutesMs = 30 * 60 * 1000L

            if (currentTime - lastUnlockTime >= thirtyMinutesMs) {
                prefs.edit().putLong("last_unlock_widget_refresh", currentTime).apply()
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, DailyWisdomWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds.isNotEmpty()) {
                    scope.launch {
                        val passage = loadRandomPassage(context)
                        for (widgetId in appWidgetIds) {
                            renderWidget(context, appWidgetManager, widgetId, passage)
                        }
                    }
                }
            }
        }
    }

    private fun renderWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        passage: WidgetPassage
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_daily_wisdom)

        views.setTextViewText(R.id.widget_tradition_badge, passage.tradition.uppercase())
        views.setTextViewText(R.id.widget_verse_text, "“${passage.translation}”")
        views.setTextViewText(R.id.widget_verse_reference, passage.reference)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PASSAGE_ID, passage.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        val refreshIntent = Intent(context, DailyWisdomWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId + 10000,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    private fun loadRandomPassage(context: Context): WidgetPassage {
        val dbFile = context.getDatabasePath("starter_content.db")
        if (!dbFile.exists() || dbFile.length() < 100_000L) {
            try {
                dbFile.parentFile?.mkdirs()
                context.assets.open("starter_content.db").use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (_: Exception) {
                return fallbackPassage
            }
        }

        return try {
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery(
                "SELECT id, tradition_id, canonical_reference, translation FROM passage_entity ORDER BY RANDOM() LIMIT 1",
                null
            )
            val result = if (cursor.moveToFirst()) {
                val id = cursor.getString(0)
                val traditionKey = cursor.getString(1)
                val reference = cursor.getString(2)
                val translation = cursor.getString(3)
                val formattedTradition = when (traditionKey.lowercase()) {
                    "hinduism" -> "HINDUISM"
                    "islam" -> "ISLAM"
                    "christianity" -> "CHRISTIANITY"
                    "judaism" -> "JUDAISM"
                    "buddhism" -> "BUDDHISM"
                    "taoism" -> "TAOISM"
                    "stoicism" -> "STOICISM"
                    else -> traditionKey.uppercase()
                }
                WidgetPassage(
                    id = id,
                    tradition = formattedTradition,
                    reference = reference,
                    translation = translation
                )
            } else {
                fallbackPassage
            }
            cursor.close()
            db.close()
            result
        } catch (_: Exception) {
            fallbackPassage
        }
    }
}
