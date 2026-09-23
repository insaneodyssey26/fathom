package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.UserNote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeUserDataRepositoryTest {

    @Test
    fun givenBookmark_whenSaved_thenEmittedByFlow() = runTest {
        val repo = FakeUserDataRepository()
        val bookmark = Bookmark(id = "bm_1", passageId = "gita_2_20", createdAtEpochMs = 1000L)

        val saveResult = repo.saveBookmark(bookmark)
        assertTrue(saveResult is FathomResult.Success)

        val bookmarks = repo.observeBookmarks().first()
        assertEquals(1, bookmarks.size)
        assertEquals("gita_2_20", bookmarks.first().passageId)

        repo.removeBookmark("gita_2_20")
        val afterRemove = repo.observeBookmarks().first()
        assertTrue(afterRemove.isEmpty())
    }

    @Test
    fun givenNote_whenUpsertedAndObserved_thenFiltersByPassageId() = runTest {
        val repo = FakeUserDataRepository()
        val note1 = UserNote("n_1", "p_1", "Reflect on duty", 1000L, 1000L)
        val note2 = UserNote("n_2", "p_2", "Reflect on peace", 1000L, 1000L)

        repo.upsertNote(note1)
        repo.upsertNote(note2)

        val p1Notes = repo.observeNotesForPassage("p_1").first()
        assertEquals(1, p1Notes.size)
        assertEquals("Reflect on duty", p1Notes.first().content)
    }
}
