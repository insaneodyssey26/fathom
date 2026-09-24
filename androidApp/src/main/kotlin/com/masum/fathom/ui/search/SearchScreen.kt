package com.masum.fathom.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.BookmarkSimple as BookmarkSimpleFill
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.BookmarkSimple
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    state: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val directoryListState = rememberLazyListState()
    val searchListState = rememberLazyListState()

    if (state.selectedPassageForShare != null) {
        BackHandler { onEvent(SearchUiEvent.OnCloseShare) }
    } else if (state.selectedPassageForContext != null) {
        BackHandler { onEvent(SearchUiEvent.OnCloseContext) }
    } else if (state.selectedBook != null) {
        BackHandler { onEvent(SearchUiEvent.OnSelectBook(null)) }
    } else if (state.searchQuery.isNotEmpty()) {
        BackHandler { onEvent(SearchUiEvent.OnQueryChange("")) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        AnimatedContent(
            targetState = state.selectedBook,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(tween(320)) { it } + fadeIn(tween(320)))
                        .togetherWith(slideOutHorizontally(tween(260)) { -it / 3 } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally(tween(280)) { it } + fadeOut(tween(200)))
                }
            },
            label = "explore_screen_transition",
            modifier = Modifier.fillMaxSize()
        ) { activeBook ->
            if (activeBook != null) {
                ChapterReaderView(
                    book = activeBook,
                    chapters = state.availableChapters,
                    selectedChapter = state.selectedChapter,
                    chapterPassagesMap = state.chapterPassagesMap,
                    isLoading = state.isLoadingChapter,
                    bookmarkedIds = state.bookmarkedPassageIds,
                    onEvent = onEvent
                )
            } else {
                DirectoryAndSearchRootView(
                    state = state,
                    directoryListState = directoryListState,
                    searchListState = searchListState,
                    onEvent = onEvent
                )
            }
        }

        state.selectedPassageForContext?.let { contextPassage ->
            PassageContextSheet(
                passage = contextPassage,
                onDismiss = { onEvent(SearchUiEvent.OnCloseContext) }
            )
        }

        state.selectedPassageForShare?.let { sharePassage ->
            QuoteCardModal(
                passage = sharePassage,
                onDismiss = { onEvent(SearchUiEvent.OnCloseShare) }
            )
        }
    }
}

@Composable
private fun DirectoryAndSearchRootView(
    state: SearchUiState,
    directoryListState: androidx.compose.foundation.lazy.LazyListState,
    searchListState: androidx.compose.foundation.lazy.LazyListState,
    onEvent: (SearchUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Explore & Search",
                        style = FathomTheme.typography.brandTitle,
                        color = FathomTheme.colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "10K+ VERSES",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 1.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchInputBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(SearchUiEvent.OnQueryChange(it)) },
                    modifier = Modifier.weight(1.06f)
                )

                TraditionSuitcaseTumbler(
                    selectedTradition = state.selectedTradition,
                    onSelectTradition = { onEvent(SearchUiEvent.OnSelectTradition(it)) },
                    modifier = Modifier.weight(0.94f)
                )
            }
        }

        SacredDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (state.searchQuery.isNotBlank()) {
                SearchResultsView(
                    query = state.searchQuery,
                    results = state.searchResults,
                    isSearching = state.isSearching,
                    bookmarkedIds = state.bookmarkedPassageIds,
                    listState = searchListState,
                    onEvent = onEvent
                )
            } else {
                ScripturesDirectoryView(
                    books = state.allBooks,
                    selectedTradition = state.selectedTradition,
                    listState = directoryListState,
                    onSelectBook = { onEvent(SearchUiEvent.OnSelectBook(it)) }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                FathomTheme.colors.background,
                                FathomTheme.colors.background.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (query.isNotEmpty()) FathomTheme.colors.accentGold.copy(alpha = 0.6f) else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
            contentDescription = null,
            tint = if (query.isNotEmpty()) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = JakartaFontFamily,
                fontSize = 13.sp,
                color = FathomTheme.colors.textPrimary
            ),
            cursorBrush = SolidColor(FathomTheme.colors.accentGold),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search verses...",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.5.sp,
                        color = FathomTheme.colors.textTertiary,
                        maxLines = 1
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.X,
                    contentDescription = "Clear",
                    tint = FathomTheme.colors.textTertiary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun TraditionSuitcaseTumbler(
    selectedTradition: TraditionId?,
    onSelectTradition: (TraditionId?) -> Unit,
    modifier: Modifier = Modifier
) {
    val items: List<Pair<TraditionId?, String>> = remember {
        listOf(
            null to "ALL CANONS",
            TraditionId.HINDUISM to "HINDUISM",
            TraditionId.BUDDHISM to "BUDDHISM",
            TraditionId.STOICISM to "STOICISM",
            TraditionId.TAOISM to "TAOISM",
            TraditionId.ISLAM to "ISLAM",
            TraditionId.CHRISTIANITY to "CHRISTIANITY",
            TraditionId.JUDAISM to "JUDAISM"
        )
    }

    val targetIndex = items.indexOfFirst { it.first == selectedTradition }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = targetIndex) { items.size }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(targetIndex) {
        if (pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val selected = items[pagerState.currentPage].first
        if (selected != selectedTradition) {
            onSelectTradition(selected)
        }
    }

    val isCustomFiltered = selectedTradition != null

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (isCustomFiltered) FathomTheme.colors.accentGold.copy(alpha = 0.55f) else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        if (isCustomFiltered) FathomTheme.colors.accentGold.copy(alpha = 0.6f)
                        else FathomTheme.colors.borderSubtle
                    )
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { page ->
                    val pair = items[page]
                    val tradition = pair.first
                    val label = pair.second
                    val isCurrent = tradition == selectedTradition
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            color = if (isCurrent && tradition != null) FathomTheme.colors.accentGold
                            else if (isCurrent) FathomTheme.colors.textPrimary
                            else FathomTheme.colors.textTertiary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    FathomTheme.colors.surfaceElevated.copy(alpha = 0.95f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    FathomTheme.colors.surfaceElevated.copy(alpha = 0.95f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val prev = (pagerState.currentPage - 1 + items.size) % items.size
                            pagerState.animateScrollToPage(prev)
                        }
                    },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretUp,
                        contentDescription = "Previous Canon",
                        tint = if (isCustomFiltered) FathomTheme.colors.accentGold.copy(alpha = 0.85f) else FathomTheme.colors.textTertiary,
                        modifier = Modifier.size(10.dp)
                    )
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            val next = (pagerState.currentPage + 1) % items.size
                            pagerState.animateScrollToPage(next)
                        }
                    },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretDown,
                        contentDescription = "Next Canon",
                        tint = if (isCustomFiltered) FathomTheme.colors.accentGold.copy(alpha = 0.85f) else FathomTheme.colors.textTertiary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultsView(
    query: String,
    results: List<Passage>,
    isSearching: Boolean,
    bookmarkedIds: Set<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onEvent: (SearchUiEvent) -> Unit
) {
    if (isSearching) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = FathomTheme.colors.accentGold)
        }
    } else if (results.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Passages Found",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 18.sp
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No sacred verses matched “$query”. Try searching for broader themes like stillness, duty, light, or path.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 96.dp)
        ) {
            item {
                Text(
                    text = "${results.size} VERSES FOUND",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            items(results, key = { it.id }) { passage ->
                SearchPassageCard(
                    passage = passage,
                    isBookmarked = passage.id in bookmarkedIds,
                    onToggleBookmark = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                    onOpenContext = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                    onOpenShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SearchPassageCard(
    passage: Passage,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onOpenContext: () -> Unit,
    onOpenShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TraditionBadge(traditionId = passage.traditionId)
            Text(
                text = passage.canonicalReference,
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!passage.originalText.isNullOrBlank()) {
            Text(
                text = passage.originalText ?: "",
                style = FathomTheme.typography.verseOriginal.copy(
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                ),
                color = FathomTheme.colors.textTertiary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
            text = passage.translation,
            style = FathomTheme.typography.verseDisplay.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = FathomTheme.colors.textPrimary
        )

        SacredDivider(modifier = Modifier.padding(vertical = 10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onOpenShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShareNetwork,
                        contentDescription = "Share Quote Card",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onOpenContext,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Info,
                        contentDescription = "Context & Commentary",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) PhosphorIcons.Fill.BookmarkSimpleFill else PhosphorIcons.Regular.BookmarkSimple,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TextButton(
                onClick = onOpenContext,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(
                    text = "EXEGESIS",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.8.sp,
                    color = FathomTheme.colors.accentGold
                )
            }
        }
    }
}

@Composable
private fun ScripturesDirectoryView(
    books: List<Book>,
    selectedTradition: TraditionId?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onSelectBook: (Book) -> Unit
) {
    val filteredBooks = if (selectedTradition != null) {
        books.filter { it.traditionId == selectedTradition }
    } else {
        books
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "SACRED CANONS & SCRIPTURES",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(filteredBooks, key = { it.id }) { book ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSelectBook(book) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.BookBookmark,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        TraditionBadge(traditionId = book.traditionId)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.title,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${book.totalPassages} verses • ${book.language.uppercase()}",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                }

                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = "Browse Chapters",
                    tint = FathomTheme.colors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ChapterReaderView(
    book: Book,
    chapters: List<Int>,
    selectedChapter: Int?,
    chapterPassagesMap: Map<Int, List<Passage>>,
    isLoading: Boolean,
    bookmarkedIds: Set<String>,
    onEvent: (SearchUiEvent) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val chapterScrollState = rememberLazyListState()

    val initialPage = if (selectedChapter != null) {
        val idx = chapters.indexOf(selectedChapter)
        if (idx >= 0) idx else 0
    } else 0

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { chapters.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                if (page in chapters.indices) {
                    val targetChapter = chapters[page]
                    if (targetChapter != selectedChapter) {
                        onEvent(SearchUiEvent.OnSelectChapter(targetChapter))
                    }
                    chapterScrollState.animateScrollToItem(page)
                }
            }
    }

    LaunchedEffect(selectedChapter) {
        if (selectedChapter != null) {
            val targetPage = chapters.indexOf(selectedChapter)
            if (targetPage >= 0 && targetPage != pagerState.currentPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onEvent(SearchUiEvent.OnSelectBook(null)) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FathomTheme.colors.surfaceElevated)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = "Back to Scriptures",
                        tint = FathomTheme.colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.title,
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.5.sp,
                        color = FathomTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${book.traditionId.displayName.uppercase()} CANON • ${book.totalPassages} VERSES",
                        style = FathomTheme.typography.caption.copy(
                            fontSize = 10.sp,
                            letterSpacing = 0.9.sp
                        ),
                        color = FathomTheme.colors.accentGold
                    )
                }
            }

            if (chapters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    state = chapterScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(chapters.size, key = { chapters[it] }) { index ->
                        val chapter = chapters[index]
                        val isSelected = pagerState.currentPage == index
                        val prefix = when (book.traditionId) {
                            TraditionId.ISLAM -> "SURAH"
                            TraditionId.TAOISM -> "CH"
                            TraditionId.STOICISM -> "BOOK"
                            else -> "CH"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.22f) else FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "$prefix $chapter",
                                fontFamily = JakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        SacredDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentCh = if (page in chapters.indices) chapters[page] else null
                val passages = currentCh?.let { chapterPassagesMap[it] } ?: emptyList()

                if (passages.isEmpty() && isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp)
                    ) {
                        items(passages, key = { it.id }) { passage ->
                            ChapterVerseCard(
                                passage = passage,
                                isBookmarked = passage.id in bookmarkedIds,
                                onToggleBookmark = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                                onOpenContext = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                                onOpenShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterVerseCard(
    passage: Passage,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onOpenContext: () -> Unit,
    onOpenShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                    .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = passage.canonicalReference.uppercase(),
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = FathomTheme.colors.accentGold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenShare,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShareNetwork,
                        contentDescription = "Share Quote Card",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onOpenContext,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Info,
                        contentDescription = "Context & Commentary",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) PhosphorIcons.Fill.BookmarkSimpleFill else PhosphorIcons.Regular.BookmarkSimple,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!passage.originalText.isNullOrBlank()) {
            Text(
                text = passage.originalText ?: "",
                style = FathomTheme.typography.verseOriginal.copy(
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                ),
                color = FathomTheme.colors.textTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = passage.translation,
            style = FathomTheme.typography.verseDisplay.copy(
                fontSize = 16.sp,
                lineHeight = 25.sp
            ),
            color = FathomTheme.colors.textPrimary
        )
    }
}
