package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.BookmarkSimple as BookmarkSimpleFill
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.BookmarkSimple
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.Warning
import com.adamglin.phosphoricons.regular.X
import com.adamglin.phosphoricons.regular.Waves
import com.masum.fathom.core.audio.LocalOpenAmbience
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily

@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val haptics = com.masum.fathom.core.haptics.LocalFathomHaptics.current
    val openSanctuary = com.masum.fathom.core.sanctuary.LocalOpenSanctuary.current
    val openAmbience = LocalOpenAmbience.current
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current
    val audioEngine = LocalSacredAudioEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()
    val ambienceEngine = LocalSanctuaryAmbienceEngine.current
    val ambienceState by ambienceEngine.state.collectAsState()

    var showScriptureSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showBetaDialog by remember { mutableStateOf(false) }

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

        is FeedUiState.Empty -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No verses found. Adjust your filters or traditions.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }

        is FeedUiState.Success -> {
            val pagerState = rememberPagerState(
                initialPage = state.currentIndex.coerceIn(0, (state.passages.size - 1).coerceAtLeast(0)),
                pageCount = { state.passages.size }
            )

            LaunchedEffect(state.currentIndex) {
                if (state.currentIndex in state.passages.indices && pagerState.currentPage != state.currentIndex) {
                    pagerState.scrollToPage(state.currentIndex)
                }
            }

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    if (page in state.passages.indices) {
                        onEvent(FeedUiEvent.OnPassageSwiped(page))
                    }
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

            val currentPassage = state.passages.getOrNull(pagerState.currentPage)
            val isCurrentBookmarked = currentPassage?.let { it.id in state.bookmarkedPassageIds } ?: false
            val isSpeakingCurrent = (playbackState as? SacredAudioState.Playing)?.passage?.id == currentPassage?.id
            val hasPrevious = pagerState.currentPage > 0
            val hasNext = pagerState.currentPage < state.passages.lastIndex

            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FathomTheme.colors.surfaceElevated)
                                    .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptics.light()
                                        showBetaDialog = true
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Warning,
                                        contentDescription = null,
                                        tint = FathomTheme.colors.accentGold,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "BETA",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.5.sp,
                                        letterSpacing = 0.5.sp,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }
                            }
                        }

                        val activeBook = state.availableBooks.find { it.id == state.selectedBookId }
                        val activeTitle = when {
                            activeBook != null -> activeBook.title
                            state.selectedTraditions.size == 1 -> state.selectedTraditions.first().displayName
                            state.selectedTraditions.size < TraditionId.entries.size -> "Filtered (${state.selectedTraditions.size})"
                            else -> "All Scriptures"
                        }
                        val isFiltered = state.selectedBookId != null || (state.selectedTraditions.isNotEmpty() && state.selectedTraditions.size < TraditionId.entries.size)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isFiltered) FathomTheme.colors.accentGold.copy(alpha = 0.7f) else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    haptics.light()
                                    showScriptureSheet = true
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFiltered) PhosphorIcons.Regular.BookBookmark else PhosphorIcons.Regular.Sparkle,
                                contentDescription = null,
                                tint = if (isFiltered) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = activeTitle,
                                style = FathomTheme.typography.traditionBadge.copy(fontSize = 11.sp),
                                color = if (isFiltered) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(11.dp)
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
                            PassageCard(passage = passage)
                        }
                    }
                }

                if (currentPassage != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
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
                                    color = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (hasPrevious) {
                                            haptics.light()
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    },
                                    enabled = hasPrevious,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.CaretUp,
                                        contentDescription = "Previous Verse",
                                        tint = if (hasPrevious) FathomTheme.colors.textSecondary else FathomTheme.colors.textTertiary.copy(alpha = 0.35f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (hasNext) {
                                            haptics.light()
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    },
                                    enabled = hasNext,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.CaretDown,
                                        contentDescription = "Next Verse",
                                        tint = if (hasNext) FathomTheme.colors.textSecondary else FathomTheme.colors.textTertiary.copy(alpha = 0.35f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(FathomTheme.colors.borderSubtle)
                                )

                                IconButton(
                                    onClick = {
                                        haptics.medium()
                                        audioEngine.toggle(currentPassage)
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.SpeakerHigh,
                                        contentDescription = "Audio Recitation",
                                        tint = if (isSpeakingCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        openAmbience()
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Waves,
                                        contentDescription = "Soundscapes & Sleep Timer",
                                        tint = if (ambienceState.isPlaying) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        onEvent(FeedUiEvent.OnOpenShare(currentPassage))
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.ShareNetwork,
                                        contentDescription = "Share Quote Card",
                                        tint = FathomTheme.colors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptics.light()
                                        onEvent(FeedUiEvent.OnOpenContext(currentPassage))
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Info,
                                        contentDescription = "Context & Commentary",
                                        tint = FathomTheme.colors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (!isCurrentBookmarked) {
                                            haptics.success()
                                        } else {
                                            haptics.light()
                                        }
                                        onEvent(FeedUiEvent.OnToggleBookmark(currentPassage))
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentBookmarked) PhosphorIcons.Fill.BookmarkSimpleFill else PhosphorIcons.Regular.BookmarkSimple,
                                        contentDescription = "Bookmark",
                                        tint = if (isCurrentBookmarked) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
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
                        selectedTraditions = state.selectedTraditions,
                        onSelectBook = { onEvent(FeedUiEvent.OnSelectBook(it)) },
                        onSelectTradition = { onEvent(FeedUiEvent.OnSelectTradition(it)) },
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

                if (showBetaDialog) {
                    Dialog(
                        onDismissRequest = { showBetaDialog = false }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(FathomTheme.colors.surfaceElevated)
                                .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                .padding(24.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.45f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Warning,
                                        contentDescription = null,
                                        tint = FathomTheme.colors.accentGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = "Closed Beta Preview",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = FathomTheme.colors.textPrimary,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Fathom is currently in closed beta testing. You might encounter bugs, incomplete features, or ongoing UI polish as updates are released.\n\nAll features are completely open and free for testing. Thank you so much for testing Fathom!",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = FathomTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Button(
                                    onClick = {
                                        haptics.light()
                                        showBetaDialog = false
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FathomTheme.colors.accentGold,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        text = "Understood",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
