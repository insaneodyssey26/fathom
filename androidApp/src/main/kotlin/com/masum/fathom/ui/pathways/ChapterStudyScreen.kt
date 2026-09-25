package com.masum.fathom.ui.pathways

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Compass
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.X
import androidx.compose.runtime.collectAsState
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.launch

@Composable
fun ChapterStudyScreen(
    chapterNumber: Int,
    chapterTitle: String,
    chapterSubtitle: String,
    bookTitle: String,
    passages: List<Passage>,
    isLoading: Boolean,
    onCompleteChapter: () -> Unit,
    onOpenPassageContext: (Passage) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioEngine = LocalSacredAudioEngine.current
    val coroutineScope = rememberCoroutineScope()
    var showJumpModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                }
            } else if (passages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No verses available for this chapter.",
                        fontFamily = JakartaFontFamily,
                        fontSize = 14.sp,
                        color = FathomTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val pagerState = rememberPagerState(initialPage = 0, pageCount = { passages.size })
                val currentPage = pagerState.currentPage
                val totalPages = passages.size
                val targetProgress = ((currentPage + 1).toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surfaceElevated)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back to Pathway",
                            tint = FathomTheme.colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = bookTitle.uppercase(),
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = FathomTheme.colors.accentGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = chapterTitle,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = FathomTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val playbackState by audioEngine.playbackState.collectAsState()
                        val currentPassage = passages[currentPage]
                        val isPlayingCurrent = (playbackState as? SacredAudioState.Playing)?.passage?.id == currentPassage.id

                        IconButton(
                            onClick = {
                                audioEngine.toggle(currentPassage)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (isPlayingCurrent) FathomTheme.colors.accentGold.copy(alpha = 0.2f)
                                    else FathomTheme.colors.surfaceElevated
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isPlayingCurrent) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                    shape = CircleShape
                                )
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingCurrent) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.SpeakerHigh,
                                contentDescription = if (isPlayingCurrent) "Pause Verse Audio" else "Play Verse Audio",
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                SacredDivider()

                val animatedProgress by animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(
                        durationMillis = 350,
                        easing = FastOutSlowInEasing
                    ),
                    label = "chapter_progress_anim"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VERSE ${currentPage + 1} OF $totalPages",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            letterSpacing = 0.8.sp,
                            color = FathomTheme.colors.accentGold
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FathomTheme.colors.surfaceElevated)
                                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { showJumpModal = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Compass,
                                        contentDescription = null,
                                        tint = FathomTheme.colors.accentGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "JUMP",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        letterSpacing = 0.5.sp,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }
                            }

                            Text(
                                text = "${(targetProgress * 100).toInt()}%",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = FathomTheme.colors.accentGold,
                        trackColor = FathomTheme.colors.surfaceElevated
                    )

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                        pageSpacing = 0.dp
                    ) { page ->
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        val clampedOffset = pageOffset.coerceIn(-1f, 1f)
                        val absOffset = kotlin.math.abs(clampedOffset)

                        val passage = passages[page]

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    cameraDistance = 14f * density
                                    rotationY = -clampedOffset * 48f
                                    transformOrigin = TransformOrigin(
                                        pivotFractionX = if (clampedOffset > 0f) 0f else 1f,
                                        pivotFractionY = 0.5f
                                    )
                                    val scale = 1f - (absOffset * 0.08f)
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = 1f - (absOffset * 0.45f)
                                }
                        ) {
                            InteractiveWisdomCard(
                                passage = passage,
                                index = page + 1,
                                total = totalPages,
                                onOpenInfo = { onOpenPassageContext(passage) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = currentPage - 1,
                                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = FathomTheme.colors.textPrimary
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FathomTheme.colors.borderSubtle)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                                        contentDescription = "Previous Verse",
                                        modifier = Modifier.size(20.dp),
                                        tint = FathomTheme.colors.accentGold
                                    )
                                    Text(
                                        text = "PREVIOUS",
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }

                        if (currentPage < totalPages - 1) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(
                                            page = currentPage + 1,
                                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FathomTheme.colors.accentGold,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "CONTINUE",
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.ArrowRight,
                                        contentDescription = "Next Verse",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = onCompleteChapter,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FathomTheme.colors.accentGold,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Sparkle,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "MASTER CHAPTER",
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (showJumpModal) {
                    JumpToVerseModal(
                        totalVerses = totalPages,
                        currentVerseIndex = currentPage,
                        onSelectVerse = { targetIdx ->
                            coroutineScope.launch {
                                pagerState.scrollToPage(targetIdx)
                            }
                            showJumpModal = false
                        },
                        onDismiss = { showJumpModal = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun JumpToVerseModal(
    totalVerses: Int,
    currentVerseIndex: Int,
    onSelectVerse: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "JUMP TO VERSE",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = FathomTheme.colors.accentGold
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FathomTheme.colors.surface)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$totalVerses verses",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 10.sp,
                                        color = FathomTheme.colors.textTertiary
                                    )
                                }
                            }
                            Text(
                                text = "Select any verse number to navigate",
                                fontFamily = JakartaFontFamily,
                                fontSize = 11.5.sp,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surface)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.X,
                                contentDescription = "Close",
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val itemsList = (1..totalVerses).toList()
                    val gridState = rememberLazyGridState()

                    LaunchedEffect(currentVerseIndex) {
                        if (currentVerseIndex > 6) {
                            gridState.scrollToItem((currentVerseIndex - 4).coerceAtLeast(0))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 48.dp),
                            state = gridState,
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(itemsList) { verseNum ->
                                val isSelected = (verseNum - 1) == currentVerseIndex
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) FathomTheme.colors.accentGold
                                            else FathomTheme.colors.surface
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            onSelectVerse(verseNum - 1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$verseNum",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (isSelected) Color.Black else FathomTheme.colors.textPrimary
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

@Composable
private fun InteractiveWisdomCard(
    passage: Passage,
    index: Int,
    total: Int,
    onOpenInfo: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        FathomTheme.colors.surfaceElevated,
                        FathomTheme.colors.surface
                    )
                )
            )
            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = passage.canonicalReference.ifEmpty { "Verse $index" },
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = FathomTheme.colors.accentGold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onOpenInfo,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surface)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Info,
                            contentDescription = "Passage Info & Commentary",
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(FathomTheme.colors.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$index of $total",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                }
            }

            val origText = passage.originalText
            if (!origText.isNullOrBlank() && origText != passage.translation) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FathomTheme.colors.surface.copy(alpha = 0.6f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = origText,
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.5.sp,
                        color = FathomTheme.colors.textTertiary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "“${passage.translation}”",
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = FathomTheme.colors.textPrimary,
                lineHeight = 26.sp
            )

            if (passage.explanations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FathomTheme.colors.surface)
                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Lightbulb,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isExpanded) "Philosophical Exegesis" else "Tap to Reveal Exegesis",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) PhosphorIcons.Regular.CaretUp else PhosphorIcons.Regular.CaretDown,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        if (isExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                val exp = passage.explanations.first()
                                val expTitle = exp.title
                                if (!expTitle.isNullOrBlank()) {
                                    Text(
                                        text = expTitle,
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = FathomTheme.colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = exp.body,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 12.5.sp,
                                    color = FathomTheme.colors.textSecondary,
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
