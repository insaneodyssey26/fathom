package com.masum.fathom.ui.library

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.BookOpen
import com.adamglin.phosphoricons.regular.BookmarkSimple
import com.adamglin.phosphoricons.regular.CalendarCheck
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Crown
import com.adamglin.phosphoricons.regular.Flame
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.Medal
import com.adamglin.phosphoricons.regular.NotePencil
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TrashSimple
import com.adamglin.phosphoricons.regular.Trophy
import com.adamglin.phosphoricons.regular.User
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.core.sanctuary.LocalOpenSanctuary
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.components.QuoteCardModal
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily

sealed interface YouSubScreen {
    data object Hub : YouSubScreen
    data object SavedPassages : YouSubScreen
    data object JournalReflections : YouSubScreen
    data object TimelineFull : YouSubScreen
    data class CanonDetail(val traditionId: TraditionId) : YouSubScreen
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var subScreen by remember { mutableStateOf<YouSubScreen>(YouSubScreen.Hub) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        when (state) {
            is LibraryUiState.Loading -> {
                YouTopBar(subScreen = subScreen, onNavigateBack = { subScreen = YouSubScreen.Hub }, state = null)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FathomTheme.colors.accentGold)
                }
            }

            is LibraryUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        style = FathomTheme.typography.commentaryBody,
                        color = FathomTheme.colors.textSecondary
                    )
                }
            }

            is LibraryUiState.Success -> {
                if (state.selectedPassageForShare != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseShare) }
                } else if (state.selectedPassageForContext != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseContext) }
                } else if (state.selectedPassageForNote != null) {
                    BackHandler { onEvent(LibraryUiEvent.OnCloseNoteDialog) }
                } else if (subScreen != YouSubScreen.Hub) {
                    BackHandler {
                        if (state.searchQuery.isNotEmpty()) {
                            onEvent(LibraryUiEvent.OnSearchQueryChanged(""))
                        }
                        subScreen = YouSubScreen.Hub
                    }
                } else if (state.searchQuery.isNotEmpty()) {
                    BackHandler { onEvent(LibraryUiEvent.OnSearchQueryChanged("")) }
                }

                YouTopBar(
                    subScreen = subScreen,
                    onNavigateBack = {
                        if (state.searchQuery.isNotEmpty()) {
                            onEvent(LibraryUiEvent.OnSearchQueryChanged(""))
                        }
                        subScreen = YouSubScreen.Hub
                    },
                    state = state
                )

                AnimatedContent(
                    targetState = subScreen,
                    transitionSpec = {
                        if (targetState != YouSubScreen.Hub) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width / 3 } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "YouScreenSubNavigation",
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { currentSubScreen ->
                    when (currentSubScreen) {
                        is YouSubScreen.Hub -> {
                            YouHubView(
                                state = state,
                                onNavigateToSaved = {
                                    onEvent(LibraryUiEvent.OnSelectTab(LibraryTab.BOOKMARKS))
                                    subScreen = YouSubScreen.SavedPassages
                                },
                                onNavigateToJournal = {
                                    onEvent(LibraryUiEvent.OnSelectTab(LibraryTab.REFLECTIONS))
                                    subScreen = YouSubScreen.JournalReflections
                                },
                                onNavigateToCanon = { tradition ->
                                    subScreen = YouSubScreen.CanonDetail(tradition)
                                },
                                onNavigateToTimeline = {
                                    subScreen = YouSubScreen.TimelineFull
                                },
                                onEvent = onEvent
                            )
                        }

                        is YouSubScreen.SavedPassages -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                SearchBarAndFilters(
                                    searchQuery = state.searchQuery,
                                    selectedTradition = state.selectedTraditionFilter,
                                    onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                                    onTraditionSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                                )
                                BookmarksList(state.bookmarks, onEvent)
                            }
                        }

                        is YouSubScreen.JournalReflections -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                SearchBarAndFilters(
                                    searchQuery = state.searchQuery,
                                    selectedTradition = state.selectedTraditionFilter,
                                    onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                                    onTraditionSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                                )
                                ReflectionsList(state.notes, onEvent)
                            }
                        }

                        is YouSubScreen.CanonDetail -> {
                            CanonDetailView(
                                traditionId = currentSubScreen.traditionId,
                                history = state.history,
                                bookmarks = state.bookmarks,
                                notes = state.notes,
                                onEvent = onEvent
                            )
                        }

                        is YouSubScreen.TimelineFull -> {
                            FullTimelineView(
                                history = state.history,
                                onEvent = onEvent
                            )
                        }
                    }
                }

                state.selectedPassageForContext?.let { contextPassage ->
                    PassageContextSheet(
                        passage = contextPassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseContext) }
                    )
                }

                val subscriptionState = LocalSubscriptionState.current
                val openPaywall = LocalOpenPaywall.current

                state.selectedPassageForNote?.let { notePassage ->
                    NoteEditDialog(
                        passage = notePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseNoteDialog) },
                        onSave = { content ->
                            if (!subscriptionState.isPro && state.notes.size >= subscriptionState.freeNotesLimit && state.notes.none { it.note.passageId == notePassage.id }) {
                                openPaywall()
                            } else {
                                onEvent(LibraryUiEvent.OnSaveNote(notePassage.id, content))
                            }
                        }
                    )
                }

                state.selectedPassageForShare?.let { sharePassage ->
                    QuoteCardModal(
                        passage = sharePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseShare) }
                    )
                }

                if (state.isEditingProfile) {
                    EditProfileSheet(
                        currentName = state.userName,
                        currentTradition = state.userTradition,
                        onSave = { name, tradition ->
                            onEvent(LibraryUiEvent.OnSaveProfile(name, tradition))
                        },
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseEditProfile) }
                    )
                }
            }
        }
    }
}

@Composable
private fun YouTopBar(
    subScreen: YouSubScreen,
    onNavigateBack: () -> Unit,
    state: LibraryUiState.Success?
) {
    val haptics = com.masum.fathom.core.haptics.LocalFathomHaptics.current
    val openSanctuary = LocalOpenSanctuary.current
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current

    val title = when (subScreen) {
        is YouSubScreen.Hub -> "you"
        is YouSubScreen.SavedPassages -> "Saved Passages"
        is YouSubScreen.JournalReflections -> "Personal Notes"
        is YouSubScreen.TimelineFull -> "Reading History"
        is YouSubScreen.CanonDetail -> "${subScreen.traditionId.displayName} Traditions"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (subScreen is YouSubScreen.Hub) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Text(
                    text = "you",
                    style = FathomTheme.typography.brandTitle,
                    color = FathomTheme.colors.textPrimary
                )
                Text(
                    text = ".",
                    style = FathomTheme.typography.brandTitle.copy(color = FathomTheme.colors.accentGold)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = {
                        haptics.light()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(FathomTheme.colors.surfaceElevated)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowLeft,
                        contentDescription = "Back to You Hub",
                        tint = FathomTheme.colors.textPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = FathomTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state != null && subScreen is YouSubScreen.Hub) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Flame,
                        contentDescription = "Daily Streak",
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${state.currentStreakDays}D STREAK",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.5.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }

            IconButton(
                onClick = {
                    haptics.light()
                    openSanctuary()
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(FathomTheme.colors.surfaceElevated)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Sliders,
                    contentDescription = "Sanctuary Settings",
                    tint = FathomTheme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun YouHubView(
    state: LibraryUiState.Success,
    onNavigateToSaved: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToCanon: (TraditionId) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current

    val topTradition = remember(state.history) {
        state.history
            .mapNotNull { it.passage?.traditionId }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
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
                        color = if (subscriptionState.isPro) FathomTheme.colors.accentGold.copy(alpha = 0.5f) else FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptics.light()
                                onEvent(LibraryUiEvent.OnOpenEditProfile)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            FathomTheme.colors.accentGold.copy(alpha = 0.25f),
                                            FathomTheme.colors.surfaceElevated
                                        )
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.accentGold.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (subscriptionState.isPro) PhosphorIcons.Regular.Crown else PhosphorIcons.Regular.User,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = state.userName,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.5.sp,
                                    color = FathomTheme.colors.textPrimary
                                )
                                Icon(
                                    imageVector = PhosphorIcons.Regular.PencilSimple,
                                    contentDescription = "Edit Profile",
                                    tint = FathomTheme.colors.accentGold.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (subscriptionState.isPro) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                                            else FathomTheme.colors.surfaceElevated
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (subscriptionState.isPro) FathomTheme.colors.accentGold.copy(alpha = 0.6f) else FathomTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (subscriptionState.isPro) "SANCTUARY+" else "FREE TIER",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.6.sp,
                                        color = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                    )
                                }

                                val displayedTradition = state.userTradition ?: topTradition?.displayName?.let { "$it Focus" }
                                displayedTradition?.let { traditionTag ->
                                    Text(
                                        text = "• $traditionTag",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.5.sp,
                                        color = FathomTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    if (!subscriptionState.isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(FathomTheme.colors.accentGold)
                                .clickable {
                                    haptics.medium()
                                    openPaywall()
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "PRO",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(168.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    FathomTheme.colors.accentGold.copy(alpha = 0.16f),
                                    FathomTheme.colors.surfaceElevated
                                )
                            )
                        )
                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FathomTheme.colors.accentGold.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Flame,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "${state.currentStreakDays}",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                color = FathomTheme.colors.accentGold
                            )
                            Text(
                                text = "DAY STREAK",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Text(
                                text = "Consecutive study",
                                fontFamily = JakartaFontFamily,
                                fontSize = 10.5.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(168.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        FathomTheme.colors.surfaceElevated,
                                        FathomTheme.colors.surface
                                    )
                                )
                            )
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.BookOpen,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${state.totalReadCount}",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FathomTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Readings",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    color = FathomTheme.colors.textTertiary
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        FathomTheme.colors.surfaceElevated,
                                        FathomTheme.colors.surface
                                    )
                                )
                            )
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Sparkle,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "${state.history.size}",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = FathomTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Verses Read",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.5.sp,
                                    color = FathomTheme.colors.textTertiary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SAVED & REFLECTIONS",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    FathomTheme.colors.surfaceElevated,
                                    FathomTheme.colors.surface
                                )
                            )
                        )
                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            haptics.light()
                            onNavigateToSaved()
                        }
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.BookmarkSimple,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretRight,
                                contentDescription = null,
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${state.bookmarks.size}",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Saved Passages",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    FathomTheme.colors.surfaceElevated,
                                    FathomTheme.colors.surface
                                )
                            )
                        )
                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        .clickable {
                            haptics.light()
                            onNavigateToJournal()
                        }
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.NotePencil,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretRight,
                                contentDescription = null,
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${state.notes.size}",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Personal Notes",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            HistoryStatsContent(
                history = state.history,
                totalCount = state.totalReadCount,
                streakDays = state.currentStreakDays,
                weeklyHeatmap = state.weeklyHeatmap,
                milestoneBadges = state.milestoneBadges,
                onNavigateToCanon = onNavigateToCanon,
                onNavigateToTimeline = onNavigateToTimeline,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun HistoryStatsContent(
    history: List<HistoryWithPassage>,
    totalCount: Int,
    streakDays: Int,
    weeklyHeatmap: List<HeatmapDay>,
    milestoneBadges: List<MilestoneBadge>,
    onNavigateToCanon: (TraditionId) -> Unit,
    onNavigateToTimeline: () -> Unit,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current
    val traditionCounts = remember(history) {
        history.mapNotNull { it.passage?.traditionId }
            .groupingBy { it }
            .eachCount()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            FathomTheme.colors.surfaceElevated,
                            FathomTheme.colors.surface
                        )
                    )
                )
                .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CalendarCheck,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "WEEKLY ACTIVITY",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 1.sp,
                            color = FathomTheme.colors.accentGold
                        )
                    }

                    Text(
                        text = "Last 7 Days",
                        fontFamily = JakartaFontFamily,
                        fontSize = 10.5.sp,
                        color = FathomTheme.colors.textTertiary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weeklyHeatmap.forEach { day ->
                        val hasRead = day.readCount > 0
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = day.dayOfWeekLabel.uppercase(),
                                fontFamily = JakartaFontFamily,
                                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                color = if (day.isToday) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                            )

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            day.readCount >= 5 -> FathomTheme.colors.accentGold
                                            day.readCount >= 2 -> FathomTheme.colors.accentGold.copy(alpha = 0.65f)
                                            hasRead -> FathomTheme.colors.accentGold.copy(alpha = 0.30f)
                                            else -> FathomTheme.colors.surface
                                        }
                                    )
                                    .border(
                                        width = if (day.isToday) 1.5.dp else 1.dp,
                                        color = if (day.isToday) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${day.dayOfMonth}",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (day.readCount >= 2) Color.Black else FathomTheme.colors.textPrimary
                                )
                            }

                            Text(
                                text = if (hasRead) "${day.readCount}" else "·",
                                fontFamily = JakartaFontFamily,
                                fontSize = 9.sp,
                                color = if (hasRead) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRADITIONS EXPLORED",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Text(
                text = "Tap to open",
                fontFamily = JakartaFontFamily,
                fontSize = 10.5.sp,
                color = FathomTheme.colors.textTertiary
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TraditionId.entries.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { tradition ->
                        val count = traditionCounts[tradition] ?: 0
                        val isExplored = count > 0

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            if (isExplored) FathomTheme.colors.surfaceElevated else FathomTheme.colors.surface,
                                            FathomTheme.colors.surface
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isExplored) FathomTheme.colors.accentGold.copy(alpha = 0.4f) else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    haptics.light()
                                    onNavigateToCanon(tradition)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = tradition.displayName,
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = if (isExplored) FathomTheme.colors.textPrimary else FathomTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = if (isExplored) "$count explored" else "0 read",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isExplored) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                    )
                                }

                                Icon(
                                    imageVector = PhosphorIcons.Regular.CaretRight,
                                    contentDescription = null,
                                    tint = if (isExplored) FathomTheme.colors.accentGold.copy(alpha = 0.8f) else FathomTheme.colors.textTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (milestoneBadges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "READING ACHIEVEMENTS",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
                val unlockedCount = milestoneBadges.count { it.isUnlocked }
                Text(
                    text = "$unlockedCount of ${milestoneBadges.size} unlocked",
                    fontFamily = JakartaFontFamily,
                    fontSize = 10.5.sp,
                    color = FathomTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                milestoneBadges.forEach { badge ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (badge.isUnlocked) FathomTheme.colors.surfaceElevated
                                else FathomTheme.colors.surface.copy(alpha = 0.6f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.4f) else FathomTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                                    else FathomTheme.colors.surface
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (badge.isUnlocked) PhosphorIcons.Regular.Trophy else PhosphorIcons.Regular.Medal,
                                contentDescription = null,
                                tint = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = badge.title,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (badge.isUnlocked) FathomTheme.colors.textPrimary else FathomTheme.colors.textSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (badge.isUnlocked) FathomTheme.colors.accentGold.copy(alpha = 0.15f)
                                            else FathomTheme.colors.surfaceElevated
                                        )
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = badge.tierName.uppercase(),
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        letterSpacing = 0.4.sp,
                                        color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = badge.description,
                                fontFamily = JakartaFontFamily,
                                fontSize = 11.5.sp,
                                color = FathomTheme.colors.textTertiary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val progRatio = (badge.currentCount.toFloat() / badge.requiredCount.toFloat()).coerceIn(0f, 1f)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { progRatio },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = FathomTheme.colors.accentGold,
                                    trackColor = FathomTheme.colors.surface
                                )
                                Text(
                                    text = "${badge.currentCount}/${badge.requiredCount}",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = if (badge.isUnlocked) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ClockCounterClockwise,
                    contentDescription = null,
                    tint = FathomTheme.colors.accentGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "RECENT READINGS",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
            }

            if (history.isNotEmpty()) {
                TextButton(
                    onClick = {
                        haptics.light()
                        onNavigateToTimeline()
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "VIEW ALL (${history.size})",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            letterSpacing = 0.6.sp,
                            color = FathomTheme.colors.accentGold
                        )
                        Icon(
                            imageVector = PhosphorIcons.Regular.CaretRight,
                            contentDescription = null,
                            tint = FathomTheme.colors.accentGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No Reading History Yet",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = FathomTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verses you read in the daily stream and scriptures will appear here in order.",
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp,
                        color = FathomTheme.colors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                history.take(4).forEach { item ->
                    PassageHistoryCard(item = item, onEvent = onEvent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "PRIVACY & STORAGE",
            style = FathomTheme.typography.canonicalHeader,
            color = FathomTheme.colors.accentGold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FathomTheme.colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Sparkle,
                    contentDescription = null,
                    tint = FathomTheme.colors.accentGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "100% On-Device Storage",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = FathomTheme.colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "All your bookmarks, notes, and reading history stay privately on your device.",
                fontFamily = JakartaFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = FathomTheme.colors.textSecondary
            )
        }
    }
}

@Composable
private fun CanonDetailView(
    traditionId: TraditionId,
    history: List<HistoryWithPassage>,
    bookmarks: List<BookmarkWithPassage>,
    notes: List<NoteWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val canonHistory = remember(history, traditionId) {
        history.filter { it.passage?.traditionId == traditionId }
    }
    val canonBookmarks = remember(bookmarks, traditionId) {
        bookmarks.filter { it.passage?.traditionId == traditionId }
    }
    val canonNotes = remember(notes, traditionId) {
        notes.filter { it.passage?.traditionId == traditionId }
    }

    var selectedSection by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 96.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                FathomTheme.colors.accentGold.copy(alpha = 0.16f),
                                FathomTheme.colors.surfaceElevated
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TraditionBadge(traditionId = traditionId)
                        Text(
                            text = "${canonHistory.size} Verses Studied",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.accentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${traditionId.displayName} Scriptures",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = FathomTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${canonHistory.size}",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = FathomTheme.colors.accentGold
                            )
                            Text(
                                text = "READ",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(FathomTheme.colors.borderSubtle)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${canonBookmarks.size}",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Text(
                                text = "SAVED",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(FathomTheme.colors.borderSubtle)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${canonNotes.size}",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Text(
                                text = "NOTES",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sections = listOf("VERSES READ (${canonHistory.size})", "SAVED (${canonBookmarks.size})", "NOTES (${canonNotes.size})")
                sections.forEachIndexed { index, label ->
                    val isSelected = selectedSection == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.18f) else FathomTheme.colors.surfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedSection = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = JakartaFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        when (selectedSection) {
            0 -> {
                if (canonHistory.isEmpty()) {
                    item {
                        EmptyCanonState(
                            title = "No Verses Read in ${traditionId.displayName}",
                            body = "Passages you explore from ${traditionId.displayName} will be recorded here."
                        )
                    }
                } else {
                    items(canonHistory, key = { it.record.passageId }) { item ->
                        PassageHistoryCard(item = item, onEvent = onEvent)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            1 -> {
                if (canonBookmarks.isEmpty()) {
                    item {
                        EmptyCanonState(
                            title = "No Bookmarks in ${traditionId.displayName}",
                            body = "Bookmark any passage from ${traditionId.displayName} to save it here."
                        )
                    }
                } else {
                    items(canonBookmarks, key = { it.bookmark.id }) { item ->
                        val historyEquivalent = HistoryWithPassage(
                            record = com.masum.fathom.core.model.user.ReadingRecord(
                                passageId = item.bookmark.passageId,
                                bookId = item.passage?.bookId ?: "",
                                firstSeenAtEpochMs = item.bookmark.createdAtEpochMs,
                                lastSeenAtEpochMs = item.bookmark.createdAtEpochMs,
                                readCount = 1,
                                isCompleted = false
                            ),
                            passage = item.passage
                        )
                        PassageHistoryCard(item = historyEquivalent, onEvent = onEvent)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            2 -> {
                if (canonNotes.isEmpty()) {
                    item {
                        EmptyCanonState(
                            title = "No Notes in ${traditionId.displayName}",
                            body = "Personal notes written on ${traditionId.displayName} verses will appear here."
                        )
                    }
                } else {
                    items(canonNotes, key = { it.note.id }) { item ->
                        ReflectionsItemCard(item = item, onEvent = onEvent)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FullTimelineView(
    history: List<HistoryWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTradition by remember { mutableStateOf<TraditionId?>(null) }

    val filteredHistory = remember(history, searchQuery, selectedTradition) {
        history.filter { item ->
            val matchesTradition = selectedTradition == null || item.passage?.traditionId == selectedTradition
            val matchesQuery = searchQuery.isBlank() || (item.passage?.translation?.contains(searchQuery, ignoreCase = true) == true ||
                    item.passage?.canonicalReference?.contains(searchQuery, ignoreCase = true) == true)
            matchesTradition && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp)
    ) {
        item {
            SearchBarAndFilters(
                searchQuery = searchQuery,
                selectedTradition = selectedTradition,
                onQueryChange = { searchQuery = it },
                onTraditionSelect = { selectedTradition = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filteredHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reading records match your search.",
                        style = FathomTheme.typography.commentaryBody,
                        color = FathomTheme.colors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredHistory, key = { it.record.passageId }) { item ->
                PassageHistoryCard(item = item, onEvent = onEvent)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun EmptyCanonState(title: String, body: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = FathomTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                fontFamily = JakartaFontFamily,
                fontSize = 13.sp,
                color = FathomTheme.colors.textTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PassageHistoryCard(
    item: HistoryWithPassage,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val context = LocalContext.current
    val passage = item.passage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable {
                passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) }
            }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                passage?.let {
                    TraditionBadge(traditionId = it.traditionId)
                    Text(
                        text = it.canonicalReference,
                        style = FathomTheme.typography.canonicalHeader,
                        color = FathomTheme.colors.textSecondary
                    )
                } ?: Text(
                    text = item.record.passageId,
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.textSecondary
                )
            }

            if (item.record.readCount > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.14f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${item.record.readCount}× READ",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.5.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = passage?.translation ?: "Passage text",
            style = FathomTheme.typography.verseDisplay.copy(
                fontSize = 15.sp,
                lineHeight = 23.sp
            ),
            color = FathomTheme.colors.textPrimary,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        SacredDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        passage?.let {
                            copyToClipboard(context, "${it.translation}\n— ${it.canonicalReference}")
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Copy,
                        contentDescription = "Copy Passage",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        passage?.let {
                            onEvent(LibraryUiEvent.OnOpenShare(it))
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShareNetwork,
                        contentDescription = "Share Quote Card",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        passage?.let { onEvent(LibraryUiEvent.OnOpenNoteDialog(it)) }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.NotePencil,
                        contentDescription = "Write Note",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            TextButton(
                onClick = { passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text(
                    text = "INSIGHTS",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.8.sp,
                    color = FathomTheme.colors.accentGold
                )
            }
        }
    }
}

@Composable
private fun ReflectionsItemCard(
    item: NoteWithPassage,
    onEvent: (LibraryUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            item.passage?.let {
                TraditionBadge(traditionId = it.traditionId)
                Text(
                    text = it.canonicalReference,
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.textSecondary
                )
            }

            IconButton(
                onClick = { onEvent(LibraryUiEvent.OnDeleteNote(item.note.id)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.TrashSimple,
                    contentDescription = "Delete Note",
                    tint = FathomTheme.colors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "“${item.passage?.translation ?: ""}”",
            style = FathomTheme.typography.verseOriginal.copy(
                fontSize = 14.5.sp,
                lineHeight = 22.sp
            ),
            color = FathomTheme.colors.textTertiary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        SacredDivider(modifier = Modifier.padding(vertical = 10.dp))

        Text(
            text = item.note.content,
            style = FathomTheme.typography.commentaryBody.copy(
                fontFamily = JakartaFontFamily,
                fontSize = 15.sp,
                lineHeight = 23.sp
            ),
            color = FathomTheme.colors.textPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            item.passage?.let { p ->
                TextButton(
                    onClick = { onEvent(LibraryUiEvent.OnOpenContext(p)) },
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = "VIEW INSIGHTS",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.8.sp,
                        color = FathomTheme.colors.accentGold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarAndFilters(
    searchQuery: String,
    selectedTradition: TraditionId?,
    onQueryChange: (String) -> Unit,
    onTraditionSelect: (TraditionId?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(FathomTheme.colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                contentDescription = null,
                tint = FathomTheme.colors.textTertiary,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JakartaFontFamily,
                    fontSize = 14.sp,
                    color = FathomTheme.colors.textPrimary
                ),
                cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search saved wisdom & notes...",
                            fontFamily = JakartaFontFamily,
                            fontSize = 13.5.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                    innerTextField()
                }
            )

            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Clear",
                        tint = FathomTheme.colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TraditionChip(
                label = "ALL",
                isSelected = selectedTradition == null,
                onClick = { onTraditionSelect(null) }
            )

            TraditionId.entries.forEach { tradition ->
                TraditionChip(
                    label = tradition.displayName.uppercase(),
                    isSelected = selectedTradition == tradition,
                    onClick = { onTraditionSelect(if (selectedTradition == tradition) null else tradition) }
                )
            }
        }
    }
}

@Composable
private fun TraditionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.18f) else FathomTheme.colors.surfaceElevated)
            .border(
                width = 1.dp,
                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontFamily = JakartaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            letterSpacing = 0.8.sp,
            color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
        )
    }
}

@Composable
private fun BookmarksList(
    bookmarks: List<BookmarkWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val context = LocalContext.current

    if (bookmarks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Saved Passages",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the bookmark icon on any passage in the feed to save it to your library.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 96.dp)
        ) {
            items(bookmarks, key = { it.bookmark.id }) { item ->
                val passage = item.passage
                var showTagEditor by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = FathomTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) }
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        passage?.let {
                            TraditionBadge(traditionId = it.traditionId)
                            Text(
                                text = it.canonicalReference,
                                style = FathomTheme.typography.canonicalHeader,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = { onEvent(LibraryUiEvent.OnRemoveBookmark(item.bookmark.passageId)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.TrashSimple,
                                contentDescription = "Remove Bookmark",
                                tint = FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = passage?.translation ?: "Passage text",
                        style = FathomTheme.typography.verseDisplay.copy(
                            fontSize = 16.5.sp,
                            lineHeight = 25.sp
                        ),
                        color = FathomTheme.colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (item.bookmark.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.bookmark.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    AnimatedVisibility(
                        visible = showTagEditor,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        TagEditorSection(
                            currentTags = item.bookmark.tags,
                            onSaveTags = { newTags ->
                                onEvent(LibraryUiEvent.OnUpdateBookmarkTags(item.bookmark.passageId, newTags))
                                showTagEditor = false
                            }
                        )
                    }

                    SacredDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    passage?.let {
                                        copyToClipboard(context, "${it.translation}\n— ${it.canonicalReference}")
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Copy,
                                    contentDescription = "Copy Passage",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    passage?.let {
                                        onEvent(LibraryUiEvent.OnOpenShare(it))
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.ShareNetwork,
                                    contentDescription = "Share Quote Card",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { showTagEditor = !showTagEditor },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Tag,
                                    contentDescription = "Tag Bookmark",
                                    tint = if (item.bookmark.tags.isNotEmpty()) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    passage?.let { onEvent(LibraryUiEvent.OnOpenNoteDialog(it)) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.NotePencil,
                                    contentDescription = "Write Note",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "INSIGHTS",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                letterSpacing = 0.8.sp,
                                color = FathomTheme.colors.accentGold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun TagEditorSection(
    currentTags: List<String>,
    onSaveTags: (List<String>) -> Unit
) {
    var tagInput by remember { mutableStateOf(currentTags.joinToString(", ")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FathomTheme.colors.surface)
            .padding(12.dp)
    ) {
        Text(
            text = "EDIT TAGS (COMMA SEPARATED)",
            style = FathomTheme.typography.caption.copy(
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = FathomTheme.colors.accentGold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JakartaFontFamily,
                    fontSize = 13.sp,
                    color = FathomTheme.colors.textPrimary
                ),
                cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = {
                    val parsed = tagInput.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    onSaveTags(parsed)
                }
            ) {
                Text(
                    text = "SAVE",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.accentGold
                )
            }
        }
    }
}

@Composable
private fun ReflectionsList(
    notes: List<NoteWithPassage>,
    onEvent: (LibraryUiEvent) -> Unit
) {
    if (notes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Personal Notes",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the pencil icon on any saved passage to record your personal notes and reflections.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 96.dp)
        ) {
            items(notes, key = { it.note.id }) { item ->
                ReflectionsItemCard(item = item, onEvent = onEvent)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun NoteEditDialog(
    passage: Passage,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val haptics = LocalFathomHaptics.current
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FathomTheme.colors.surface,
        title = {
            Column {
                Text(
                    text = "PERSONAL NOTE",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.3.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = passage.canonicalReference,
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "“${passage.translation}”",
                    style = FathomTheme.typography.verseOriginal.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    ),
                    color = FathomTheme.colors.textTertiary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    textStyle = TextStyle(
                        fontFamily = JakartaFontFamily,
                        fontSize = 14.5.sp,
                        lineHeight = 22.sp,
                        color = FathomTheme.colors.textPrimary
                    ),
                    cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 110.dp, max = 200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = FathomTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                text = "Write your reflection or personal note...",
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                        innerTextField()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (noteText.isNotBlank()) {
                        haptics.success()
                        onSave(noteText)
                    }
                },
                enabled = noteText.isNotBlank()
            ) {
                Text(
                    text = "SAVE NOTE",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (noteText.isNotBlank()) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    haptics.light()
                    onDismiss()
                }
            ) {
                Text(
                    text = "CANCEL",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.textSecondary
                )
            }
        }
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Fathom Wisdom", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Passage copied to clipboard", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    currentName: String,
    currentTradition: String?,
    onSave: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(currentName) }
    var selectedTradition by remember { mutableStateOf(currentTradition) }

    val traditionOptions = remember {
        listOf(
            null to "Universal / Seeker",
            TraditionId.HINDUISM.displayName to "Hinduism",
            TraditionId.BUDDHISM.displayName to "Buddhism",
            TraditionId.STOICISM.displayName to "Stoicism",
            TraditionId.TAOISM.displayName to "Taoism",
            TraditionId.ISLAM.displayName to "Islam",
            TraditionId.CHRISTIANITY.displayName to "Christianity",
            TraditionId.JUDAISM.displayName to "Judaism"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surfaceElevated,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FathomTheme.colors.borderSubtle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Profile",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FathomTheme.colors.textPrimary
                )
                IconButton(onClick = {
                    haptics.light()
                    onDismiss()
                }) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "YOUR NAME",
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = FathomTheme.colors.textPrimary
                ),
                cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surface)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                decorationBox = { innerTextField ->
                    if (name.isEmpty()) {
                        Text(
                            text = "Enter your name...",
                            fontFamily = JakartaFontFamily,
                            fontSize = 15.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SPIRITUAL PATH / FOCUS (OPTIONAL)",
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                traditionOptions.chunked(2).forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPair.forEach { (optionKey, label) ->
                            val isSelected = selectedTradition == optionKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.18f)
                                        else FathomTheme.colors.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.8f) else FathomTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        haptics.light()
                                        selectedTradition = optionKey
                                    }
                                    .padding(vertical = 12.dp, horizontal = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FathomTheme.colors.accentGold)
                    .clickable {
                        haptics.medium()
                        onSave(name, selectedTradition)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SAVE PROFILE",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = Color.Black
                )
            }
        }
    }
}
