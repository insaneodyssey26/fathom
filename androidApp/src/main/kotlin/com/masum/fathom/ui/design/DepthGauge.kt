package com.masum.fathom.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.ui.theme.FathomTheme
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DepthGauge(
    position: () -> Float,
    count: Int,
    hue: Color,
    modifier: Modifier = Modifier,
    height: Dp = 280.dp
) {
    val colors = FathomTheme.colors
    val faint = colors.textTertiary
    val window = 9
    val spacing = 15.dp

    Box(modifier = modifier.width(44.dp).height(height), contentAlignment = Alignment.CenterEnd) {
        Canvas(modifier = Modifier.width(44.dp).height(height)) {
            val pos = position()
            val base = pos.roundToInt()
            val midY = size.height / 2f
            val step = spacing.toPx()
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
                    isCurrent -> 20.dp.toPx() * (1f - dist)  + 8.dp.toPx() * dist
                    long -> 12.dp.toPx()
                    else -> 6.dp.toPx()
                }
                val color = if (isCurrent) hue.copy(alpha = 1f - dist) else faint.copy(alpha = 0.16f + 0.5f * fade)
                drawLine(
                    color = color,
                    start = Offset(size.width - 4.dp.toPx(), y),
                    end = Offset(size.width - 4.dp.toPx() - length, y),
                    strokeWidth = if (isCurrent) 1.6.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            text = (position().roundToInt() + 1).toString().padStart(3, '0'),
            style = FathomTheme.typography.mono.copy(fontSize = 9.sp),
            color = hue,
            modifier = Modifier.offset(x = (-30).dp, y = (-1).dp)
        )
    }
}
