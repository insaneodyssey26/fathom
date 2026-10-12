package com.masum.fathom.core.repository

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    suspend fun saveBookmark(bookmark: Bookmark): FathomResult<Unit, DataError.Local>
    suspend fun removeBookmark(passageId: String): FathomResult<Unit, DataError.Local>
    fun observeBookmarks(): Flow<List<Bookmark>>

    suspend fun upsertNote(note: UserNote): FathomResult<Unit, DataError.Local>
    suspend fun deleteNote(noteId: String): FathomResult<Unit, DataError.Local>
    fun observeNotesForPassage(passageId: String): Flow<List<UserNote>>
    fun observeAllNotes(): Flow<List<UserNote>>

    suspend fun recordReading(passageId: String, bookId: String): FathomResult<Unit, DataError.Local>
    suspend fun restoreReadingRecord(record: ReadingRecord): FathomResult<Unit, DataError.Local>
    fun observeReadingHistory(): Flow<List<ReadingRecord>>
}

