package com.masum.fathom.core.database

import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.database.content.FathomContentDatabase
import com.masum.fathom.core.model.TraditionId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlDelightPassageRepositoryTest {

    private lateinit var database: FathomContentDatabase
    private lateinit var repository: SqlDelightPassageRepository

    @BeforeTest
    fun setUp() {
        val driver = createTestContentDriver()
        database = FathomContentDatabase(driver)
        repository = SqlDelightPassageRepository(database)

        val queries = database.contentDatabaseQueries
        queries.insertTradition("hinduism", "Hinduism", "Ancient Indian philosophical tradition")
        queries.insertBook("bhagavad_gita", "hinduism", "Bhagavad Gita", "Vyasa", "Sanskrit", 700L, 1L)

        queries.insertPassage(
            id = "gita_2_20",
            book_id = "bhagavad_gita",
            tradition_id = "hinduism",
            canonical_reference = "BG 2.20",
            original_text = "न जायते म्रियते वा कदाचिन्...",
            translation = "The soul is never born nor dies at any time.",
            order_index = 20L,
            chapter = 2L,
            verse_number = 20L
        )

        queries.insertTheme("soul", "Eternal Soul", "Themes relating to consciousness and eternity")
        queries.linkPassageTheme("gita_2_20", "soul")

        queries.insertExplanation(
            id = "exp_1",
            passage_id = "gita_2_20",
            title = "The Eternal Nature",
            body = "This verse establishes the permanence of the self.",
            author = "Traditional Commentary",
            source = "Ancient Texts Archive"
        )
    }

    @Test
    fun givenSeededPassage_whenGetPassageCalled_thenReturnsPassageWithThemesAndExplanations() = runTest {
        val result = repository.getPassage("gita_2_20")

        assertTrue(result is FathomResult.Success)
        val passage = result.data
        assertEquals("gita_2_20", passage.id)
        assertEquals(TraditionId.HINDUISM, passage.traditionId)
        assertEquals("BG 2.20", passage.canonicalReference)
        assertEquals(1, passage.themes.size)
        assertEquals("Eternal Soul", passage.themes.first().name)
        assertEquals(1, passage.explanations.size)
        assertEquals("The Eternal Nature", passage.explanations.first().title)
    }

    @Test
    fun givenBookPassages_whenGetPassagesForBook_thenReturnsOrderedPaginatedList() = runTest {
        val result = repository.getPassagesForBook("bhagavad_gita", offset = 0, limit = 10)

        assertTrue(result is FathomResult.Success)
        assertEquals(1, result.data.size)
        assertEquals("gita_2_20", result.data.first().id)
    }

    @Test
    fun givenPassageId_whenObserved_thenEmitsStateFlowUpdates() = runTest {
        val result = repository.observePassage("gita_2_20").first()

        assertTrue(result is FathomResult.Success)
        assertEquals("BG 2.20", result.data.canonicalReference)
    }
}
