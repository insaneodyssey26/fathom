package com.masum.fathom.ui.feed

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.BookmarkSimple as BookmarkFilled
import com.adamglin.phosphoricons.fill.Pause as PauseFill
import com.adamglin.phosphoricons.fill.Play as PlayFill
import com.adamglin.phosphoricons.light.BookmarkSimple
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.ChatCenteredText
import com.adamglin.phosphoricons.light.ShareFat
import com.adamglin.phosphoricons.light.SpeakerHigh
import com.adamglin.phosphoricons.light.DotsThree
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.audio.LocalOpenAmbience
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.ScreenHeader
import com.masum.fathom.ui.design.StateFrame
import com.masum.fathom.ui.design.DepthGauge
import com.masum.fathom.ui.design.Equalizer
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.NavToneEffect
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FeedScreen(
    state: FeedUiState,
    onEvent: (FeedUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showScriptureSheet by remember { mutableStateOf(false) }
    var showBetaNote by remember { mutableStateOf(false) }

    when (state) {
        is FeedUiState.Loading -> StateFrame(modifier) {
            BreathingMark()
        }

        is FeedUiState.Error -> StateFrame(modifier) {
            QuietMessage(title = "Something went quiet", body = state.message)
        }

        is FeedUiState.Empty -> StateFrame(modifier) {
            QuietMessage(
                title = "Nothing here yet",
                body = "These filters leave no verses to show.",
                action = "Show every scripture",
                onAction = {
                    onEvent(FeedUiEvent.OnSelectBook(null))
                    onEvent(FeedUiEvent.OnSelectTradition(null))
                }
            )
        }

        is FeedUiState.Success -> FeedContent(
            state = state,
            onEvent = onEvent,
            modifier = modifier,
            onOpenScriptures = { showScriptureSheet = true },
            onOpenBeta = { showBetaNote = true }
        )
    }

    if (state is FeedUiState.Success) {
        state.selectedPassageForContext?.let {
            PassageContextSheet(passage = it, onDismiss = { onEvent(FeedUiEvent.OnCloseContext) })
        }
        state.selectedPassageForShare?.let {
            QuoteCardModal(passage = it, onDismiss = { onEvent(FeedUiEvent.OnCloseShare) })
        }
        if (showScriptureSheet) {
            ScriptureSelectorSheet(
                availableBooks = state.availableBooks,
                selectedBookId = state.selectedBookId,
                selectedTraditions = state.selectedTraditions,
                alignedTraditions = state.alignedTraditions,
                onSelectBook = { onEvent(FeedUiEvent.OnSelectBook(it)) },
                onSelectTradition = { onEvent(FeedUiEvent.OnSelectTradition(it)) },
                onToggleAlignedTradition = { onEvent(FeedUiEvent.OnToggleAlignedTradition(it)) },
                onClearAlignedTraditions = { onEvent(FeedUiEvent.OnClearAlignedTraditions) },
                onDismiss = { showScriptureSheet = false }
            )
        }
    }
    if (showBetaNote) BetaNote(onDismiss = { showBetaNote = false })
}

@Composable
private fun FeedContent(
    state: FeedUiState.Success,
    onEvent: (FeedUiEvent) -> Unit,
    onOpenScriptures: () -> Unit,
    onOpenBeta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val haptics = LocalFathomHaptics.current
    val shell = LocalShell.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val colors = FathomTheme.colors
    val audioEngine = LocalSacredAudioEngine.current
    val ambienceEngine = LocalSanctuaryAmbienceEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()
    val ambienceState by ambienceEngine.state.collectAsState()
    val openAmbience = LocalOpenAmbience.current
    val openMore = com.masum.fathom.ui.shell.LocalOpenMore.current

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
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page in state.passages.indices) {
                onEvent(FeedUiEvent.OnPassageSwiped(page))
            }
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { haptics.light() }
    }

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        snapPositionalThreshold = 0.3f,
        snapAnimationSpec = tween(durationMillis = 460, easing = FathomMotion.Emphasized)
    )

    val currentPassage = state.passages.getOrNull(pagerState.currentPage)
    val tone = rememberTone(currentPassage?.traditionId)
    NavToneEffect(currentPassage?.traditionId)
    val bookmarked = currentPassage?.let { it.id in state.bookmarkedPassageIds } ?: false
    val speaking = (playbackState as? SacredAudioState.Playing)?.passage?.id == currentPassage?.id

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = modifier.fillMaxSize(),
            background = {
                Atmosphere(
                    tone = tone,
                    modifier = Modifier.fillMaxSize(),
                    phase = { (pagerState.currentPage + pagerState.currentPageOffsetFraction) * 0.85f }
                ) {
                    val pagePadding = PaddingValues(
                        top = 84.dp,
                        bottom = 96.dp + shell.bottomInset
                    )

                    VerticalPager(
                        state = pagerState,
                        flingBehavior = fling,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize().statusBarsPadding()
                    ) { page ->
                        val passage = state.passages[page]
                        val distance = { page - (pagerState.currentPage + pagerState.currentPageOffsetFraction) }
                        VersePage(
                            passage = passage,
                            reveal = { (1f - abs(distance()) * 1.1f).coerceIn(0f, 1f) },
                            contentPadding = pagePadding,
                            modifier = Modifier.graphicsLayer {
                                val d = distance().coerceIn(-1f, 1f)
                                val a = abs(d)
                                translationY = -d * size.height * 0.1f
                                alpha = 1f - a * 0.85f
                                val s = 1f - a * 0.05f
                                scaleX = s
                                scaleY = s
                            }
                        )
                    }
                }
            },
            overlay = {
            Header(
                state = state,
                traditionId = currentPassage?.traditionId,
                onOpenScriptures = onOpenScriptures,
                onOpenBeta = onOpenBeta,
                onOpenMore = {
                    haptics.light()
                    openMore()
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(start = shell.startInset)
            )

            DepthGauge(
                position = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                count = state.passages.size,
                hue = tone.hue,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp, bottom = 40.dp)
            )

            if (currentPassage != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = shell.bottomInset + 14.dp, start = shell.startInset + 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActionPill(
                        speaking = speaking,
                        bookmarked = bookmarked,
                        onListen = {
                            haptics.medium()
                            audioEngine.toggle(currentPassage)
                        },
                        onReflect = { onEvent(FeedUiEvent.OnOpenContext(currentPassage)) },
                        onShare = { onEvent(FeedUiEvent.OnOpenShare(currentPassage)) },
                        onSave = {
                            if (!bookmarked) haptics.success() else haptics.light()
                            onEvent(FeedUiEvent.OnToggleBookmark(currentPassage))
                        }
                    )
                    if (ambienceState.currentTrack != null) {
                        AmbienceTrackPill(
                            track = ambienceState.currentTrack!!,
                            playing = ambienceState.isPlaying,
                            hue = tone.hue,
                            onTogglePlay = {
                                haptics.light()
                                ambienceEngine.togglePlayPause()
                            },
                            onStop = {
                                haptics.light()
                                ambienceEngine.stop()
                            },
                            onClick = {
                                haptics.light()
                                openAmbience()
                            }
                        )
                    }
                }
            }
            }
        )
    }
}

@Composable
private fun AmbienceTrackPill(
    track: com.masum.fathom.core.audio.SoundscapeTrack,
    playing: Boolean,
    hue: Color,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val maxTextWidth = when {
        screenWidth < 340 -> 46.dp
        screenWidth < 380 -> 58.dp
        else -> 72.dp
    }
    Row(
        modifier = modifier
            .height(42.dp)
            .glass(RoundedCornerShape(50))
            .tappable(scale = 0.96f, onClick = onClick)
            .padding(start = if (screenWidth < 340) 8.dp else 10.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Equalizer(color = hue, size = 13.dp, active = playing)
        Text(
            text = track.title,
            style = FathomTheme.typography.caption.copy(fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = if (playing) colors.textPrimary else colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = maxTextWidth)
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (playing) hue else colors.surfaceElevated)
                .tappable(scale = 0.90f, haptic = false, onClick = onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                contentDescription = if (playing) "Pause soundscape" else "Play soundscape",
                tint = if (playing) FathomTheme.tone.onHue else colors.textPrimary,
                modifier = Modifier.size(11.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .tappable(scale = 0.88f, haptic = false, onClick = onStop),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PhosphorIcons.Light.X,
                contentDescription = "Stop soundscape",
                tint = colors.textTertiary,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
private fun Header(
    state: FeedUiState.Success,
    traditionId: TraditionId?,
    onOpenScriptures: () -> Unit,
    onOpenBeta: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors

    val activeBook = state.availableBooks.find { it.id == state.selectedBookId }
    val title = when {
        activeBook != null -> activeBook.title
        state.selectedTraditions.size == 1 -> state.selectedTraditions.first().displayName
        state.alignedTraditions.size == 1 -> "Leaning · ${state.alignedTraditions.first().displayName}"
        state.alignedTraditions.size > 1 -> "Leaning · ${state.alignedTraditions.size} traditions"
        state.selectedTraditions.size < TraditionId.entries.size -> "${state.selectedTraditions.size} traditions"
        else -> "All scriptures"
    }

    ScreenHeader(
        title = "fathom",
        traditionId = traditionId,
        modifier = modifier,
        onTitleClick = onOpenBeta,
        badge = "Beta"
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .widthIn(max = 200.dp)
                .height(40.dp)
                .glass(RoundedCornerShape(50))
                .tappable(scale = 0.96f, onClick = onOpenScriptures)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = FathomTheme.typography.label,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(PhosphorIcons.Light.CaretDown, null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
        }
        Box {
            GlassIconButton(
                icon = PhosphorIcons.Light.DotsThree,
                contentDescription = "More",
                onClick = onOpenMore
            )
        }
    }
}

@Composable
private fun ActionPill(
    speaking: Boolean,
    bookmarked: Boolean,
    onListen: () -> Unit,
    onReflect: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val cell = when {
        screenWidth < 340 -> 32.dp
        screenWidth < 380 -> 34.dp
        else -> 36.dp
    }
    val iconSize = when {
        screenWidth < 340 -> 16.dp
        screenWidth < 380 -> 17.dp
        else -> 18.dp
    }
    Row(
        modifier = modifier
            .height(42.dp)
            .glass(RoundedCornerShape(50))
            .padding(horizontal = if (screenWidth < 340) 4.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (screenWidth < 340) 1.dp else 2.dp)
    ) {
        PillAction(cell = cell, onClick = onListen, description = "Listen") {
            if (speaking) Equalizer(color = tone.hue, size = 15.dp) else Icon(PhosphorIcons.Light.SpeakerHigh, null, tint = colors.textPrimary, modifier = Modifier.size(iconSize))
        }
        PillAction(cell = cell, onClick = onReflect, description = "Context and commentary") {
            Icon(PhosphorIcons.Light.ChatCenteredText, null, tint = colors.textPrimary, modifier = Modifier.size(iconSize))
        }
        PillAction(cell = cell, onClick = onShare, description = "Share as card") {
            Icon(PhosphorIcons.Light.ShareFat, null, tint = colors.textPrimary, modifier = Modifier.size(iconSize))
        }
        PillAction(cell = cell, onClick = onSave, description = "Save passage") {
            SaveGlyph(saved = bookmarked, hue = tone.hue, size = iconSize)
        }
    }
}

@Composable
private fun PillAction(cell: Dp, onClick: () -> Unit, description: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(cell)
            .clip(CircleShape)
            .tappable(scale = 0.86f, haptic = false, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SaveGlyph(saved: Boolean, hue: Color, size: Dp = 18.dp) {
    val colors = FathomTheme.colors
    val burst = remember { Animatable(1f) }
    val pop = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(saved) }
    LaunchedEffect(saved) {
        if (saved && !previous) {
            burst.snapTo(0f)
            pop.snapTo(0.6f)
            launch { burst.animateTo(1f, tween(620, easing = FathomMotion.Emphasized)) }
            pop.animateTo(1f, FathomMotion.bounce())
        }
        previous = saved
    }
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size * 2)) {
            val p = burst.value
            if (p < 1f) {
                val radius = this.size.minDimension / 2f * (0.35f + 0.65f * p)
                for (i in 0 until 8) {
                    val angle = (i / 8f) * 6.2831855f
                    val center = Offset(
                        this.size.width / 2f + kotlin.math.cos(angle) * radius,
                        this.size.height / 2f + kotlin.math.sin(angle) * radius
                    )
                    drawCircle(hue.copy(alpha = 1f - p), radius = 2.2.dp.toPx() * (1f - p * 0.6f), center = center)
                }
            }
        }
        Icon(
            imageVector = if (saved) PhosphorIcons.Fill.BookmarkFilled else PhosphorIcons.Light.BookmarkSimple,
            contentDescription = null,
            tint = if (saved) hue else colors.textPrimary,
            modifier = Modifier.size(size).graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
        )
    }
}

@Composable
private fun BetaNote(onDismiss: () -> Unit) {
    val colors = FathomTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(colors.surfaceElevated)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MonoLabel(text = "Closed beta", color = FathomTheme.tone.hue)
            Text(text = "You are early.", style = FathomTheme.typography.headline, color = colors.textPrimary)
            Text(
                text = "Fathom is in closed testing. Expect rough edges while the experience is refined. Every feature is open for you to try, and your feedback shapes what ships.",
                style = FathomTheme.typography.body,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(6.dp))
            FathomButton(text = "Understood", onClick = onDismiss, modifier = Modifier.fillMaxWidth())
        }
    }
}
