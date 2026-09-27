package com.masum.fathom.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.offset
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
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
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
    val haptics = LocalFathomHaptics.current
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = "explore",
                        style = FathomTheme.typography.brandTitle,
                        color = FathomTheme.colors.textPrimary
                    )
                    Text(
                        text = ".",
                        style = FathomTheme.typography.brandTitle.copy(color = FathomTheme.colors.accentGold)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (subscriptionState.isPro) FathomTheme.colors.accentGold.copy(alpha = 0.16f)
                                else FathomTheme.colors.surfaceElevated
                            )
                            .border(
                                width = 1.dp,
                                color = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                haptics.medium()
                                openPaywall()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Crown,
                                contentDescription = "Sanctuary+ Pro",
                                tint = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (subscriptionState.isPro) "PATRON" else "PRO",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.5.sp,
                                color = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                            .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
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
            }

            Spacer(modifier = Modifier.height(14.dp))

            SearchInputBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(SearchUiEvent.OnQueryChange(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            CanonFilterRow(
                selectedTradition = state.selectedTradition,
                onSelectTradition = { onEvent(SearchUiEvent.OnSelectTradition(it)) }
            )

            if (state.searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                ThematicSuggestionsRow(
                    onSelectTheme = { theme ->
                        haptics.light()
                        onEvent(SearchUiEvent.OnQueryChange(theme))
                    }
                )
            }
        }

        SacredDivider(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))

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
    val haptics = LocalFathomHaptics.current

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (query.isNotEmpty()) FathomTheme.colors.accentGold.copy(alpha = 0.7f) else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.MagnifyingGlass,
            contentDescription = null,
            tint = if (query.isNotEmpty()) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = JakartaFontFamily,
                fontSize = 13.5.sp,
                color = FathomTheme.colors.textPrimary
            ),
            cursorBrush = SolidColor(FathomTheme.colors.accentGold),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Search verses, keywords, wisdom...",
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp,
                        color = FathomTheme.colors.textTertiary,
                        maxLines = 1
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = {
                    haptics.light()
                    onQueryChange("")
                },
                modifier = Modifier.size(24.dp)
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
private fun CanonFilterRow(
    selectedTradition: TraditionId?,
    onSelectTradition: (TraditionId?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val gradientWidth = with(androidx.compose.ui.platform.LocalDensity.current) { 32.dp.toPx() }
    val gradientColor = FathomTheme.colors.surfaceElevated.copy(alpha = 0.9f)
    val items: List<Pair<TraditionId, String>> = remember {
        listOf(
            TraditionId.HINDUISM to "HINDUISM",
            TraditionId.BUDDHISM to "BUDDHISM",
            TraditionId.STOICISM to "STOICISM",
            TraditionId.TAOISM to "TAOISM",
            TraditionId.ISLAM to "ISLAM",
            TraditionId.CHRISTIANITY to "CHRISTIANITY",
            TraditionId.JUDAISM to "JUDAISM"
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (selectedTradition == null) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                    else FathomTheme.colors.surfaceElevated.copy(alpha = 0.4f)
                )
                .border(
                    1.dp,
                    if (selectedTradition == null) FathomTheme.colors.accentGold.copy(alpha = 0.8f) 
                    else FathomTheme.colors.borderSubtle,
                    RoundedCornerShape(16.dp)
                )
                .clickable {
                    haptics.light()
                    onSelectTradition(null)
                }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ALL",
                fontFamily = JakartaFontFamily,
                fontWeight = if (selectedTradition == null) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.5.sp,
                letterSpacing = 0.6.sp,
                color = if (selectedTradition == null) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(FathomTheme.colors.surfaceElevated.copy(alpha = 0.4f))
                .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(gradientColor, Color.Transparent),
                                startX = 0f,
                                endX = gradientWidth
                            ),
                            size = Size(width = gradientWidth, height = size.height)
                        )
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, gradientColor),
                                startX = size.width - gradientWidth,
                                endX = size.width
                            ),
                            topLeft = Offset(x = size.width - gradientWidth, y = 0f),
                            size = Size(width = gradientWidth, height = size.height)
                        )
                    },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items, key = { it.second }) { (tradition, label) ->
                    val isSelected = tradition == selectedTradition
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.8f)
                                else FathomTheme.colors.borderSubtle.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptics.light()
                                onSelectTradition(tradition)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = JakartaFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.6.sp,
                            color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ThematicSuggestionsRow(
    onSelectTheme: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themes = remember {
        listOf(
            "Stillness",
            "Duty",
            "Peace",
            "Courage",
            "Light",
            "Surrender",
            "Truth",
            "Devotion"
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = PhosphorIcons.Regular.Sparkle,
            contentDescription = null,
            tint = FathomTheme.colors.accentGold,
            modifier = Modifier.size(13.dp)
        )

        Text(
            text = "THEMES:",
            fontFamily = JakartaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            letterSpacing = 1.sp,
            color = FathomTheme.colors.accentGold
        )

        Spacer(modifier = Modifier.width(2.dp))

        themes.forEach { theme ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(FathomTheme.colors.surface)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onSelectTheme(theme) }
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = theme,
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.sp,
                    color = FathomTheme.colors.textTertiary
                )
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
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${results.size} VERSES FOUND",
                        style = FathomTheme.typography.canonicalHeader,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }

            items(results, key = { it.id }) { passage ->
                SearchPassageCard(
                    passage = passage,
                    isBookmarked = passage.id in bookmarkedIds,
                    onToggleBookmark = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                    onOpenContext = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                    onOpenShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) }
                )
                Spacer(modifier = Modifier.height(14.dp))
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
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        FathomTheme.colors.surfaceElevated,
                        FathomTheme.colors.surface
                    )
                )
            )
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
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

        Spacer(modifier = Modifier.height(12.dp))

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

        SacredDivider(modifier = Modifier.padding(vertical = 12.dp))

        val audioEngine = LocalSacredAudioEngine.current
        val playbackState by audioEngine.playbackState.collectAsState()
        val haptics = LocalFathomHaptics.current
        val isSpeakingThis = (playbackState as? SacredAudioState.Playing)?.passage?.id == passage.id

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = {
                        haptics.medium()
                        audioEngine.toggle(passage)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.SpeakerHigh,
                        contentDescription = "Sacred Recitation",
                        tint = if (isSpeakingThis) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptics.light()
                        onOpenShare()
                    },
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
                    onClick = {
                        haptics.light()
                        onOpenContext()
                    },
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
                    onClick = {
                        if (!isBookmarked) {
                            haptics.success()
                        } else {
                            haptics.light()
                        }
                        onToggleBookmark()
                    },
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
                onClick = {
                    haptics.light()
                    onOpenContext()
                },
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Text(
                    text = "INSIGHTS",
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
    val haptics = LocalFathomHaptics.current
    val filteredBooks = if (selectedTradition != null) {
        books.filter { it.traditionId == selectedTradition }
    } else {
        books
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CANONS & SCRIPTURES",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
                Text(
                    text = "${filteredBooks.size} Books Available",
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.sp,
                    color = FathomTheme.colors.textTertiary
                )
            }
        }

        items(filteredBooks, key = { it.id }) { book ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                FathomTheme.colors.surfaceElevated,
                                FathomTheme.colors.surface
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable {
                        haptics.light()
                        onSelectBook(book)
                    }
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.BookBookmark,
                    contentDescription = null,
                    tint = FathomTheme.colors.accentGold.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 30.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        TraditionBadge(traditionId = book.traditionId)
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FathomTheme.colors.background.copy(alpha = 0.6f))
                                .border(1.dp, FathomTheme.colors.borderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretRight,
                                contentDescription = "Browse Chapters",
                                tint = FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = book.title,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = FathomTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${book.totalPassages} VERSES",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = book.language.uppercase(),
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
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
    val haptics = LocalFathomHaptics.current
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

    LaunchedEffect(pagerState.currentPage, chapters) {
        if (chapters.isNotEmpty() && pagerState.currentPage in chapters.indices) {
            val targetChapter = chapters[pagerState.currentPage]
            if (targetChapter != selectedChapter) {
                haptics.light()
                onEvent(SearchUiEvent.OnSelectChapter(targetChapter))
            }
            val targetScrollIndex = (pagerState.currentPage - 1).coerceAtLeast(0)
            chapterScrollState.animateScrollToItem(targetScrollIndex)
        }
    }

    LaunchedEffect(selectedChapter, chapters) {
        if (selectedChapter != null && chapters.isNotEmpty()) {
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
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

                        val animatedBg by animateColorAsState(
                            targetValue = if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.22f) else FathomTheme.colors.surfaceElevated,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "chapter_tab_bg"
                        )
                        val animatedBorder by animateColorAsState(
                            targetValue = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "chapter_tab_border"
                        )
                        val animatedTextColor by animateColorAsState(
                            targetValue = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                            label = "chapter_tab_text"
                        )

                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(animatedBg)
                                .border(
                                    width = 1.dp,
                                    color = animatedBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) {
                                    if (pagerState.currentPage != index) {
                                        haptics.light()
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(index)
                                        }
                                        onEvent(SearchUiEvent.OnSelectChapter(chapter))
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$prefix $chapter",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = animatedTextColor
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
                key = { page -> if (page in chapters.indices) chapters[page] else page },
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentCh = if (page in chapters.indices) chapters[page] else null
                val passages = currentCh?.let { chapterPassagesMap[it] } ?: emptyList()
                val isChapterLoading = isLoading && selectedChapter == currentCh

                if (passages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChapterLoading || isLoading) {
                            CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                        } else {
                            Text(
                                text = "No verses found in this chapter",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.sp,
                                color = FathomTheme.colors.textSecondary
                            )
                        }
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
                            Spacer(modifier = Modifier.height(12.dp))
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
    val audioEngine = LocalSacredAudioEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()
    val isSpeakingThis = (playbackState as? SacredAudioState.Playing)?.passage?.id == passage.id

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        FathomTheme.colors.surfaceElevated,
                        FathomTheme.colors.surface
                    )
                )
            )
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(18.dp)
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
                    onClick = { audioEngine.toggle(passage) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.SpeakerHigh,
                        contentDescription = "Sacred Recitation",
                        tint = if (isSpeakingThis) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

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
