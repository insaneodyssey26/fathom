package com.masum.fathom.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.user.Bookmark
import com.masum.fathom.core.model.user.FeedConfig
import com.masum.fathom.core.model.user.FeedMode
import com.masum.fathom.core.repository.FeedRepository
import com.masum.fathom.core.repository.UserDataRepository
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
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private var currentConfig = FeedConfig()

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
            is FeedUiEvent.OnChangeTheme -> handleChangeTheme(event.theme)
            is FeedUiEvent.OnToggleTradition -> handleToggleTradition(event.traditionId)
            is FeedUiEvent.OnChangeMode -> handleChangeMode(event.mode)
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
            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 10)) {
                is FathomResult.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = FeedUiState.Empty
                    } else {
                        _uiState.value = FeedUiState.Success(
                            passages = result.data,
                            currentIndex = 0,
                            activeMode = currentConfig.mode,
                            selectedTraditions = currentConfig.selectedTraditions
                        )
                        recordCurrentReading(result.data.first())
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

        if (index >= currentState.passages.size - 3 && !currentState.isLoadingMore) {
            loadMorePassages()
        }
    }

    private fun loadMorePassages() {
        val currentState = _uiState.value as? FeedUiState.Success ?: return
        _uiState.update { currentState.copy(isLoadingMore = true) }

        viewModelScope.launch {
            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 10)) {
                is FathomResult.Success -> {
                    _uiState.update {
                        if (it is FeedUiState.Success) {
                            it.copy(
                                passages = it.passages + result.data,
                                isLoadingMore = false
                            )
                        } else it
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
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedPassageForContext = passage) else state
        }
    }

    private fun handleCloseContext() {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedPassageForContext = null) else state
        }
    }

    private fun handleChangeTheme(theme: ReadingTheme) {
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(activeTheme = theme) else state
        }
    }

    private fun handleToggleTradition(traditionId: com.masum.fathom.core.model.TraditionId) {
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

        currentConfig = currentConfig.copy(selectedTraditions = updatedTraditions)
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(selectedTraditions = updatedTraditions) else state
        }

        refreshFeedPreservingState()
    }

    private fun handleChangeMode(mode: FeedMode) {
        currentConfig = currentConfig.copy(mode = mode)
        _uiState.update { state ->
            if (state is FeedUiState.Success) state.copy(activeMode = mode) else state
        }
        refreshFeedPreservingState()
    }

    private fun refreshFeedPreservingState() {
        viewModelScope.launch {
            when (val result = feedRepository.getFeedBatch(currentConfig, batchSize = 10)) {
                is FathomResult.Success -> {
                    _uiState.update { state ->
                        if (state is FeedUiState.Success) {
                            state.copy(
                                passages = if (result.data.isNotEmpty()) result.data else state.passages,
                                currentIndex = 0
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
}

