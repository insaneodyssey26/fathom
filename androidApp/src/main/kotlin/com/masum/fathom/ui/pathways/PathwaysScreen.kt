package com.masum.fathom.ui.pathways

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowDown
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.Check
import com.adamglin.phosphoricons.light.Sliders
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.DockedTopPanel
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.Glyph
import com.masum.fathom.ui.design.GlyphKind
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.NavToneEffect
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.ScreenHeader
import com.masum.fathom.ui.design.StateFrame
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.feed.ScriptureSelectorSheet
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PathwaysScreen(
    state: PathwaysUiState,
    onEvent: (PathwaysUiEvent) -> Unit,
    onOpenChapter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    var showScriptureSheet by remember { mutableStateOf(false) }

    when (state) {
        is PathwaysUiState.Loading -> StateFrame(modifier) { BreathingMark() }
        is PathwaysUiState.Error -> StateFrame(modifier) { QuietMessage(title = "Something went quiet", body = state.message) }
        is PathwaysUiState.Success -> {
            if (showScriptureSheet) BackHandler { showScriptureSheet = false }

            Box(modifier.fillMaxSize()) {
                PathwaysHome(
                    state = state,
                    onOpenChapter = onOpenChapter,
                    onOpenScriptures = { showScriptureSheet = true }
                )
            }

            if (showScriptureSheet) {
                ScriptureSelectorSheet(
                    availableBooks = state.availableBooks,
                    selectedBookId = state.selectedBook.id,
                    onSelectBook = { bookId ->
                        haptics.light()
                        if (bookId != null) onEvent(PathwaysUiEvent.OnSelectBook(bookId))
                        showScriptureSheet = false
                    },
                    onDismiss = {
                        haptics.light()
                        showScriptureSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun PathwaysChapterScreen(
    state: PathwaysUiState,
    chapterNumber: Int,
    onEvent: (PathwaysUiEvent) -> Unit,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val success = state as? PathwaysUiState.Success
    val lastPassages = remember { mutableStateOf(emptyList<com.masum.fathom.core.model.Passage>()) }
    if (success != null && success.chapterPassages.isNotEmpty()) lastPassages.value = success.chapterPassages
    val lastTitle = remember { mutableStateOf("") }
    if (success != null) lastTitle.value = success.selectedBook.title

    if (success == null) {
        StateFrame(modifier) { BreathingMark() }
        return
    }

    val chapter = success.currentPathway.chapters.find { it.chapterNumber == chapterNumber }
    ChapterStudyScreen(
        chapterNumber = chapterNumber,
        chapterTitle = chapter?.title ?: "Chapter $chapterNumber",
        chapterSubtitle = chapter?.subtitle ?: "",
        bookTitle = lastTitle.value,
        passages = success.chapterPassages.ifEmpty { if (success.isLoadingChapter) emptyList() else lastPassages.value },
        isLoading = success.isLoadingChapter && lastPassages.value.isEmpty(),
        onCompleteChapter = onComplete,
        onOpenPassageContext = { onEvent(PathwaysUiEvent.OnOpenPassageContext(it)) },
        onDismiss = onBack,
        modifier = modifier
    )
}

@Composable
fun PathwaysOverlays(state: PathwaysUiState, onEvent: (PathwaysUiEvent) -> Unit) {
    val success = state as? PathwaysUiState.Success ?: return
    if (success.selectedPassageForContext != null) BackHandler { onEvent(PathwaysUiEvent.OnClosePassageContext) }
    success.selectedPassageForContext?.let {
        PassageContextSheet(passage = it, onDismiss = { onEvent(PathwaysUiEvent.OnClosePassageContext) })
    }
    success.completionReward?.let {
        ChapterCelebrationModal(reward = it, onDismiss = { onEvent(PathwaysUiEvent.OnDismissReward) })
    }
}

@Composable
private fun PathwaysHome(
    state: PathwaysUiState.Success,
    onOpenChapter: (Int) -> Unit,
    onOpenScriptures: () -> Unit
) {
    val pathway = state.currentPathway
    val tone = rememberTone(pathway.book.traditionId)
    NavToneEffect(pathway.book.traditionId)
    val shell = LocalShell.current
    val haptics = LocalFathomHaptics.current
    val openSanctuary = LocalOpenSanctuary.current
    val colors = FathomTheme.colors
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val currentIndex = remember(pathway.chapters) { pathway.chapters.indexOfFirst { it.isCurrent } }

    CompositionLocalProvider(LocalTone provides tone) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val wide = maxWidth >= 840.dp
            val heroInList = if (wide) 0 else 1
            var panelHeight by remember { androidx.compose.runtime.mutableIntStateOf(0) }
            val topPad = with(androidx.compose.ui.platform.LocalDensity.current) { panelHeight.toDp() } + 24.dp
            val bottomPad = shell.bottomInset + 28.dp

            val jumpVisible by remember(currentIndex, heroInList) {
                derivedStateOf {
                    currentIndex >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.index == currentIndex + heroInList }
                }
            }

            LaunchedEffect(pathway.book.id, currentIndex) {
                if (currentIndex > 1) {
                    kotlinx.coroutines.delay(500)
                    listState.animateScrollToItem(currentIndex + heroInList, scrollOffset = -220)
                }
            }

            GlassLayer(
                modifier = Modifier.fillMaxSize(),
                background = {
                    Atmosphere(
                        tone = tone,
                        modifier = Modifier.fillMaxSize(),
                        phase = { listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 700f }
                    ) {
                        if (wide) {
                            Row(Modifier.fillMaxSize().padding(start = shell.startInset)) {
                                Column(
                                    modifier = Modifier
                                        .weight(0.42f)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())
                                        .padding(start = 32.dp, end = 24.dp, top = topPad, bottom = bottomPad)
                                ) {
                                    BookHero(pathway = pathway, onContinue = { openCurrent(pathway, onOpenChapter) })
                                }
                                ChapterList(
                                    pathway = pathway,
                                    listState = listState,
                                    onOpenChapter = onOpenChapter,
                                    includeHero = false,
                                    contentPadding = PaddingValues(top = topPad, bottom = bottomPad, end = 32.dp, start = 8.dp),
                                    modifier = Modifier.weight(0.58f).fillMaxHeight()
                                )
                            }
                        } else {
                            ChapterList(
                                pathway = pathway,
                                listState = listState,
                                onOpenChapter = onOpenChapter,
                                includeHero = true,
                                contentPadding = PaddingValues(top = topPad, bottom = bottomPad),
                                modifier = Modifier.fillMaxSize().padding(start = shell.startInset)
                            )
                        }
                    }
                },
                overlay = {
                    DockedTopPanel(onHeight = { panelHeight = it }, bottomPadding = 14.dp) {
                        ScreenHeader(
                            title = "paths",
                            traditionId = pathway.book.traditionId
                        ) {
                            Row(
                                modifier = Modifier
                                    .widthIn(max = 190.dp)
                                    .height(44.dp)
                                    .glass(RoundedCornerShape(50))
                                    .tappable(scale = 0.96f) {
                                        haptics.light()
                                        onOpenScriptures()
                                    }
                                    .padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.selectedBook.title,
                                    style = FathomTheme.typography.label,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Icon(PhosphorIcons.Light.CaretDown, null, tint = colors.textSecondary, modifier = Modifier.size(13.dp))
                            }
                            GlassIconButton(
                                icon = PhosphorIcons.Light.Sliders,
                                contentDescription = "Settings",
                                onClick = {
                                    haptics.light()
                                    openSanctuary()
                                }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = jumpVisible,
                        enter = fadeIn(tween(220)) + scaleIn(tween(300, easing = FathomMotion.Emphasized), initialScale = 0.85f),
                        exit = fadeOut(tween(160)) + scaleOut(tween(200), targetScale = 0.9f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = shell.startInset, bottom = shell.bottomInset + 14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .height(48.dp)
                                .glass(RoundedCornerShape(50))
                                .tappable {
                                    haptics.medium()
                                    scope.launch { listState.animateScrollToItem(currentIndex + heroInList, scrollOffset = -220) }
                                }
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(PhosphorIcons.Light.ArrowDown, null, tint = tone.hue, modifier = Modifier.size(16.dp))
                            Text(text = "Where you left off", style = FathomTheme.typography.label, color = colors.textPrimary)
                        }
                    }
                }
            )
        }
    }
}

private fun openCurrent(pathway: BookPathway, onOpenChapter: (Int) -> Unit) {
    val target = pathway.chapters.firstOrNull { it.isCurrent }
        ?: pathway.chapters.firstOrNull { !it.isCompleted }
        ?: pathway.chapters.firstOrNull()
    target?.let { onOpenChapter(it.chapterNumber) }
}

@Composable
private fun ChapterList(
    pathway: BookPathway,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onOpenChapter: (Int) -> Unit,
    includeHero: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val shell = LocalShell.current
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        if (includeHero) {
            item(key = "hero") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Column(Modifier.widthIn(max = shell.contentMaxWidth).padding(horizontal = 28.dp).padding(bottom = 36.dp)) {
                        BookHero(pathway = pathway, onContinue = { openCurrent(pathway, onOpenChapter) })
                    }
                }
            }
        }
        itemsIndexed(pathway.chapters, key = { _, c -> c.chapterNumber }) { index, chapter ->
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                ChapterRow(
                    chapter = chapter,
                    previous = pathway.chapters.getOrNull(index - 1),
                    isFirst = index == 0,
                    isLast = index == pathway.chapters.lastIndex,
                    onClick = { onOpenChapter(chapter.chapterNumber) },
                    modifier = Modifier.widthIn(max = shell.contentMaxWidth).padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun BookHero(pathway: BookPathway, onContinue: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val started = pathway.completedChapters > 0 || pathway.readVerses > 0
    val finished = pathway.totalChapters > 0 && pathway.completedChapters >= pathway.totalChapters
    val current = pathway.chapters.firstOrNull { it.isCurrent }

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        MonoLabel(text = pathway.book.traditionId.displayName, color = tone.hue)
        Spacer(Modifier.height(14.dp))
        Text(
            text = pathway.book.title,
            style = typography.display,
            color = colors.textPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        pathway.book.author?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(10.dp))
            Text(text = it, style = typography.commentaryBody.copy(fontStyle = FontStyle.Italic), color = colors.textSecondary)
        }

        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            DepthDial(progress = pathway.progressPercent, hue = tone.hue)
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Stat(value = "${pathway.completedChapters}/${pathway.totalChapters}", label = "Chapters")
                Stat(value = "${pathway.readVerses}", label = "of ${pathway.totalVerses} verses")
            }
        }

        Spacer(Modifier.height(32.dp))

        FathomButton(
            text = when {
                finished -> "Read again"
                started && current != null -> "Continue chapter ${current.chapterNumber}"
                else -> "Begin the path"
            },
            onClick = onContinue,
            trailing = PhosphorIcons.Light.ArrowRight,
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = value, style = FathomTheme.typography.headline, color = FathomTheme.colors.textPrimary)
        MonoLabel(text = label)
    }
}

@Composable
private fun DepthDial(progress: Float, hue: Color) {
    val colors = FathomTheme.colors
    val sweep = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        sweep.animateTo(progress.coerceIn(0f, 1f), tween(1400, 200, FathomMotion.Emphasized))
    }
    val ticks = 64
    Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outer = size.minDimension / 2f
            val filled = (sweep.value * ticks)
            for (i in 0 until ticks) {
                val angle = (i.toFloat() / ticks) * 2f * Math.PI.toFloat() - Math.PI.toFloat() / 2f
                val long = i % 8 == 0
                val inner = outer - if (long) 14.dp.toPx() else 8.dp.toPx()
                val dir = Offset(kotlin.math.cos(angle), kotlin.math.sin(angle))
                val on = i < filled
                drawLine(
                    color = if (on) hue else colors.hairline.copy(alpha = if (long) 1f else 0.8f),
                    start = center + dir * inner,
                    end = center + dir * outer,
                    strokeWidth = if (long) 2.dp.toPx() else 1.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = "${(sweep.value * 100).roundToInt()}",
                style = FathomTheme.typography.numeral.copy(fontSize = 40.sp, lineHeight = 40.sp),
                color = colors.textPrimary
            )
            Text(
                text = "%",
                style = FathomTheme.typography.mono,
                color = colors.textTertiary,
                modifier = Modifier.padding(top = 5.dp, start = 2.dp)
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: ChapterProgress,
    previous: ChapterProgress?,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val done = chapter.isCompleted
    val current = chapter.isCurrent
    val upcoming = !done && !current
    val above = if (previous?.isCompleted == true) tone.hue else colors.hairline
    val below = if (done) tone.hue else colors.hairline

    val railWidth = 56.dp
    val nodeY = 40.dp
    val ticksColor = if (done) tone.hue.copy(alpha = 0.55f) else colors.textTertiary.copy(alpha = 0.28f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 118.dp)
            .tappable(scale = 0.985f, onClick = onClick)
            .drawRailBehind(
                railWidthDp = railWidth,
                nodeYDp = nodeY,
                isFirst = isFirst,
                isLast = isLast,
                above = above,
                below = below,
                ticks = ticksColor
            )
    ) {
        Box(Modifier.width(railWidth).height(nodeY * 2), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.padding(top = nodeY - 14.dp).size(28.dp), contentAlignment = Alignment.Center) {
                Glyph(
                    kind = when (chapter.nodeType) {
                        MilestoneNodeType.GATEWAY -> GlyphKind.Square
                        MilestoneNodeType.DISCIPLE -> GlyphKind.Circle
                        MilestoneNodeType.SANCTUARY -> GlyphKind.Hexagon
                        MilestoneNodeType.TEMPLE -> GlyphKind.Star
                    },
                    color = when {
                        done -> tone.hue
                        current -> tone.hue
                        else -> colors.textTertiary
                    },
                    size = if (current) 24.dp else 20.dp,
                    filled = done || current
                )
                if (done) {
                    Icon(PhosphorIcons.Light.Check, null, tint = tone.onHue, modifier = Modifier.size(11.dp))
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(top = nodeY - 16.dp, bottom = 28.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                MonoLabel(text = "Chapter ${chapter.chapterNumber.toString().padStart(2, '0')}", color = if (current) tone.hue else colors.textTertiary)
                when {
                    done -> MonoLabel(text = "Complete", color = tone.hue.copy(alpha = 0.8f))
                    current -> MonoLabel(text = "In progress", color = colors.textSecondary)
                }
            }
            Text(
                text = chapter.title,
                style = typography.title.copy(fontSize = if (current) 25.sp else 22.sp, lineHeight = if (current) 30.sp else 27.sp),
                color = if (upcoming) colors.textSecondary else colors.textPrimary
            )
            if (chapter.subtitle.isNotBlank()) {
                Text(
                    text = chapter.subtitle,
                    style = typography.commentaryBody.copy(fontFamily = ScriptureFamily, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 22.sp),
                    color = colors.textTertiary
                )
            }
            if (current && chapter.completedVerses > 0) {
                Spacer(Modifier.height(4.dp))
                VerseProgress(done = chapter.completedVerses, total = chapter.totalVerses)
            } else {
                MonoLabel(text = "${chapter.totalVerses} verses", color = colors.textTertiary.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun VerseProgress(done: Int, total: Int) {
    val tone = FathomTheme.tone
    val colors = FathomTheme.colors
    val fraction = if (total > 0) done.toFloat() / total else 0f
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(colors.hairline)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(tone.hue))
        }
        MonoLabel(text = "$done of $total verses", color = colors.textSecondary)
    }
}

private fun Modifier.drawRailBehind(
    railWidthDp: androidx.compose.ui.unit.Dp,
    nodeYDp: androidx.compose.ui.unit.Dp,
    isFirst: Boolean,
    isLast: Boolean,
    above: Color,
    below: Color,
    ticks: Color
): Modifier = this.then(
    Modifier.drawBehind {
        val x = railWidthDp.toPx() / 2f
        val nodeY = nodeYDp.toPx()
        val gap = 18.dp.toPx()
        val stroke = 1.5.dp.toPx()
        if (!isFirst) drawLine(above, Offset(x, 0f), Offset(x, nodeY - gap), strokeWidth = stroke, cap = StrokeCap.Round)
        if (!isLast) drawLine(below, Offset(x, nodeY + gap), Offset(x, size.height), strokeWidth = stroke, cap = StrokeCap.Round)
        var y = nodeY + gap + 8.dp.toPx()
        var i = 0
        while (y < size.height - 4.dp.toPx() && !isLast) {
            val len = if (i % 4 == 3) 9.dp.toPx() else 5.dp.toPx()
            drawLine(ticks, Offset(x - 6.dp.toPx(), y), Offset(x - 6.dp.toPx() - len, y), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
            y += 10.dp.toPx()
            i++
        }
    }
)
