package com.masum.fathom.core.model.user

import com.masum.fathom.core.model.TraditionId

enum class FeedMode {
    EXPLORATION_DISCOVERY,
    SEQUENTIAL_STUDY,
    THEMATIC
}

data class FeedConfig(
    val mode: FeedMode = FeedMode.EXPLORATION_DISCOVERY,
    val selectedTraditions: Set<TraditionId> = TraditionId.entries.toSet(),
    val selectedThemeIds: Set<String> = emptySet(),
    val selectedBookId: String? = null,
    val enableSeenFilter: Boolean = true,
    val traditionDiversityFactor: Float = 0.7f
)
