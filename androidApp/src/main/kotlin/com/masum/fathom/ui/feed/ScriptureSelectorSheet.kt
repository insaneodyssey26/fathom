package com.masum.fathom.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.Check
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.toneFor

@Composable
fun ScriptureSelectorSheet(
    availableBooks: List<Book>,
    selectedBookId: String?,
    selectedTraditions: Set<TraditionId> = emptySet(),
    onSelectBook: (String?) -> Unit,
    onSelectTradition: (TraditionId?) -> Unit = {},
    onDismiss: () -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val grouped = remember(availableBooks) { availableBooks.groupBy { it.traditionId } }
    val activeTradition = availableBooks.find { it.id == selectedBookId }?.traditionId
        ?: if (selectedTraditions.size == 1) selectedTraditions.first() else null
    val expanded = remember { mutableStateMapOf<TraditionId, Boolean>().apply { activeTradition?.let { put(it, true) } } }
    val everything = selectedBookId == null && (selectedTraditions.isEmpty() || selectedTraditions.size == TraditionId.entries.size)

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(overline = "Library", title = "Choose a scripture", subtitle = "Read one text, one tradition, or everything.", onClose = onDismiss)
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Hairline()
            SelectorRow(
                title = "All scriptures",
                caption = "A blend across every tradition",
                traditionId = null,
                selected = everything,
                onClick = {
                    haptics.light()
                    onSelectTradition(null)
                    onSelectBook(null)
                    onDismiss()
                }
            )
            TraditionId.entries.forEach { tradition ->
                val books = grouped[tradition].orEmpty()
                if (books.isEmpty()) return@forEach
                val open = expanded[tradition] == true
                val wholeSelected = selectedBookId == null && selectedTraditions.size == 1 && tradition in selectedTraditions
                val active = wholeSelected || books.any { it.id == selectedBookId }
                TraditionGroup(
                    tradition = tradition,
                    books = books,
                    open = open,
                    active = active,
                    wholeSelected = wholeSelected,
                    selectedBookId = selectedBookId,
                    onToggle = {
                        haptics.light()
                        expanded[tradition] = !open
                    },
                    onSelectWhole = {
                        haptics.light()
                        onSelectTradition(tradition)
                        onDismiss()
                    },
                    onSelectBook = {
                        haptics.light()
                        onSelectBook(it)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun SelectorRow(
    title: String,
    caption: String,
    traditionId: TraditionId?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    inset: Boolean = false
) {
    val colors = FathomTheme.colors
    val tone = remember(traditionId, colors.isDark, colors.background) { toneFor(traditionId, colors) }
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.985f, haptic = false, onClick = onClick).padding(vertical = 16.dp).padding(start = if (inset) 44.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!inset) TraditionEmblem(traditionId = traditionId, color = if (selected) tone.hue else colors.textTertiary, size = 26.dp, filled = selected)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = FathomTheme.typography.title,
                    color = if (selected) colors.textPrimary else colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                MonoLabel(text = caption, color = colors.textTertiary)
            }
            if (selected) Icon(PhosphorIcons.Light.Check, "Selected", tint = tone.hue, modifier = Modifier.size(20.dp))
        }
        Hairline()
    }
}

@Composable
private fun TraditionGroup(
    tradition: TraditionId,
    books: List<Book>,
    open: Boolean,
    active: Boolean,
    wholeSelected: Boolean,
    selectedBookId: String?,
    onToggle: () -> Unit,
    onSelectWhole: () -> Unit,
    onSelectBook: (String) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = remember(tradition, colors.isDark, colors.background) { toneFor(tradition, colors) }
    val turn by animateFloatAsState(if (open) 180f else 0f, FathomMotion.glide(), label = "group_turn")
    val ink by animateColorAsState(if (active) colors.textPrimary else colors.textSecondary, tween(220), label = "group_ink")

    Column(modifier = Modifier.fillMaxWidth().animateContentSize(FathomMotion.glide())) {
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.985f, haptic = false, onClick = onToggle).padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TraditionEmblem(traditionId = tradition, color = if (active) tone.hue else colors.textTertiary, size = 26.dp, filled = active)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = tradition.displayName, style = FathomTheme.typography.title, color = ink)
                MonoLabel(text = "${books.size} ${if (books.size == 1) "text" else "texts"}", color = colors.textTertiary)
            }
            Icon(PhosphorIcons.Light.CaretDown, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp).rotate(turn))
        }
        AnimatedVisibility(
            visible = open,
            enter = expandVertically(tween(380, easing = FathomMotion.Emphasized)) + fadeIn(tween(260, 60)),
            exit = shrinkVertically(tween(240)) + fadeOut(tween(140))
        ) {
            Column {
                SelectorRow(
                    title = "Every text in ${tradition.displayName}",
                    caption = "The whole tradition",
                    traditionId = tradition,
                    selected = wholeSelected,
                    onClick = onSelectWhole,
                    inset = true
                )
                books.forEach { book ->
                    SelectorRow(
                        title = book.title,
                        caption = "${book.language} · ${book.totalPassages} verses",
                        traditionId = tradition,
                        selected = book.id == selectedBookId,
                        onClick = { onSelectBook(book.id) },
                        inset = true
                    )
                }
            }
        }
        Hairline()
    }
}
