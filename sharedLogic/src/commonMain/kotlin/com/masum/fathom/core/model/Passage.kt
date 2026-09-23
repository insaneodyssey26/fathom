package com.masum.fathom.core.model

data class Passage(
    val id: String,
    val bookId: String,
    val traditionId: TraditionId,
    val canonicalReference: String,
    val originalText: String?,
    val translation: String,
    val orderIndex: Int,
    val chapter: Int? = null,
    val verseNumber: Int? = null,
    val themes: List<Theme> = emptyList(),
    val explanations: List<Explanation> = emptyList()
)
