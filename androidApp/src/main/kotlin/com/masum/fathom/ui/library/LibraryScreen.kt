package com.masum.fathom.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.TrashSimple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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
                .padding(top = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = FathomTheme.typography.brandTitle,
                color = FathomTheme.colors.textPrimary
            )
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

                LaunchedEffect(state.selectedTab) {
                    if (pagerState.currentPage != state.selectedTab.ordinal) {
                        pagerState.animateScrollToPage(state.selectedTab.ordinal)
                    }
                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = FathomTheme.colors.background,
                    contentColor = FathomTheme.colors.accentGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = FathomTheme.colors.accentGold
                        )
                    },
                    divider = {}
                ) {
                    LibraryTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = tab.name,
                                    style = FathomTheme.typography.traditionBadge,
                                    color = if (pagerState.currentPage == index) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (LibraryTab.entries[page]) {
                        LibraryTab.BOOKMARKS -> BookmarksList(state.bookmarks, onEvent)
                        LibraryTab.REFLECTIONS -> ReflectionsPlaceholder()
                        LibraryTab.HISTORY -> HistoryStatsView(state.history, state.totalReadCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksList(
    bookmarks: List<BookmarkWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved passages yet.\nTap the bookmark icon on any passage in the feed to save it here.",
                style = FathomTheme.typography.commentaryBody,
                color = FathomTheme.colors.textTertiary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            items(bookmarks, key = { it.bookmark.id }) { item ->
                val passage = item.passage
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(FathomTheme.colors.surface)
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
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.TrashSimple,
                                contentDescription = "Remove Bookmark",
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = passage?.translation ?: "Passage text",
                        style = FathomTheme.typography.verseDisplay.copy(fontSize = 17.sp, lineHeight = 26.sp),
                        color = FathomTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ReflectionsPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Personal reflections and marginalia notes appear here.",
            style = FathomTheme.typography.commentaryBody,
            color = FathomTheme.colors.textTertiary
        )
    }
}

@Composable
private fun HistoryStatsView(
    history: List<com.masum.fathom.core.model.user.ReadingRecord>,
    totalCount: Int
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text(
            text = "EXPLORATION RECORD",
            style = FathomTheme.typography.canonicalHeader,
            color = FathomTheme.colors.accentGold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "$totalCount Verses Explored",
            style = FathomTheme.typography.commentaryTitle.copy(
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.3).sp
            ),
            color = FathomTheme.colors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${history.size} Unique Passages Encountered",
            style = FathomTheme.typography.commentaryBody.copy(
                fontFamily = JakartaFontFamily,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            color = FathomTheme.colors.textSecondary
        )
    }
}
