package com.masum.fathom.ui.library

import androidx.compose.runtime.Immutable
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.ReadingRecord
import com.masum.fathom.core.model.user.UserNote

enum class LibraryTab {
    BOOKMARKS,
    REFLECTIONS,
    HISTORY
}

data class BookmarkWithPassage(
    val bookmark: Bookmark,
    val passage: Passage?
)

data class NoteWithPassage(
    val note: UserNote,
    val passage: Passage?
)

data class HistoryWithPassage(
    val record: ReadingRecord,
    val passage: Passage?
)

data class HeatmapDay(
    val dayOfWeekLabel: String,
    val dayOfMonth: Int,
    val dateString: String,
    val readCount: Int,
    val isToday: Boolean
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val traditionId: TraditionId?,
    val description: String,
    val requiredCount: Int,
    val currentCount: Int,
    val isUnlocked: Boolean,
    val tierName: String
)

@Immutable
sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    data class Success(
        val selectedTab: LibraryTab = LibraryTab.BOOKMARKS,
        val bookmarks: List<BookmarkWithPassage> = emptyList(),
        val notes: List<NoteWithPassage> = emptyList(),
        val history: List<HistoryWithPassage> = emptyList(),
        val totalReadCount: Int = 0,
        val currentStreakDays: Int = 0,
        val weeklyHeatmap: List<HeatmapDay> = emptyList(),
        val milestoneBadges: List<MilestoneBadge> = emptyList(),
        val selectedPassageForNote: Passage? = null,
        val selectedPassageForContext: Passage? = null,
        val selectedPassageForShare: Passage? = null,
        val selectedTraditionFilter: TraditionId? = null,
        val selectedTagFilter: String? = null,
        val searchQuery: String = ""
    ) : LibraryUiState

    data class Error(val message: String) : LibraryUiState
}

sealed interface LibraryUiEvent {
    data class OnSelectTab(val tab: LibraryTab) : LibraryUiEvent
    data class OnRemoveBookmark(val passageId: String) : LibraryUiEvent
    data class OnUpdateBookmarkTags(val passageId: String, val tags: List<String>) : LibraryUiEvent
    data class OnOpenContext(val passage: Passage) : LibraryUiEvent
    data object OnCloseContext : LibraryUiEvent
    data class OnOpenShare(val passage: Passage) : LibraryUiEvent
    data object OnCloseShare : LibraryUiEvent
    data class OnOpenNoteDialog(val passage: Passage) : LibraryUiEvent
    data object OnCloseNoteDialog : LibraryUiEvent
    data class OnSaveNote(val passageId: String, val content: String) : LibraryUiEvent
    data class OnDeleteNote(val noteId: String) : LibraryUiEvent
    data class OnFilterTradition(val traditionId: TraditionId?) : LibraryUiEvent
    data class OnFilterTag(val tag: String?) : LibraryUiEvent
    data class OnSearchQueryChanged(val query: String) : LibraryUiEvent
}

