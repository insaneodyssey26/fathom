package com.masum.fathom.core.feed

import com.masum.fathom.core.common.result.DataError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.FeedConfig
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.core.repository.FeedRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedEngine(
    private val contentDatabase: FathomContentDatabase,
    private val userDatabase: FathomUserDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : FeedRepository {

    private val contentQueries = contentDatabase.contentDatabaseQueries
    private val userQueries = userDatabase.userDatabaseQueries

    override suspend fun getFeedBatch(config: FeedConfig, batchSize: Int): FathomResult<List<Passage>, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val seenPassageIds: Set<String> = if (config.enableSeenFilter) {
                    userQueries.selectSeenPassageIds().executeAsList().toSet()
                } else {
                    emptySet()
                }

                val passages: List<Passage> = when (config.mode) {
                    FeedMode.EXPLORATION_DISCOVERY -> getExplorationBatch(config, batchSize, seenPassageIds)
                    FeedMode.THEMATIC -> getThematicBatch(config, batchSize, seenPassageIds)
                    FeedMode.SEQUENTIAL_STUDY -> getSequentialBatch(config, batchSize)
                }

                FathomResult.Success(passages)
            } catch (_: Throwable) {
                FathomResult.Failure(DataError.Local.CORRUPTED_DATABASE)
            }
        }

    private fun getExplorationBatch(
        config: FeedConfig,
        batchSize: Int,
        seenPassageIds: Set<String>
    ): List<Passage> {
        val traditionKeys = config.selectedTraditions.map { it.key }
        if (traditionKeys.isEmpty()) return emptyList()

        val candidateLimit = (batchSize * 3L).coerceAtLeast(30L)
        val candidates = contentQueries.selectCandidatePassages(
            tradition_ids = traditionKeys,
            limit = candidateLimit
        ).executeAsList()

        val unseen = candidates.filter { it.id !in seenPassageIds }
        val pool = if (unseen.size >= batchSize) unseen else candidates

        return balanceDiversity(pool, batchSize)
    }

    private fun getThematicBatch(
        config: FeedConfig,
        batchSize: Int,
        seenPassageIds: Set<String>
    ): List<Passage> {
        val themeIds = config.selectedThemeIds.toList()
        if (themeIds.isEmpty()) {
            return getExplorationBatch(config, batchSize, seenPassageIds)
        }

        val candidateLimit = (batchSize * 3L).coerceAtLeast(30L)
        val candidates = contentQueries.selectCandidatePassagesByThemes(
            theme_ids = themeIds,
            limit = candidateLimit
        ).executeAsList()

        val unseen = candidates.filter { it.id !in seenPassageIds }
        val pool = if (unseen.size >= batchSize) unseen else candidates

        return balanceDiversity(pool, batchSize)
    }

    private fun getSequentialBatch(
        config: FeedConfig,
        batchSize: Int
    ): List<Passage> {
        val bookId = config.selectedBookId ?: return emptyList()
        val entities = contentQueries.selectPassagesForBook(
            book_id = bookId,
            limit = batchSize.toLong(),
            offset = 0L
        ).executeAsList()

        return entities.map { entity ->
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
                verseNumber = entity.verse_number?.toInt()
            )
        }
    }

    private fun balanceDiversity(
        entities: List<com.masum.fathom.core.database.content.Passage_entity>,
        batchSize: Int
    ): List<Passage> {
        if (entities.isEmpty()) return emptyList()

        val result = mutableListOf<Passage>()
        val remaining = entities.toMutableList()
        var lastTraditionId: String? = null

        while (result.size < batchSize && remaining.isNotEmpty()) {
            val candidateIndex = remaining.indexOfFirst { it.tradition_id != lastTraditionId }
            val selected = if (candidateIndex != -1) {
                remaining.removeAt(candidateIndex)
            } else {
                remaining.removeAt(0)
            }

            lastTraditionId = selected.tradition_id
            val traditionId = TraditionId.fromKeyOrNull(selected.tradition_id) ?: TraditionId.HINDUISM

            result.add(
                Passage(
                    id = selected.id,
                    bookId = selected.book_id,
                    traditionId = traditionId,
                    canonicalReference = selected.canonical_reference,
                    originalText = selected.original_text,
                    translation = selected.translation,
                    orderIndex = selected.order_index.toInt(),
                    chapter = selected.chapter?.toInt(),
                    verseNumber = selected.verse_number?.toInt()
                )
            )
        }

        return result
    }
}
