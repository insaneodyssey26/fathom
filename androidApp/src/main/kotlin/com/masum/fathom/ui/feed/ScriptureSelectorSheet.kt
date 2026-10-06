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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.adamglin.phosphoricons.light.Check
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.rememberSheetCloser
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
    alignedTraditions: Set<TraditionId> = emptySet(),
    allowAllScriptures: Boolean = true,
    showWholeTraditionOption: Boolean = true,
    onSelectBook: (String?) -> Unit,
    onSelectTradition: (TraditionId?) -> Unit = {},
    onToggleAlignedTradition: (TraditionId) -> Unit = {},
    onClearAlignedTraditions: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val colors = FathomTheme.colors
    val grouped = remember(availableBooks) { availableBooks.groupBy { it.traditionId } }
    val activeTradition = availableBooks.find { it.id == selectedBookId }?.traditionId
        ?: if (selectedTraditions.size == 1) selectedTraditions.first() else null
    val expanded = remember { mutableStateMapOf<TraditionId, Boolean>().apply { activeTradition?.let { put(it, true) } } }
    val isAllScriptures = selectedBookId == null && (selectedTraditions.isEmpty() || selectedTraditions.size == TraditionId.entries.size)

    val allCaption = when {
        !isAllScriptures -> "A blend across every tradition"
        alignedTraditions.isEmpty() -> "Balanced blend across every tradition"
        alignedTraditions.size == 1 -> "Prioritizing ${alignedTraditions.first().displayName} with universal discovery"
        else -> "Prioritizing ${alignedTraditions.size} traditions with universal discovery"
    }

    FathomSheet(onDismiss = onDismiss) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(overline = "Library", title = "Choose a scripture", subtitle = "Read one text, one tradition, or everything.", onClose = onDismiss)
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            if (allowAllScriptures) {
                Hairline()

                Column(modifier = Modifier.fillMaxWidth()) {
                    SelectorRow(
                        title = "All scriptures",
                        caption = allCaption,
                        traditionId = null,
                        selected = isAllScriptures,
                        onClick = {
                            haptics.light()
                            onSelectTradition(null)
                            onSelectBook(null)
                            closeSheet()
                        }
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MonoLabel(
                                text = "Lean feed towards (optional)",
                                color = colors.textTertiary
                            )
                            if (alignedTraditions.isNotEmpty()) {
                                Text(
                                    text = "Reset",
                                    style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
                                    color = colors.textSecondary,
                                    modifier = Modifier.tappable(scale = 0.95f, onClick = {
                                        haptics.light()
                                        onClearAlignedTraditions()
                                    })
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TraditionId.entries.forEach { tradition ->
                                val isSelected = isAllScriptures && tradition in alignedTraditions
                                AlignedTraditionChip(
                                    tradition = tradition,
                                    selected = isSelected,
                                    onClick = {
                                        haptics.light()
                                        onToggleAlignedTradition(tradition)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Hairline()

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
                    showWholeTraditionOption = showWholeTraditionOption,
                    onToggle = {
                        haptics.light()
                        expanded[tradition] = !open
                    },
                    onSelectWhole = {
                        haptics.light()
                        onSelectTradition(tradition)
                        closeSheet()
                    },
                    onSelectBook = {
                        haptics.light()
                        onSelectBook(it)
                        closeSheet()
                    }
                )
            }
        }
    }
}

@Composable
private fun AlignedTraditionChip(
    tradition: TraditionId,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = remember(tradition, colors.isDark, colors.background) { toneFor(tradition, colors) }
    val bg = if (selected) tone.hue.copy(alpha = 0.18f) else colors.surfaceElevated
    val border = if (selected) tone.hue.copy(alpha = 0.6f) else colors.hairline

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .tappable(scale = 0.95f, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TraditionEmblem(traditionId = tradition, color = if (selected) tone.hue else colors.textSecondary, size = 16.dp, filled = selected)
        Text(
            text = tradition.displayName,
            style = FathomTheme.typography.caption.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
            color = if (selected) colors.textPrimary else colors.textSecondary
        )
        if (selected) {
            Icon(PhosphorIcons.Light.Check, null, tint = tone.hue, modifier = Modifier.size(12.dp))
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
    showWholeTraditionOption: Boolean,
    onToggle: () -> Unit,
    onSelectWhole: () -> Unit,
    onSelectBook: (String) -> Unit
) {
    val colors = FathomTheme.colors
    val tone = remember(tradition, colors.isDark, colors.background) { toneFor(tradition, colors) }
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(320, easing = FathomMotion.Emphasized), label = "group_turn")
    val ink by animateColorAsState(if (active) colors.textPrimary else colors.textSecondary, tween(240), label = "group_ink")

    Column(modifier = Modifier.fillMaxWidth()) {
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
            enter = expandVertically(tween(340, easing = FathomMotion.Emphasized)) + fadeIn(tween(240, 60, easing = FathomMotion.Emphasized)),
            exit = shrinkVertically(tween(300, easing = FathomMotion.Emphasized)) + fadeOut(tween(220, easing = FathomMotion.Emphasized))
        ) {
            Column {
                if (showWholeTraditionOption) {
                    SelectorRow(
                        title = "Every text in ${tradition.displayName}",
                        caption = "The whole tradition",
                        traditionId = tradition,
                        selected = wholeSelected,
                        onClick = onSelectWhole,
                        inset = true
                    )
                }
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
