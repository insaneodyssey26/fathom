package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.masum.fathom.ui.design.tappable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.WidthClass
import com.masum.fathom.ui.design.fadeEdges
import com.masum.fathom.ui.design.softReveal
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun VersePage(
    passage: Passage,
    reveal: () -> Float,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    endGutter: androidx.compose.ui.unit.Dp = 64.dp,
    showThemes: Boolean = true,
    footer: (@Composable () -> Unit)? = null
) {
    val shell = LocalShell.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val wide = shell.widthClass == WidthClass.Expanded && !passage.originalText.isNullOrBlank()
    val length = passage.translation.length
    val base = when {
        length < 90 -> 36
        length < 170 -> 30
        length < 320 -> 26
        else -> 22
    }
    val size = if (shell.widthClass == WidthClass.Compact) base else base + 4
    val verseStyle = typography.verseDisplay.copy(
        fontSize = size.sp,
        lineHeight = (size * 1.28f).sp,
        color = colors.textPrimary
    )
    val originalLong = (passage.originalText?.length ?: 0) > 110
    val originalStyle = typography.verseOriginal.copy(
        color = colors.textSecondary,
        textDirection = TextDirection.Content,
        fontSize = if (originalLong) 16.sp else 19.sp,
        lineHeight = if (originalLong) 25.sp else 32.sp
    )

    var showFullOriginal by remember(passage.id) { mutableStateOf(false) }
    val collapsible = originalLong
    val originalText: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = passage.originalText.orEmpty(),
                style = originalStyle,
                maxLines = if (collapsible && !showFullOriginal) 3 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis
            )
            if (collapsible) {
                MonoLabel(
                    text = if (showFullOriginal) "Show less" else "Show full original",
                    color = tone.hue,
                    modifier = Modifier.tappable(scale = 0.95f, haptic = false) { showFullOriginal = !showFullOriginal }.padding(vertical = 4.dp)
                )
            }
        }
    }
    val originalScroll = rememberScrollState()
    val translationScroll = rememberScrollState()
    val singleScroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(start = shell.startInset),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = if (wide) 1040.dp else shell.contentMaxWidth)
                .fillMaxWidth()
                .padding(start = 28.dp, end = endGutter),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = passage.traditionId.displayName, color = tone.hue)
                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(colors.textTertiary))
                MonoLabel(
                    text = passage.canonicalReference,
                    modifier = Modifier.weight(1f, fill = false),
                    color = colors.textSecondary
                )
            }

            Spacer(Modifier.height(if (shell.isShort) 14.dp else 28.dp))

            if (wide) {
                Row(horizontalArrangement = Arrangement.spacedBy(48.dp), verticalAlignment = Alignment.Top) {
                    Column(
                        modifier = Modifier.weight(0.8f).fadeEdges(top = if (originalScroll.canScrollBackward) 12.dp else 0.dp, bottom = if (originalScroll.canScrollForward) 32.dp else 0.dp).verticalScroll(originalScroll).softReveal(reveal),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        originalText()
                    }
                    Column(
                        modifier = Modifier.weight(1.2f).fadeEdges(top = if (translationScroll.canScrollBackward) 12.dp else 0.dp, bottom = if (translationScroll.canScrollForward) 32.dp else 0.dp).verticalScroll(translationScroll).softReveal(reveal),
                        verticalArrangement = Arrangement.spacedBy(22.dp)
                    ) {
                        Text(text = passage.translation, style = verseStyle)
                        footer?.invoke()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fadeEdges(top = if (singleScroll.canScrollBackward) 12.dp else 0.dp, bottom = if (singleScroll.canScrollForward) 32.dp else 0.dp)
                        .verticalScroll(singleScroll)
                        .softReveal(reveal),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    if (!passage.originalText.isNullOrBlank()) {
                        originalText()
                        Hairline(modifier = Modifier.width(36.dp), color = tone.hue.copy(alpha = 0.7f))
                    }
                    Text(text = passage.translation, style = verseStyle)
                    footer?.invoke()
                }
            }

            if (showThemes && passage.themes.isNotEmpty()) {
                Spacer(Modifier.height(if (shell.isShort) 12.dp else 26.dp))
                Text(
                    text = passage.themes.take(3).joinToString("   ") { "#" + it.name.lowercase() },
                    style = typography.mono.copy(letterSpacing = 0.6.sp),
                    color = colors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
