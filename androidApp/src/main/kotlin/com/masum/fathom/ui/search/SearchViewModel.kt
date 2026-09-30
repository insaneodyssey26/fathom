package com.masum.fathom.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.SacredTopic
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private var topicJob: Job? = null

    init {
        loadAllBooks()
        observeBookmarks()
    }

    fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.OnQueryChange -> handleQueryChange(event.query)
            is SearchUiEvent.OnSelectTradition -> handleSelectTradition(event.traditionId)
            is SearchUiEvent.OnSelectTopic -> handleSelectTopic(event.topic)
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
        } else if (_uiState.value.selectedTopic != null) {
            loadTopicPassages(_uiState.value.selectedTopic!!, traditionId)
        }
    }

    private fun handleSelectTopic(topic: SacredTopic?) {
        _uiState.update { it.copy(selectedTopic = topic) }
        if (topic == null) {
            _uiState.update { it.copy(topicPassages = emptyList(), isLoadingTopic = false) }
            return
        }
        loadTopicPassages(topic, _uiState.value.selectedTradition)
    }

    private fun loadTopicPassages(topic: SacredTopic, traditionId: TraditionId?) {
        topicJob?.cancel()
        topicJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTopic = true) }
            
            val deferreds = topic.searchTerms.map { term ->
                async {
                    when (val res = passageRepository.searchPassages(term, traditionId, limit = 30)) {
                        is FathomResult.Success -> res.data
                        is FathomResult.Failure -> emptyList()
                    }
                }
            }
            val allResults = deferreds.awaitAll().flatten()
            val distinctPassages = allResults.distinctBy { it.id }
            val sortedPassages = distinctPassages.sortedByDescending { passage ->
                val text = (passage.translation + " " + (passage.originalText ?: "")).lowercase()
                topic.keywords.count { kw -> text.contains(kw.lowercase()) }
            }
            _uiState.update {
                it.copy(
                    topicPassages = sortedPassages,
                    isLoadingTopic = false
                )
            }
        }
    }

    private fun handleSelectBook(book: Book?) {
        if (book == null) {
            _uiState.update { it.copy(selectedBook = null) }
            return
        }

        val isSameBook = _uiState.value.selectedBook?.id == book.id
        if (!isSameBook) {
            _uiState.update {
                it.copy(
                    selectedBook = book,
                    selectedChapter = null,
                    availableChapters = emptyList(),
                    chapterPassages = emptyList(),
                    chapterPassagesMap = emptyMap()
                )
            }

            viewModelScope.launch {
                when (val result = passageRepository.getChaptersForBook(book.id)) {
                    is FathomResult.Success -> {
                        val chapters = result.data
                        val initialChapter = chapters.firstOrNull() ?: 1
                        _uiState.update {
                            it.copy(
                                availableChapters = chapters,
                                selectedChapter = initialChapter
                            )
                        }
                        handleSelectChapter(initialChapter)
                    }
                    is FathomResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                availableChapters = emptyList(),
                                selectedChapter = null
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleSelectChapter(chapter: Int?) {
        val currentBook = _uiState.value.selectedBook ?: return
        if (chapter == null) {
            _uiState.update { it.copy(selectedChapter = null, chapterPassages = emptyList()) }
            return
        }

        val cachedPassages = _uiState.value.chapterPassagesMap[chapter]
        if (cachedPassages != null) {
            _uiState.update {
                it.copy(
                    selectedChapter = chapter,
                    chapterPassages = cachedPassages,
                    isLoadingChapter = false
                )
            }
            return
        }

        _uiState.update { it.copy(selectedChapter = chapter, isLoadingChapter = true) }

        viewModelScope.launch {
            when (val result = passageRepository.getPassagesForBookChapter(currentBook.id, chapter)) {
                is FathomResult.Success -> {
                    _uiState.update { currentState ->
                        val newMap = currentState.chapterPassagesMap + (chapter to result.data)
                        currentState.copy(
                            chapterPassages = result.data,
                            chapterPassagesMap = newMap,
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
