package com.masum.fathom.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Sliders
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.SacredTopic
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.ui.components.PassageEntry
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.DockedTopPanel
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.NavToneEffect
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.ScreenHeader
import com.masum.fathom.ui.design.WidthClass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone


@Composable
fun SearchScreen(
    state: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit,
    onOpenBook: (com.masum.fathom.core.model.Book) -> Unit,
    modifier: Modifier = Modifier
) {
    val directoryListState = rememberLazyListState()
    val searchListState = rememberLazyListState()

    when {
        state.selectedTopic != null -> BackHandler { onEvent(SearchUiEvent.OnSelectTopic(null)) }
        state.searchQuery.isNotEmpty() -> BackHandler { onEvent(SearchUiEvent.OnQueryChange("")) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ExploreRoot(
            state = state,
            directoryListState = directoryListState,
            searchListState = searchListState,
            onOpenBook = onOpenBook,
            onEvent = onEvent
        )
    }
}

@Composable
fun SearchBookScreen(
    state: SearchUiState,
    book: com.masum.fathom.core.model.Book,
    onEvent: (SearchUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        BookReader(
            book = book,
            chapters = state.availableChapters,
            selectedChapter = state.selectedChapter,
            chapterPassagesMap = state.chapterPassagesMap,
            isLoading = state.isLoadingChapter,
            bookmarkedIds = state.bookmarkedPassageIds,
            onBack = onBack,
            onEvent = onEvent
        )
    }
}

@Composable
fun SearchOverlays(state: SearchUiState, onEvent: (SearchUiEvent) -> Unit) {
    if (state.selectedPassageForShare != null) BackHandler { onEvent(SearchUiEvent.OnCloseShare) }
    else if (state.selectedPassageForContext != null) BackHandler { onEvent(SearchUiEvent.OnCloseContext) }
    state.selectedPassageForContext?.let {
        PassageContextSheet(passage = it, onDismiss = { onEvent(SearchUiEvent.OnCloseContext) })
    }
    state.selectedPassageForShare?.let {
        QuoteCardModal(passage = it, onDismiss = { onEvent(SearchUiEvent.OnCloseShare) })
    }
}

@Composable
private fun ExploreRoot(
    state: SearchUiState,
    directoryListState: LazyListState,
    searchListState: LazyListState,
    onOpenBook: (com.masum.fathom.core.model.Book) -> Unit,
    onEvent: (SearchUiEvent) -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val openSanctuary = LocalOpenSanctuary.current
    val shell = LocalShell.current
    val colors = FathomTheme.colors
    val density = LocalDensity.current

    val focusTradition = state.selectedTradition ?: state.selectedTopic?.seed()
    val tone = rememberTone(focusTradition)
    NavToneEffect(state.selectedTradition)

    var panelHeight by remember { mutableIntStateOf(0) }
    val panelDp = with(density) { panelHeight.toDp() }
    val searching = state.searchQuery.isNotBlank()
    val inTopic = !searching && state.selectedTopic != null
    val listState = if (searching || inTopic) searchListState else directoryListState

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = Modifier.fillMaxSize(),
            background = {
                Atmosphere(
                    tone = tone,
                    modifier = Modifier.fillMaxSize(),
                    phase = { listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 800f }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(start = shell.startInset),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().widthIn(max = shell.contentMaxWidth + 96.dp),
                            contentPadding = PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = panelDp + 8.dp,
                                bottom = shell.bottomInset + 32.dp
                            )
                        ) {
                            item(key = "search_and_chips") {
                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
                                    EditorialSearchField(
                                        query = state.searchQuery,
                                        onQueryChange = { onEvent(SearchUiEvent.OnQueryChange(it)) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(14.dp))
                                    TraditionChips(
                                        selected = state.selectedTradition,
                                        onSelect = {
                                            haptics.light()
                                            onEvent(SearchUiEvent.OnSelectTradition(it))
                                        },
                                        horizontalPadding = 0.dp
                                    )
                                }
                            }
                            val currentTopic = state.selectedTopic
                            when {
                                searching -> resultsItems(state, onEvent)
                                !searching && currentTopic != null -> topicItems(state, currentTopic, onEvent)
                                else -> directoryItems(state, shell.widthClass != WidthClass.Compact, onOpenBook, onEvent)
                            }


                        }
                    }
                }
            },
            overlay = {
                DockedTopPanel(onHeight = { panelHeight = it }) {
                    ScreenHeader(
                        title = "explore",
                        traditionId = focusTradition,
                    ) {
                        GlassIconButton(
                            icon = PhosphorIcons.Light.Sliders,
                            contentDescription = "Settings",
                            onClick = {
                                haptics.light()
                                openSanctuary()
                            }
                        )
                    }
                }
            }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.directoryItems(
    state: SearchUiState,
    wide: Boolean,
    onOpenBook: (com.masum.fathom.core.model.Book) -> Unit,
    onEvent: (SearchUiEvent) -> Unit
) {
    item(key = "topics_heading") {
        SectionHeading(overline = "Cross-tradition", title = "Begin with a feeling", modifier = Modifier.padding(bottom = 20.dp))
    }
    item(key = "topics") {
        TopicMosaic(columns = if (wide) 4 else 2, onSelect = { onEvent(SearchUiEvent.OnSelectTopic(it)) })
    }
    val books = if (state.selectedTradition != null) state.allBooks.filter { it.traditionId == state.selectedTradition } else state.allBooks
    item(key = "books_heading") {
        SectionHeading(
            overline = "Canons",
            title = "The scriptures",
            trailing = "${books.size} ${if (books.size == 1) "book" else "books"}",
            modifier = Modifier.padding(top = 44.dp, bottom = 6.dp)
        )
    }
    items(books, key = { it.id }) { book ->
        BookRow(book = book, onClick = { onOpenBook(book) })
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.topicItems(
    state: SearchUiState,
    topic: SacredTopic,
    onEvent: (SearchUiEvent) -> Unit
) {
    item(key = "topic_header") {
        TopicHeader(topic = topic, count = state.topicPassages.size, loading = state.isLoadingTopic, onBack = { onEvent(SearchUiEvent.OnSelectTopic(null)) })
    }
    when {
        state.isLoadingTopic -> item(key = "topic_loading") {
            Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) { BreathingMark() }
        }
        state.topicPassages.isEmpty() -> item(key = "topic_empty") {
            Box(Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                QuietMessage(title = "No verses yet", body = "This theme has no passages in the selected tradition.")
            }
        }
        else -> items(state.topicPassages, key = { it.id }) { passage ->
            PassageEntry(
                passage = passage,
                saved = passage.id in state.bookmarkedPassageIds,
                onSave = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                onReflect = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                onShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) },
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.resultsItems(
    state: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit
) {
    when {
        state.isSearching -> item(key = "searching") {
            Box(Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) { BreathingMark() }
        }
        state.searchResults.isEmpty() -> item(key = "no_results") {
            Box(Modifier.fillMaxWidth().padding(vertical = 72.dp), contentAlignment = Alignment.Center) {
                QuietMessage(
                    title = "Nothing found",
                    body = "No verses matched “${state.searchQuery}”. Try a broader idea such as stillness, duty, light, or path."
                )
            }
        }
        else -> {
            item(key = "results_heading") {
                MonoLabel(
                    text = "${state.searchResults.size} ${if (state.searchResults.size == 1) "verse" else "verses"}",
                    color = FathomTheme.tone.hue,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(state.searchResults, key = { it.id }) { passage ->
                PassageEntry(
                    passage = passage,
                    saved = passage.id in state.bookmarkedPassageIds,
                    onSave = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                    onReflect = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                    onShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) },
                    highlight = state.searchQuery,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun TopicHeader(topic: SacredTopic, count: Int, loading: Boolean, onBack: () -> Unit) {
    val colors = FathomTheme.colors
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.tappable(scale = 0.95f, onClick = onBack).padding(vertical = 6.dp)) {
            MonoLabel(text = "← All themes", color = colors.textSecondary)
        }
        Text(text = topic.title, style = FathomTheme.typography.display, color = colors.textPrimary)
        Text(text = topic.subtitle, style = FathomTheme.typography.commentaryBody, color = colors.textSecondary)
        if (!loading) MonoLabel(text = "$count verses across traditions", color = FathomTheme.tone.hue, modifier = Modifier.padding(top = 4.dp))
    }
}
