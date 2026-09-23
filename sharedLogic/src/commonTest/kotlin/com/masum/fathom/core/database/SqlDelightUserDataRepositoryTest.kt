package com.masum.fathom.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.UserNote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlDelightUserDataRepositoryTest {

    private lateinit var database: FathomUserDatabase
    private lateinit var repository: SqlDelightUserDataRepository

    @BeforeTest
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FathomUserDatabase.Schema.create(driver)
        database = FathomUserDatabase(driver)
        repository = SqlDelightUserDataRepository(database)
    }

    @Test
    fun givenBookmark_whenSaved_thenCanBeObservedAndDeleted() = runTest {
        val bookmark = Bookmark(
            id = "bm_1",
            passageId = "gita_2_20",
            createdAtEpochMs = 1000L,
            tags = listOf("soul", "immortal")
        )

        val saveResult = repository.saveBookmark(bookmark)
        assertTrue(saveResult is FathomResult.Success)

        val bookmarks = repository.observeBookmarks().first()
        assertEquals(1, bookmarks.size)
        assertEquals("gita_2_20", bookmarks.first().passageId)
        assertEquals(listOf("soul", "immortal"), bookmarks.first().tags)

        repository.removeBookmark("gita_2_20")
        val afterDelete = repository.observeBookmarks().first()
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun givenNote_whenUpserted_thenCanBeFilteredByPassageId() = runTest {
        val note = UserNote(
            id = "n_1",
            passageId = "dhammapada_1",
            content = "Mind is the forerunner of all states.",
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )

        repository.upsertNote(note)

        val notes = repository.observeNotesForPassage("dhammapada_1").first()
        assertEquals(1, notes.size)
        assertEquals("Mind is the forerunner of all states.", notes.first().content)
    }

    @Test
    fun givenReadingRecord_whenRecordedMultipleTimes_thenIncrementsReadCount() = runTest {
        repository.recordReading("stoic_1", "meditations")
        repository.recordReading("stoic_1", "meditations")

        val history = repository.observeReadingHistory().first()
        assertEquals(1, history.size)
        assertEquals(2, history.first().readCount)
    }
}
