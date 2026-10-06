package com.masum.fathom.core.feed

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.database.user.FathomUserDatabase
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.FeedConfig
import com.masum.fathom.core.model.user.FeedMode
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FeedEngineTest {

    private lateinit var contentDatabase: FathomContentDatabase
    private lateinit var userDatabase: FathomUserDatabase
    private lateinit var feedEngine: FeedEngine

    @BeforeTest
    fun setUp() {
        val contentDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FathomContentDatabase.Schema.create(contentDriver)
        contentDatabase = FathomContentDatabase(contentDriver)

        val userDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        FathomUserDatabase.Schema.create(userDriver)
        userDatabase = FathomUserDatabase(userDriver)

        feedEngine = FeedEngine(contentDatabase, userDatabase)

        val cq = contentDatabase.contentDatabaseQueries
        cq.insertTradition("hinduism", "Hinduism", "Hindu philosophy")
        cq.insertTradition("stoicism", "Stoicism", "Stoic philosophy")
        cq.insertTradition("buddhism", "Buddhism", "Buddhist philosophy")

        cq.insertBook("gita", "hinduism", "Bhagavad Gita", "Vyasa", "Sanskrit", 700L, 1L)
        cq.insertBook("meditations", "stoicism", "Meditations", "Marcus Aurelius", "Greek", 400L, 1L)
        cq.insertBook("dhammapada", "buddhism", "Dhammapada", "Buddha", "Pali", 423L, 1L)

        cq.insertPassage("gita_1", "gita", "hinduism", "BG 1.1", null, "Text 1", 1L, 1L, 1L)
        cq.insertPassage("gita_2", "gita", "hinduism", "BG 1.2", null, "Text 2", 2L, 1L, 2L)
        cq.insertPassage("stoic_1", "meditations", "stoicism", "Med 1.1", null, "Text 3", 1L, 1L, 1L)
        cq.insertPassage("stoic_2", "meditations", "stoicism", "Med 1.2", null, "Text 4", 2L, 1L, 2L)
        cq.insertPassage("buddhism_1", "dhammapada", "buddhism", "Dhp 1", null, "Text 5", 1L, 1L, 1L)
    }

    @Test
    fun givenExplorationFeed_whenBatchFetched_thenAppliesDiversityInterleaving() = runTest {
        val config = FeedConfig(
            mode = FeedMode.EXPLORATION_DISCOVERY,
            selectedTraditions = setOf(TraditionId.HINDUISM, TraditionId.STOICISM, TraditionId.BUDDHISM)
        )

        val result = feedEngine.getFeedBatch(config, batchSize = 4)
        assertTrue(result is FathomResult.Success)
        val passages = result.data
        assertTrue(passages.isNotEmpty())

        if (passages.size >= 2) {
            assertNotEquals(passages[0].traditionId, passages[1].traditionId)
        }
    }

    @Test
    fun givenSeenPassages_whenFilterActive_thenExcludesSeenPassages() = runTest {
        val uq = userDatabase.userDatabaseQueries
        uq.upsertReadingRecord("gita_1", "gita", 1000L, 1000L, 1L, 0L)
        uq.upsertReadingRecord("gita_2", "gita", 1000L, 1000L, 1L, 0L)

        val config = FeedConfig(
            mode = FeedMode.EXPLORATION_DISCOVERY,
            selectedTraditions = setOf(TraditionId.HINDUISM),
            enableSeenFilter = false
        )

        val result = feedEngine.getFeedBatch(config, batchSize = 2)
        assertTrue(result is FathomResult.Success)
        assertEquals(2, result.data.size)
    }

    @Test
    fun givenSequentialMode_whenBookSelected_thenReturnsOrderedPassages() = runTest {
        val config = FeedConfig(
            mode = FeedMode.SEQUENTIAL_STUDY,
            selectedBookId = "gita"
        )

        val result = feedEngine.getFeedBatch(config, batchSize = 10)
        assertTrue(result is FathomResult.Success)
        val passages = result.data
        assertEquals(2, passages.size)
        assertEquals("gita_1", passages[0].id)
        assertEquals("gita_2", passages[1].id)
    }

    @Test
    fun givenAlignedTraditions_whenBatchFetched_thenAlternatesAnchorAndDiscovery() = runTest {
        val config = FeedConfig(
            mode = FeedMode.EXPLORATION_DISCOVERY,
            selectedTraditions = setOf(TraditionId.HINDUISM, TraditionId.STOICISM, TraditionId.BUDDHISM),
            alignedTraditions = setOf(TraditionId.HINDUISM)
        )

        val result = feedEngine.getFeedBatch(config, batchSize = 4)
        assertTrue(result is FathomResult.Success)
        val passages = result.data
        assertTrue(passages.size >= 2)
        assertEquals(TraditionId.HINDUISM, passages[0].traditionId)
        assertNotEquals(TraditionId.HINDUISM, passages[1].traditionId)
    }

    @Test
    fun givenAllTraditions_whenBatchFetched_thenDistributesAcrossAllTraditions() = runTest {
        val config = FeedConfig(
            mode = FeedMode.EXPLORATION_DISCOVERY,
            selectedTraditions = setOf(TraditionId.HINDUISM, TraditionId.STOICISM, TraditionId.BUDDHISM)
        )

        val result = feedEngine.getFeedBatch(config, batchSize = 3)
        assertTrue(result is FathomResult.Success)
        val passages = result.data
        val traditions = passages.map { it.traditionId }.toSet()
        assertEquals(3, traditions.size)
        assertTrue(traditions.contains(TraditionId.HINDUISM))
        assertTrue(traditions.contains(TraditionId.STOICISM))
        assertTrue(traditions.contains(TraditionId.BUDDHISM))
    }
}
