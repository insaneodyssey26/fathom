package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
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

            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = FathomTheme.colors.background,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "FATHOM",
                                style = FathomTheme.typography.canonicalHeader,
                                color = FathomTheme.colors.textPrimary
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = FathomTheme.colors.background
                        )
                    )
                }
            ) { innerPadding ->
                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) { page ->
                    val passage = state.passages[page]
                    PassageCard(
                        passage = passage,
                        isBookmarked = passage.id in state.bookmarkedPassageIds,
                        onToggleBookmark = { onEvent(FeedUiEvent.OnToggleBookmark(passage)) },
                        onOpenContext = { onEvent(FeedUiEvent.OnOpenContext(passage)) }
                    )
                }

                state.selectedPassageForContext?.let { contextPassage ->
                    PassageContextSheet(
                        passage = contextPassage,
                        onDismiss = { onEvent(FeedUiEvent.OnCloseContext) }
                    )
                }
            }
        }
    }
}
