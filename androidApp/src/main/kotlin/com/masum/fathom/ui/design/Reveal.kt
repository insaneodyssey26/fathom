package com.masum.fathom.ui.design

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.softReveal(progress: () -> Float, softness: Float = 0.42f, rise: Float = 14f): Modifier =
    this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
            translationY = (1f - progress().coerceIn(0f, 1f)) * rise * density
        }
        .drawWithContent {
            drawContent()
            val p = progress().coerceIn(0f, 1f)
            if (p < 1f) {
                val h = size.height.coerceAtLeast(1f)
                val lead = p * (1f + softness)
                val tail = (lead - softness).coerceIn(0f, 1f)
                val head = lead.coerceIn(tail, 1f)
                drawRect(
                    brush = Brush.verticalGradient(
                        0f to Color.Black,
                        tail to Color.Black,
                        head to Color.Transparent,
                        1f to Color.Transparent,
                        startY = 0f,
                        endY = h
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }

fun Modifier.fadeEdges(top: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Hairline, bottom: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Hairline): Modifier =
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val h = size.height.coerceAtLeast(1f)
            val t = (top.toPx() / h).coerceIn(0f, 0.5f)
            val b = (bottom.toPx() / h).coerceIn(0f, 0.5f)
            drawRect(
                brush = Brush.verticalGradient(
                    0f to if (t > 0f) Color.Transparent else Color.Black,
                    t.coerceAtLeast(0.0001f) to Color.Black,
                    (1f - b).coerceAtLeast(t + 0.0002f) to Color.Black,
                    1f to if (b > 0f) Color.Transparent else Color.Black,
                    startY = 0f,
                    endY = h
                ),
                blendMode = BlendMode.DstIn
            )
        }
