package com.masum.fathom.ui.search

import androidx.compose.runtime.Immutable
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId

@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val selectedTradition: TraditionId? = null,
    val searchResults: List<Passage> = emptyList(),
    val isSearching: Boolean = false,
    val allBooks: List<Book> = emptyList(),
    val selectedBook: Book? = null,
    val availableChapters: List<Int> = emptyList(),
    val selectedChapter: Int? = null,
    val chapterPassages: List<Passage> = emptyList(),
    val chapterPassagesMap: Map<Int, List<Passage>> = emptyMap(),
    val isLoadingChapter: Boolean = false,
    val selectedPassageForContext: Passage? = null,
    val selectedPassageForShare: Passage? = null,
    val bookmarkedPassageIds: Set<String> = emptySet()
)

sealed interface SearchUiEvent {
    data class OnQueryChange(val query: String) : SearchUiEvent
    data class OnSelectTradition(val traditionId: TraditionId?) : SearchUiEvent
    data class OnSelectBook(val book: Book?) : SearchUiEvent
    data class OnSelectChapter(val chapter: Int?) : SearchUiEvent
    data class OnToggleBookmark(val passage: Passage) : SearchUiEvent
    data class OnOpenContext(val passage: Passage) : SearchUiEvent
    data object OnCloseContext : SearchUiEvent
    data class OnOpenShare(val passage: Passage) : SearchUiEvent
    data object OnCloseShare : SearchUiEvent
}
