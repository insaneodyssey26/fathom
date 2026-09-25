package com.masum.fathom.ui.library

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarCheck
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Flame
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Medal
import com.adamglin.phosphoricons.regular.NotePencil
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TrashSimple
import com.adamglin.phosphoricons.regular.Trophy
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val openSanctuary = LocalOpenSanctuary.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "library",
                    style = FathomTheme.typography.brandTitle,
                    color = FathomTheme.colors.textPrimary
                )
                Text(
                    text = ".",
                    style = FathomTheme.typography.brandTitle.copy(color = FathomTheme.colors.accentGold)
                )
            }

            IconButton(
                onClick = openSanctuary,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FathomTheme.colors.surfaceElevated)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Sliders,
                    contentDescription = "Sanctuary Settings",
                    tint = FathomTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        when (state) {
            is LibraryUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                }
            }

            is LibraryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = FathomTheme.typography.commentaryBody,
                        color = FathomTheme.colors.textSecondary
                    )
                }
            }

            is LibraryUiState.Success -> {
                if (state.selectedPassageForShare != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseShare) }
                } else if (state.selectedPassageForContext != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseContext) }
                } else if (state.selectedPassageForNote != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseNoteDialog) }
                } else if (state.searchQuery.isNotEmpty()) {
                    BackHandler { onEvent(LibraryUiEvent.OnSearchQueryChanged("")) }
                }

                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(
                    initialPage = state.selectedTab.ordinal,
                    pageCount = { LibraryTab.entries.size }
                )

                LaunchedEffect(pagerState.currentPage) {
                    val targetTab = LibraryTab.entries[pagerState.currentPage]
                    if (targetTab != state.selectedTab) {
                        onEvent(LibraryUiEvent.OnSelectTab(targetTab))
                    }
                }

                SacredSegmentedTabBar(
                    pagerState = pagerState,
                    bookmarksCount = state.bookmarks.size,
                    notesCount = state.notes.size,
                    onTabSelected = { tab ->
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = tab.ordinal,
                                animationSpec = androidx.compose.animation.core.tween(
                                    durationMillis = 280,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                )
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 2,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (LibraryTab.entries[page]) {
                            LibraryTab.BOOKMARKS -> Column(modifier = Modifier.fillMaxSize()) {
                                SearchBarAndFilters(
                                    searchQuery = state.searchQuery,
                                    selectedTradition = state.selectedTraditionFilter,
                                    onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                                    onTraditionSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                                )
                                BookmarksList(state.bookmarks, onEvent)
                            }
                            LibraryTab.REFLECTIONS -> Column(modifier = Modifier.fillMaxSize()) {
                                SearchBarAndFilters(
                                    searchQuery = state.searchQuery,
                                    selectedTradition = state.selectedTraditionFilter,
                                    onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                                    onTraditionSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                                )
                                ReflectionsList(state.notes, onEvent)
                            }
                            LibraryTab.HISTORY -> HistoryStatsView(
                                history = state.history,
                                totalCount = state.totalReadCount,
                                streakDays = state.currentStreakDays,
                                weeklyHeatmap = state.weeklyHeatmap,
                                milestoneBadges = state.milestoneBadges,
                                onEvent = onEvent
                            )
                        }
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

                state.selectedPassageForContext?.let { contextPassage ->
                    PassageContextSheet(
                        passage = contextPassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseContext) }
                    )
                }

                state.selectedPassageForNote?.let { notePassage ->
                    NoteEditDialog(
                        passage = notePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseNoteDialog) },
                        onSave = { content ->
                            onEvent(LibraryUiEvent.OnSaveNote(notePassage.id, content))
                        }
                    )
                }

                state.selectedPassageForShare?.let { sharePassage ->
                    QuoteCardModal(
                        passage = sharePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseShare) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SacredSegmentedTabBar(
    pagerState: androidx.compose.foundation.pager.PagerState,
    bookmarksCount: Int,
    notesCount: Int,
    onTabSelected: (LibraryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(36.dp)) {
            val segmentWidth = maxWidth / LibraryTab.entries.size
            val continuousPage = pagerState.currentPage + pagerState.currentPageOffsetFraction
            val indicatorOffset = segmentWidth * continuousPage
            val activeIndex = kotlin.math.round(continuousPage).toInt().coerceIn(0, LibraryTab.entries.size - 1)

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f))
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold,
                        shape = RoundedCornerShape(12.dp)
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LibraryTab.entries.forEachIndexed { index, tab ->
                    val isSelected = activeIndex == index
                    val label = when (tab) {
                        LibraryTab.BOOKMARKS -> "SAVED ($bookmarksCount)"
                        LibraryTab.REFLECTIONS -> "REFLECTIONS ($notesCount)"
                        LibraryTab.HISTORY -> "HISTORY"
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab) },
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
private fun SearchBarAndFilters(
    searchQuery: String,
    selectedTradition: TraditionId?,
    onQueryChange: (String) -> Unit,
    onTraditionSelect: (TraditionId?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                value = searchQuery,
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
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search saved wisdom & insights...",
                            fontFamily = JakartaFontFamily,
                            fontSize = 13.5.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                    innerTextField()
                }
            )

            if (searchQuery.isNotEmpty()) {
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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TraditionChip(
                label = "ALL",
                isSelected = selectedTradition == null,
                onClick = { onTraditionSelect(null) }
            )

            TraditionId.entries.forEach { tradition ->
                TraditionChip(
                    label = tradition.displayName.uppercase(),
                    isSelected = selectedTradition == tradition,
                    onClick = { onTraditionSelect(if (selectedTradition == tradition) null else tradition) }
                )
            }
        }
    }
}

@Composable
private fun TraditionChip(
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
private fun BookmarksList(
    bookmarks: List<BookmarkWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val context = LocalContext.current

    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Saved Passages",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the bookmark icon on any passage in the feed to save it to your sacred sanctuary.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 96.dp)
        ) {
            items(bookmarks, key = { it.bookmark.id }) { item ->
                val passage = item.passage
                var showTagEditor by remember { mutableStateOf(false) }

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
                        .clickable {
                            passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) }
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        passage?.let {
                            TraditionBadge(traditionId = it.traditionId)
                            Text(
                                text = it.canonicalReference,
                                style = FathomTheme.typography.canonicalHeader,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = { onEvent(LibraryUiEvent.OnRemoveBookmark(item.bookmark.passageId)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.TrashSimple,
                                contentDescription = "Remove Bookmark",
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = passage?.translation ?: "Passage text",
                        style = FathomTheme.typography.verseDisplay.copy(
                            fontSize = 16.5.sp,
                            lineHeight = 25.sp
                        ),
                        color = FathomTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.bookmark.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.bookmark.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    AnimatedVisibility(
                        visible = showTagEditor,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TagEditorSection(
                            currentTags = item.bookmark.tags,
                            onSaveTags = { newTags ->
                                onEvent(LibraryUiEvent.OnUpdateBookmarkTags(item.bookmark.passageId, newTags))
                                showTagEditor = false
                            }
                        )
                    }

                    SacredDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    passage?.let {
                                        copyToClipboard(context, "${it.translation}\n— ${it.canonicalReference}")
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Copy,
                                    contentDescription = "Copy Passage",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    passage?.let {
                                        onEvent(LibraryUiEvent.OnOpenShare(it))
                                    }
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
                                onClick = { showTagEditor = !showTagEditor },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Tag,
                                    contentDescription = "Tag Bookmark",
                                    tint = if (item.bookmark.tags.isNotEmpty()) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    passage?.let { onEvent(LibraryUiEvent.OnOpenNoteDialog(it)) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.NotePencil,
                                    contentDescription = "Write Reflection",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } },
                            contentPadding = PaddingValues(horizontal = 8.dp)
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TagEditorSection(
    currentTags: List<String>,
    onSaveTags: (List<String>) -> Unit
) {
    var tagInput by remember { mutableStateOf(currentTags.joinToString(", ")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FathomTheme.colors.surface)
            .padding(12.dp)
    ) {
        Text(
            text = "EDIT TAGS (COMMA SEPARATED)",
            style = FathomTheme.typography.caption.copy(
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = FathomTheme.colors.accentGold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JakartaFontFamily,
                    fontSize = 13.sp,
                    color = FathomTheme.colors.textPrimary
                ),
                cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = {
                    val parsed = tagInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    onSaveTags(parsed)
                }
            ) {
                Text(
                    text = "SAVE",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.accentGold
                )
            }
        }
    }
}

@Composable
private fun ReflectionsList(
    notes: List<NoteWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Personal Reflections",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the pencil icon on any saved passage to record your personal contemplations and marginalia notes.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 96.dp)
        ) {
            items(notes, key = { it.note.id }) { item ->
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
                        item.passage?.let {
                            TraditionBadge(traditionId = it.traditionId)
                            Text(
                                text = it.canonicalReference,
                                style = FathomTheme.typography.canonicalHeader,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = { onEvent(LibraryUiEvent.OnDeleteNote(item.note.id)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.TrashSimple,
                                contentDescription = "Delete Reflection",
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "“${item.passage?.translation ?: ""}”",
                        style = FathomTheme.typography.verseOriginal.copy(
                            fontSize = 14.5.sp,
                            lineHeight = 22.sp
                        ),
                        color = FathomTheme.colors.textTertiary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    SacredDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = item.note.content,
                        style = FathomTheme.typography.commentaryBody.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 15.sp,
                            lineHeight = 23.sp
                        ),
                        color = FathomTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        item.passage?.let { p ->
                            TextButton(
                                onClick = { onEvent(LibraryUiEvent.OnOpenContext(p)) },
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "OPEN CONTEXT",
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
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NoteEditDialog(
    passage: Passage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FathomTheme.colors.surface,
        title = {
            Column {
                Text(
                    text = "SACRED MARGINALIA",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = passage.canonicalReference,
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "“${passage.translation}”",
                    style = FathomTheme.typography.verseOriginal.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    color = FathomTheme.colors.textTertiary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    textStyle = TextStyle(
                        fontFamily = JakartaFontFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = FathomTheme.colors.textPrimary
                    ),
                    cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp, max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = FathomTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                text = "Write your contemplation, lesson, or marginalia note...",
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                        innerTextField()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        onSave(noteText)
                    }
                },
                enabled = noteText.isNotBlank()
            ) {
                Text(
                    text = "SAVE REFLECTION",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (noteText.isNotBlank()) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }
    )
}

@Composable
private fun HistoryStatsView(
    history: List<HistoryWithPassage>,
    totalCount: Int,
    streakDays: Int,
    weeklyHeatmap: List<HeatmapDay>,
    milestoneBadges: List<MilestoneBadge>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val context = LocalContext.current
    val traditionCounts = remember(history) {
        history.mapNotNull { it.passage?.traditionId }
            .groupingBy { it }
            .eachCount()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 20.dp, bottom = 96.dp)
    ) {
        item {
            Text(
                text = "CONTEMPLATIVE PRACTICE",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                FathomTheme.colors.accentGold.copy(alpha = 0.16f),
                                FathomTheme.colors.surfaceElevated
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FathomTheme.colors.accentGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Flame,
                            contentDescription = "Daily Streak Flame",
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$streakDays",
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = FathomTheme.colors.accentGold
                            )
                            Text(
                                text = if (streakDays == 1) "DAY STREAK" else "DAYS STREAK",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = FathomTheme.colors.accentGold
                            )
                        }
                        Text(
                            text = if (streakDays > 0) "Your flame of wisdom is burning bright" else "Contemplate a verse today to ignite streak",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CalendarCheck,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "WEEKLY HEATMAP",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 1.sp,
                                color = FathomTheme.colors.accentGold
                            )
                        }

                        Text(
                            text = "Last 7 Days",
                            fontFamily = JakartaFontFamily,
                            fontSize = 10.5.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weeklyHeatmap.forEach { day ->
                            val hasRead = day.readCount > 0
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = day.dayOfWeekLabel.uppercase(),
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = if (day.isToday) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                )

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                day.readCount >= 5 -> FathomTheme.colors.accentGold
                                                day.readCount >= 2 -> FathomTheme.colors.accentGold.copy(alpha = 0.65f)
                                                hasRead -> FathomTheme.colors.accentGold.copy(alpha = 0.30f)
                                                else -> FathomTheme.colors.surface
                                            }
                                        )
                                        .border(
                                            width = if (day.isToday) 1.5.dp else 1.dp,
                                            color = if (day.isToday) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${day.dayOfMonth}",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (day.readCount >= 2) Color.Black else FathomTheme.colors.textPrimary
                                    )
                                }

                                Text(
                                    text = if (hasRead) "${day.readCount}" else "·",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 9.sp,
                                    color = if (hasRead) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$totalCount",
                        style = FathomTheme.typography.brandTitle.copy(
                            fontFamily = SyneFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FathomTheme.colors.accentGold
                    )
                    Text(
                        text = "Total Contemplations",
                        style = FathomTheme.typography.caption.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.5.sp
                        ),
                        color = FathomTheme.colors.textSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${history.size}",
                        style = FathomTheme.typography.brandTitle.copy(
                            fontFamily = SyneFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FathomTheme.colors.textPrimary
                    )
                    Text(
                        text = "Unique Verses Explored",
                        style = FathomTheme.typography.caption.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.5.sp
                        ),
                        color = FathomTheme.colors.textSecondary
                    )
                }
            }

            if (milestoneBadges.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONTEMPLATIVE MILESTONES",
                        style = FathomTheme.typography.canonicalHeader,
                        color = FathomTheme.colors.accentGold
                    )
                    val unlockedCount = milestoneBadges.count { it.isUnlocked }
                    Text(
                        text = "$unlockedCount of ${milestoneBadges.size} unlocked",
                        fontFamily = JakartaFontFamily,
                        fontSize = 10.5.sp,
                        color = FathomTheme.colors.textTertiary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    milestoneBadges.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (badge.isUnlocked) FathomTheme.colors.surfaceElevated
                                    else FathomTheme.colors.surface.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.4f) else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                                        else FathomTheme.colors.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (badge.isUnlocked) PhosphorIcons.Regular.Trophy else PhosphorIcons.Regular.Medal,
                                    contentDescription = null,
                                    tint = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = badge.title,
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (badge.isUnlocked) FathomTheme.colors.textPrimary else FathomTheme.colors.textSecondary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.15f)
                                                else FathomTheme.colors.surfaceElevated
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    ) {
                                        Text(
                                            text = badge.tierName.uppercase(),
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.4.sp,
                                            color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = badge.description,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.5.sp,
                                    color = FathomTheme.colors.textTertiary,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val progRatio = (badge.currentCount.toFloat() / badge.requiredCount.toFloat()).coerceIn(0f, 1f)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    androidx.compose.material3.LinearProgressIndicator(
                                        progress = { progRatio },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = FathomTheme.colors.accentGold,
                                        trackColor = FathomTheme.colors.surface
                                    )
                                    Text(
                                        text = "${badge.currentCount}/${badge.requiredCount}",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp,
                                        color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (traditionCounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "CANONS EXPLORED",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    traditionCounts.forEach { (tradition, count) ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = tradition.displayName.uppercase(),
                                style = FathomTheme.typography.traditionBadge,
                                color = FathomTheme.colors.accentGold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CHRONOLOGICAL TIMELINE",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.ClockCounterClockwise,
                    contentDescription = null,
                    tint = FathomTheme.colors.textTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (history.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = FathomTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Reading History Yet",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Verses you contemplate in the daily stream and scriptures will appear here in chronological order.",
                            fontFamily = JakartaFontFamily,
                            fontSize = 13.sp,
                            color = FathomTheme.colors.textTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(history, key = { it.record.passageId }) { item ->
                val passage = item.passage
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
                        .clickable {
                            passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) }
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            passage?.let {
                                TraditionBadge(traditionId = it.traditionId)
                                Text(
                                    text = it.canonicalReference,
                                    style = FathomTheme.typography.canonicalHeader,
                                    color = FathomTheme.colors.textSecondary
                                )
                            } ?: Text(
                                text = item.record.passageId,
                                style = FathomTheme.typography.canonicalHeader,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        if (item.record.readCount > 1) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.14f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${item.record.readCount}× READ",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 0.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = passage?.translation ?: "Passage text",
                        style = FathomTheme.typography.verseDisplay.copy(
                            fontSize = 15.sp,
                            lineHeight = 23.sp
                        ),
                        color = FathomTheme.colors.textPrimary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    SacredDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    passage?.let {
                                        copyToClipboard(context, "${it.translation}\n— ${it.canonicalReference}")
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Copy,
                                    contentDescription = "Copy Passage",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    passage?.let {
                                        onEvent(LibraryUiEvent.OnOpenShare(it))
                                    }
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
                                    passage?.let { onEvent(LibraryUiEvent.OnOpenNoteDialog(it)) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.NotePencil,
                                    contentDescription = "Write Reflection",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } },
                            contentPadding = PaddingValues(horizontal = 8.dp)
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
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "OFFLINE INTEGRITY",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "100% On-Device Storage",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = FathomTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "All your bookmarks, notes, marginalia, and reading history reside in private local SQLite databases on your device.",
                    fontFamily = JakartaFontFamily,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Fathom Wisdom", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Passage copied to clipboard", Toast.LENGTH_SHORT).show()
}
