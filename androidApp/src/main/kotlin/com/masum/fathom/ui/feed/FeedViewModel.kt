package com.masum.fathom.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.FeedConfig
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.core.repository.FeedRepository
import com.masum.fathom.core.repository.PassageRepository
import com.masum.fathom.core.repository.UserDataRepository
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val userDataRepository: UserDataRepository,
    private val passageRepository: PassageRepository,
    private val sanctuaryPreferences: SanctuaryPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentConfig = FeedConfig(
        selectedTraditions = sanctuaryPreferences.settings.value.feedSelectedTraditions,
        selectedBookId = sanctuaryPreferences.settings.value.feedSelectedBookId,
        alignedTraditions = sanctuaryPreferences.settings.value.feedAlignedTraditions,
        mode = sanctuaryPreferences.settings.value.feedMode
    )
    private var availableBooks: List<Book> = emptyList()
    private var pinnedPassage: Passage? = null

    init {
        observeBookmarks()
        loadInitialFeed()
    }

    fun onEvent(event: FeedUiEvent) {
        when (event) {
            is FeedUiEvent.OnPassageSwiped -> handlePassageSwiped(event.index)
            is FeedUiEvent.OnToggleBookmark -> handleToggleBookmark(event.passage)
            is FeedUiEvent.OnOpenContext -> handleOpenContext(event.passage)
            is FeedUiEvent.OnCloseContext -> handleCloseContext()
            is FeedUiEvent.OnOpenShare -> handleOpenShare(event.passage)
            is FeedUiEvent.OnCloseShare -> handleCloseShare()
            is FeedUiEvent.OnChangeTheme -> handleChangeTheme(event.theme)
            is FeedUiEvent.OnToggleTradition -> handleToggleTradition(event.traditionId)
            is FeedUiEvent.OnChangeMode -> handleChangeMode(event.mode)
            is FeedUiEvent.OnSelectBook -> handleSelectBook(event.bookId)
            is FeedUiEvent.OnSelectTradition -> setTraditionFocus(event.traditionId)
            is FeedUiEvent.OnToggleAlignedTradition -> handleToggleAlignedTradition(event.traditionId)
            is FeedUiEvent.OnClearAlignedTraditions -> handleClearAlignedTraditions()
        }
    }

    private fun observeBookmarks() {
        userDataRepository.observeBookmarks()
            .onEach { bookmarks ->
                val bookmarkedIds = bookmarks.map { it.passageId }.toSet()
                _uiState.update { state ->
                    if (state is FeedUiState.Success) {
                        state.copy(bookmarkedPassageIds = bookmarkedIds)
                    } else {
                        state
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadInitialFeed() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            when (val booksResult = passageRepository.getAllBooks()) {
                is FathomResult.Success -> availableBooks = booksResult.data
                is FathomResult.Failure -> Unit
            }

            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 20)) {
                is FathomResult.Success -> {
                    val pinned = pinnedPassage
                    val initial = if (pinned != null) listOf(pinned) + result.data.filter { it.id != pinned.id } else result.data
                    if (initial.isEmpty()) {
                        _uiState.value = FeedUiState.Empty
                    } else {
                        _uiState.value = FeedUiState.Success(
                            passages = initial,
                            currentIndex = 0,
                            availableBooks = availableBooks,
                            selectedBookId = currentConfig.selectedBookId,
                            activeMode = currentConfig.mode,
                            selectedTraditions = currentConfig.selectedTraditions,
                            alignedTraditions = currentConfig.alignedTraditions
                        )
                        recordCurrentReading(initial.first())
                    }
                }
                is FathomResult.Failure -> {
                    _uiState.value = FeedUiState.Error("Unable to load wisdom feed. Please try again.")
                }
            }
        }
    }

    private fun handlePassageSwiped(index: Int) {
        val currentState = _uiState.value as? FeedUiState.Success ?: return
        _uiState.update { currentState.copy(currentIndex = index) }

        if (index in currentState.passages.indices) {
            recordCurrentReading(currentState.passages[index])
        }

        if (index >= currentState.passages.size - 6 && !currentState.isLoadingMore) {
            loadMorePassages()
        }
    }

    private fun loadMorePassages() {
        val currentState = _uiState.value as? FeedUiState.Success ?: return
        if (currentState.isLoadingMore) return
        _uiState.update { currentState.copy(isLoadingMore = true) }

        viewModelScope.launch {
            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 20)) {
                is FathomResult.Success -> {
                    var shouldFetchAgain = false
                    _uiState.update {
                        if (it is FeedUiState.Success) {
                            val existingIds = it.passages.map { p -> p.id }.toSet()
                            val newPassages = result.data.filter { p -> p.id !in existingIds }
                            val updatedPassages = it.passages + newPassages
                            if (newPassages.isNotEmpty() && it.currentIndex >= updatedPassages.size - 6) {
                                shouldFetchAgain = true
                            }
                            it.copy(
                                passages = updatedPassages,
                                isLoadingMore = false
                            )
                        } else it
                    }
                    if (shouldFetchAgain) {
                        loadMorePassages()
                    }
                }
                is FathomResult.Failure -> {
                    _uiState.update {
                        if (it is FeedUiState.Success) it.copy(isLoadingMore = false) else it
                    }
                }
            }
        }
    }

    private fun handleToggleBookmark(passage: Passage) {
        val currentState = _uiState.value as? FeedUiState.Success ?: return
        val isBookmarked = passage.id in currentState.bookmarkedPassageIds

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

    private fun recordCurrentReading(passage: Passage) {
        viewModelScope.launch {
            userDataRepository.recordReading(passage.id, passage.bookId)
        }
    }

    private fun handleOpenContext(passage: Passage) {
        viewModelScope.launch {
            when (val result = passageRepository.getPassage(passage.id)) {
                is FathomResult.Success -> {
                    _uiState.update { state ->
                        if (state is FeedUiState.Success) state.copy(selectedPassageForContext = result.data) else state
                    }
                }
                is FathomResult.Failure -> {
                    _uiState.update { state ->
                        if (state is FeedUiState.Success) state.copy(selectedPassageForContext = passage) else state
                    }
                }
            }
        }
    }

    private fun handleCloseContext() {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedPassageForContext = null) else state
        }
    }

    private fun handleOpenShare(passage: Passage) {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedPassageForShare = passage) else state
        }
    }

    private fun handleCloseShare() {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedPassageForShare = null) else state
        }
    }

    private fun handleChangeTheme(theme: ReadingTheme) {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(activeTheme = theme) else state
        }
    }

    private fun handleSelectBook(bookId: String?) {
        sanctuaryPreferences.updateFeedBook(bookId)
        sanctuaryPreferences.updateFeedAlignedTraditions(emptySet())
        currentConfig = currentConfig.copy(selectedBookId = bookId, alignedTraditions = emptySet())
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedBookId = bookId, alignedTraditions = emptySet()) else state
        }
        refreshFeedPreservingState()
    }

    private fun handleToggleTradition(traditionId: TraditionId) {
        val currentState = _uiState.value as? FeedUiState.Success ?: return
        val updatedTraditions = if (traditionId in currentState.selectedTraditions) {
            if (currentState.selectedTraditions.size > 1) {
                currentState.selectedTraditions - traditionId
            } else {
                currentState.selectedTraditions
            }
        } else {
            currentState.selectedTraditions + traditionId
        }

        sanctuaryPreferences.updateFeedTraditions(updatedTraditions)
        sanctuaryPreferences.updateFeedBook(null)
        sanctuaryPreferences.updateFeedAlignedTraditions(emptySet())
        currentConfig = currentConfig.copy(
            selectedTraditions = updatedTraditions,
            selectedBookId = null,
            alignedTraditions = emptySet()
        )
        _uiState.update { state ->
            if (state is FeedUiState.Success) {
                state.copy(
                    selectedTraditions = updatedTraditions,
                    selectedBookId = null,
                    alignedTraditions = emptySet()
                )
            } else state
        }

        refreshFeedPreservingState()
    }

    fun setTraditionFocus(traditionId: TraditionId?) {
        val traditions = if (traditionId == null) {
            TraditionId.entries.toSet()
        } else {
            setOf(traditionId)
        }
        sanctuaryPreferences.updateFeedTraditions(traditions)
        sanctuaryPreferences.updateFeedBook(null)
        sanctuaryPreferences.updateFeedAlignedTraditions(emptySet())
        currentConfig = currentConfig.copy(
            selectedTraditions = traditions,
            selectedBookId = null,
            alignedTraditions = emptySet()
        )
        _uiState.update { state ->
            if (state is FeedUiState.Success) {
                state.copy(
                    selectedTraditions = traditions,
                    selectedBookId = null,
                    alignedTraditions = emptySet()
                )
            } else state
        }
        refreshFeedPreservingState()
    }

    private fun handleToggleAlignedTradition(traditionId: TraditionId) {
        val updated = if (traditionId in currentConfig.alignedTraditions) {
            currentConfig.alignedTraditions - traditionId
        } else {
            currentConfig.alignedTraditions + traditionId
        }
        sanctuaryPreferences.updateFeedAlignedTraditions(updated)
        sanctuaryPreferences.updateFeedTraditions(TraditionId.entries.toSet())
        sanctuaryPreferences.updateFeedBook(null)
        currentConfig = currentConfig.copy(
            selectedTraditions = TraditionId.entries.toSet(),
            selectedBookId = null,
            alignedTraditions = updated
        )
        _uiState.update { state ->
            if (state is FeedUiState.Success) {
                state.copy(
                    selectedTraditions = TraditionId.entries.toSet(),
                    selectedBookId = null,
                    alignedTraditions = updated
                )
            } else state
        }
        refreshFeedPreservingState()
    }

    private fun handleClearAlignedTraditions() {
        sanctuaryPreferences.updateFeedAlignedTraditions(emptySet())
        sanctuaryPreferences.updateFeedTraditions(TraditionId.entries.toSet())
        sanctuaryPreferences.updateFeedBook(null)
        currentConfig = currentConfig.copy(
            selectedTraditions = TraditionId.entries.toSet(),
            selectedBookId = null,
            alignedTraditions = emptySet()
        )
        _uiState.update { state ->
            if (state is FeedUiState.Success) {
                state.copy(
                    selectedTraditions = TraditionId.entries.toSet(),
                    selectedBookId = null,
                    alignedTraditions = emptySet()
                )
            } else state
        }
        refreshFeedPreservingState()
    }

    private fun handleChangeMode(mode: FeedMode) {
        sanctuaryPreferences.updateFeedMode(mode)
        currentConfig = currentConfig.copy(mode = mode)
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(activeMode = mode) else state
        }
        refreshFeedPreservingState()
    }

    private fun refreshFeedPreservingState() {
        viewModelScope.launch {
            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 20)) {
                is FathomResult.Success -> {
                    _uiState.update { state ->
                        if (state is FeedUiState.Success) {
                            state.copy(
                                passages = if (result.data.isNotEmpty()) result.data else state.passages,
                                currentIndex = 0,
                                availableBooks = availableBooks,
                                selectedBookId = currentConfig.selectedBookId,
                                selectedTraditions = currentConfig.selectedTraditions,
                                alignedTraditions = currentConfig.alignedTraditions
                            )
                        } else state
                    }
                    if (result.data.isNotEmpty()) {
                        recordCurrentReading(result.data.first())
                    }
                }
                is FathomResult.Failure -> Unit
            }
        }
    }

    fun loadSpecificPassage(passageId: String) {
        viewModelScope.launch {
            when (val result = passageRepository.getPassage(passageId)) {
                is FathomResult.Success -> {
                    val targetPassage = result.data
                    pinnedPassage = targetPassage
                    _uiState.update { state ->
                        if (state is FeedUiState.Success) {
                            val existingIndex = state.passages.indexOfFirst { it.id == passageId }
                            if (existingIndex != -1) {
                                state.copy(currentIndex = existingIndex)
                            } else {
                                val updatedList = listOf(targetPassage) + state.passages
                                state.copy(passages = updatedList, currentIndex = 0)
                            }
                        } else {
                            FeedUiState.Success(
                                passages = listOf(targetPassage),
                                currentIndex = 0,
                                bookmarkedPassageIds = emptySet(),
                                activeTheme = ReadingTheme.OBSIDIAN,
                                selectedTraditions = currentConfig.selectedTraditions,
                                selectedBookId = currentConfig.selectedBookId,
                                availableBooks = availableBooks
                            )
                        }
                    }
                    recordCurrentReading(targetPassage)
                }
                is FathomResult.Failure -> Unit
            }
        }
    }
}
