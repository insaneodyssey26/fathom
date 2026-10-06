package com.masum.fathom.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.MainActivity
import com.masum.fathom.R
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.toneFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.FileOutputStream

data class WidgetPassage(
    val id: String,
    val traditionId: TraditionId?,
    val reference: String,
    val translation: String
)

object WidgetRepository {
    const val ACTION_REFRESH_WIDGET = "com.masum.fathom.ACTION_REFRESH_WIDGET"
    const val EXTRA_PASSAGE_ID = "extra_passage_id"

    val fallbackPassage = WidgetPassage(
        id = "meditations_5_16",
        traditionId = TraditionId.STOICISM,
        reference = "Meditations 5.16",
        translation = "The soul becomes dyed with the color of its thoughts."
    )

    fun readTheme(context: Context): ReadingTheme {
        val stored = context.getSharedPreferences("fathom_sanctuary_prefs", Context.MODE_PRIVATE).getString("reading_theme", null)
        return runCatching { ReadingTheme.valueOf(stored.orEmpty()) }.getOrDefault(ReadingTheme.OBSIDIAN)
    }

    fun loadRandomPassage(context: Context): WidgetPassage {
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
                WidgetPassage(
                    id = id,
                    traditionId = TraditionId.fromKeyOrNull(traditionKey),
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

    fun updateAll(context: Context) {
        val providers = listOf(
            DailyWisdomWidgetProvider::class.java,
            CompactWisdomWidgetProvider::class.java,
            HorizonWisdomWidgetProvider::class.java
        )
        val appWidgetManager = AppWidgetManager.getInstance(context)
        for (provider in providers) {
            val component = ComponentName(context, provider)
            val ids = appWidgetManager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, provider).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}

class DailyWisdomWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH_WIDGET = WidgetRepository.ACTION_REFRESH_WIDGET
        const val EXTRA_PASSAGE_ID = WidgetRepository.EXTRA_PASSAGE_ID

        fun updateAllWidgets(context: Context) {
            WidgetRepository.updateAll(context)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            val passage = WidgetRepository.loadRandomPassage(context)
            for (widgetId in appWidgetIds) {
                render(context, appWidgetManager, widgetId, passage)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scope.launch { render(context, appWidgetManager, appWidgetId, WidgetRepository.loadRandomPassage(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == ACTION_REFRESH_WIDGET || action == Intent.ACTION_DATE_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
            updateAll(context)
        } else if (action == Intent.ACTION_USER_PRESENT) {
            val prefs = context.getSharedPreferences("fathom_widget_prefs", Context.MODE_PRIVATE)
            val lastUnlockTime = prefs.getLong("last_unlock_widget_refresh", 0L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUnlockTime >= 30 * 60 * 1000L) {
                prefs.edit().putLong("last_unlock_widget_refresh", currentTime).apply()
                updateAll(context)
            }
        }
    }

    private fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DailyWisdomWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            scope.launch {
                val passage = WidgetRepository.loadRandomPassage(context)
                for (widgetId in appWidgetIds) {
                    render(context, appWidgetManager, widgetId, passage)
                }
            }
        }
    }

    private fun render(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, passage: WidgetPassage) {
        val views = RemoteViews(context.packageName, R.layout.widget_daily_wisdom)
        val colors = WidgetRepository.readTheme(context).palette()
        val tone = toneFor(passage.traditionId, colors)
        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250).coerceAtLeast(200)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 120).coerceAtLeast(110)
        val scale = minOf(1f, kotlin.math.sqrt(1_200_000f / (widthDp * density * heightDp * density)))
        val widthPx = (widthDp * density * scale).toInt()
        val heightPx = (heightDp * density * scale).toInt()

        views.setImageViewBitmap(
            R.id.widget_background,
            WidgetArt.background(widthPx, heightPx, colors, tone, 28f * density * scale, passage.id.hashCode().toLong())
        )
        views.setImageViewBitmap(R.id.widget_emblem, WidgetArt.emblem(context, passage.traditionId, tone.hue.toArgb(), (22 * density).toInt()))
        views.setTextViewText(R.id.widget_tradition_badge, (passage.traditionId?.displayName ?: "Fathom").uppercase())
        views.setTextColor(R.id.widget_tradition_badge, tone.hue.toArgb())
        views.setTextViewText(R.id.widget_verse_text, passage.translation)
        views.setTextColor(R.id.widget_verse_text, colors.textPrimary.toArgb())
        views.setTextViewText(R.id.widget_verse_reference, passage.reference.uppercase())
        views.setTextColor(R.id.widget_verse_reference, colors.textSecondary.toArgb())
        views.setImageViewBitmap(R.id.widget_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (18 * density).toInt()))
        views.setInt(R.id.widget_btn_refresh, "setColorFilter", colors.textSecondary.toArgb())

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PASSAGE_ID, passage.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(context, widgetId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        val refreshIntent = Intent(context, DailyWisdomWidgetProvider::class.java).apply { action = ACTION_REFRESH_WIDGET }
        val refreshPendingIntent = PendingIntent.getBroadcast(context, widgetId + 10000, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}

class CompactWisdomWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            val passage = WidgetRepository.loadRandomPassage(context)
            for (widgetId in appWidgetIds) {
                render(context, appWidgetManager, widgetId, passage)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scope.launch { render(context, appWidgetManager, appWidgetId, WidgetRepository.loadRandomPassage(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == WidgetRepository.ACTION_REFRESH_WIDGET || action == Intent.ACTION_DATE_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
            updateAll(context)
        } else if (action == Intent.ACTION_USER_PRESENT) {
            val prefs = context.getSharedPreferences("fathom_widget_prefs", Context.MODE_PRIVATE)
            val lastUnlockTime = prefs.getLong("last_unlock_widget_refresh", 0L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUnlockTime >= 30 * 60 * 1000L) {
                prefs.edit().putLong("last_unlock_widget_refresh", currentTime).apply()
                updateAll(context)
            }
        }
    }

    private fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, CompactWisdomWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            scope.launch {
                val passage = WidgetRepository.loadRandomPassage(context)
                for (widgetId in appWidgetIds) {
                    render(context, appWidgetManager, widgetId, passage)
                }
            }
        }
    }

    private fun render(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, passage: WidgetPassage) {
        val views = RemoteViews(context.packageName, R.layout.widget_compact_wisdom)
        val colors = WidgetRepository.readTheme(context).palette()
        val tone = toneFor(passage.traditionId, colors)
        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 140).coerceAtLeast(120)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 120).coerceAtLeast(110)
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        views.setImageViewBitmap(
            R.id.widget_background,
            WidgetArt.background(widthPx, heightPx, colors, tone, 24f * density, passage.id.hashCode().toLong())
        )
        views.setImageViewBitmap(R.id.widget_emblem, WidgetArt.emblem(context, passage.traditionId, tone.hue.toArgb(), (18 * density).toInt()))
        views.setTextViewText(R.id.widget_tradition_badge, (passage.traditionId?.displayName ?: "Fathom").uppercase())
        views.setTextColor(R.id.widget_tradition_badge, tone.hue.toArgb())
        views.setTextViewText(R.id.widget_verse_text, passage.translation)
        views.setTextColor(R.id.widget_verse_text, colors.textPrimary.toArgb())
        views.setTextViewText(R.id.widget_verse_reference, passage.reference.uppercase())
        views.setTextColor(R.id.widget_verse_reference, colors.textSecondary.toArgb())
        views.setImageViewBitmap(R.id.widget_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (13 * density).toInt()))
        views.setInt(R.id.widget_btn_refresh, "setColorFilter", colors.textSecondary.toArgb())

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(WidgetRepository.EXTRA_PASSAGE_ID, passage.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(context, widgetId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        val refreshIntent = Intent(context, CompactWisdomWidgetProvider::class.java).apply { action = WidgetRepository.ACTION_REFRESH_WIDGET }
        val refreshPendingIntent = PendingIntent.getBroadcast(context, widgetId + 20000, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}

class HorizonWisdomWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            val passage = WidgetRepository.loadRandomPassage(context)
            for (widgetId in appWidgetIds) {
                render(context, appWidgetManager, widgetId, passage)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scope.launch { render(context, appWidgetManager, appWidgetId, WidgetRepository.loadRandomPassage(context)) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == WidgetRepository.ACTION_REFRESH_WIDGET || action == Intent.ACTION_DATE_CHANGED || action == Intent.ACTION_TIMEZONE_CHANGED) {
            updateAll(context)
        } else if (action == Intent.ACTION_USER_PRESENT) {
            val prefs = context.getSharedPreferences("fathom_widget_prefs", Context.MODE_PRIVATE)
            val lastUnlockTime = prefs.getLong("last_unlock_widget_refresh", 0L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastUnlockTime >= 30 * 60 * 1000L) {
                prefs.edit().putLong("last_unlock_widget_refresh", currentTime).apply()
                updateAll(context)
            }
        }
    }

    private fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, HorizonWisdomWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            scope.launch {
                val passage = WidgetRepository.loadRandomPassage(context)
                for (widgetId in appWidgetIds) {
                    render(context, appWidgetManager, widgetId, passage)
                }
            }
        }
    }

    private fun render(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, passage: WidgetPassage) {
        val views = RemoteViews(context.packageName, R.layout.widget_horizon_wisdom)
        val colors = WidgetRepository.readTheme(context).palette()
        val tone = toneFor(passage.traditionId, colors)
        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 260).coerceAtLeast(200)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 60).coerceAtLeast(50)
        val widthPx = (widthDp * density).toInt()
        val heightPx = (heightDp * density).toInt()

        views.setImageViewBitmap(
            R.id.widget_background,
            WidgetArt.background(widthPx, heightPx, colors, tone, 22f * density, passage.id.hashCode().toLong())
        )
        views.setImageViewBitmap(R.id.widget_emblem, WidgetArt.emblem(context, passage.traditionId, tone.hue.toArgb(), (24 * density).toInt()))
        views.setTextViewText(R.id.widget_tradition_badge, (passage.traditionId?.displayName ?: "Fathom").uppercase())
        views.setTextColor(R.id.widget_tradition_badge, tone.hue.toArgb())
        views.setTextViewText(R.id.widget_verse_text, passage.translation)
        views.setTextColor(R.id.widget_verse_text, colors.textPrimary.toArgb())
        views.setTextViewText(R.id.widget_verse_reference, passage.reference.uppercase())
        views.setTextColor(R.id.widget_verse_reference, colors.textSecondary.toArgb())
        views.setImageViewBitmap(R.id.widget_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (13 * density).toInt()))
        views.setInt(R.id.widget_btn_refresh, "setColorFilter", colors.textSecondary.toArgb())

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(WidgetRepository.EXTRA_PASSAGE_ID, passage.id)
        }
        val openAppPendingIntent = PendingIntent.getActivity(context, widgetId, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        val refreshIntent = Intent(context, HorizonWisdomWidgetProvider::class.java).apply { action = WidgetRepository.ACTION_REFRESH_WIDGET }
        val refreshPendingIntent = PendingIntent.getBroadcast(context, widgetId + 30000, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
