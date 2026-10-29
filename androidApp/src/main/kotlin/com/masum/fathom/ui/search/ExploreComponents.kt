package com.masum.fathom.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowUpRight
import com.adamglin.phosphoricons.light.MagnifyingGlass
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.model.Book
import com.masum.fathom.core.model.SacredTopic
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.toneFor

fun SacredTopic.seed(): TraditionId? = when (this) {
    SacredTopic.PEACE_ANXIETY -> TraditionId.TAOISM
    SacredTopic.COURAGE_STRENGTH -> TraditionId.HINDUISM
    SacredTopic.FORGIVENESS_GRACE -> TraditionId.CHRISTIANITY
    SacredTopic.GRIEF_SOLACE -> TraditionId.STOICISM
    SacredTopic.WISDOM_PURPOSE -> TraditionId.BUDDHISM
    SacredTopic.GRATITUDE_JOY -> TraditionId.ISLAM
    SacredTopic.SURRENDER_TRUST -> TraditionId.JUDAISM
    SacredTopic.STILLNESS_PRESENCE -> null
}

@Composable
fun EditorialSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val line by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(300), label = "search_line")
    val glyph by animateColorAsState(if (focused) tone.hue else colors.textTertiary, tween(300), label = "search_glyph")
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(52.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(PhosphorIcons.Light.MagnifyingGlass, null, tint = glyph, modifier = Modifier.size(22.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = "Verses, ideas, feelings",
                        style = typography.headline.copy(fontSize = 22.sp, lineHeight = 28.sp),
                        color = colors.textTertiary.copy(alpha = 0.75f),
                        maxLines = 1
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    interactionSource = source,
                    textStyle = typography.headline.copy(fontSize = 22.sp, lineHeight = 28.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(tone.hue),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboard?.hide()
                        focus.clearFocus()
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(tween(160)) + scaleIn(tween(200), initialScale = 0.7f),
                exit = fadeOut(tween(120)) + scaleOut(tween(140), targetScale = 0.7f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceElevated)
                        .tappable(scale = 0.88f) { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Light.X, "Clear search", tint = colors.textSecondary, modifier = Modifier.size(15.dp))
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(line))
    }
}

@Composable
fun TraditionChips(
    selected: TraditionId?,
    onSelect: (TraditionId?) -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TraditionChip(label = "All", id = null, selected = selected == null, onClick = { onSelect(null) })
        }
        items(TraditionId.entries.toList()) { id ->
            TraditionChip(label = id.displayName, id = id, selected = selected == id, onClick = { onSelect(id) })
        }
    }
}

@Composable
private fun TraditionChip(label: String, id: TraditionId?, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = remember(id, colors.isDark, colors.background) { toneFor(id, colors) }
    val container by animateColorAsState(if (selected) tone.hue else Color.Transparent, tween(260), label = "chip_bg")
    val content by animateColorAsState(if (selected) tone.onHue else colors.textSecondary, tween(260), label = "chip_fg")
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(container)
            .border(0.75.dp, if (selected) Color.Transparent else colors.hairline, shape)
            .tappable(scale = 0.94f, onClick = onClick)
            .padding(start = 12.dp, end = 16.dp)
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TraditionEmblem(traditionId = id, color = if (selected) tone.onHue else tone.hue, size = 16.dp, filled = selected)
        Text(text = label, style = FathomTheme.typography.label, color = content, maxLines = 1)
    }
}

@Composable
fun TopicMosaic(
    columns: Int,
    onSelect: (SacredTopic) -> Unit,
    modifier: Modifier = Modifier
) {
    val topics = SacredTopic.entries
    val colCount = if (columns > 2) columns else 2
    val rows = topics.chunked(colCount)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowTopics ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowTopics.forEach { topic ->
                    val index = topics.indexOf(topic)
                    TopicTile(
                        topic = topic,
                        index = index,
                        modifier = Modifier.weight(1f).height(160.dp),
                        onClick = { onSelect(topic) }
                    )
                }
                if (rowTopics.size < colCount) {
                    repeat(colCount - rowTopics.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicTile(
    topic: SacredTopic,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = remember(topic, colors.isDark, colors.background) { toneFor(topic.seed(), colors) }
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        tone.hue.copy(alpha = if (colors.isDark) 0.24f else 0.20f),
                        tone.hue.copy(alpha = 0.05f)
                    ),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset(600f, 600f)
                )
            )
            .border(1.dp, tone.hue.copy(alpha = 0.22f), shape)
            .tappable(scale = 0.98f, onClick = onClick)
            .clipToBounds()
    ) {
        Icon(
            imageVector = topic.icon,
            contentDescription = null,
            tint = tone.hue.copy(alpha = 0.14f),
            modifier = Modifier
                .size(124.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 28.dp, y = 24.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            MonoLabel(text = (index + 1).toString().padStart(2, '0'), color = tone.hue)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = topic.title,
                    style = typography.title.copy(
                        fontSize = 19.sp,
                        lineHeight = 23.sp
                    ),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = topic.subtitle,
                    style = typography.caption,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun BookRow(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = remember(book.traditionId, colors.isDark, colors.background) { toneFor(book.traditionId, colors) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tappable(scale = 0.985f, onClick = onClick)
                .padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            TraditionEmblem(traditionId = book.traditionId, color = tone.hue, size = 30.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = book.title,
                    style = typography.title.copy(fontSize = 22.sp, lineHeight = 27.sp),
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                MonoLabel(
                    text = "${book.traditionId.displayName} · ${book.language} · ${book.totalPassages} verses",
                    color = colors.textTertiary
                )
            }
            Icon(PhosphorIcons.Light.ArrowUpRight, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
        Hairline()
    }
}

@Composable
fun SectionHeading(overline: String, title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MonoLabel(text = overline, color = FathomTheme.tone.hue)
            Text(text = title, style = FathomTheme.typography.headline, color = FathomTheme.colors.textPrimary)
        }
        if (trailing != null) MonoLabel(text = trailing, color = FathomTheme.colors.textTertiary)
    }
}
