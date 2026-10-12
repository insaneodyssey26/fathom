package com.masum.fathom.core.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.MainActivity
import com.masum.fathom.R
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.feed.PassageExegesis
import com.masum.fathom.ui.feed.ScriptureExegesisEngine
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.toneFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WidgetPassage(
    val id: String,
    val traditionId: TraditionId?,
    val reference: String,
    val translation: String,
    val explanationTitle: String? = null,
    val explanationBody: String? = null,
    val spiritualInsight: String? = null,
    val sourceAttribution: String? = null
)

data class StreakSummary(
    val streakDays: Int,
    val hasReadToday: Boolean,
    val weeklyHistory: List<Boolean>,
    val totalReads: Int
)

object WidgetRepository {
    const val ACTION_REFRESH_WIDGET = "com.masum.fathom.ACTION_REFRESH_WIDGET"
    const val ACTION_TOGGLE_BOOKMARK = "com.masum.fathom.ACTION_TOGGLE_BOOKMARK"
    const val ACTION_TOGGLE_INFO = "com.masum.fathom.ACTION_TOGGLE_INFO"
    const val EXTRA_PASSAGE_ID = "extra_passage_id"
    private const val PREFS_NAME = "fathom_widget_state"

    val fallbackPassage = WidgetPassage(
        id = "meditations_5_16",
        traditionId = TraditionId.STOICISM,
        reference = "Meditations 5.16",
        translation = "The soul becomes dyed with the color of its thoughts.",
        explanationTitle = "Tranquility of Mind and Character",
        explanationBody = "Marcus Aurelius reminds himself that the mind takes the shape and color of what it frequently contemplates. Constant attention to noble truths transforms the interior disposition.",
        spiritualInsight = "Reflect on what impressions and thoughts you allow into your awareness today; guard your inner sanctuary.",
        sourceAttribution = "Marcus Aurelius • Stoic Corpus"
    )

    fun readTheme(context: Context): ReadingTheme {
        val stored = context.getSharedPreferences("fathom_sanctuary_prefs", Context.MODE_PRIVATE).getString("reading_theme", null)
        return runCatching { ReadingTheme.valueOf(stored.orEmpty()) }.getOrDefault(ReadingTheme.OBSIDIAN)
    }

    fun getWidgetPassageId(context: Context, widgetId: Int): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("passage_id_$widgetId", null) ?: fallbackPassage.id
    }

    fun setWidgetPassageId(context: Context, widgetId: Int, passageId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("passage_id_$widgetId", passageId)
            .apply()
    }

    fun getWidgetShowInfo(context: Context, widgetId: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("show_info_$widgetId", false)
    }

    fun setWidgetShowInfo(context: Context, widgetId: Int, showInfo: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("show_info_$widgetId", showInfo)
            .apply()
    }

    private fun loadFromQuery(context: Context, query: String, args: Array<String>? = null): WidgetPassage {
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
            val cursor = db.rawQuery(query, args)
            val result = if (cursor.moveToFirst()) {
                val id = cursor.getString(0)
                val bookId = cursor.getString(1)
                val traditionKey = cursor.getString(2)
                val reference = cursor.getString(3)
                val translation = cursor.getString(4)
                val chapter = if (cursor.isNull(5)) null else cursor.getInt(5)
                val verseNumber = if (cursor.isNull(6)) null else cursor.getInt(6)
                val dbTitle = if (cursor.columnCount > 7 && !cursor.isNull(7)) cursor.getString(7) else null
                val dbBody = if (cursor.columnCount > 8 && !cursor.isNull(8)) cursor.getString(8) else null
                val dbAuthor = if (cursor.columnCount > 9 && !cursor.isNull(9)) cursor.getString(9) else null
                val dbSource = if (cursor.columnCount > 10 && !cursor.isNull(10)) cursor.getString(10) else null

                val traditionId = TraditionId.fromKeyOrNull(traditionKey) ?: TraditionId.STOICISM
                val exegesis: PassageExegesis = if (!dbBody.isNullOrBlank()) {
                    PassageExegesis(
                        title = dbTitle ?: "Canonical Meaning & Context",
                        contextSummary = dbBody,
                        spiritualInsight = "",
                        sourceAttribution = listOfNotNull(dbAuthor, dbSource).joinToString(" • ")
                    )
                } else {
                    val dummyPassage = Passage(
                        id = id,
                        bookId = bookId,
                        traditionId = traditionId,
                        canonicalReference = reference,
                        originalText = null,
                        translation = translation,
                        orderIndex = 0,
                        chapter = chapter,
                        verseNumber = verseNumber
                    )
                    ScriptureExegesisEngine.generateExegesis(dummyPassage)
                }

                WidgetPassage(
                    id = id,
                    traditionId = traditionId,
                    reference = reference,
                    translation = translation,
                    explanationTitle = exegesis.title,
                    explanationBody = exegesis.contextSummary,
                    spiritualInsight = exegesis.spiritualInsight,
                    sourceAttribution = exegesis.sourceAttribution
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

    fun loadRandomPassage(context: Context): WidgetPassage {
        return loadFromQuery(
            context,
            "SELECT p.id, p.book_id, p.tradition_id, p.canonical_reference, p.translation, p.chapter, p.verse_number, e.title, e.body, e.author, e.source FROM passage_entity p LEFT JOIN explanation_entity e ON e.passage_id = p.id ORDER BY RANDOM() LIMIT 1"
        )
    }

    fun loadPassageById(context: Context, id: String): WidgetPassage {
        return loadFromQuery(
            context,
            "SELECT p.id, p.book_id, p.tradition_id, p.canonical_reference, p.translation, p.chapter, p.verse_number, e.title, e.body, e.author, e.source FROM passage_entity p LEFT JOIN explanation_entity e ON e.passage_id = p.id WHERE p.id = ? LIMIT 1",
            arrayOf(id)
        )
    }

    fun isBookmarked(context: Context, passageId: String): Boolean {
        val dbFile = context.getDatabasePath("app_user_data.db")
        if (!dbFile.exists()) return false
        return try {
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery("SELECT 1 FROM bookmark_entity WHERE passage_id = ? LIMIT 1", arrayOf(passageId))
            val exists = cursor.moveToFirst()
            cursor.close()
            db.close()
            exists
        } catch (_: Exception) {
            false
        }
    }

    fun toggleBookmark(context: Context, passageId: String): Boolean {
        return try {
            val dbFile = context.getDatabasePath("app_user_data.db")
            dbFile.parentFile?.mkdirs()
            val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS bookmark_entity (" +
                    "id TEXT NOT NULL PRIMARY KEY, " +
                    "passage_id TEXT NOT NULL UNIQUE, " +
                    "created_at_epoch_ms INTEGER NOT NULL, " +
                    "tags_csv TEXT NOT NULL" +
                    ")"
            )
            val cursor = db.rawQuery("SELECT 1 FROM bookmark_entity WHERE passage_id = ? LIMIT 1", arrayOf(passageId))
            val exists = cursor.moveToFirst()
            cursor.close()

            val newState = if (exists) {
                db.execSQL("DELETE FROM bookmark_entity WHERE passage_id = ?", arrayOf<Any>(passageId))
                false
            } else {
                db.execSQL(
                    "INSERT OR REPLACE INTO bookmark_entity(id, passage_id, created_at_epoch_ms, tags_csv) VALUES (?, ?, ?, ?)",
                    arrayOf<Any>("bm_$passageId", passageId, System.currentTimeMillis(), "")
                )
                true
            }
            db.close()
            newState
        } catch (_: Exception) {
            false
        }
    }

    fun getStreakSummary(context: Context): StreakSummary {
        val dbFile = context.getDatabasePath("app_user_data.db")
        if (!dbFile.exists()) {
            return StreakSummary(0, false, List(7) { false }, 0)
        }
        return try {
            val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val cursor = db.rawQuery("SELECT last_seen_at_epoch_ms, read_count FROM reading_record_entity", null)
            val daysWithReads = mutableMapOf<String, Long>()
            val dayKeyFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            var totalReads = 0
            while (cursor.moveToNext()) {
                val epochMs = cursor.getLong(0)
                val count = cursor.getLong(1)
                totalReads += count.toInt()
                val key = dayKeyFormatter.format(Date(epochMs))
                daysWithReads[key] = (daysWithReads[key] ?: 0L) + count
            }
            cursor.close()
            db.close()

            val nowMs = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            cal.timeInMillis = nowMs
            val todayKey = dayKeyFormatter.format(cal.time)
            val hasReadToday = (daysWithReads[todayKey] ?: 0L) > 0L

            var streak = 0
            val checkCal = Calendar.getInstance()
            checkCal.timeInMillis = nowMs
            if (hasReadToday) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                val yKey = dayKeyFormatter.format(checkCal.time)
                if ((daysWithReads[yKey] ?: 0L) > 0L) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                }
            }
            while (true) {
                val key = dayKeyFormatter.format(checkCal.time)
                if ((daysWithReads[key] ?: 0L) > 0L) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }

            val weekly = mutableListOf<Boolean>()
            val weekCal = Calendar.getInstance()
            weekCal.timeInMillis = nowMs
            weekCal.add(Calendar.DAY_OF_YEAR, -6)
            for (i in 0..6) {
                val key = dayKeyFormatter.format(weekCal.time)
                weekly.add((daysWithReads[key] ?: 0L) > 0L)
                weekCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            StreakSummary(streak, hasReadToday, weekly, totalReads)
        } catch (_: Exception) {
            StreakSummary(0, false, List(7) { false }, 0)
        }
    }

    fun isPatron(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences("fathom_subscription_prefs", Context.MODE_PRIVATE)
            val isPatron = prefs.getBoolean("is_patron", false)
            val expiresAt = prefs.getLong("subscription_expires_at", 0L)
            val isExpired = expiresAt > 0 && System.currentTimeMillis() > expiresAt
            isPatron && !isExpired
        } catch (_: Exception) {
            false
        }
    }

    fun updateAll(context: Context) {
        val providers = listOf(
            DailyWisdomWidgetProvider::class.java,
            SanctuaryStreakWidgetProvider::class.java
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
        const val ACTION_TOGGLE_BOOKMARK = WidgetRepository.ACTION_TOGGLE_BOOKMARK
        const val ACTION_TOGGLE_INFO = WidgetRepository.ACTION_TOGGLE_INFO
        const val EXTRA_PASSAGE_ID = WidgetRepository.EXTRA_PASSAGE_ID

        fun updateAllWidgets(context: Context) {
            WidgetRepository.updateAll(context)
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            for (widgetId in appWidgetIds) {
                val passage = WidgetRepository.loadRandomPassage(context)
                WidgetRepository.setWidgetPassageId(context, widgetId, passage.id)
                WidgetRepository.setWidgetShowInfo(context, widgetId, false)
                render(context, appWidgetManager, widgetId, passage)
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_scroll_list)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scope.launch {
            val passageId = WidgetRepository.getWidgetPassageId(context, appWidgetId)
            val passage = WidgetRepository.loadPassageById(context, passageId)
            render(context, appWidgetManager, appWidgetId, passage)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_scroll_list)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        when (action) {
            ACTION_REFRESH_WIDGET -> {
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    scope.launch {
                        val passage = WidgetRepository.loadRandomPassage(context)
                        WidgetRepository.setWidgetPassageId(context, widgetId, passage.id)
                        WidgetRepository.setWidgetShowInfo(context, widgetId, false)
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        render(context, appWidgetManager, widgetId, passage)
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_scroll_list)
                    }
                } else {
                    updateAll(context)
                }
            }
            ACTION_TOGGLE_BOOKMARK -> {
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val passageId = intent.getStringExtra(EXTRA_PASSAGE_ID) ?: WidgetRepository.getWidgetPassageId(context, widgetId)
                scope.launch {
                    WidgetRepository.toggleBookmark(context, passageId)
                    val passage = WidgetRepository.loadPassageById(context, passageId)
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        render(context, appWidgetManager, widgetId, passage)
                    } else {
                        updateAll(context)
                    }
                }
            }
            ACTION_TOGGLE_INFO -> {
                val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    scope.launch {
                        val currentShowInfo = WidgetRepository.getWidgetShowInfo(context, widgetId)
                        WidgetRepository.setWidgetShowInfo(context, widgetId, !currentShowInfo)
                        val passageId = WidgetRepository.getWidgetPassageId(context, widgetId)
                        val passage = WidgetRepository.loadPassageById(context, passageId)
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        render(context, appWidgetManager, widgetId, passage)
                        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_scroll_list)
                    }
                }
            }
            Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                updateAll(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                val prefs = context.getSharedPreferences("fathom_widget_prefs", Context.MODE_PRIVATE)
                val lastUnlockTime = prefs.getLong("last_unlock_widget_refresh", 0L)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUnlockTime >= 30 * 60 * 1000L) {
                    prefs.edit().putLong("last_unlock_widget_refresh", currentTime).apply()
                    updateAll(context)
                }
            }
        }
    }

    private fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DailyWisdomWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            scope.launch {
                for (widgetId in appWidgetIds) {
                    val passage = WidgetRepository.loadRandomPassage(context)
                    WidgetRepository.setWidgetPassageId(context, widgetId, passage.id)
                    WidgetRepository.setWidgetShowInfo(context, widgetId, false)
                    render(context, appWidgetManager, widgetId, passage)
                    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_scroll_list)
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

        val isBookmarked = WidgetRepository.isBookmarked(context, passage.id)
        val showInfo = WidgetRepository.getWidgetShowInfo(context, widgetId)

        views.setImageViewBitmap(
            R.id.widget_background,
            WidgetArt.background(widthPx, heightPx, colors, tone, 28f * density * scale, passage.id.hashCode().toLong())
        )
        views.setImageViewBitmap(R.id.widget_emblem, WidgetArt.emblem(context, passage.traditionId, tone.hue.toArgb(), (22 * density).toInt()))

        if (showInfo) {
            views.setTextViewText(R.id.widget_tradition_badge, "SANCTUARY EXEGESIS")
            views.setTextColor(R.id.widget_tradition_badge, tone.hue.toArgb())
            views.setInt(R.id.widget_btn_info, "setColorFilter", tone.hue.toArgb())
        } else {
            views.setTextViewText(R.id.widget_tradition_badge, (passage.traditionId?.displayName ?: "Fathom").uppercase())
            views.setTextColor(R.id.widget_tradition_badge, tone.hue.toArgb())
            views.setInt(R.id.widget_btn_info, "setColorFilter", colors.textSecondary.toArgb())
        }

        views.setImageViewResource(
            R.id.widget_btn_bookmark,
            if (isBookmarked) R.drawable.widget_icon_bookmark_fill else R.drawable.widget_icon_bookmark
        )
        views.setInt(
            R.id.widget_btn_bookmark,
            "setColorFilter",
            if (isBookmarked) tone.hue.toArgb() else colors.textSecondary.toArgb()
        )

        views.setInt(R.id.widget_btn_refresh, "setColorFilter", colors.textSecondary.toArgb())
        views.setImageViewBitmap(R.id.widget_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (18 * density).toInt()))
        views.setTextViewText(R.id.widget_verse_reference, passage.reference.uppercase())
        views.setTextColor(R.id.widget_verse_reference, colors.textSecondary.toArgb())

        val serviceIntent = Intent(context, DailyWisdomViewsService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.widget_scroll_list, serviceIntent)

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
        views.setPendingIntentTemplate(R.id.widget_scroll_list, openAppPendingIntent)

        val infoIntent = Intent(context, DailyWisdomWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_INFO
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val infoPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId + 50000,
            infoIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_info, infoPendingIntent)

        val bookmarkIntent = Intent(context, DailyWisdomWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_BOOKMARK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra(EXTRA_PASSAGE_ID, passage.id)
        }
        val bookmarkPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId + 20000,
            bookmarkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_bookmark, bookmarkPendingIntent)

        val refreshIntent = Intent(context, DailyWisdomWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
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
}

class SanctuaryStreakWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        scope.launch {
            val streakSummary = WidgetRepository.getStreakSummary(context)
            for (widgetId in appWidgetIds) {
                render(context, appWidgetManager, widgetId, streakSummary)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        scope.launch {
            val streakSummary = WidgetRepository.getStreakSummary(context)
            render(context, appWidgetManager, appWidgetId, streakSummary)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_USER_PRESENT ||
            action == WidgetRepository.ACTION_REFRESH_WIDGET
        ) {
            updateAll(context)
        }
    }

    private fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, SanctuaryStreakWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            scope.launch {
                val streakSummary = WidgetRepository.getStreakSummary(context)
                for (widgetId in appWidgetIds) {
                    render(context, appWidgetManager, widgetId, streakSummary)
                }
            }
        }
    }

    private fun render(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, streak: StreakSummary) {
        val views = RemoteViews(context.packageName, R.layout.widget_sanctuary_streak)
        val colors = WidgetRepository.readTheme(context).palette()
        val tone = toneFor(TraditionId.STOICISM, colors)
        val density = context.resources.displayMetrics.density
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 220).coerceAtLeast(200)
        val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110).coerceAtLeast(100)
        val scale = minOf(1f, kotlin.math.sqrt(1_200_000f / (widthDp * density * heightDp * density)))
        val widthPx = (widthDp * density * scale).toInt()
        val heightPx = (heightDp * density * scale).toInt()

        val isPatron = WidgetRepository.isPatron(context)
        if (!isPatron) {
            views.setImageViewBitmap(
                R.id.widget_streak_background,
                WidgetArt.background(widthPx, heightPx, colors, tone, 28f * density * scale, 108108108L)
            )
            views.setImageViewBitmap(R.id.widget_streak_emblem, WidgetArt.emblem(context, TraditionId.STOICISM, tone.hue.toArgb(), (20 * density).toInt()))
            views.setTextViewText(R.id.widget_streak_header_title, "SANCTUARY PATRON")
            views.setTextColor(R.id.widget_streak_header_title, tone.hue.toArgb())
            views.setImageViewBitmap(R.id.widget_streak_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (16 * density).toInt()))

            views.setTextViewText(R.id.widget_streak_count, "✦")
            views.setTextColor(R.id.widget_streak_count, tone.hue.toArgb())

            views.setTextViewText(R.id.widget_streak_count_label, "CONSECRATED RITUALS")
            views.setTextColor(R.id.widget_streak_count_label, tone.hue.toArgb())

            views.setTextViewText(R.id.widget_streak_status_text, "Streak counter reserved for Sanctuary Patrons")
            views.setTextColor(R.id.widget_streak_status_text, colors.textSecondary.toArgb())

            val dotIds = listOf(
                R.id.widget_dot_0,
                R.id.widget_dot_1,
                R.id.widget_dot_2,
                R.id.widget_dot_3,
                R.id.widget_dot_4,
                R.id.widget_dot_5,
                R.id.widget_dot_6
            )
            for (i in 0..6) {
                views.setTextColor(dotIds[i], colors.hairline.toArgb())
            }

            views.setTextViewText(R.id.widget_streak_footer, "TAP TO UNLOCK SANCTUARY PATRON →")
            views.setTextColor(R.id.widget_streak_footer, tone.hue.toArgb())

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                widgetId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_streak_root, openAppPendingIntent)
            appWidgetManager.updateAppWidget(widgetId, views)
            return
        }

        views.setImageViewBitmap(
            R.id.widget_streak_background,
            WidgetArt.background(widthPx, heightPx, colors, tone, 28f * density * scale, 108108108L)
        )
        views.setImageViewBitmap(R.id.widget_streak_emblem, WidgetArt.emblem(context, TraditionId.STOICISM, tone.hue.toArgb(), (20 * density).toInt()))
        views.setTextViewText(R.id.widget_streak_header_title, "SANCTUARY RITUALS")
        views.setTextColor(R.id.widget_streak_header_title, tone.hue.toArgb())
        views.setImageViewBitmap(R.id.widget_streak_wordmark, WidgetArt.wordmark(context, colors.textPrimary.toArgb(), (16 * density).toInt()))

        views.setTextViewText(R.id.widget_streak_count, streak.streakDays.toString())
        views.setTextColor(R.id.widget_streak_count, colors.textPrimary.toArgb())

        val streakLabel = if (streak.streakDays == 1) "DAY CONSECRATED" else "DAYS CONSECRATED"
        views.setTextViewText(R.id.widget_streak_count_label, streakLabel)
        views.setTextColor(R.id.widget_streak_count_label, tone.hue.toArgb())

        if (streak.hasReadToday) {
            views.setTextViewText(R.id.widget_streak_status_text, "✦ Daily Contemplation Completed")
            views.setTextColor(R.id.widget_streak_status_text, colors.textPrimary.toArgb())
        } else {
            views.setTextViewText(R.id.widget_streak_status_text, "✧ Daily Contemplation Awaits")
            views.setTextColor(R.id.widget_streak_status_text, colors.textSecondary.toArgb())
        }

        val dotIds = listOf(
            R.id.widget_dot_0,
            R.id.widget_dot_1,
            R.id.widget_dot_2,
            R.id.widget_dot_3,
            R.id.widget_dot_4,
            R.id.widget_dot_5,
            R.id.widget_dot_6
        )
        for (i in 0..6) {
            val isDone = streak.weeklyHistory.getOrElse(i) { false }
            views.setTextColor(
                dotIds[i],
                if (isDone) tone.hue.toArgb() else colors.hairline.toArgb()
            )
        }

        views.setTextColor(R.id.widget_streak_footer, colors.textSecondary.toArgb())

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_streak_root, openAppPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }
}
