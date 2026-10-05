package com.masum.fathom.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.NotePencil
import com.adamglin.phosphoricons.light.PencilSimpleLine
import com.adamglin.phosphoricons.light.Tag
import com.adamglin.phosphoricons.light.Trash
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.EntryAction
import com.masum.fathom.ui.components.PassageEntry
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.QuietMessage
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.toneFor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun ListFrame(
    topPadding: Dp,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val shell = LocalShell.current
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight().widthIn(max = shell.contentMaxWidth + 96.dp).padding(start = shell.startInset),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = topPadding + 16.dp, bottom = shell.bottomInset + 40.dp),
            content = content
        )
    }
}

@Composable
private fun EmptyJournal(title: String, body: String, topPadding: Dp) {
    val shell = LocalShell.current
    Box(Modifier.fillMaxSize().padding(start = shell.startInset, top = topPadding, bottom = shell.bottomInset).padding(32.dp), contentAlignment = Alignment.Center) {
        QuietMessage(title = title, body = body)
    }
}

@Composable
fun SavedList(
    bookmarks: List<BookmarkWithPassage>,
    searching: Boolean,
    topPadding: Dp,
    onEvent: (LibraryUiEvent) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyJournal(
            title = if (searching) "No matches" else "Nothing saved yet",
            body = if (searching) "No saved passage fits this search." else "Tap the bookmark on any verse to keep it here.",
            topPadding = topPadding
        )
        return
    }
    ListFrame(topPadding) {
        items(bookmarks, key = { it.bookmark.id }) { item ->
            val passage = item.passage ?: return@items
            SavedEntry(item = item, passage = passage, onEvent = onEvent)
        }
    }
}

@Composable
private fun SavedEntry(item: BookmarkWithPassage, passage: Passage, onEvent: (LibraryUiEvent) -> Unit) {
    val colors = FathomTheme.colors
    var tagging by remember(item.bookmark.id) { mutableStateOf(false) }
    val hue = remember(passage.traditionId, colors.isDark, colors.background) { toneFor(passage.traditionId, colors).hue }

    PassageEntry(
        passage = passage,
        saved = true,
        onSave = { onEvent(LibraryUiEvent.OnRemoveBookmark(item.bookmark.passageId)) },
        onReflect = { onEvent(LibraryUiEvent.OnOpenContext(passage)) },
        onShare = { onEvent(LibraryUiEvent.OnOpenShare(passage)) },
        modifier = Modifier.padding(top = 26.dp),
        extraContent = {
            if (item.bookmark.tags.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                MonoLabel(text = item.bookmark.tags.joinToString("   ") { "#$it" }, color = hue, maxLines = 2)
            }
            AnimatedVisibility(
                visible = tagging,
                enter = expandVertically(tween(320)) + fadeIn(tween(240)),
                exit = shrinkVertically(tween(220)) + fadeOut(tween(140))
            ) {
                TagEditor(
                    current = item.bookmark.tags,
                    onSave = {
                        onEvent(LibraryUiEvent.OnUpdateBookmarkTags(item.bookmark.passageId, it))
                        tagging = false
                    }
                )
            }
        },
        extraActions = {
            EntryAction(onClick = { onEvent(LibraryUiEvent.OnOpenNoteDialog(passage)) }) {
                Icon(PhosphorIcons.Light.NotePencil, "Write a reflection", tint = colors.textSecondary, modifier = Modifier.size(21.dp))
            }
            EntryAction(onClick = { tagging = !tagging }) {
                Icon(PhosphorIcons.Light.Tag, "Edit tags", tint = if (item.bookmark.tags.isNotEmpty() || tagging) hue else colors.textSecondary, modifier = Modifier.size(21.dp))
            }
        }
    )
}

@Composable
private fun TagEditor(current: List<String>, onSave: (List<String>) -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    var input by remember { mutableStateOf(current.joinToString(", ")) }
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val rule by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(240), label = "tag_rule")
    Column(Modifier.fillMaxWidth().padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MonoLabel(text = "Tags, separated by commas")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                interactionSource = source,
                textStyle = FathomTheme.typography.body.copy(fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(tone.hue),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            )
            Box(Modifier.tappable(scale = 0.94f) { onSave(input.split(",").map { it.trim() }.filter { it.isNotBlank() }) }.padding(8.dp)) {
                MonoLabel(text = "Save", color = tone.hue)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(rule))
    }
}

@Composable
fun NotesList(
    notes: List<NoteWithPassage>,
    searching: Boolean,
    topPadding: Dp,
    onEvent: (LibraryUiEvent) -> Unit
) {
    if (notes.isEmpty()) {
        EmptyJournal(
            title = if (searching) "No matches" else "No reflections yet",
            body = if (searching) "No reflection fits this search." else "Write on a saved passage and your thoughts will live here.",
            topPadding = topPadding
        )
        return
    }
    ListFrame(topPadding) {
        items(notes, key = { it.note.id }) { item -> NoteEntry(item = item, onEvent = onEvent) }
    }
}

@Composable
private fun NoteEntry(item: NoteWithPassage, onEvent: (LibraryUiEvent) -> Unit) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val passage = item.passage
    val hue = remember(passage?.traditionId, colors.isDark, colors.background) { toneFor(passage?.traditionId, colors).hue }
    val stamp = remember(item.note.updatedAtEpochMs) {
        Instant.ofEpochMilli(item.note.updatedAtEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
    }
    Column(Modifier.fillMaxWidth().padding(top = 26.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            MonoLabel(text = passage?.canonicalReference ?: "Passage", color = hue, modifier = Modifier.weight(1f, fill = false))
            MonoLabel(text = stamp, color = colors.textTertiary)
        }
        passage?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it.translation,
                style = typography.commentaryBody.copy(fontFamily = ScriptureFamily, fontStyle = FontStyle.Italic, fontSize = 16.sp, lineHeight = 23.sp),
                color = colors.textTertiary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
            Box(Modifier.padding(end = 16.dp).width(2.dp).fillMaxHeight().background(hue))
            Text(text = item.note.content, style = typography.verseDisplay.copy(fontSize = 21.sp, lineHeight = 30.sp), color = colors.textPrimary)
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically) {
            passage?.let { p ->
                EntryAction(onClick = { onEvent(LibraryUiEvent.OnOpenNoteDialog(p)) }) {
                    Icon(PhosphorIcons.Light.PencilSimpleLine, "Edit reflection", tint = colors.textSecondary, modifier = Modifier.size(21.dp))
                }
            }
            EntryAction(onClick = { onEvent(LibraryUiEvent.OnDeleteNote(item.note.id)) }) {
                Icon(PhosphorIcons.Light.Trash, "Delete reflection", tint = colors.textSecondary, modifier = Modifier.size(21.dp))
            }
        }
        Hairline(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun TimelineList(history: List<HistoryWithPassage>, topPadding: Dp, onOpen: (Passage) -> Unit) {
    if (history.isEmpty()) {
        EmptyJournal(title = "No history yet", body = "Verses you read in the feed and the scriptures will be recorded here.", topPadding = topPadding)
        return
    }
    val zone = remember { ZoneId.systemDefault() }
    val grouped = remember(history) {
        history.sortedByDescending { it.record.lastSeenAtEpochMs }
            .groupBy { Instant.ofEpochMilli(it.record.lastSeenAtEpochMs).atZone(zone).toLocalDate() }
    }
    val today = remember { LocalDate.now() }
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE · d MMMM", Locale.getDefault()) }
    ListFrame(topPadding) {
        grouped.forEach { (date, entries) ->
            item(key = "day_$date") {
                MonoLabel(
                    text = when (date) {
                        today -> "Today"
                        today.minusDays(1) -> "Yesterday"
                        else -> date.format(formatter)
                    },
                    color = FathomTheme.tone.hue,
                    modifier = Modifier.padding(top = 28.dp, bottom = 2.dp)
                )
            }
            items(entries, key = { "h_${it.record.passageId}_${it.record.lastSeenAtEpochMs}" }) { entry ->
                HistoryRow(item = entry, onClick = { entry.passage?.let(onOpen) })
            }
        }
    }
}

@Composable
fun CanonDetail(
    traditionId: TraditionId,
    history: List<HistoryWithPassage>,
    bookmarks: List<BookmarkWithPassage>,
    notes: List<NoteWithPassage>,
    topPadding: Dp,
    onEvent: (LibraryUiEvent) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val canonHistory = remember(history, traditionId) { history.filter { it.passage?.traditionId == traditionId } }
    val canonSaved = remember(bookmarks, traditionId) { bookmarks.filter { it.passage?.traditionId == traditionId } }
    val canonNotes = remember(notes, traditionId) { notes.filter { it.passage?.traditionId == traditionId } }
    var section by remember { mutableIntStateOf(0) }
    val labels = listOf("Read" to canonHistory.size, "Saved" to canonSaved.size, "Notes" to canonNotes.size)
    val listState = rememberLazyListState()

    ListFrame(topPadding) {
        item(key = "canon_header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                TraditionEmblem(traditionId = traditionId, color = tone.hue, size = 52.dp, filled = true)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = traditionId.displayName, style = FathomTheme.typography.display, color = colors.textPrimary)
                    MonoLabel(text = "${canonHistory.size} read · ${canonSaved.size} saved · ${canonNotes.size} notes", color = colors.textTertiary)
                }
            }
        }
        item(key = "canon_tabs") {
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                labels.forEachIndexed { index, (label, count) ->
                    val selected = section == index
                    val ink by animateColorAsState(if (selected) colors.textPrimary else colors.textTertiary, tween(220), label = "canon_tab")
                    Column(
                        modifier = Modifier.tappable(scale = 0.94f, haptic = false) { section = index },
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                            Text(text = label, style = FathomTheme.typography.headline.copy(fontSize = 24.sp, lineHeight = 28.sp), color = ink)
                            MonoLabel(text = count.toString(), color = if (selected) tone.hue else colors.textTertiary)
                        }
                        Box(Modifier.width(24.dp).height(2.dp).background(if (selected) tone.hue else androidx.compose.ui.graphics.Color.Transparent))
                    }
                }
            }
        }
        when (section) {
            0 -> if (canonHistory.isEmpty()) item(key = "e0") { EmptySection("Nothing read from ${traditionId.displayName} yet.") }
            else items(canonHistory, key = { "ch_${it.record.passageId}" }) { entry -> HistoryRow(item = entry, onClick = { entry.passage?.let { onEvent(LibraryUiEvent.OnOpenContext(it)) } }) }

            1 -> if (canonSaved.isEmpty()) item(key = "e1") { EmptySection("No saved passages from ${traditionId.displayName}.") }
            else items(canonSaved, key = { "cs_${it.bookmark.id}" }) { entry -> entry.passage?.let { SavedEntry(item = entry, passage = it, onEvent = onEvent) } }

            else -> if (canonNotes.isEmpty()) item(key = "e2") { EmptySection("No reflections on ${traditionId.displayName} yet.") }
            else items(canonNotes, key = { "cn_${it.note.id}" }) { entry -> NoteEntry(item = entry, onEvent = onEvent) }
        }
    }
}

@Composable
private fun EmptySection(text: String) {
    Text(text = text, style = FathomTheme.typography.body, color = FathomTheme.colors.textTertiary, modifier = Modifier.padding(vertical = 28.dp))
}
