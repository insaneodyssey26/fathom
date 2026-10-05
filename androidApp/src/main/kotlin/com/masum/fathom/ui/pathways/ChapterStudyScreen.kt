package com.masum.fathom.ui.pathways

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.Pause as PauseFill
import com.adamglin.phosphoricons.light.ArrowLeft
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.ChatCenteredText
import com.adamglin.phosphoricons.light.SpeakerHigh
import com.adamglin.phosphoricons.light.Check
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.components.MeaningBlock
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.StateFrame
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.feed.VersePage
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    val tradition = passages.firstOrNull()?.traditionId

    when {
        isLoading -> StateFrame(modifier, traditionId = tradition) { BreathingMark() }
        passages.isEmpty() -> StateFrame(modifier, traditionId = tradition) {
            QuietMessage(
                title = "Nothing to read here",
                body = "No verses are available for this chapter yet.",
                action = "Back to the path",
                onAction = onDismiss
            )
        }
        else -> StudyContent(
            chapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            bookTitle = bookTitle,
            passages = passages,
            onCompleteChapter = onCompleteChapter,
            onOpenPassageContext = onOpenPassageContext,
            onDismiss = onDismiss,
            modifier = modifier
        )
    }
}

@Composable
private fun StudyContent(
    chapterNumber: Int,
    chapterTitle: String,
    bookTitle: String,
    passages: List<Passage>,
    onCompleteChapter: () -> Unit,
    onOpenPassageContext: (Passage) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier
) {
    val haptics = LocalFathomHaptics.current
    val colors = FathomTheme.colors
    val audioEngine = LocalSacredAudioEngine.current
    val scope = rememberCoroutineScope()
    var showJump by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { passages.size })
    val page = pagerState.currentPage
    val total = passages.size
    val tone = rememberTone(passages[page].traditionId)
    val playback by audioEngine.playbackState.collectAsState()
    val speaking = (playback as? SacredAudioState.Playing)?.passage?.id == passages[page].id
    val isLast = page == total - 1
    val progress by animateFloatAsState((page + 1f) / total, tween(420, easing = FathomMotion.Emphasized), label = "study_progress")

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = modifier.fillMaxSize(),
            background = {
                Atmosphere(
                    tone = tone,
                    modifier = Modifier.fillMaxSize(),
                    phase = { pagerState.currentPage + pagerState.currentPageOffsetFraction }
                ) {
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize().statusBarsPadding()
                    ) { index ->
                        val passage = passages[index]
                        val distance = { index - (pagerState.currentPage + pagerState.currentPageOffsetFraction) }
                        VersePage(
                            passage = passage,
                            reveal = { (1f - abs(distance()) * 1.1f).coerceIn(0f, 1f) },
                            contentPadding = PaddingValues(top = 148.dp, bottom = 128.dp),
                            endGutter = 28.dp,
                            showThemes = false,
                            footer = if (passage.explanations.isNotEmpty()) {
                                { MeaningBlock(passage = passage) }
                            } else null,
                            modifier = Modifier.graphicsLayer {
                                val d = distance().coerceIn(-1f, 1f)
                                translationX = -d * size.width * 0.08f
                                alpha = 1f - abs(d) * 0.9f
                            }
                        )
                    }
                }
            },
            overlay = {
                Column(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp).padding(top = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        GlassIconButton(
                            icon = PhosphorIcons.Light.ArrowLeft,
                            contentDescription = "Back to path",
                            onClick = {
                                haptics.light()
                                onDismiss()
                            }
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MonoLabel(text = "$bookTitle · ${chapterNumber.toString().padStart(2, '0')}", color = tone.hue)
                            Text(
                                text = chapterTitle,
                                style = FathomTheme.typography.title,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        GlassIconButton(
                            icon = if (speaking) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Light.SpeakerHigh,
                            contentDescription = if (speaking) "Pause recitation" else "Listen",
                            tint = if (speaking) tone.hue else colors.textPrimary,
                            onClick = {
                                haptics.light()
                                audioEngine.toggle(passages[page])
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .tappable(scale = 0.95f) { showJump = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                MonoLabel(text = "Verse ${page + 1} / $total", color = colors.textSecondary)
                                Icon(PhosphorIcons.Light.CaretDown, null, tint = colors.textTertiary, modifier = Modifier.size(12.dp))
                            }
                        }
                        MonoLabel(text = "${(progress * 100).toInt()}%", color = colors.textTertiary)
                    }
                    Box(Modifier.fillMaxWidth().padding(top = 10.dp).height(2.dp).background(colors.hairline)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(tone.hue))
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                        .widthIn(max = 640.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (page > 0) {
                        GlassIconButton(
                            icon = PhosphorIcons.Light.ArrowLeft,
                            contentDescription = "Previous verse",
                            size = 56.dp,
                            onClick = {
                                haptics.light()
                                scope.launch { pagerState.animateScrollToPage(page - 1, animationSpec = tween(420, easing = FathomMotion.Emphasized)) }
                            }
                        )
                    }
                    GlassIconButton(
                        icon = PhosphorIcons.Light.ChatCenteredText,
                        contentDescription = "Context and commentary",
                        size = 56.dp,
                        onClick = {
                            haptics.light()
                            onOpenPassageContext(passages[page])
                        }
                    )
                    FathomButton(
                        text = if (isLast) "Complete chapter" else "Next verse",
                        onClick = {
                            if (isLast) {
                                haptics.success()
                                onCompleteChapter()
                            } else {
                                scope.launch { pagerState.animateScrollToPage(page + 1, animationSpec = tween(420, easing = FathomMotion.Emphasized)) }
                            }
                        },
                        trailing = if (isLast) PhosphorIcons.Light.Check else PhosphorIcons.Light.ArrowRight,
                        style = ButtonStyle.Tone,
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                }
            }
        )
    }

    if (showJump) {
        CompositionLocalProvider(LocalTone provides tone) {
            JumpSheet(
                total = total,
                current = page,
                onSelect = { target ->
                    scope.launch { pagerState.scrollToPage(target) }
                    showJump = false
                },
                onDismiss = { showJump = false }
            )
        }
    }
}

@Composable
private fun JumpSheet(total: Int, current: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val haptics = LocalFathomHaptics.current
    val gridState: LazyGridState = rememberLazyGridState()

    LaunchedEffect(current) {
        if (current > 12) gridState.scrollToItem((current - 8).coerceAtLeast(0))
    }

    FathomSheet(onDismiss = onDismiss) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(
            overline = "Navigate",
            title = "Jump to a verse",
            subtitle = "$total verses in this chapter",
            onClose = onDismiss
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            state = gridState,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(max = 320.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items((1..total).toList()) { number ->
                val selected = number - 1 == current
                val passed = number - 1 < current
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            when {
                                selected -> tone.hue
                                passed -> tone.hue.copy(alpha = 0.14f)
                                else -> colors.surfaceElevated
                            }
                        )
                        .tappable(scale = 0.92f, haptic = false) {
                            haptics.light()
                            closeSheet { onSelect(number - 1) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        style = FathomTheme.typography.label,
                        color = if (selected) tone.onHue else if (passed) tone.hue else colors.textPrimary
                    )
                }
            }
        }
    }
}
