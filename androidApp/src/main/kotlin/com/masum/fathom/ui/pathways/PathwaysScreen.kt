package com.masum.fathom.ui.pathways

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.feed.ScriptureSelectorSheet
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import kotlinx.coroutines.launch

@Composable
fun PathwaysScreen(
    state: PathwaysUiState,
    onEvent: (PathwaysUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val openSanctuary = LocalOpenSanctuary.current
    var showScriptureSheet by remember { mutableStateOf(false) }

    if (state is PathwaysUiState.Success) {
        if (state.selectedPassageForContext != null) {
            androidx.activity.compose.BackHandler { onEvent(PathwaysUiEvent.OnClosePassageContext) }
        } else if (state.activeChapterForReading != null) {
            androidx.activity.compose.BackHandler { onEvent(PathwaysUiEvent.OnCloseChapter) }
        } else if (showScriptureSheet) {
            androidx.activity.compose.BackHandler { showScriptureSheet = false }
        }
    }

    when (state) {
        is PathwaysUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FathomTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FathomTheme.colors.accentGold)
            }
        }

        is PathwaysUiState.Error -> {
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

        is PathwaysUiState.Success -> {
            val pathway = state.currentPathway
            val subscriptionState = com.masum.fathom.core.subscription.LocalSubscriptionState.current
            val openPaywall = com.masum.fathom.core.subscription.LocalOpenPaywall.current

            AnimatedContent(
                targetState = state.activeChapterForReading,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(240)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { -it / 3 },
                                    animationSpec = tween(250)
                                ) + fadeOut(tween(200))
                            )
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { -it / 3 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(tween(240)))
                            .togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(250)
                                ) + fadeOut(tween(200))
                            )
                    }
                },
                label = "chapter_study_transition",
                modifier = modifier.fillMaxSize()
            ) { activeChapterNumber ->
                if (activeChapterNumber != null) {
                    val chapterProg = pathway.chapters.find { it.chapterNumber == activeChapterNumber }
                    ChapterStudyScreen(
                        chapterNumber = activeChapterNumber,
                        chapterTitle = chapterProg?.title ?: "Chapter $activeChapterNumber",
                        chapterSubtitle = chapterProg?.subtitle ?: "",
                        bookTitle = state.selectedBook.title,
                        passages = state.chapterPassages,
                        isLoading = state.isLoadingChapter,
                        onCompleteChapter = { onEvent(PathwaysUiEvent.OnCompleteChapter(activeChapterNumber)) },
                        onOpenPassageContext = { onEvent(PathwaysUiEvent.OnOpenPassageContext(it)) },
                        onDismiss = { onEvent(PathwaysUiEvent.OnCloseChapter) }
                    )
                } else {
                    val listState = rememberLazyListState()
                    val scope = rememberCoroutineScope()
                    val activeChapterIndex = remember(pathway.chapters) {
                        pathway.chapters.indexOfFirst { it.isCurrent }
                    }
                    val showJumpToActive by remember(activeChapterIndex) {
                        derivedStateOf {
                            activeChapterIndex > 1 && listState.firstVisibleItemIndex < 1
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(FathomTheme.colors.background)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 10.dp, bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    Text(
                                        text = "path",
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
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(FathomTheme.colors.surfaceElevated)
                                            .border(
                                                width = 1.dp,
                                                color = FathomTheme.colors.accentGold.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                haptics.light()
                                                showScriptureSheet = true
                                            }
                                            .padding(horizontal = 12.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Regular.BookBookmark,
                                            contentDescription = null,
                                            tint = FathomTheme.colors.accentGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = state.selectedBook.title,
                                            style = FathomTheme.typography.traditionBadge.copy(fontSize = 11.sp),
                                            color = FathomTheme.colors.accentGold,
                                            maxLines = 1
                                        )
                                        Icon(
                                            imageVector = PhosphorIcons.Regular.CaretDown,
                                            contentDescription = null,
                                            tint = FathomTheme.colors.textTertiary,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            haptics.light()
                                            openSanctuary()
                                        },
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
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                contentPadding = PaddingValues(bottom = 120.dp, top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    PathwayOverviewCard(
                                        pathway = pathway,
                                        onOpenScripturePicker = { showScriptureSheet = true }
                                    )
                                }

                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "THE SACRED ROAD",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            letterSpacing = 1.5.sp,
                                            color = FathomTheme.colors.accentGold
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = PhosphorIcons.Regular.Crown,
                                                contentDescription = null,
                                                tint = FathomTheme.colors.accentGold,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "${pathway.completedChapters}/${pathway.totalChapters} Chapters",
                                                fontFamily = JakartaFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = FathomTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }

                                item {
                                    SacredRoadStream(
                                        chapters = pathway.chapters,
                                        onOpenChapter = { chNum ->
                                            haptics.light()
                                            onEvent(PathwaysUiEvent.OnOpenChapter(chNum))
                                        }
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = showJumpToActive,
                            enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 20.dp, bottom = 100.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                                        spotColor = FathomTheme.colors.accentGold.copy(alpha = 0.5f)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(FathomTheme.colors.surfaceElevated)
                                    .border(
                                        width = 1.dp,
                                        color = FathomTheme.colors.accentGold,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clickable {
                                        haptics.medium()
                                        scope.launch {
                                            val targetItem = if (subscriptionState.isPro) 2 else 3
                                            listState.animateScrollToItem(targetItem)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Compass,
                                    contentDescription = "Jump to Active Step",
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "ACTIVE STEP",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                        }
                    }
                }
            }

            if (showScriptureSheet) {
                ScriptureSelectorSheet(
                    availableBooks = state.availableBooks,
                    selectedBookId = state.selectedBook.id,
                    onSelectBook = { bookId ->
                        haptics.light()
                        if (bookId != null) {
                            onEvent(PathwaysUiEvent.OnSelectBook(bookId))
                        }
                        showScriptureSheet = false
                    },
                    onDismiss = {
                        haptics.light()
                        showScriptureSheet = false
                    }
                )
            }

            state.selectedPassageForContext?.let { contextPassage ->
                PassageContextSheet(
                    passage = contextPassage,
                    onDismiss = { onEvent(PathwaysUiEvent.OnClosePassageContext) }
                )
            }

            if (state.completionReward != null) {
                ChapterCelebrationModal(
                    reward = state.completionReward,
                    onDismiss = { onEvent(PathwaysUiEvent.OnDismissReward) }
                )
            }
        }
    }
}

@Composable
fun PathwayOverviewCard(
    pathway: BookPathway,
    onOpenScripturePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val traditionId = pathway.book.traditionId
    val progressPercent = pathway.progressPercent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
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
                color = FathomTheme.colors.accentGold.copy(alpha = 0.45f),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TraditionBadge(traditionId = traditionId)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.14f))
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${pathway.wisdomXp} WISDOM XP",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = pathway.book.title,
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FathomTheme.colors.textPrimary
                    )

                    val author = pathway.book.author
                    if (!author.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = author,
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${pathway.completedChapters} of ${pathway.totalChapters} Chapters Mastered",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }

                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = FathomTheme.colors.surfaceElevated,
                        strokeWidth = 5.dp
                    )
                    CircularProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier.fillMaxSize(),
                        color = FathomTheme.colors.accentGold,
                        strokeWidth = 5.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Text(
                            text = "ROAD",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 7.5.sp,
                            letterSpacing = 0.5.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SacredDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pathway.readVerses} of ${pathway.totalVerses} Verses Completed",
                    fontFamily = JakartaFontFamily,
                    fontSize = 11.5.sp,
                    color = FathomTheme.colors.textTertiary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .clickable { onOpenScripturePicker() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "CHANGE SCRIPTURE",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = FathomTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}
