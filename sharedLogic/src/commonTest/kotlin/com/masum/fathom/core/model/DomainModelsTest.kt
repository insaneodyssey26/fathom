package com.masum.fathom.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DomainModelsTest {

    @Test
    fun givenTraditionKey_whenFromKeyOrNull_thenResolvesCorrectly() {
        assertEquals(TraditionId.HINDUISM, TraditionId.fromKeyOrNull("hinduism"))
        assertEquals(TraditionId.STOICISM, TraditionId.fromKeyOrNull("STOICISM"))
        assertNull(TraditionId.fromKeyOrNull("unknown_tradition"))
    }

    @Test
    fun givenPassageWithExplanationsAndThemes_whenConstructed_thenRetainsIntegrity() {
        val theme = Theme(id = "impermanence", name = "Impermanence")
        val explanation = Explanation(
            id = "exp_1",
            passageId = "gita_2_20",
            title = "The Eternal Soul",
            body = "The soul is neither born nor does it die.",
            author = "Traditional Commentary",
            source = "Sanskrit Heritage"
        )
        val passage = Passage(
            id = "gita_2_20",
            bookId = "bhagavad_gita",
            traditionId = TraditionId.HINDUISM,
            canonicalReference = "BG 2.20",
            originalText = "न जायते म्रियते वा कदाचिन्...",
            translation = "The soul is never born nor does it die at any time.",
            orderIndex = 20,
            chapter = 2,
            verseNumber = 20,
            themes = listOf(theme),
            explanations = listOf(explanation)
        )

        assertEquals("gita_2_20", passage.id)
        assertEquals(TraditionId.HINDUISM, passage.traditionId)
        assertEquals(1, passage.themes.size)
        assertEquals(1, passage.explanations.size)
        assertEquals("BG 2.20", passage.canonicalReference)
        assertNotNull(passage.originalText)
    }
}
