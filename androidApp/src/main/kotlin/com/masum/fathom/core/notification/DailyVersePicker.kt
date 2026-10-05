package com.masum.fathom.core.notification

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.VerseSource
import com.masum.fathom.ui.shell.traditionIdForLabel
import java.time.LocalDate

data class DailyVerse(
    val id: String,
    val traditionId: TraditionId,
    val reference: String,
    val translation: String
)

object DailyVersePicker {

    private val fallback = DailyVerse(
        id = "meditations_5_16",
        traditionId = TraditionId.STOICISM,
        reference = "Meditations 5.16",
        translation = "The soul becomes dyed with the color of its thoughts."
    )

    fun pickFor(context: Context, date: LocalDate, source: VerseSource, userTradition: String?): DailyVerse {
        val dbFile = context.getDatabasePath("starter_content.db")
        if (!dbFile.exists() || dbFile.length() < 100_000L) return fallback

        return runCatching {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                val focus = if (source == VerseSource.MY_TRADITION) traditionIdForLabel(userTradition) else null
                val where = buildString {
                    append("LENGTH(translation) BETWEEN 48 AND 190 AND translation NOT LIKE '%[%' ")
                    if (focus != null) append("AND tradition_id = '${focus.key}' ")
                }
                val count = db.rawQuery("SELECT COUNT(*) FROM passage_entity WHERE $where", null).use {
                    if (it.moveToFirst()) it.getLong(0) else 0L
                }
                if (count == 0L) return@use fallback
                val offset = Math.floorMod(date.toEpochDay() * 2654435761L + 97L, count)
                db.rawQuery(
                    "SELECT id, tradition_id, canonical_reference, translation FROM passage_entity WHERE $where ORDER BY id LIMIT 1 OFFSET $offset",
                    null
                ).use { cursor ->
                    if (!cursor.moveToFirst()) return@use fallback
                    DailyVerse(
                        id = cursor.getString(0),
                        traditionId = TraditionId.fromKeyOrNull(cursor.getString(1)) ?: TraditionId.STOICISM,
                        reference = cursor.getString(2),
                        translation = cursor.getString(3).trim()
                    )
                }
            }
        }.getOrDefault(fallback)
    }
}
