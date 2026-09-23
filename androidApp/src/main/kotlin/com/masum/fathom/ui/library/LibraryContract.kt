package com.masum.fathom.ui.library

import androidx.compose.runtime.Immutable
import com.masum.fathom.core.model.Passage
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

@Immutable
sealed interface LibraryUiState {
    data object Loading : LibraryUiState

    data class Success(
        val selectedTab: LibraryTab = LibraryTab.BOOKMARKS,
        val bookmarks: List<BookmarkWithPassage> = emptyList(),
        val notes: List<NoteWithPassage> = emptyList(),
        val history: List<ReadingRecord> = emptyList(),
        val totalReadCount: Int = 0,
        val selectedPassageForNote: Passage? = null
    ) : LibraryUiState

    data class Error(val message: String) : LibraryUiState
}

sealed interface LibraryUiEvent {
    data class OnSelectTab(val tab: LibraryTab) : LibraryUiEvent
    data class OnRemoveBookmark(val passageId: String) : LibraryUiEvent
    data class OnOpenNoteDialog(val passage: Passage) : LibraryUiEvent
    data object OnCloseNoteDialog : LibraryUiEvent
    data class OnSaveNote(val passageId: String, val content: String) : LibraryUiEvent
    data class OnDeleteNote(val noteId: String) : LibraryUiEvent
}
