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
                    FeedMode.SEQUENTIAL_STUDY -> getSequentialBatch(config, batchSize, seenPassageIds)
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
        val candidateLimit = (batchSize * 5L).coerceAtLeast(80L)
        if (config.selectedBookId != null) {
            val candidates = contentQueries.selectCandidatePassagesForBook(
                book_id = config.selectedBookId,
                limit = candidateLimit
            ).executeAsList()
            val unseen = candidates.filter { it.id !in seenPassageIds }
            val pool = if (unseen.size >= batchSize) {
                unseen
            } else if (unseen.isNotEmpty()) {
                val seenRemaining = candidates.filter { it.id in seenPassageIds }
                unseen + seenRemaining
            } else {
                candidates
            }
            return balanceDiversity(pool, batchSize)
        }

        val aligned = config.alignedTraditions
        if (aligned.isNotEmpty() && (config.selectedTraditions.size > aligned.size || config.selectedTraditions.containsAll(aligned))) {
            val anchorKeys = aligned.map { it.key }
            val discoveryKeys = (config.selectedTraditions - aligned).map { it.key }.ifEmpty {
                (TraditionId.entries.toSet() - aligned).map { it.key }
            }
            val anchorQuota = (candidateLimit / anchorKeys.size.coerceAtLeast(1)).coerceAtLeast(10L)
            val anchorPools = anchorKeys.associateWith { key ->
                val raw = contentQueries.selectCandidatePassages(tradition_ids = listOf(key), limit = anchorQuota).executeAsList()
                val unseen = raw.filter { it.id !in seenPassageIds }
                val seen = raw.filter { it.id in seenPassageIds }
                (unseen + seen).toMutableList()
            }

            val discoveryQuota = (candidateLimit / discoveryKeys.size.coerceAtLeast(1)).coerceAtLeast(10L)
            val discoveryPools = discoveryKeys.associateWith { key ->
                val raw = contentQueries.selectCandidatePassages(tradition_ids = listOf(key), limit = discoveryQuota).executeAsList()
                val unseen = raw.filter { it.id !in seenPassageIds }
                val seen = raw.filter { it.id in seenPassageIds }
                (unseen + seen).toMutableList()
            }

            val result = mutableListOf<Passage>()
            val activeAnchor = anchorKeys.filter { anchorPools[it]?.isNotEmpty() == true }.shuffled().toMutableList()
            val activeDiscovery = discoveryKeys.filter { discoveryPools[it]?.isNotEmpty() == true }.shuffled().toMutableList()

            var anchorIdx = 0
            var discoveryIdx = 0
            var takeAnchor = true

            while (result.size < batchSize && (activeAnchor.isNotEmpty() || activeDiscovery.isNotEmpty())) {
                val entity = if (takeAnchor && activeAnchor.isNotEmpty()) {
                    val key = activeAnchor[anchorIdx % activeAnchor.size]
                    val pool = anchorPools[key]!!
                    val item = pool.removeAt(0)
                    if (pool.isEmpty()) {
                        activeAnchor.remove(key)
                    } else {
                        anchorIdx++
                    }
                    item
                } else if (activeDiscovery.isNotEmpty()) {
                    val key = activeDiscovery[discoveryIdx % activeDiscovery.size]
                    val pool = discoveryPools[key]!!
                    val item = pool.removeAt(0)
                    if (pool.isEmpty()) {
                        activeDiscovery.remove(key)
                    } else {
                        discoveryIdx++
                    }
                    item
                } else if (activeAnchor.isNotEmpty()) {
                    val key = activeAnchor[anchorIdx % activeAnchor.size]
                    val pool = anchorPools[key]!!
                    val item = pool.removeAt(0)
                    if (pool.isEmpty()) {
                        activeAnchor.remove(key)
                    } else {
                        anchorIdx++
                    }
                    item
                } else {
                    break
                }

                val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM
                result.add(
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
                takeAnchor = !takeAnchor
            }
            return result
        }

        val traditionKeys = config.selectedTraditions.map { it.key }
        if (traditionKeys.isEmpty()) return emptyList()
        val perTraditionQuota = (candidateLimit / traditionKeys.size.coerceAtLeast(1)).coerceAtLeast(10L)
        val pools = traditionKeys.associateWith { key ->
            val raw = contentQueries.selectCandidatePassages(
                tradition_ids = listOf(key),
                limit = perTraditionQuota
            ).executeAsList()
            val unseen = raw.filter { it.id !in seenPassageIds }
            val seen = raw.filter { it.id in seenPassageIds }
            (unseen + seen).toMutableList()
        }

        return interleaveTraditions(pools, batchSize)
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

        val candidateLimit = (batchSize * 5L).coerceAtLeast(80L)
        val candidates = contentQueries.selectCandidatePassagesByThemes(
            theme_ids = themeIds,
            limit = candidateLimit
        ).executeAsList()

        val pools = candidates.groupBy { it.tradition_id }.mapValues { (_, list) ->
            val unseen = list.filter { it.id !in seenPassageIds }
            val seen = list.filter { it.id in seenPassageIds }
            (unseen + seen).toMutableList()
        }

        return interleaveTraditions(pools, batchSize)
    }

    private fun getSequentialBatch(
        config: FeedConfig,
        batchSize: Int,
        seenPassageIds: Set<String>
    ): List<Passage> {
        val bookId = config.selectedBookId ?: return emptyList()
        val offset = seenPassageIds.size.toLong()
        val entities = contentQueries.selectPassagesForBook(
            book_id = bookId,
            limit = batchSize.toLong(),
            offset = offset
        ).executeAsList()
        val resolvedEntities = if (entities.isEmpty() && offset > 0) {
            contentQueries.selectPassagesForBook(
                book_id = bookId,
                limit = batchSize.toLong(),
                offset = 0L
            ).executeAsList()
        } else {
            entities
        }

        return resolvedEntities.map { entity ->
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

    private fun interleaveTraditions(
        traditionPools: Map<String, MutableList<com.masum.fathom.core.database.content.Passage_entity>>,
        batchSize: Int
    ): List<Passage> {
        val result = mutableListOf<Passage>()
        val activeTraditions = traditionPools.keys.filter { traditionPools[it]?.isNotEmpty() == true }.shuffled().toMutableList()
        var index = 0

        while (result.size < batchSize && activeTraditions.isNotEmpty()) {
            val key = activeTraditions[index % activeTraditions.size]
            val pool = traditionPools[key]
            if (pool != null && pool.isNotEmpty()) {
                val entity = pool.removeAt(0)
                val traditionId = TraditionId.fromKeyOrNull(entity.tradition_id) ?: TraditionId.HINDUISM
                result.add(
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
                if (pool.isEmpty()) {
                    activeTraditions.remove(key)
                } else {
                    index++
                }
            } else {
                activeTraditions.remove(key)
            }
        }
        return result
    }

    private fun balanceDiversity(
        entities: List<com.masum.fathom.core.database.content.Passage_entity>,
        batchSize: Int
    ): List<Passage> {
        if (entities.isEmpty()) return emptyList()
        val pools = entities.groupBy { it.tradition_id }.mapValues { (_, list) -> list.toMutableList() }
        return interleaveTraditions(pools, batchSize)
    }
}
