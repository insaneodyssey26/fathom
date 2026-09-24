package com.masum.fathom.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.asSuccessOrNull
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
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
    private val _selectedPassageForContext = MutableStateFlow<Passage?>(null)
    private val _selectedPassageForShare = MutableStateFlow<Passage?>(null)
    private val _selectedTraditionFilter = MutableStateFlow<TraditionId?>(null)
    private val _selectedTagFilter = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")

    init {
        observeLibraryData()
    }

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.OnSelectTab -> _selectedTab.value = event.tab
            is LibraryUiEvent.OnRemoveBookmark -> removeBookmark(event.passageId)
            is LibraryUiEvent.OnUpdateBookmarkTags -> updateBookmarkTags(event.passageId, event.tags)
            is LibraryUiEvent.OnOpenContext -> _selectedPassageForContext.value = event.passage
            is LibraryUiEvent.OnCloseContext -> _selectedPassageForContext.value = null
            is LibraryUiEvent.OnOpenShare -> _selectedPassageForShare.value = event.passage
            is LibraryUiEvent.OnCloseShare -> _selectedPassageForShare.value = null
            is LibraryUiEvent.OnOpenNoteDialog -> _selectedPassageForNote.value = event.passage
            is LibraryUiEvent.OnCloseNoteDialog -> _selectedPassageForNote.value = null
            is LibraryUiEvent.OnSaveNote -> saveNote(event.passageId, event.content)
            is LibraryUiEvent.OnDeleteNote -> deleteNote(event.noteId)
            is LibraryUiEvent.OnFilterTradition -> _selectedTraditionFilter.value = event.traditionId
            is LibraryUiEvent.OnFilterTag -> _selectedTagFilter.value = event.tag
            is LibraryUiEvent.OnSearchQueryChanged -> _searchQuery.value = event.query
        }
    }

    private fun observeLibraryData() {
        val filterFlow = combine(
            _selectedTraditionFilter,
            _selectedTagFilter,
            _searchQuery
        ) { traditionFilter, tagFilter, query ->
            Triple(traditionFilter, tagFilter, query)
        }

        val uiControlsFlow = combine(
            _selectedTab,
            _selectedPassageForNote,
            _selectedPassageForContext,
            _selectedPassageForShare
        ) { tab, notePassage, contextPassage, sharePassage ->
            listOf(tab, notePassage, contextPassage, sharePassage)
        }

        combine(
            userDataRepository.observeBookmarks(),
            userDataRepository.observeAllNotes(),
            userDataRepository.observeReadingHistory(),
            filterFlow,
            uiControlsFlow
        ) { bookmarks, notes, history, (traditionFilter, tagFilter, query), uiControls ->
            val tab = uiControls[0] as LibraryTab
            val notePassage = uiControls[1] as? Passage
            val contextPassage = uiControls[2] as? Passage
            val sharePassage = uiControls[3] as? Passage

            val bookmarkItems = bookmarks.map { bookmark ->
                val passage = passageRepository.getPassage(bookmark.passageId).asSuccessOrNull()
                BookmarkWithPassage(bookmark, passage)
            }

            val noteItems = notes.map { note ->
                val passage = passageRepository.getPassage(note.passageId).asSuccessOrNull()
                NoteWithPassage(note, passage)
            }

            val historyItems = history.map { record ->
                val passage = passageRepository.getPassage(record.passageId).asSuccessOrNull()
                HistoryWithPassage(record, passage)
            }

            val filteredBookmarks = bookmarkItems.filter { item ->
                val matchesTradition = traditionFilter == null || item.passage?.traditionId == traditionFilter
                val matchesTag = tagFilter == null || tagFilter in item.bookmark.tags
                val matchesQuery = query.isBlank() || (item.passage?.translation?.contains(query, ignoreCase = true) == true ||
                        item.passage?.canonicalReference?.contains(query, ignoreCase = true) == true)
                matchesTradition && matchesTag && matchesQuery
            }

            val filteredNotes = noteItems.filter { item ->
                val matchesTradition = traditionFilter == null || item.passage?.traditionId == traditionFilter
                val matchesQuery = query.isBlank() || (item.note.content.contains(query, ignoreCase = true) ||
                        item.passage?.canonicalReference?.contains(query, ignoreCase = true) == true ||
                        item.passage?.translation?.contains(query, ignoreCase = true) == true)
                matchesTradition && matchesQuery
            }

            val totalReads = history.sumOf { it.readCount }

            LibraryUiState.Success(
                selectedTab = tab,
                bookmarks = filteredBookmarks,
                notes = filteredNotes,
                history = historyItems,
                totalReadCount = totalReads,
                selectedPassageForNote = notePassage,
                selectedPassageForContext = contextPassage,
                selectedPassageForShare = sharePassage,
                selectedTraditionFilter = traditionFilter,
                selectedTagFilter = tagFilter,
                searchQuery = query
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

    private fun updateBookmarkTags(passageId: String, tags: List<String>) {
        viewModelScope.launch {
            val bookmark = Bookmark(
                id = "bm_${passageId}",
                passageId = passageId,
                createdAtEpochMs = System.currentTimeMillis(),
                tags = tags
            )
            userDataRepository.saveBookmark(bookmark)
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

