package com.masum.fathom.core.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.model.Explanation
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.Theme
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.repository.PassageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightPassageRepository(
    private val database: FathomContentDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : PassageRepository {

    private val queries = database.contentDatabaseQueries

    override suspend fun getPassage(id: String): FathomResult<Passage, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val entity = queries.selectPassageById(id).executeAsOneOrNull()
                    ?: return@withContext FathomResult.Failure(DataError.Local.NOT_FOUND)

                val themes = queries.selectThemesForPassage(id).executeAsList().map {
                    Theme(id = it.id, name = it.name, description = it.description)
                }

                val explanations = queries.selectExplanationsForPassage(id).executeAsList().map {
                    Explanation(
                        id = it.id,
                        passageId = it.passage_id,
                        title = it.title,
                        body = it.body,
                        author = it.author,
                        source = it.source
                    )
                }

                val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM

                val passage = Passage(
                    id = entity.id,
                    bookId = entity.book_id,
                    traditionId = traditionId,
                    canonicalReference = entity.canonical_reference,
                    originalText = entity.original_text,
                    translation = entity.translation,
                    orderIndex = entity.order_index.toInt(),
                    chapter = entity.chapter?.toInt(),
                    verseNumber = entity.verse_number?.toInt(),
                    themes = themes,
                    explanations = explanations
                )

                FathomResult.Success(passage)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override suspend fun getPassagesForBook(
        bookId: String,
        offset: Int,
        limit: Int
    ): FathomResult<List<Passage>, DataError.Local> = withContext(ioDispatcher) {
        try {
            val entities = queries.selectPassagesForBook(
                book_id = bookId,
                limit = limit.toLong(),
                offset = offset.toLong()
            ).executeAsList()

            val passages = entities.map { entity ->
                val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM
                Passage(
                    id = entity.id,
                    bookId = entity.book_id,
                    traditionId = traditionId,
                    canonicalReference = entity.canonical_reference,
                    originalText = entity.original_text,
                    translation = entity.translation,
                    orderIndex = entity.order_index.toInt(),
                    chapter = entity.chapter?.toInt(),
                    verseNumber = entity.verse_number?.toInt(),
                    themes = emptyList(),
                    explanations = emptyList()
                )
            }

            FathomResult.Success(passages)
        } catch (_: Throwable) {
            FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
        }
    }

    override suspend fun getAllBooks(): FathomResult<List<com.masum.fathom.core.model.Book>, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val entities = queries.selectAllBooks().executeAsList()
                val books = entities.map { entity ->
                    val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM
                    com.masum.fathom.core.model.Book(
                        id = entity.id,
                        traditionId = traditionId,
                        title = entity.title,
                        author = entity.author,
                        language = entity.language,
                        totalPassages = entity.total_passages.toInt(),
                        isBundled = entity.is_bundled == 1L
                    )
                }
                FathomResult.Success(books)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    override fun observePassage(id: String): Flow<FathomResult<Passage, DataError.Local>> {
        return queries.selectPassageById(id)
            .asFlow()
            .mapToOneOrNull(ioDispatcher)
            .map { entity ->
                if (entity == null) {
                    FathomResult.Failure(DataError.Local.NOT_FOUND)
                } else {
                    val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM
                    FathomResult.Success(
                        Passage(
                            id = entity.id,
                            bookId = entity.book_id,
                            traditionId = traditionId,
                            canonicalReference = entity.canonical_reference,
                            originalText = entity.original_text,
                            translation = entity.translation,
                            orderIndex = entity.order_index.toInt(),
                            chapter = entity.chapter?.toInt(),
                            verseNumber = entity.verse_number?.toInt()
                        )
                    )
                }
            }
    }
}
