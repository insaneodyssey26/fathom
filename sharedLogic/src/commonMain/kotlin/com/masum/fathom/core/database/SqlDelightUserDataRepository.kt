package com.masum.fathom.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightUserDataRepository(
    private val database: FathomUserDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : UserDataRepository {

    private val queries = database.userDatabaseQueries

    override suspend fun saveBookmark(bookmark: Bookmark): FathomResult<Unit, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                queries.insertBookmark(
                    id = bookmark.id,
                    passage_id = bookmark.passageId,
                    created_at_epoch_ms = bookmark.createdAtEpochMs,
                    tags_csv = bookmark.tags.joinToString(",")
                )
                FathomResult.Success(Unit)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override suspend fun removeBookmark(passageId: String): FathomResult<Unit, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                queries.deleteBookmarkByPassageId(passageId)
                FathomResult.Success(Unit)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override fun observeBookmarks(): Flow<List<Bookmark>> {
        return queries.selectAllBookmarks()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { entity ->
                    Bookmark(
                        id = entity.id,
                        passageId = entity.passage_id,
                        createdAtEpochMs = entity.created_at_epoch_ms,
                        tags = if (entity.tags_csv.isBlank()) emptyList() else entity.tags_csv.split(",")
                    )
                }
            }
    }

    override suspend fun upsertNote(note: UserNote): FathomResult<Unit, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                queries.upsertNote(
                    id = note.id,
                    passage_id = note.passageId,
                    content = note.content,
                    created_at_epoch_ms = note.createdAtEpochMs,
                    updated_at_epoch_ms = note.updatedAtEpochMs
                )
                FathomResult.Success(Unit)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override suspend fun deleteNote(noteId: String): FathomResult<Unit, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                queries.deleteNoteById(noteId)
                FathomResult.Success(Unit)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override fun observeNotesForPassage(passageId: String): Flow<List<UserNote>> {
        return queries.selectNotesForPassage(passageId)
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { entity ->
                    UserNote(
                        id = entity.id,
                        passageId = entity.passage_id,
                        content = entity.content,
                        createdAtEpochMs = entity.created_at_epoch_ms,
                        updatedAtEpochMs = entity.updated_at_epoch_ms
                    )
                }
            }
    }

    override fun observeAllNotes(): Flow<List<UserNote>> {
        return queries.selectAllNotes()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { entity ->
                    UserNote(
                        id = entity.id,
                        passageId = entity.passage_id,
                        content = entity.content,
                        createdAtEpochMs = entity.created_at_epoch_ms,
                        updatedAtEpochMs = entity.updated_at_epoch_ms
                    )
                }
            }
    }

    override suspend fun recordReading(passageId: String, bookId: String): FathomResult<Unit, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val now = 1000L
                queries.upsertReadingRecord(
                    passage_id = passageId,
                    book_id = bookId,
                    first_seen_at_epoch_ms = now,
                    last_seen_at_epoch_ms = now,
                    read_count = 1L,
                    is_completed = 0L
                )
                FathomResult.Success(Unit)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override fun observeReadingHistory(): Flow<List<ReadingRecord>> {
        return queries.selectAllReadingRecords()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list ->
                list.map { entity ->
                    ReadingRecord(
                        passageId = entity.passage_id,
                        bookId = entity.book_id,
                        firstSeenAtEpochMs = entity.first_seen_at_epoch_ms,
                        lastSeenAtEpochMs = entity.last_seen_at_epoch_ms,
                        readCount = entity.read_count.toInt(),
                        isCompleted = entity.is_completed == 1L
                    )
                }
            }
    }
}
