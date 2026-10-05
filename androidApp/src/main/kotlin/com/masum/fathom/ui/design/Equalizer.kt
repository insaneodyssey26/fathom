package com.masum.fathom.ui.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Equalizer(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp, active: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "eq")
    val a by transition.animateFloat(0.25f, 1f, infiniteRepeatable(tween(520, easing = LinearEasing), RepeatMode.Reverse), label = "a")
    val b by transition.animateFloat(1f, 0.3f, infiniteRepeatable(tween(430, easing = LinearEasing), RepeatMode.Reverse), label = "b")
    val c by transition.animateFloat(0.4f, 0.95f, infiniteRepeatable(tween(610, easing = LinearEasing), RepeatMode.Reverse), label = "c")
    Canvas(modifier.size(size)) {
        val w = 3.dp.toPx()
        val gap = (this.size.width - w * 3) / 2f
        val levels = if (active) listOf(a, b, c) else listOf(0.3f, 0.3f, 0.3f)
        levels.forEachIndexed { i, v ->
            val h = this.size.height * v
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (w + gap), (this.size.height - h) / 2f),
                size = Size(w, h),
                cornerRadius = CornerRadius(w / 2f)
            )
        }
    }
}
