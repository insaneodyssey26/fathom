package com.masum.fathom.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.BookmarkSimple as BookmarkFilled
import com.adamglin.phosphoricons.fill.Pause as PauseFilled
import com.adamglin.phosphoricons.light.BookmarkSimple
import com.adamglin.phosphoricons.light.ChatCenteredText
import com.adamglin.phosphoricons.light.ShareFat
import com.adamglin.phosphoricons.light.SpeakerHigh
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.toneFor
import kotlinx.coroutines.launch

fun highlighted(text: String, query: String, color: Color, background: Color): AnnotatedString {
    val terms = query.trim().split(Regex("\\s+")).filter { it.length > 1 }
    if (terms.isEmpty()) return AnnotatedString(text)
    val lower = text.lowercase()
    return buildAnnotatedString {
        append(text)
        terms.forEach { term ->
            val needle = term.lowercase()
            var index = lower.indexOf(needle)
            while (index >= 0) {
                addStyle(SpanStyle(color = color, background = background), index, index + needle.length)
                index = lower.indexOf(needle, index + needle.length)
            }
        }
    }
}

@Composable
fun PassageEntry(
    passage: Passage,
    saved: Boolean,
    onSave: () -> Unit,
    onReflect: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    highlight: String = "",
    showTradition: Boolean = true,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    extraActions: (@Composable RowScope.() -> Unit)? = null
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current
    val audio = LocalSacredAudioEngine.current
    val playback by audio.playbackState.collectAsState()
    val speaking = (playback as? SacredAudioState.Playing)?.passage?.id == passage.id
    val hue = remember(passage.traditionId, colors.isDark, colors.background) { toneFor(passage.traditionId, colors).hue }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (showTradition) {
                MonoLabel(text = passage.traditionId.displayName, color = hue)
                Box(Modifier.size(3.dp).clip(CircleShape).background(colors.textTertiary))
            }
            MonoLabel(text = passage.canonicalReference, color = colors.textSecondary, modifier = Modifier.weight(1f, fill = false))
        }

        val original = passage.originalText
        if (!original.isNullOrBlank() && original != passage.translation) {
            Spacer(Modifier.height(14.dp))
            Text(
                text = original,
                style = typography.verseOriginal.copy(fontSize = 16.sp, lineHeight = 25.sp, textDirection = TextDirection.Content),
                color = colors.textTertiary,
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            text = highlighted(passage.translation, highlight, hue, hue.copy(alpha = 0.16f)),
            style = typography.verseDisplay.copy(fontSize = 21.sp, lineHeight = 30.sp),
            color = colors.textPrimary
        )

        extraContent?.invoke(this)

        if (passage.explanations.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            MeaningBlock(passage = passage, hue = hue)
        } else {
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            extraActions?.invoke(this)
            EntryAction(onClick = {
                haptics.medium()
                audio.toggle(passage)
            }) {
                Icon(
                    imageVector = if (speaking) PhosphorIcons.Fill.PauseFilled else PhosphorIcons.Light.SpeakerHigh,
                    contentDescription = "Listen",
                    tint = if (speaking) hue else colors.textSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }
            EntryAction(onClick = onReflect) {
                Icon(PhosphorIcons.Light.ChatCenteredText, "Context", tint = colors.textSecondary, modifier = Modifier.size(21.dp))
            }
            EntryAction(onClick = onShare) {
                Icon(PhosphorIcons.Light.ShareFat, "Share", tint = colors.textSecondary, modifier = Modifier.size(21.dp))
            }
            EntryAction(onClick = {
                if (!saved) haptics.success() else haptics.light()
                onSave()
            }) {
                SaveMark(saved = saved, hue = hue)
            }
        }
        Hairline(modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
fun EntryAction(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .tappable(scale = 0.86f, haptic = false, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SaveMark(saved: Boolean, hue: Color) {
    val colors = FathomTheme.colors
    val burst = remember { Animatable(1f) }
    val pop = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(saved) }
    LaunchedEffect(saved) {
        if (saved && !previous) {
            burst.snapTo(0f)
            pop.snapTo(0.6f)
            launch { burst.animateTo(1f, tween(560, easing = FathomMotion.Emphasized)) }
            pop.animateTo(1f, FathomMotion.bounce())
        }
        previous = saved
    }

    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(40.dp)) {
            val p = burst.value
            if (p < 1f) {
                val radius = size.minDimension / 2f * (0.35f + 0.65f * p)
                for (i in 0 until 8) {
                    val angle = (i / 8f) * 6.2831855f
                    drawCircle(
                        hue.copy(alpha = 1f - p),
                        radius = 2.dp.toPx() * (1f - p * 0.6f),
                        center = Offset(size.width / 2f + kotlin.math.cos(angle) * radius, size.height / 2f + kotlin.math.sin(angle) * radius)
                    )
                }
            }
        }
        Icon(
            imageVector = if (saved) PhosphorIcons.Fill.BookmarkFilled else PhosphorIcons.Light.BookmarkSimple,
            contentDescription = "Save",
            tint = if (saved) hue else colors.textSecondary,
            modifier = Modifier.size(21.dp).graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
            }
        )
    }
}
