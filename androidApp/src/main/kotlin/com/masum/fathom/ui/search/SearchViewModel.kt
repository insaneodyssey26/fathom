package com.masum.fathom.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val passageRepository: PassageRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAllBooks()
        observeBookmarks()
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.OnQueryChange -> handleQueryChange(event.query)
            is SearchUiEvent.OnSelectTradition -> handleSelectTradition(event.traditionId)
            is SearchUiEvent.OnSelectBook -> handleSelectBook(event.book)
            is SearchUiEvent.OnSelectChapter -> handleSelectChapter(event.chapter)
            is SearchUiEvent.OnToggleBookmark -> handleToggleBookmark(event.passage)
            is SearchUiEvent.OnOpenContext -> handleOpenContext(event.passage)
            is SearchUiEvent.OnCloseContext -> handleCloseContext()
            is SearchUiEvent.OnOpenShare -> handleOpenShare(event.passage)
            is SearchUiEvent.OnCloseShare -> handleCloseShare()
        }
    }

    private fun loadAllBooks() {
        viewModelScope.launch {
            when (val result = passageRepository.getAllBooks()) {
                is FathomResult.Success -> {
                    _uiState.update { it.copy(allBooks = result.data) }
                }
                is FathomResult.Failure -> Unit
            }
        }
    }

    private fun observeBookmarks() {
        userDataRepository.observeBookmarks()
            .onEach { bookmarks ->
                val ids = bookmarks.map { it.passageId }.toSet()
                _uiState.update { it.copy(bookmarkedPassageIds = ids) }
            }
            .launchIn(viewModelScope)
    }

    private fun handleQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(220)
            _uiState.update { it.copy(isSearching = true) }
            val currentTradition = _uiState.value.selectedTradition
            when (val result = passageRepository.searchPassages(query.trim(), currentTradition, limit = 60)) {
                is FathomResult.Success -> {
                    _uiState.update { it.copy(searchResults = result.data, isSearching = false) }
                }
                is FathomResult.Failure -> {
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                }
            }
        }
    }

    private fun handleSelectTradition(traditionId: TraditionId?) {
        _uiState.update { it.copy(selectedTradition = traditionId) }
        if (_uiState.value.searchQuery.isNotBlank()) {
            handleQueryChange(_uiState.value.searchQuery)
        }
    }

    private fun handleSelectBook(book: Book?) {
        _uiState.update {
            it.copy(
                selectedBook = book,
                selectedChapter = null,
                availableChapters = emptyList(),
                chapterPassages = emptyList()
            )
        }

        if (book != null) {
            viewModelScope.launch {
                when (val result = passageRepository.getChaptersForBook(book.id)) {
                    is FathomResult.Success -> {
                        val chapters = result.data
                        _uiState.update { it.copy(availableChapters = chapters) }
                        if (chapters.isNotEmpty()) {
                            handleSelectChapter(chapters.first())
                        }
                    }
                    is FathomResult.Failure -> Unit
                }
            }
        }
    }

    private fun handleSelectChapter(chapter: Int?) {
        val currentBook = _uiState.value.selectedBook ?: return
        _uiState.update { it.copy(selectedChapter = chapter, isLoadingChapter = true) }

        if (chapter != null) {
            viewModelScope.launch {
                when (val result = passageRepository.getPassagesForBookChapter(currentBook.id, chapter)) {
                    is FathomResult.Success -> {
                        _uiState.update {
                            it.copy(
                                chapterPassages = result.data,
                                isLoadingChapter = false
                            )
                        }
                    }
                    is FathomResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                chapterPassages = emptyList(),
                                isLoadingChapter = false
                            )
                        }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(chapterPassages = emptyList(), isLoadingChapter = false) }
        }
    }

    private fun handleToggleBookmark(passage: Passage) {
        val isBookmarked = passage.id in _uiState.value.bookmarkedPassageIds
        viewModelScope.launch {
            if (isBookmarked) {
                userDataRepository.removeBookmark(passage.id)
            } else {
                val bookmark = Bookmark(
                    id = "bm_${passage.id}",
                    passageId = passage.id,
                    createdAtEpochMs = System.currentTimeMillis()
                )
                userDataRepository.saveBookmark(bookmark)
            }
        }
    }

    private fun handleOpenContext(passage: Passage) {
        viewModelScope.launch {
            when (val result = passageRepository.getPassage(passage.id)) {
                is FathomResult.Success -> {
                    _uiState.update { it.copy(selectedPassageForContext = result.data) }
                }
                is FathomResult.Failure -> {
                    _uiState.update { it.copy(selectedPassageForContext = passage) }
                }
            }
        }
    }

    private fun handleCloseContext() {
        _uiState.update { it.copy(selectedPassageForContext = null) }
    }

    private fun handleOpenShare(passage: Passage) {
        _uiState.update { it.copy(selectedPassageForShare = passage) }
    }

    private fun handleCloseShare() {
        _uiState.update { it.copy(selectedPassageForShare = null) }
    }
}
