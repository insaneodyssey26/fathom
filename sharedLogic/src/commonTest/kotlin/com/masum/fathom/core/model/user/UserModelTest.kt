package com.masum.fathom.core.model.user

import com.masum.fathom.core.model.TraditionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserModelTest {

    @Test
    fun givenDefaultFeedConfig_thenAllTraditionsEnabledAndSeenFilterActive() {
        val config = FeedConfig()

        assertEquals(FeedMode.EXPLORATION_DISCOVERY, config.mode)
        assertEquals(TraditionId.entries.size, config.selectedTraditions.size)
        assertTrue(config.enableSeenFilter)
        assertEquals(0.7f, config.traditionDiversityFactor)
    }

    @Test
    fun givenBookmarkAndNote_whenConstructed_thenMaintainsIntegrity() {
        val now = 1711200000000L
        val bookmark = Bookmark(
            id = "bm_1",
            passageId = "gita_2_20",
            createdAtEpochMs = now,
            tags = listOf("favorite", "soul")
        )
        val note = UserNote(
            id = "note_1",
            passageId = "gita_2_20",
            content = "Reflect on this when overwhelmed.",
            createdAtEpochMs = now,
            updatedAtEpochMs = now
        )

        assertEquals("bm_1", bookmark.id)
        assertEquals(2, bookmark.tags.size)
        assertEquals("note_1", note.id)
        assertEquals("Reflect on this when overwhelmed.", note.content)
    }

    @Test
    fun givenReadingRecord_whenUpdated_thenTracksCountAndCompletion() {
        val record = ReadingRecord(
            passageId = "meditations_4_3",
            bookId = "meditations",
            firstSeenAtEpochMs = 1000L,
            lastSeenAtEpochMs = 2000L,
            readCount = 3,
            isCompleted = true
        )

        assertEquals("meditations_4_3", record.passageId)
        assertEquals(3, record.readCount)
        assertTrue(record.isCompleted)
    }
}
