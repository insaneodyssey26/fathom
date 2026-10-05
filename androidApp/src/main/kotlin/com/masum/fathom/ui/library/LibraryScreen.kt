package com.masum.fathom.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowLeft
import com.adamglin.phosphoricons.light.Sliders
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.BreathingMark
import com.masum.fathom.ui.design.DockedTopPanel
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.NavToneEffect
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.ScreenHeader
import com.masum.fathom.ui.design.StateFrame
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.search.EditorialSearchField
import com.masum.fathom.ui.search.TraditionChips
import com.masum.fathom.ui.shell.traditionIdForLabel
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import com.masum.fathom.ui.whatsnew.WhatsNewModal
import androidx.compose.foundation.lazy.rememberLazyListState

sealed interface JournalPage {
    data object Hub : JournalPage
    data object Saved : JournalPage
    data object Reflections : JournalPage
    data object Timeline : JournalPage
    data class Canon(val traditionId: TraditionId) : JournalPage
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    page: JournalPage,
    onNavigate: (JournalPage) -> Unit,
    onBack: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is LibraryUiState.Loading -> StateFrame(modifier) { BreathingMark() }
        is LibraryUiState.Error -> StateFrame(modifier) { QuietMessage(title = "Something went quiet", body = state.message) }
        is LibraryUiState.Success -> JournalPageScreen(state = state, onEvent = onEvent, page = page, onNavigate = onNavigate, onBack = onBack, onOpenWhatsNew = onOpenWhatsNew, modifier = modifier)
    }
}

@Composable
private fun JournalPageScreen(
    state: LibraryUiState.Success,
    onEvent: (LibraryUiEvent) -> Unit,
    page: JournalPage,
    onNavigate: (JournalPage) -> Unit,
    onBack: () -> Unit,
    onOpenWhatsNew: () -> Unit,
    modifier: Modifier
) {
    val haptics = LocalFathomHaptics.current
    val openSanctuary = LocalOpenSanctuary.current
    val density = LocalDensity.current

    var panelHeight by remember { mutableIntStateOf(0) }
    val hubListState = rememberLazyListState()

    val userTradition = traditionIdForLabel(state.userTradition)
    val focus = (page as? JournalPage.Canon)?.traditionId ?: userTradition
    val tone = rememberTone(focus)
    NavToneEffect(focus)
    val panelDp = with(density) { panelHeight.toDp() }

    if (page == JournalPage.Saved) LaunchedEffect(Unit) { onEvent(LibraryUiEvent.OnSelectTab(LibraryTab.BOOKMARKS)) }
    if (page == JournalPage.Reflections) LaunchedEffect(Unit) { onEvent(LibraryUiEvent.OnSelectTab(LibraryTab.REFLECTIONS)) }
    if (page != JournalPage.Hub) {
        DisposableEffect(Unit) {
            onDispose {
                onEvent(LibraryUiEvent.OnSearchQueryChanged(""))
                onEvent(LibraryUiEvent.OnFilterTradition(null))
            }
        }
    }
    if (page == JournalPage.Hub && state.searchQuery.isNotEmpty()) BackHandler { onEvent(LibraryUiEvent.OnSearchQueryChanged("")) }

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = modifier.fillMaxSize(),
            background = {
                Atmosphere(tone = tone, modifier = Modifier.fillMaxSize(), phase = { hubListState.firstVisibleItemIndex + hubListState.firstVisibleItemScrollOffset / 800f }) {
                    val searching = state.searchQuery.isNotBlank() || state.selectedTraditionFilter != null
                    when (page) {
                        JournalPage.Hub -> JournalHub(
                            state = state,
                            listState = hubListState,
                            topPadding = panelDp,
                            onOpenSaved = { onNavigate(JournalPage.Saved) },
                            onOpenReflections = { onNavigate(JournalPage.Reflections) },
                            onOpenCanon = { onNavigate(JournalPage.Canon(it)) },
                            onOpenTimeline = { onNavigate(JournalPage.Timeline) },
                            onOpenWhatsNew = onOpenWhatsNew,
                            onEditProfile = { onEvent(LibraryUiEvent.OnOpenEditProfile) },
                            onOpenPassage = { onEvent(LibraryUiEvent.OnOpenContext(it)) }
                        )
                        JournalPage.Saved -> SavedList(state.bookmarks, searching, panelDp, onEvent)
                        JournalPage.Reflections -> NotesList(state.notes, searching, panelDp, onEvent)
                        JournalPage.Timeline -> TimelineList(state.history, panelDp) { onEvent(LibraryUiEvent.OnOpenContext(it)) }
                        is JournalPage.Canon -> CanonDetail(page.traditionId, state.history, state.bookmarks, state.notes, panelDp, onEvent)
                    }
                }
            },
            overlay = {
                DockedTopPanel(onHeight = { panelHeight = it }) {
                    if (page == JournalPage.Hub) {
                        ScreenHeader(title = "journal", traditionId = userTradition) {
                            GlassIconButton(
                                icon = PhosphorIcons.Light.Sliders,
                                contentDescription = "Settings",
                                onClick = {
                                    haptics.light()
                                    openSanctuary()
                                }
                            )
                        }
                    } else {
                        BackHeader(
                            overline = when (page) {
                                JournalPage.Saved -> "${state.bookmarks.size} kept"
                                JournalPage.Reflections -> "${state.notes.size} written"
                                JournalPage.Timeline -> "${state.history.size} verses"
                                else -> "Tradition"
                            },
                            title = when (page) {
                                JournalPage.Saved -> "Saved passages"
                                JournalPage.Reflections -> "Reflections"
                                JournalPage.Timeline -> "Reading history"
                                is JournalPage.Canon -> page.traditionId.displayName
                                JournalPage.Hub -> ""
                            },
                            onBack = {
                                haptics.light()
                                onBack()
                            }
                        )
                        if (page == JournalPage.Saved || page == JournalPage.Reflections) {
                            Spacer(Modifier.height(8.dp))
                            EditorialSearchField(
                                query = state.searchQuery,
                                onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            TraditionChips(
                                selected = state.selectedTraditionFilter,
                                onSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LibraryOverlays(state: LibraryUiState, onEvent: (LibraryUiEvent) -> Unit) {
    val success = state as? LibraryUiState.Success ?: return
    val subscription = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current

    when {
        success.selectedPassageForShare != null -> BackHandler { onEvent(LibraryUiEvent.OnCloseShare) }
        success.selectedPassageForContext != null -> BackHandler { onEvent(LibraryUiEvent.OnCloseContext) }
        success.selectedPassageForNote != null -> BackHandler { onEvent(LibraryUiEvent.OnCloseNoteDialog) }
    }

    success.selectedPassageForContext?.let {
        PassageContextSheet(passage = it, onDismiss = { onEvent(LibraryUiEvent.OnCloseContext) })
    }
    success.selectedPassageForShare?.let {
        QuoteCardModal(passage = it, onDismiss = { onEvent(LibraryUiEvent.OnCloseShare) })
    }
    success.selectedPassageForNote?.let { passage ->
        NoteSheet(
            passage = passage,
            initial = success.notes.firstOrNull { it.note.passageId == passage.id }?.note?.content.orEmpty(),
            onDismiss = { onEvent(LibraryUiEvent.OnCloseNoteDialog) },
            onSave = { content ->
                val limitReached = !subscription.isPro &&
                    success.notes.size >= subscription.freeNotesLimit &&
                    success.notes.none { it.note.passageId == passage.id }
                if (limitReached) openPaywall() else onEvent(LibraryUiEvent.OnSaveNote(passage.id, content))
            }
        )
    }
    if (success.isEditingProfile) {
        ProfileSheet(
            currentName = success.userName,
            currentTradition = success.userTradition,
            onSave = { name, tradition -> onEvent(LibraryUiEvent.OnSaveProfile(name, tradition)) },
            onDismiss = { onEvent(LibraryUiEvent.OnCloseEditProfile) }
        )
    }
}

@Composable
private fun BackHeader(overline: String, title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassIconButton(icon = PhosphorIcons.Light.ArrowLeft, contentDescription = "Back", onClick = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MonoLabel(text = overline, color = FathomTheme.tone.hue)
            Text(text = title, style = FathomTheme.typography.title.copy(fontSize = 24.sp), color = FathomTheme.colors.textPrimary)
        }
    }
}
