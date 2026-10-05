package com.masum.fathom.ui.search

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowLeft
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.PassageEntry
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.DockedTopPanel
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.NavToneEffect
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.launch

@Composable
fun BookReader(
    book: Book,
    chapters: List<Int>,
    selectedChapter: Int?,
    chapterPassagesMap: Map<Int, List<Passage>>,
    isLoading: Boolean,
    bookmarkedIds: Set<String>,
    onBack: () -> Unit,
    onEvent: (SearchUiEvent) -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val shell = LocalShell.current
    val colors = FathomTheme.colors
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tone = rememberTone(book.traditionId)
    NavToneEffect(book.traditionId)
    val tabsState = rememberLazyListState()
    var panelHeight by remember { mutableIntStateOf(0) }

    val initialPage = selectedChapter?.let { chapters.indexOf(it).takeIf { index -> index >= 0 } } ?: 0
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { chapters.size })

    LaunchedEffect(pagerState.currentPage, chapters) {
        if (chapters.isNotEmpty() && pagerState.currentPage in chapters.indices) {
            val target = chapters[pagerState.currentPage]
            if (target != selectedChapter) {
                haptics.light()
                onEvent(SearchUiEvent.OnSelectChapter(target))
            }
            tabsState.animateScrollToItem((pagerState.currentPage - 1).coerceAtLeast(0))
        }
    }
    LaunchedEffect(selectedChapter, chapters) {
        if (selectedChapter != null && chapters.isNotEmpty()) {
            val page = chapters.indexOf(selectedChapter)
            if (page >= 0 && page != pagerState.currentPage) pagerState.scrollToPage(page)
        }
    }

    val prefix = when (book.traditionId) {
        TraditionId.ISLAM -> "Surah"
        TraditionId.STOICISM -> "Book"
        else -> "Ch"
    }

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = Modifier.fillMaxSize(),
            background = {
                Atmosphere(tone = tone, modifier = Modifier.fillMaxSize(), phase = { pagerState.currentPage + pagerState.currentPageOffsetFraction }) {
                    HorizontalPager(
                        state = pagerState,
                        key = { page -> if (page in chapters.indices) chapters[page] else page },
                        modifier = Modifier.fillMaxSize().padding(start = shell.startInset)
                    ) { page ->
                        val chapter = chapters.getOrNull(page)
                        val passages = chapter?.let { chapterPassagesMap[it] } ?: emptyList()
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            if (passages.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (isLoading) BreathingMark() else QuietMessage(title = "Nothing here", body = "No verses found in this chapter.")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxHeight().widthIn(max = shell.contentMaxWidth + 96.dp),
                                    contentPadding = PaddingValues(
                                        start = 24.dp,
                                        end = 24.dp,
                                        top = with(density) { panelHeight.toDp() } + 12.dp,
                                        bottom = shell.bottomInset + 32.dp
                                    )
                                ) {
                                    items(passages, key = { it.id }) { passage ->
                                        PassageEntry(
                                            passage = passage,
                                            saved = passage.id in bookmarkedIds,
                                            onSave = { onEvent(SearchUiEvent.OnToggleBookmark(passage)) },
                                            onReflect = { onEvent(SearchUiEvent.OnOpenContext(passage)) },
                                            onShare = { onEvent(SearchUiEvent.OnOpenShare(passage)) },
                                            showTradition = false,
                                            modifier = Modifier.padding(top = 26.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            overlay = {
                DockedTopPanel(onHeight = { panelHeight = it }, bottomPadding = 12.dp) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GlassIconButton(
                            icon = PhosphorIcons.Light.ArrowLeft,
                            contentDescription = "Back to scriptures",
                            onClick = {
                                haptics.light()
                                onBack()
                            }
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MonoLabel(text = "${book.traditionId.displayName} · ${book.totalPassages} verses", color = tone.hue)
                            Text(
                                text = book.title,
                                style = FathomTheme.typography.title,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (chapters.isNotEmpty()) {
                        LazyRow(
                            state = tabsState,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            items(chapters.size, key = { chapters[it] }) { index ->
                                ChapterTab(
                                    prefix = prefix,
                                    number = chapters[index],
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        haptics.light()
                                        scope.launch { pagerState.animateScrollToPage(index, animationSpec = tween(420, easing = FathomMotion.Emphasized)) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun ChapterTab(prefix: String, number: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val ink by animateColorAsState(if (selected) colors.textPrimary else colors.textTertiary, tween(220), label = "tab_ink")
    val rule by animateColorAsState(if (selected) tone.hue else androidx.compose.ui.graphics.Color.Transparent, tween(260), label = "tab_rule")
    Column(
        modifier = Modifier.tappable(scale = 0.94f, haptic = false, onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        MonoLabel(text = prefix, color = ink)
        Text(text = number.toString(), style = FathomTheme.typography.headline.copy(fontSize = 24.sp, lineHeight = 28.sp), color = ink)
        Box(Modifier.width(22.dp).height(2.dp).background(rule))
    }
}
