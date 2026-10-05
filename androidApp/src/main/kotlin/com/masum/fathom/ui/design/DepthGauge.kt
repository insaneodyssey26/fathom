package com.masum.fathom.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DepthGauge(
    position: () -> Float,
    count: Int,
    hue: Color,
    onScrub: (Int) -> Unit,
    onStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hint: Boolean = false,
    onHintShown: () -> Unit = {},
    height: Dp = 320.dp
) {
    val colors = FathomTheme.colors
    val haptics = LocalFathomHaptics.current
    val scope = rememberCoroutineScope()
    val faint = colors.textTertiary
    val window = 10
    val spacing = 15.dp

    val lift = remember { Animatable(0f) }
    var scrubbing by remember { mutableStateOf(false) }
    var scrubStart by remember { mutableIntStateOf(0) }
    var scrubTarget by remember { mutableIntStateOf(0) }
    var dragPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(scrubbing) {
        lift.animateTo(if (scrubbing) 1f else 0f, FathomMotion.bounce())
    }
    LaunchedEffect(hint) {
        if (hint) {
            kotlinx.coroutines.delay(1400)
            repeat(2) {
                lift.animateTo(0.7f, tween(380, easing = FathomMotion.Emphasized))
                lift.animateTo(0f, tween(520, easing = FathomMotion.Emphasized))
            }
            onHintShown()
        }
    }

    val current = position().roundToInt().coerceIn(0, (count - 1).coerceAtLeast(0))

    Box(
        modifier = modifier
            .width(64.dp)
            .height(height)
            .semantics {
                contentDescription = "Verse position"
                stateDescription = "Verse ${current + 1} of $count"
                customActions = listOf(
                    CustomAccessibilityAction("Next verse") { onStep(1); true },
                    CustomAccessibilityAction("Previous verse") { onStep(-1); true }
                )
            }
            .pointerInput(count) {
                detectTapGestures { offset ->
                    val step = if (offset.y < size.height / 2f) -1 else 1
                    haptics.light()
                    onStep(step)
                }
            }
            .pointerInput(count) {
                val stepPx = spacing.toPx()
                detectVerticalDragGestures(
                    onDragStart = {
                        scrubbing = true
                        scrubStart = position().roundToInt().coerceIn(0, (count - 1).coerceAtLeast(0))
                        scrubTarget = scrubStart
                        dragPx = 0f
                        haptics.medium()
                    },
                    onDragEnd = { scrubbing = false },
                    onDragCancel = { scrubbing = false },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragPx += amount
                        val rel = -dragPx / stepPx
                        val boost = 1f + (abs(rel) / 24f).coerceAtMost(2.5f)
                        val target = (scrubStart + (rel * boost).roundToInt()).coerceIn(0, (count - 1).coerceAtLeast(0))
                        if (target != scrubTarget) {
                            scrubTarget = target
                            haptics.light()
                            onScrub(target)
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pos = position()
            val base = pos.roundToInt()
            val midY = size.height / 2f
            val step = spacing.toPx() * (1f + 0.18f * lift.value)
            val reach = 1f + 0.55f * lift.value
            if (lift.value > 0.01f) {
                drawLine(
                    color = hue.copy(alpha = 0.18f * lift.value),
                    start = Offset(size.width - 4.dp.toPx(), midY - size.height * 0.46f),
                    end = Offset(size.width - 4.dp.toPx(), midY + size.height * 0.46f),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            for (i in -window..window) {
                val idx = base + i
                if (idx < 0 || idx >= count) continue
                val rel = idx - pos
                val dist = abs(rel)
                if (dist > window) continue
                val y = midY + rel * step
                val fade = (1f - dist / window).coerceIn(0f, 1f)
                val isCurrent = dist < 0.5f
                val long = idx % 5 == 0
                val length = when {
                    isCurrent -> (20.dp.toPx() * (1f - dist) + 8.dp.toPx() * dist) * reach
                    long -> 12.dp.toPx() * reach
                    else -> 6.dp.toPx() * reach
                }
                val boosted = faint.copy(alpha = (0.16f + 0.5f * fade + 0.25f * lift.value).coerceAtMost(0.95f))
                val color = if (isCurrent) hue.copy(alpha = 1f - dist) else boosted
                drawLine(
                    color = color,
                    start = Offset(size.width - 4.dp.toPx(), y),
                    end = Offset(size.width - 4.dp.toPx() - length, y),
                    strokeWidth = if (isCurrent) 1.6.dp.toPx() + 0.8.dp.toPx() * lift.value else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .offset(x = (-30).dp - (20.dp * lift.value))
                .graphicsLayer { transformOrigin = androidx.compose.ui.graphics.TransformOrigin(1f, 0.5f) }
                .clip(RoundedCornerShape(14.dp))
                .background(colors.background.copy(alpha = 0.86f * lift.value))
                .padding(horizontal = (10 * lift.value).dp, vertical = (6 * lift.value).dp)
        ) {
            Text(
                text = (current + 1).toString().padStart(3, '0'),
                style = FathomTheme.typography.mono.copy(fontSize = (9f + 11f * lift.value).sp, lineHeight = (12f + 12f * lift.value).sp),
                color = hue
            )
            if (lift.value > 0.05f) {
                Text(
                    text = "of ${count.toString().padStart(3, '0')}",
                    style = FathomTheme.typography.mono.copy(fontSize = 9.sp),
                    color = colors.textTertiary,
                    modifier = Modifier.graphicsLayer { alpha = lift.value }
                )
            }
        }
    }
}
