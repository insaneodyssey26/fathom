package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeUserDataRepository : UserDataRepository {

    private val bookmarksFlow = MutableStateFlow<Map<String, Bookmark>>(emptyMap())
    private val notesFlow = MutableStateFlow<Map<String, UserNote>>(emptyMap())
    private val readingFlow = MutableStateFlow<Map<String, ReadingRecord>>(emptyMap())

    override suspend fun saveBookmark(bookmark: Bookmark): FathomResult<Unit, DataError.Local> {
        bookmarksFlow.value += (bookmark.passageId to bookmark)
        return FathomResult.Success(Unit)
    }

    override suspend fun removeBookmark(passageId: String): FathomResult<Unit, DataError.Local> {
        bookmarksFlow.value -= passageId
        return FathomResult.Success(Unit)
    }

    override fun observeBookmarks() = bookmarksFlow.asStateFlow().map { it.values.toList() }

    override suspend fun upsertNote(note: UserNote): FathomResult<Unit, DataError.Local> {
        notesFlow.value += (note.id to note)
        return FathomResult.Success(Unit)
    }

    override suspend fun deleteNote(noteId: String): FathomResult<Unit, DataError.Local> {
        notesFlow.value -= noteId
        return FathomResult.Success(Unit)
    }

    override fun observeNotesForPassage(passageId: String) =
        notesFlow.asStateFlow().map { map -> map.values.filter { it.passageId == passageId } }

    override fun observeAllNotes() =
        notesFlow.asStateFlow().map { map -> map.values.toList() }

    override suspend fun recordReading(passageId: String, bookId: String): FathomResult<Unit, DataError.Local> {
        val existing = readingFlow.value[passageId]
        val now = 1000L
        val updated = existing?.copy(
            lastSeenAtEpochMs = now,
            readCount = existing.readCount + 1
        ) ?: ReadingRecord(
            passageId = passageId,
            bookId = bookId,
            firstSeenAtEpochMs = now,
            lastSeenAtEpochMs = now
        )
        readingFlow.value += (passageId to updated)
        return FathomResult.Success(Unit)
    }

    override suspend fun restoreReadingRecord(record: ReadingRecord): FathomResult<Unit, DataError.Local> {
        readingFlow.value += (record.passageId to record)
        return FathomResult.Success(Unit)
    }

    override fun observeReadingHistory() = readingFlow.asStateFlow().map { it.values.toList() }

}
