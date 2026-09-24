package com.masum.fathom.ui.library

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.NotePencil
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TrashSimple
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.feed.PassageContextSheet
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = FathomTheme.typography.brandTitle,
                color = FathomTheme.colors.textPrimary
            )
        }

        when (state) {
            is LibraryUiState.Loading -> {
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
                val coroutineScope = rememberCoroutineScope()
                val pagerState = rememberPagerState(
                    initialPage = state.selectedTab.ordinal,
                    pageCount = { LibraryTab.entries.size }
                )

                LaunchedEffect(pagerState.currentPage) {
                    val targetTab = LibraryTab.entries[pagerState.currentPage]
                    if (targetTab != state.selectedTab) {
                        onEvent(LibraryUiEvent.OnSelectTab(targetTab))
                    }
                }

                LaunchedEffect(state.selectedTab) {
                    if (pagerState.currentPage != state.selectedTab.ordinal) {
                        pagerState.animateScrollToPage(state.selectedTab.ordinal)
                    }
                }

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = FathomTheme.colors.background,
                    contentColor = FathomTheme.colors.accentGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = FathomTheme.colors.accentGold
                        )
                    },
                    divider = {}
                ) {
                    LibraryTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = when (tab) {
                                        LibraryTab.BOOKMARKS -> "SAVED (${state.bookmarks.size})"
                                        LibraryTab.REFLECTIONS -> "REFLECTIONS (${state.notes.size})"
                                        LibraryTab.HISTORY -> "HISTORY"
                                    },
                                    style = FathomTheme.typography.traditionBadge,
                                    color = if (pagerState.currentPage == index) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                                )
                            }
                        )
                    }
                }

                if (state.selectedTab == LibraryTab.BOOKMARKS || state.selectedTab == LibraryTab.REFLECTIONS) {
                    SearchBarAndFilters(
                        searchQuery = state.searchQuery,
                        selectedTradition = state.selectedTraditionFilter,
                        onQueryChange = { onEvent(LibraryUiEvent.OnSearchQueryChanged(it)) },
                        onTraditionSelect = { onEvent(LibraryUiEvent.OnFilterTradition(it)) }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (LibraryTab.entries[page]) {
                        LibraryTab.BOOKMARKS -> BookmarksList(state.bookmarks, onEvent)
                        LibraryTab.REFLECTIONS -> ReflectionsList(state.notes, onEvent)
                        LibraryTab.HISTORY -> HistoryStatsView(state.history, state.totalReadCount)
                    }
                }

                state.selectedPassageForContext?.let { contextPassage ->
                    PassageContextSheet(
                        passage = contextPassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseContext) }
                    )
                }

                state.selectedPassageForNote?.let { notePassage ->
                    NoteEditDialog(
                        passage = notePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseNoteDialog) },
                        onSave = { content ->
                            onEvent(LibraryUiEvent.OnSaveNote(notePassage.id, content))
                        }
                    )
                }

                state.selectedPassageForShare?.let { sharePassage ->
                    com.masum.fathom.ui.components.QuoteCardModal(
                        passage = sharePassage,
                        onDismiss = { onEvent(LibraryUiEvent.OnCloseShare) }
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
            .padding(horizontal = 20.dp, vertical = 8.dp)
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
                            text = "Search saved wisdom & insights...",
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
                        fontFamily = SyneFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the bookmark icon on any passage in the feed to save it to your sacred sanctuary.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)
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
                                    contentDescription = "Write Reflection",
                                    tint = FathomTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "EXEGESIS",
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
                    text = "No Personal Reflections",
                    style = FathomTheme.typography.commentaryTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the pencil icon on any saved passage to record your personal contemplations and marginalia notes.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            items(notes, key = { it.note.id }) { item ->
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
                                contentDescription = "Delete Reflection",
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
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "OPEN CONTEXT",
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
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FathomTheme.colors.surface,
        title = {
            Column {
                Text(
                    text = "SACRED MARGINALIA",
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
                        fontFamily = SyneFontFamily,
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
                                text = "Write your contemplation, lesson, or marginalia note...",
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
                        onSave(noteText)
                    }
                },
                enabled = noteText.isNotBlank()
            ) {
                Text(
                    text = "SAVE REFLECTION",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (noteText.isNotBlank()) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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


@Composable
private fun HistoryStatsView(
    history: List<com.masum.fathom.core.model.user.ReadingRecord>,
    totalCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "EXPLORATION RECORD",
            style = FathomTheme.typography.canonicalHeader,
            color = FathomTheme.colors.accentGold
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FathomTheme.colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    color = FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalCount",
                    style = FathomTheme.typography.brandTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.accentGold
                )
                Text(
                    text = "Total Contemplations",
                    style = FathomTheme.typography.caption.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp
                    ),
                    color = FathomTheme.colors.textSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${history.size}",
                    style = FathomTheme.typography.brandTitle.copy(
                        fontFamily = SyneFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = FathomTheme.colors.textPrimary
                )
                Text(
                    text = "Unique Verses Explored",
                    style = FathomTheme.typography.caption.copy(
                        fontFamily = JakartaFontFamily,
                        fontSize = 13.sp
                    ),
                    color = FathomTheme.colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "OFFLINE INTEGRITY",
            style = FathomTheme.typography.canonicalHeader,
            color = FathomTheme.colors.accentGold
        )

        Spacer(modifier = Modifier.height(10.dp))

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
            Text(
                text = "100% On-Device Storage",
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = FathomTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "All your bookmarks, notes, marginalia, and reading history reside in private local SQLite databases on your device.",
                fontFamily = JakartaFontFamily,
                fontSize = 13.5.sp,
                lineHeight = 20.sp,
                color = FathomTheme.colors.textSecondary
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("Fathom Wisdom", text)
    clipboard?.setPrimaryClip(clip)
    Toast.makeText(context, "Passage copied to clipboard", Toast.LENGTH_SHORT).show()
}

