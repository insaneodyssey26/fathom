package com.masum.fathom.ui.feed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Crown
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showScriptureSheet by remember { mutableStateOf(false) }
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current

    if (state is FeedUiState.Success) {
        if (state.selectedPassageForShare != null) {
            androidx.activity.compose.BackHandler { onEvent(FeedUiEvent.OnCloseShare) }
        } else if (state.selectedPassageForContext != null) {
            androidx.activity.compose.BackHandler { onEvent(FeedUiEvent.OnCloseContext) }
        } else if (showScriptureSheet) {
            androidx.activity.compose.BackHandler { showScriptureSheet = false }
        } else if (showFilterSheet) {
            androidx.activity.compose.BackHandler { showFilterSheet = false }
        }
    }

    when (state) {
        is FeedUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FathomTheme.colors.accentGold)
            }
        }

        is FeedUiState.Empty -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No wisdom passages found for the selected traditions or scriptures.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }

        is FeedUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.message,
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }

        is FeedUiState.Success -> {
            val coroutineScope = rememberCoroutineScope()
            val pagerState = rememberPagerState(
                initialPage = state.currentIndex,
                pageCount = { state.passages.size }
            )

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }
                    .collect { page ->
                        onEvent(FeedUiEvent.OnPassageSwiped(page))
                    }
            }

            val flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapPositionalThreshold = 0.35f,
                snapAnimationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 380,
                    easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 1.0f, 0.5f, 1.0f)
                )
            )

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 10.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "fathom",
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
                        val activeBook = state.availableBooks.find { it.id == state.selectedBookId }
                        val activeTitle = activeBook?.title ?: "All Scriptures"

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (state.selectedBookId != null) FathomTheme.colors.accentGold.copy(alpha = 0.7f) else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { showScriptureSheet = true }
                                .padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (state.selectedBookId != null) PhosphorIcons.Regular.BookBookmark else PhosphorIcons.Regular.Sparkle,
                                contentDescription = null,
                                tint = if (state.selectedBookId != null) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = activeTitle,
                                style = FathomTheme.typography.traditionBadge.copy(fontSize = 11.sp),
                                color = if (state.selectedBookId != null) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 135.dp)
                            )
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(11.dp)
                            )
                        }

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
                                .clickable { openPaywall() }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
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

                        IconButton(
                            onClick = { showFilterSheet = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Sliders,
                                contentDescription = "Tradition and Theme Filters",
                                tint = FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                VerticalPager(
                    state = pagerState,
                    flingBehavior = flingBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    val clampedOffset = pageOffset.coerceIn(-1f, 1f)
                    val absOffset = kotlin.math.abs(clampedOffset)

                    val passage = state.passages[page]

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                cameraDistance = 8f * density
                                rotationX = -clampedOffset * 42f
                                transformOrigin = TransformOrigin(
                                    pivotFractionX = 0.5f,
                                    pivotFractionY = if (clampedOffset > 0f) 0f else 1f
                                )
                                translationY = if (clampedOffset > 0f) clampedOffset * (size.height * 0.14f) else 0f
                                val scale = 1f - (absOffset * 0.08f)
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - (absOffset * 0.42f)
                            }
                    ) {
                        PassageCard(
                            passage = passage,
                            isBookmarked = passage.id in state.bookmarkedPassageIds,
                            onToggleBookmark = { onEvent(FeedUiEvent.OnToggleBookmark(passage)) },
                            onOpenContext = { onEvent(FeedUiEvent.OnOpenContext(passage)) },
                            onShare = { onEvent(FeedUiEvent.OnOpenShare(passage)) },
                            onScrollPrevious = if (page > 0) {
                                {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page - 1)
                                    }
                                }
                            } else null,
                            onScrollNext = if (page < state.passages.lastIndex) {
                                {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(page + 1)
                                    }
                                }
                            } else null,
                            hasPrevious = page > 0,
                            hasNext = page < state.passages.lastIndex
                        )
                    }
                }
            }

            state.selectedPassageForContext?.let { contextPassage ->
                PassageContextSheet(
                    passage = contextPassage,
                    onDismiss = { onEvent(FeedUiEvent.OnCloseContext) }
                )
            }

            state.selectedPassageForShare?.let { sharePassage ->
                com.masum.fathom.ui.components.QuoteCardModal(
                    passage = sharePassage,
                    onDismiss = { onEvent(FeedUiEvent.OnCloseShare) }
                )
            }

            if (showScriptureSheet) {
                ScriptureSelectorSheet(
                    availableBooks = state.availableBooks,
                    selectedBookId = state.selectedBookId,
                    onSelectBook = { onEvent(FeedUiEvent.OnSelectBook(it)) },
                    onDismiss = { showScriptureSheet = false }
                )
            }

            if (showFilterSheet) {
                TraditionFilterSheet(
                    selectedTraditions = state.selectedTraditions,
                    availableBooks = state.availableBooks,
                    selectedBookId = state.selectedBookId,
                    activeMode = state.activeMode,
                    activeTheme = state.activeTheme,
                    onToggleTradition = { onEvent(FeedUiEvent.OnToggleTradition(it)) },
                    onSelectBook = { onEvent(FeedUiEvent.OnSelectBook(it)) },
                    onChangeMode = { onEvent(FeedUiEvent.OnChangeMode(it)) },
                    onChangeTheme = { onEvent(FeedUiEvent.OnChangeTheme(it)) },
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}


