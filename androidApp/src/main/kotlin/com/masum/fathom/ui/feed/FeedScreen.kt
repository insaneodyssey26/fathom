package com.masum.fathom.ui.feed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomTheme


@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

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
                    text = "No wisdom passages found for the selected traditions.",
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
                snapPositionalThreshold = 0.15f,
                snapAnimationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 420,
                    easing = androidx.compose.animation.core.CubicBezierEasing(0.22f, 1.0f, 0.36f, 1.0f)
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
                        .padding(horizontal = 24.dp)
                        .padding(top = 10.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FATHOM",
                            style = FathomTheme.typography.brandTitle,
                            color = FathomTheme.colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(FathomTheme.colors.accentGold)
                        )
                    }

                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surfaceElevated)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tradition and Theme Filters",
                            tint = FathomTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
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
                            onOpenContext = { onEvent(FeedUiEvent.OnOpenContext(passage)) }
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

            if (showFilterSheet) {
                TraditionFilterSheet(
                    selectedTraditions = state.selectedTraditions,
                    activeMode = state.activeMode,
                    activeTheme = state.activeTheme,
                    onToggleTradition = { onEvent(FeedUiEvent.OnToggleTradition(it)) },
                    onChangeMode = { onEvent(FeedUiEvent.OnChangeMode(it)) },
                    onChangeTheme = { onEvent(FeedUiEvent.OnChangeTheme(it)) },
                    onDismiss = { showFilterSheet = false }
                )
            }
        }
    }
}

