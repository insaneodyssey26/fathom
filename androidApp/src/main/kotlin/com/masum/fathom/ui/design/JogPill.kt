package com.masum.fathom.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun JogPill(
    onStep: (Int) -> Unit,
    hue: Color,
    modifier: Modifier = Modifier,
    hint: Boolean = false,
    onHintShown: () -> Unit = {},
    height: Dp = 176.dp
) {
    val colors = FathomTheme.colors
    val haptics = LocalFathomHaptics.current
    val scope = rememberCoroutineScope()
    val knobSize = 30.dp
    val width = 42.dp

    val shift = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    var norm by remember { mutableFloatStateOf(0f) }
    var travelPx by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(hint) {
        if (hint) {
            delay(1500)
            repeat(2) {
                shift.animateTo(-travelPx * 0.55f, tween(340, easing = FathomMotion.Emphasized))
                shift.animateTo(travelPx * 0.55f, tween(560, easing = FathomMotion.Emphasized))
                shift.animateTo(0f, FathomMotion.bounce())
            }
            onHintShown()
        }
    }

    LaunchedEffect(dragging) {
        while (dragging) {
            val magnitude = abs(norm)
            if (magnitude < 0.2f) {
                delay(40)
                continue
            }
            val direction = if (norm > 0f) 1 else -1
            haptics.light()
            onStep(direction)
            val strength = ((magnitude - 0.2f) / 0.8f).coerceIn(0f, 1f)
            delay((780 - 640 * strength).toLong())
        }
    }

    val pull = (abs(shift.value) / travelPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(colors.background.copy(alpha = 0.45f))
            .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
            .semantics {
                contentDescription = "Verse navigation"
                customActions = listOf(
                    CustomAccessibilityAction("Next verse") { onStep(1); true },
                    CustomAccessibilityAction("Previous verse") { onStep(-1); true }
                )
            }
            .pointerInput(Unit) {
                travelPx = (size.height - knobSize.toPx() - 8.dp.toPx()) / 2f
                detectVerticalDragGestures(
                    onDragStart = {
                        dragging = true
                        haptics.medium()
                    },
                    onDragEnd = {
                        dragging = false
                        norm = 0f
                        scope.launch { shift.animateTo(0f, FathomMotion.bounce()) }
                    },
                    onDragCancel = {
                        dragging = false
                        norm = 0f
                        scope.launch { shift.animateTo(0f, FathomMotion.bounce()) }
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        val next = (shift.value + amount).coerceIn(-travelPx, travelPx)
                        norm = next / travelPx
                        scope.launch { shift.snapTo(next) }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.width(width).height(height)) {
            val cx = size.width / 2f
            val tickColor = colors.textTertiary
            val gap = 7.dp.toPx()
            val top = 22.dp.toPx()
            var y = top
            var i = 0
            while (y < size.height - top) {
                val centerDistance = abs(y - size.height / 2f) / (size.height / 2f)
                val len = if (i % 4 == 0) 7.dp.toPx() else 4.dp.toPx()
                drawLine(
                    color = tickColor.copy(alpha = 0.14f + 0.18f * (1f - centerDistance)),
                    start = Offset(cx - len / 2f, y),
                    end = Offset(cx + len / 2f, y),
                    strokeWidth = 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
                y += gap
                i++
            }
            val chevron = 4.dp.toPx()
            val activeUp = if (norm < -0.2f) 1f else 0.4f
            val activeDown = if (norm > 0.2f) 1f else 0.4f
            drawLine(hue.copy(alpha = activeUp), Offset(cx - chevron, 13.dp.toPx() + chevron * 0.6f), Offset(cx, 13.dp.toPx()), 1.6.dp.toPx(), StrokeCap.Round)
            drawLine(hue.copy(alpha = activeUp), Offset(cx + chevron, 13.dp.toPx() + chevron * 0.6f), Offset(cx, 13.dp.toPx()), 1.6.dp.toPx(), StrokeCap.Round)
            val by = size.height - 13.dp.toPx()
            drawLine(hue.copy(alpha = activeDown), Offset(cx - chevron, by - chevron * 0.6f), Offset(cx, by), 1.6.dp.toPx(), StrokeCap.Round)
            drawLine(hue.copy(alpha = activeDown), Offset(cx + chevron, by - chevron * 0.6f), Offset(cx, by), 1.6.dp.toPx(), StrokeCap.Round)
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(0, shift.value.roundToInt()) }
                .size(knobSize)
                .graphicsLayer {
                    val s = 1f + 0.12f * pull
                    scaleX = s
                    scaleY = s + 0.2f * pull
                }
                .clip(CircleShape)
                .background(hue)
                .border(2.5.dp, colors.background.copy(alpha = 0.55f), CircleShape)
        )
    }
}
