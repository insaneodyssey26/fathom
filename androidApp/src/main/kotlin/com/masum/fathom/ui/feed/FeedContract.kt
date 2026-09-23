package com.masum.fathom.ui.feed

import androidx.compose.runtime.Immutable
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.ui.theme.ReadingTheme

@Immutable
sealed interface FeedUiState {
    data object Loading : FeedUiState

    data class Success(
        val passages: List<Passage>,
        val currentIndex: Int = 0,
        val bookmarkedPassageIds: Set<String> = emptySet(),
        val activeTheme: ReadingTheme = ReadingTheme.OBSIDIAN,
        val activeMode: FeedMode = FeedMode.EXPLORATION_DISCOVERY,
        val selectedTraditions: Set<TraditionId> = TraditionId.entries.toSet(),
        val selectedPassageForContext: Passage? = null,
        val isLoadingMore: Boolean = false
    ) : FeedUiState

    data object Empty : FeedUiState

    data class Error(val message: String) : FeedUiState
}

sealed interface FeedUiEvent {
    data class OnPassageSwiped(val index: Int) : FeedUiEvent
    data class OnToggleBookmark(val passage: Passage) : FeedUiEvent
    data class OnOpenContext(val passage: Passage) : FeedUiEvent
    data object OnCloseContext : FeedUiEvent
    data class OnChangeTheme(val theme: ReadingTheme) : FeedUiEvent
    data class OnToggleTradition(val traditionId: TraditionId) : FeedUiEvent
    data class OnChangeMode(val mode: FeedMode) : FeedUiEvent
}
