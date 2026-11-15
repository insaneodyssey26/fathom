package com.masum.fathom.ui.feed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.BookmarkSimple
import kotlin.math.cos
import kotlin.math.sin

private const val RayCount = 14
private const val Tau = 6.2831855f

@Composable
fun SaveBloom(
    origin: () -> Offset,
    progress: () -> Float,
    hue: Color,
    modifier: Modifier = Modifier
) {
    val glyphSize = 64.dp
    val glyphPx = with(LocalDensity.current) { glyphSize.roundToPx() }
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val p = progress()
            if (p >= 1f) return@Canvas
            val center = origin()
            val eased = 1f - (1f - p) * (1f - p) * (1f - p)
            val glowRadius = 40.dp.toPx() + 150.dp.toPx() * eased
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(hue.copy(alpha = 0.34f * (1f - p)), Color.Transparent),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )
            val ringRadius = 26.dp.toPx() + 96.dp.toPx() * eased
            drawCircle(
                color = hue.copy(alpha = 0.55f * (1f - p)),
                radius = ringRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx() * (1f - p) + 0.5.dp.toPx())
            )
            for (i in 0 until RayCount) {
                val angle = (i.toFloat() / RayCount) * Tau + 0.2f
                val inner = ringRadius * 0.72f
                val outer = ringRadius * (0.72f + 0.32f * (1f - p))
                drawLine(
                    color = hue.copy(alpha = 0.8f * (1f - p)),
                    start = Offset(center.x + cos(angle) * inner, center.y + sin(angle) * inner),
                    end = Offset(center.x + cos(angle) * outer, center.y + sin(angle) * outer),
                    strokeWidth = 1.6.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
        Icon(
            imageVector = PhosphorIcons.Fill.BookmarkSimple,
            contentDescription = null,
            tint = hue,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    val c = origin()
                    IntOffset((c.x - glyphPx / 2f).toInt(), (c.y - glyphPx / 2f).toInt())
                }
                .size(glyphSize)
                .graphicsLayer {
                    val p = progress()
                    val rise = if (p < 0.28f) p / 0.28f else 1f
                    val settle = 0.6f + 0.55f * rise - 0.15f * (if (p > 0.28f) (p - 0.28f) / 0.72f else 0f)
                    scaleX = settle
                    scaleY = settle
                    translationY = -28.dp.toPx() * p
                    alpha = if (p >= 1f) 0f else if (p < 0.55f) 1f else 1f - (p - 0.55f) / 0.45f
                }
        )
    }
}
