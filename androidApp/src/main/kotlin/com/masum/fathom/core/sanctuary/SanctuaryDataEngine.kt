package com.masum.fathom.core.sanctuary

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

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
            String.format(Locale.US, "%.1f MB", contentDbFile.length() / (1024.0 * 1024.0))
        } else {
            "12.4 MB"
        }

        val userSize = if (userDbFile.exists()) {
            String.format(Locale.US, "%.0f KB", userDbFile.length() / 1024.0)
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

    suspend fun exportVaultArchive(): Uri {
        val bookmarks: List<Bookmark> = userDataRepository.observeBookmarks().first()
        val notes: List<UserNote> = userDataRepository.observeAllNotes().first()
        val readingRecords: List<ReadingRecord> = userDataRepository.observeReadingHistory().first()

        val root = JSONObject()
        root.put("app", "Fathom Sacred Wisdom")
        root.put("version", 2)
        root.put("format", "fathom-vault-v1")
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

        val readingArray = JSONArray()
        readingRecords.forEach { r ->
            val obj = JSONObject()
            obj.put("passage_id", r.passageId)
            obj.put("book_id", r.bookId)
            obj.put("first_seen_at_epoch_ms", r.firstSeenAtEpochMs)
            obj.put("last_seen_at_epoch_ms", r.lastSeenAtEpochMs)
            obj.put("read_count", r.readCount)
            obj.put("is_completed", r.isCompleted)
            readingArray.put(obj)
        }
        root.put("reading_records", readingArray)

        val jsonBytes = root.toString().toByteArray(Charsets.UTF_8)
        val byteStream = ByteArrayOutputStream()
        GZIPOutputStream(byteStream).use { gzip ->
            gzip.write(jsonBytes)
        }
        val compressedBytes = byteStream.toByteArray()

        val backupsDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val dateStamp = java.time.LocalDate.now().toString()
        val backupFile = File(backupsDir, "fathom-vault-$dateStamp.fathom")
        backupFile.writeBytes(compressedBytes)

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)

    }

    suspend fun exportVaultBytes(): ByteArray {
        val bookmarks: List<Bookmark> = userDataRepository.observeBookmarks().first()
        val notes: List<UserNote> = userDataRepository.observeAllNotes().first()
        val readingRecords: List<ReadingRecord> = userDataRepository.observeReadingHistory().first()

        val root = JSONObject()
        root.put("app", "Fathom Sacred Wisdom")
        root.put("version", 2)
        root.put("format", "fathom-vault-v1")
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

        val readingArray = JSONArray()
        readingRecords.forEach { r ->
            val obj = JSONObject()
            obj.put("passage_id", r.passageId)
            obj.put("book_id", r.bookId)
            obj.put("first_seen_at_epoch_ms", r.firstSeenAtEpochMs)
            obj.put("last_seen_at_epoch_ms", r.lastSeenAtEpochMs)
            obj.put("read_count", r.readCount)
            obj.put("is_completed", r.isCompleted)
            readingArray.put(obj)
        }
        root.put("reading_records", readingArray)

        val jsonBytes = root.toString().toByteArray(Charsets.UTF_8)
        val byteStream = ByteArrayOutputStream()
        GZIPOutputStream(byteStream).use { gzip ->
            gzip.write(jsonBytes)
        }
        return byteStream.toByteArray()
    }

    suspend fun restoreVaultArchive(inputStream: InputStream): Result<Int> {

        return try {
            val decompressedBytes = GZIPInputStream(inputStream).use { gzip ->
                gzip.readBytes()
            }
            val jsonString = String(decompressedBytes, Charsets.UTF_8)
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

            if (root.has("reading_records")) {
                val rArr = root.getJSONArray("reading_records")
                for (i in 0 until rArr.length()) {
                    val item = rArr.getJSONObject(i)
                    val passageId = item.getString("passage_id")
                    val bookId = item.optString("book_id", "")
                    val firstSeen = item.optLong("first_seen_at_epoch_ms", System.currentTimeMillis())
                    val lastSeen = item.optLong("last_seen_at_epoch_ms", firstSeen)
                    val readCount = item.optInt("read_count", 1)
                    val isCompleted = item.optBoolean("is_completed", false)
                    userDataRepository.restoreReadingRecord(
                        ReadingRecord(
                            passageId = passageId,
                            bookId = bookId,
                            firstSeenAtEpochMs = firstSeen,
                            lastSeenAtEpochMs = lastSeen,
                            readCount = readCount,
                            isCompleted = isCompleted
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

