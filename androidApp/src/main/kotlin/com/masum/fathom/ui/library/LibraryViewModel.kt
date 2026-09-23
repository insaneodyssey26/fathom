package com.masum.fathom.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.user.UserNote
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LibraryViewModel(
    private val userDataRepository: UserDataRepository,
    private val passageRepository: PassageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(LibraryTab.BOOKMARKS)
    private val _selectedPassageForNote = MutableStateFlow<Passage?>(null)

    init {
        observeLibraryData()
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.OnSelectTab -> _selectedTab.value = event.tab
            is LibraryUiEvent.OnRemoveBookmark -> removeBookmark(event.passageId)
            is LibraryUiEvent.OnOpenNoteDialog -> _selectedPassageForNote.value = event.passage
            is LibraryUiEvent.OnCloseNoteDialog -> _selectedPassageForNote.value = null
            is LibraryUiEvent.OnSaveNote -> saveNote(event.passageId, event.content)
            is LibraryUiEvent.OnDeleteNote -> deleteNote(event.noteId)
        }
    }

    private fun observeLibraryData() {
        combine(
            userDataRepository.observeBookmarks(),
            userDataRepository.observeReadingHistory(),
            _selectedTab,
            _selectedPassageForNote
        ) { bookmarks, history, tab, notePassage ->
            val bookmarkItems = bookmarks.map { bookmark ->
                val passage = passageRepository.getPassage(bookmark.passageId).asSuccessOrNull()
                BookmarkWithPassage(bookmark, passage)
            }

            val totalReads = history.sumOf { it.readCount }

            LibraryUiState.Success(
                selectedTab = tab,
                bookmarks = bookmarkItems,
                history = history,
                totalReadCount = totalReads,
                selectedPassageForNote = notePassage
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private fun removeBookmark(passageId: String) {
        viewModelScope.launch {
            userDataRepository.removeBookmark(passageId)
        }
    }

    private fun saveNote(passageId: String, content: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val note = UserNote(
                id = "note_${now}",
                passageId = passageId,
                content = content,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
            userDataRepository.upsertNote(note)
            _selectedPassageForNote.value = null
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            userDataRepository.deleteNote(noteId)
        }
    }
}
