package com.masum.fathom.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.adamglin.phosphoricons.regular.CaretRight
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

@Composable
fun SearchScreen(
    state: SearchUiState,
    onEvent: (SearchUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.selectedPassageForShare != null) {
        BackHandler { onEvent(SearchUiEvent.OnCloseShare) }
    } else if (state.selectedPassageForContext != null) {
        BackHandler { onEvent(SearchUiEvent.OnCloseContext) }
    } else if (state.selectedBook != null) {
        BackHandler { onEvent(SearchUiEvent.OnSelectBook(null)) }
    } else if (state.searchQuery.isNotEmpty()) {
        BackHandler { onEvent(SearchUiEvent.OnQueryChange("")) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        if (state.selectedBook == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore & Search",
                        style = FathomTheme.typography.brandTitle,
                        color = FathomTheme.colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SearchInputBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(SearchUiEvent.OnQueryChange(it)) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TraditionFilterBar(
                    selectedTradition = state.selectedTradition,
                    onSelectTradition = { onEvent(SearchUiEvent.OnSelectTradition(it)) }
                )
            }

            SacredDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (state.selectedBook != null) {
                ChapterReaderView(
                    book = state.selectedBook,
                    chapters = state.availableChapters,
                    selectedChapter = state.selectedChapter,
                    passages = state.chapterPassages,
                    isLoading = state.isLoadingChapter,
                    bookmarkedIds = state.bookmarkedPassageIds,
                    onEvent = onEvent
                )
            } else if (state.searchQuery.isNotBlank()) {
                SearchResultsView(
                    query = state.searchQuery,
                    results = state.searchResults,
                    isSearching = state.isSearching,
                    bookmarkedIds = state.bookmarkedPassageIds,
                    onEvent = onEvent
                )
            } else {
                ScripturesDirectoryView(
                    books = state.allBooks,
                    selectedTradition = state.selectedTradition,
                    onSelectBook = { onEvent(SearchUiEvent.OnSelectBook(it)) }
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
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
            contentDescription = null,
            tint = FathomTheme.colors.textTertiary,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = JakartaFontFamily,
                fontSize = 14.sp,
                color = FathomTheme.colors.textPrimary
            ),
            cursorBrush = SolidColor(FathomTheme.colors.accentGold),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search 10,000+ sacred verses, themes, words...",
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.5.sp,
                        color = FathomTheme.colors.textTertiary
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
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun TraditionFilterBar(
    selectedTradition: TraditionId?,
    onSelectTradition: (TraditionId?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterPill(
            label = "ALL TRADITIONS",
            isSelected = selectedTradition == null,
            onClick = { onSelectTradition(null) }
        )

        TraditionId.entries.forEach { tradition ->
            FilterPill(
                label = tradition.displayName.uppercase(),
                isSelected = selectedTradition == tradition,
                onClick = {
                    onSelectTradition(if (selectedTradition == tradition) null else tradition)
                }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.18f) else FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = JakartaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            letterSpacing = 0.8.sp,
            color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
        )
    }
}

@Composable
private fun SearchResultsView(
    query: String,
    results: List<Passage>,
    isSearching: Boolean,
    bookmarkedIds: Set<String>,
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
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
    onSelectBook: (Book) -> Unit
) {
    val filteredBooks = if (selectedTradition != null) {
        books.filter { it.traditionId == selectedTradition }
    } else {
        books
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(14.dp)
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.BookBookmark,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(20.dp)
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
                            fontSize = 16.5.sp,
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
    passages: List<Passage>,
    isLoading: Boolean,
    bookmarkedIds: Set<String>,
    onEvent: (SearchUiEvent) -> Unit
) {
    val listState = rememberLazyListState()
    val chapterScrollState = rememberLazyListState()

    LaunchedEffect(selectedChapter) {
        listState.scrollToItem(0)
        selectedChapter?.let { ch ->
            val index = chapters.indexOf(ch)
            if (index >= 0) {
                chapterScrollState.animateScrollToItem(index)
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
                        fontSize = 17.sp,
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
                    items(chapters, key = { it }) { chapter ->
                        val isSelected = selectedChapter == chapter
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
                                .clickable { onEvent(SearchUiEvent.OnSelectChapter(chapter)) }
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
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
