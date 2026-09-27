package com.masum.fathom.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.bold.Check
import com.adamglin.phosphoricons.regular.BookBookmark
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Sparkle
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.theme.FathomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptureSelectorSheet(
    availableBooks: List<Book>,
    selectedBookId: String?,
    onSelectBook: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = com.masum.fathom.core.haptics.LocalFathomHaptics.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val groupedByTradition = remember(availableBooks) { availableBooks.groupBy { it.traditionId } }
    val activeTradition = availableBooks.find { it.id == selectedBookId }?.traditionId
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.78f).dp

    val expandedMap = remember {
        mutableStateMapOf<TraditionId, Boolean>().apply {
            if (activeTradition != null) {
                put(activeTradition, true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = FathomTheme.colors.borderSubtle,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    text = "SCRIPTURES & BOOKS",
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Choose a specific text or explore across all traditions",
                    style = FathomTheme.typography.caption,
                    color = FathomTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val isAllSelected = selectedBookId == null
            AllScripturesMinimalRow(
                isSelected = isAllSelected,
                onClick = {
                    haptics.light()
                    onSelectBook(null)
                    onDismiss()
                }
            )

            SacredDivider(modifier = Modifier.padding(vertical = 12.dp))

            TraditionId.entries.forEach { tradition ->
                val books = groupedByTradition[tradition]
                if (!books.isNullOrEmpty()) {
                    val isExpanded = expandedMap[tradition] == true
                    val hasActiveBook = books.any { it.id == selectedBookId }

                    TraditionAccordionSection(
                        tradition = tradition,
                        books = books,
                        isExpanded = isExpanded,
                        hasActiveBook = hasActiveBook,
                        selectedBookId = selectedBookId,
                        onToggleExpand = {
                            expandedMap[tradition] = !isExpanded
                        },
                        onSelectBook = { bookId ->
                            onSelectBook(bookId)
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun AllScripturesMinimalRow(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) FathomTheme.colors.surfaceElevated else androidx.compose.ui.graphics.Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.8f) else FathomTheme.colors.borderSubtle,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Sparkle,
                contentDescription = null,
                tint = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = "All Scriptures",
                    style = FathomTheme.typography.commentaryBody.copy(fontSize = 14.sp),
                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                )
                Text(
                    text = "Global blend across all active traditions",
                    style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
                    color = FathomTheme.colors.textTertiary
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = PhosphorIcons.Bold.Check,
                contentDescription = "Selected",
                tint = FathomTheme.colors.accentGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TraditionAccordionSection(
    tradition: TraditionId,
    books: List<Book>,
    isExpanded: Boolean,
    hasActiveBook: Boolean,
    selectedBookId: String?,
    onToggleExpand: () -> Unit,
    onSelectBook: (String) -> Unit
) {
    val haptics = com.masum.fathom.core.haptics.LocalFathomHaptics.current
    val interactionSource = remember { MutableInteractionSource() }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chevron_rot"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (hasActiveBook || isExpanded) FathomTheme.colors.surfaceElevated.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (hasActiveBook) FathomTheme.colors.accentGold.copy(alpha = 0.4f) else FathomTheme.colors.borderSubtle.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptics.light()
                        onToggleExpand()
                    }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (hasActiveBook) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary)
                )
                Text(
                    text = tradition.displayName,
                    style = FathomTheme.typography.commentaryBody.copy(fontSize = 14.sp),
                    color = if (hasActiveBook) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                )
                Text(
                    text = "(${books.size})",
                    style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
                    color = FathomTheme.colors.textTertiary
                )
            }

            Icon(
                imageVector = PhosphorIcons.Regular.CaretDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (hasActiveBook) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(chevronRotation)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
            exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            ) {
                books.forEach { book ->
                    val isBookSelected = book.id == selectedBookId
                    val bookInteraction = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isBookSelected) FathomTheme.colors.surfaceElevated else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable(
                                interactionSource = bookInteraction,
                                indication = null,
                                onClick = {
                                    haptics.light()
                                    onSelectBook(book.id)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.BookBookmark,
                                contentDescription = null,
                                tint = if (isBookSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Column {
                                Text(
                                    text = book.title,
                                    style = FathomTheme.typography.commentaryBody.copy(fontSize = 13.sp),
                                    color = if (isBookSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                                )
                                val subText = buildString {
                                    append("${book.totalPassages} passages")
                                    if (!book.language.isNullOrBlank()) {
                                        append(" • ${book.language}")
                                    }
                                }
                                Text(
                                    text = subText,
                                    style = FathomTheme.typography.caption.copy(fontSize = 10.sp),
                                    color = FathomTheme.colors.textTertiary
                                )
                            }
                        }

                        if (isBookSelected) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.Check,
                                contentDescription = "Selected",
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
