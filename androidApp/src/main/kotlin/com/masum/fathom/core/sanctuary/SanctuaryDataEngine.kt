package com.masum.fathom.core.sanctuary

import android.content.Context
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SanctuaryStorageStats(
    val totalCanons: Int = 7,
    val totalBooks: Int = 36,
    val totalVerses: Int = 15163,
    val totalBookmarks: Int = 0,
    val totalReflections: Int = 0,
    val totalHistoryItems: Int = 0,
    val contentDbSizeMb: String = "12.4 MB",
    val userDbSizeKb: String = "48 KB"
)

class SanctuaryDataEngine(
    private val context: Context,
    private val userDataRepository: UserDataRepository,
    private val passageRepository: PassageRepository
) {

    suspend fun getStorageStats(): SanctuaryStorageStats {
        val bookmarks = userDataRepository.observeBookmarks().first()
        val notes = userDataRepository.observeAllNotes().first()
        val history = userDataRepository.observeReadingHistory().first()
        val booksCount = passageRepository.getAllBooks().asSuccessOrNull()?.size ?: 36

        val contentDbFile = context.getDatabasePath("starter_content.db")
        val userDbFile = context.getDatabasePath("app_user_data.db")

        val contentSize = if (contentDbFile.exists()) {
            String.format(java.util.Locale.US, "%.1f MB", contentDbFile.length() / (1024.0 * 1024.0))
        } else {
            "12.4 MB"
        }

        val userSize = if (userDbFile.exists()) {
            String.format(java.util.Locale.US, "%.0f KB", userDbFile.length() / 1024.0)
        } else {
            "32 KB"
        }

        return SanctuaryStorageStats(
            totalCanons = 7,
            totalBooks = booksCount,
            totalVerses = 15163,
            totalBookmarks = bookmarks.size,
            totalReflections = notes.size,
            totalHistoryItems = history.size,
            contentDbSizeMb = contentSize,
            userDbSizeKb = userSize
        )
    }

    suspend fun exportBackupJson(): String {
        val bookmarks: List<Bookmark> = userDataRepository.observeBookmarks().first()
        val notes: List<UserNote> = userDataRepository.observeAllNotes().first()

        val root = JSONObject()
        root.put("app", "Fathom Sacred Wisdom")
        root.put("version", 1)
        root.put("exported_at_epoch_ms", System.currentTimeMillis())

        val bookmarksArray = JSONArray()
        bookmarks.forEach { b ->
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("passage_id", b.passageId)
            obj.put("created_at_epoch_ms", b.createdAtEpochMs)
            val tagsArr = JSONArray()
            b.tags.forEach { tagsArr.put(it) }
            obj.put("tags", tagsArr)
            bookmarksArray.put(obj)
        }
        root.put("bookmarks", bookmarksArray)

        val notesArray = JSONArray()
        notes.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("passage_id", n.passageId)
            obj.put("content", n.content)
            obj.put("created_at_epoch_ms", n.createdAtEpochMs)
            obj.put("updated_at_epoch_ms", n.updatedAtEpochMs)
            notesArray.put(obj)
        }
        root.put("reflections", notesArray)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            var count = 0

            if (root.has("bookmarks")) {
                val bArr = root.getJSONArray("bookmarks")
                for (i in 0 until bArr.length()) {
                    val item = bArr.getJSONObject(i)
                    val id = item.getString("id")
                    val passageId = item.getString("passage_id")
                    val epoch = item.optLong("created_at_epoch_ms", System.currentTimeMillis())
                    val tags = mutableListOf<String>()
                    if (item.has("tags")) {
                        val tArr = item.getJSONArray("tags")
                        for (j in 0 until tArr.length()) {
                            tags.add(tArr.getString(j))
                        }
                    }
                    userDataRepository.saveBookmark(
                        Bookmark(
                            id = id,
                            passageId = passageId,
                            createdAtEpochMs = epoch,
                            tags = tags
                        )
                    )
                    count++
                }
            }

            if (root.has("reflections")) {
                val nArr = root.getJSONArray("reflections")
                for (i in 0 until nArr.length()) {
                    val item = nArr.getJSONObject(i)
                    val id = item.getString("id")
                    val passageId = item.getString("passage_id")
                    val content = item.getString("content")
                    val created = item.optLong("created_at_epoch_ms", System.currentTimeMillis())
                    val updated = item.optLong("updated_at_epoch_ms", created)
                    userDataRepository.upsertNote(
                        UserNote(
                            id = id,
                            passageId = passageId,
                            content = content,
                            createdAtEpochMs = created,
                            updatedAtEpochMs = updated
                        )
                    )
                    count++
                }
            }

            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
