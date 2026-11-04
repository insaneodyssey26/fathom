package com.masum.fathom.core.sanctuary

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class SanctuaryVaultRestoreTest {

    private class TestUserDataRepository : UserDataRepository {
        private val bookmarks = MutableStateFlow<Map<String, Bookmark>>(emptyMap())
        private val notes = MutableStateFlow<Map<String, UserNote>>(emptyMap())
        private val history = MutableStateFlow<Map<String, ReadingRecord>>(emptyMap())

        override suspend fun saveBookmark(bookmark: Bookmark): FathomResult<Unit, DataError.Local> {
            bookmarks.value += (bookmark.id to bookmark)
            return FathomResult.Success(Unit)
        }

        override suspend fun removeBookmark(passageId: String): FathomResult<Unit, DataError.Local> {
            bookmarks.value = bookmarks.value.filterValues { it.passageId != passageId }
            return FathomResult.Success(Unit)
        }

        override fun observeBookmarks(): Flow<List<Bookmark>> = bookmarks.asStateFlow().map { it.values.toList() }

        override suspend fun upsertNote(note: UserNote): FathomResult<Unit, DataError.Local> {
            notes.value += (note.id to note)
            return FathomResult.Success(Unit)
        }

        override suspend fun deleteNote(noteId: String): FathomResult<Unit, DataError.Local> {
            notes.value -= noteId
            return FathomResult.Success(Unit)
        }

        override fun observeNotesForPassage(passageId: String): Flow<List<UserNote>> =
            notes.asStateFlow().map { map -> map.values.filter { it.passageId == passageId } }

        override fun observeAllNotes(): Flow<List<UserNote>> =
            notes.asStateFlow().map { it.values.toList() }

        override suspend fun recordReading(passageId: String, bookId: String): FathomResult<Unit, DataError.Local> {
            val record = ReadingRecord(passageId, bookId, 1000L, 1000L, 1, false)
            history.value += (passageId to record)
            return FathomResult.Success(Unit)
        }

        override suspend fun restoreReadingRecord(record: ReadingRecord): FathomResult<Unit, DataError.Local> {
            history.value += (record.passageId to record)
            return FathomResult.Success(Unit)
        }

        override fun observeReadingHistory(): Flow<List<ReadingRecord>> =
            history.asStateFlow().map { it.values.toList() }
    }

    private class TestPassageRepository : PassageRepository {
        override suspend fun getPassage(id: String): FathomResult<Passage, DataError.Local> =
            FathomResult.Failure(DataError.Local.NOT_FOUND)

        override suspend fun getPassagesForBook(bookId: String, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local> =
            FathomResult.Success(emptyList())

        override suspend fun getAllBooks(): FathomResult<List<Book>, DataError.Local> =
            FathomResult.Success(emptyList())

        override suspend fun getBooksForTradition(traditionId: TraditionId): FathomResult<List<Book>, DataError.Local> =
            FathomResult.Success(emptyList())

        override suspend fun searchPassages(query: String, traditionId: TraditionId?, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local> =
            FathomResult.Success(emptyList())

        override suspend fun getChaptersForBook(bookId: String): FathomResult<List<Int>, DataError.Local> =
            FathomResult.Success(emptyList())

        override suspend fun getPassagesForBookChapter(bookId: String, chapter: Int): FathomResult<List<Passage>, DataError.Local> =
            FathomResult.Success(emptyList())

        override fun observePassage(id: String): Flow<FathomResult<Passage, DataError.Local>> = emptyFlow()
    }

    @Test
    fun testGzipCompressedVaultRestore() = runTest {
        val userRepo = TestUserDataRepository()
        val passageRepo = TestPassageRepository()
        val engine = SanctuaryDataEngine(
            context = null,
            userDataRepository = userRepo,
            passageRepository = passageRepo
        )

        val root = JSONObject().apply {
            put("app", "Fathom Sacred Wisdom")
            put("version", 2)
            put("format", "fathom-vault-v1")
            put("bookmarks", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "bm_1")
                    put("passage_id", "gita_2_20")
                    put("created_at_epoch_ms", 1700000000000L)
                    put("tags", JSONArray().put("eternity").put("soul"))
                })
            })
            put("reflections", JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "note_1")
                    put("passage_id", "gita_2_20")
                    put("content", "Deep stillness.")
                    put("created_at_epoch_ms", 1700000000000L)
                    put("updated_at_epoch_ms", 1700000000000L)
                })
            })
            put("reading_records", JSONArray().apply {
                put(JSONObject().apply {
                    put("passage_id", "gita_2_20")
                    put("book_id", "bhagavad_gita")
                    put("first_seen_at_epoch_ms", 1700000000000L)
                    put("last_seen_at_epoch_ms", 1700000000000L)
                    put("read_count", 3)
                    put("is_completed", true)
                })
            })
        }

        val byteStream = ByteArrayOutputStream()
        GZIPOutputStream(byteStream).use { gzip ->
            gzip.write(root.toString().toByteArray(Charsets.UTF_8))
        }

        val result = engine.restoreVaultArchive(ByteArrayInputStream(byteStream.toByteArray()))
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(1, summary.bookmarksRestored)
        assertEquals(1, summary.reflectionsRestored)
        assertEquals(1, summary.historyRestored)
        assertEquals(3, summary.totalRestored)
    }

    @Test
    fun testPlainJsonVaultRestore() = runTest {
        val userRepo = TestUserDataRepository()
        val passageRepo = TestPassageRepository()
        val engine = SanctuaryDataEngine(
            context = null,
            userDataRepository = userRepo,
            passageRepository = passageRepo
        )

        val rawJson = """
            {
              "bookmarks": [
                { "id": "bm_2", "passage_id": "dhammapada_1", "created_at_epoch_ms": 1700000000000, "tags": ["mind"] }
              ],
              "reflections": [
                { "id": "note_2", "passage_id": "dhammapada_1", "content": "Mind precedes all.", "created_at_epoch_ms": 1700000000000 }
              ]
            }
        """.trimIndent()

        val result = engine.restoreVaultArchive(ByteArrayInputStream(rawJson.toByteArray(Charsets.UTF_8)))
        assertTrue(result.isSuccess)
        val summary = result.getOrThrow()
        assertEquals(1, summary.bookmarksRestored)
        assertEquals(1, summary.reflectionsRestored)
        assertEquals(0, summary.historyRestored)
        assertEquals(2, summary.totalRestored)
    }

    @Test
    fun testCorruptVaultArchiveReturnsFailure() = runTest {
        val userRepo = TestUserDataRepository()
        val passageRepo = TestPassageRepository()
        val engine = SanctuaryDataEngine(
            context = null,
            userDataRepository = userRepo,
            passageRepository = passageRepo
        )

        val corruptBytes = byteArrayOf(0x1F, 0x8B.toByte(), 0x00, 0x12, 0x34)
        val result = engine.restoreVaultArchive(ByteArrayInputStream(corruptBytes))
        assertTrue(result.isFailure)
    }

    @Test
    fun testEmptyVaultArchiveReturnsFailure() = runTest {
        val userRepo = TestUserDataRepository()
        val passageRepo = TestPassageRepository()
        val engine = SanctuaryDataEngine(
            context = null,
            userDataRepository = userRepo,
            passageRepository = passageRepo
        )

        val result = engine.restoreVaultArchive(ByteArrayInputStream(ByteArray(0)))
        assertTrue(result.isFailure)
    }

    @Test
    fun testMarkdownArchiveExport() = runTest {
        val userRepo = TestUserDataRepository()
        userRepo.saveBookmark(Bookmark("bm_md", "gita_2_20", 1700000000000L, listOf("soul", "courage")))
        userRepo.upsertNote(UserNote("note_md", "gita_2_20", "A beacon of inner light.", 1700000000000L, 1700000000000L))

        val passageRepo = object : PassageRepository {
            override suspend fun getPassage(id: String): FathomResult<Passage, DataError.Local> {
                return if (id == "gita_2_20") {
                    FathomResult.Success(
                        Passage(
                            id = "gita_2_20",
                            bookId = "bhagavad_gita",
                            traditionId = TraditionId.HINDUISM,
                            canonicalReference = "BG 2.20",
                            originalText = "न जायते म्रियते वा",
                            translation = "The soul is neither born nor dies.",
                            orderIndex = 20
                        )
                    )
                } else {
                    FathomResult.Failure(DataError.Local.NOT_FOUND)
                }
            }
            override suspend fun getPassagesForBook(bookId: String, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local> = FathomResult.Success(emptyList())
            override suspend fun getAllBooks(): FathomResult<List<Book>, DataError.Local> = FathomResult.Success(emptyList())
            override suspend fun getBooksForTradition(traditionId: TraditionId): FathomResult<List<Book>, DataError.Local> = FathomResult.Success(emptyList())
            override suspend fun searchPassages(query: String, traditionId: TraditionId?, offset: Int, limit: Int): FathomResult<List<Passage>, DataError.Local> = FathomResult.Success(emptyList())
            override suspend fun getChaptersForBook(bookId: String): FathomResult<List<Int>, DataError.Local> = FathomResult.Success(emptyList())
            override suspend fun getPassagesForBookChapter(bookId: String, chapter: Int): FathomResult<List<Passage>, DataError.Local> = FathomResult.Success(emptyList())
            override fun observePassage(id: String): Flow<FathomResult<Passage, DataError.Local>> = emptyFlow()
        }

        val engine = SanctuaryDataEngine(
            context = null,
            userDataRepository = userRepo,
            passageRepository = passageRepo
        )

        val markdown = engine.exportMarkdownArchive()
        assertTrue(markdown.contains("# Fathom Sanctuary Journal"))
        assertTrue(markdown.contains("## Reflections & Notes"))
        assertTrue(markdown.contains("BG 2.20"))
        assertTrue(markdown.contains("A beacon of inner light."))
        assertTrue(markdown.contains("The soul is neither born nor dies."))
        assertTrue(markdown.contains("#soul #courage"))
    }
}
