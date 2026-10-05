package com.masum.fathom.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import com.masum.fathom.core.model.TraditionId

private fun polygonFor(id: TraditionId?): RoundedPolygon = when (id) {
    TraditionId.HINDUISM -> RoundedPolygon.star(12, innerRadius = 0.58f, rounding = CornerRounding(0.18f), innerRounding = CornerRounding(0.12f))
    TraditionId.BUDDHISM -> RoundedPolygon.star(8, innerRadius = 0.7f, rounding = CornerRounding(0.14f))
    TraditionId.STOICISM -> RoundedPolygon.star(4, innerRadius = 0.72f, rounding = CornerRounding(0.04f))
    TraditionId.TAOISM -> RoundedPolygon.circle(numVertices = 12)
    TraditionId.ISLAM -> RoundedPolygon.star(8, innerRadius = 0.707f, rounding = CornerRounding(0.02f))
    TraditionId.CHRISTIANITY -> RoundedPolygon.star(4, innerRadius = 0.42f, rounding = CornerRounding(0.28f), innerRounding = CornerRounding(0.18f))
    TraditionId.JUDAISM -> RoundedPolygon.star(6, innerRadius = 0.577f, rounding = CornerRounding(0.02f))
    null -> RoundedPolygon.star(6, innerRadius = 0.78f, rounding = CornerRounding(0.4f))
}.normalized()

private val polygons: Map<TraditionId?, RoundedPolygon> by lazy {
    (TraditionId.entries + listOf<TraditionId?>(null)).associateWith { polygonFor(it) }
}

private fun Morph.toComposePath(progress: Float, into: Path): Path {
    into.rewind()
    val cubics = asCubics(progress)
    if (cubics.isEmpty()) return into
    into.moveTo(cubics.first().anchor0X, cubics.first().anchor0Y)
    cubics.forEach {
        into.cubicTo(it.control0X, it.control0Y, it.control1X, it.control1Y, it.anchor1X, it.anchor1Y)
    }
    into.close()
    return into
}

@Composable
fun TraditionEmblem(
    traditionId: TraditionId?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    filled: Boolean = false
) {
    var pair by remember { mutableStateOf(traditionId to traditionId) }
    val progress = remember { Animatable(1f) }
    val path = remember { Path() }
    val morph = remember(pair) { Morph(polygons.getValue(pair.first), polygons.getValue(pair.second)) }

    LaunchedEffect(traditionId) {
        if (pair.second != traditionId) {
            pair = pair.second to traditionId
            progress.snapTo(0f)
            progress.animateTo(1f, spring(dampingRatio = 0.66f, stiffness = 110f))
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val strokePx = 1.5.dp.toPx()
        val radius = this.size.minDimension / 2f * 0.92f
        translate(this.size.width / 2f, this.size.height / 2f) {
            scale(radius * 2f, radius * 2f, pivot = Offset.Zero) {
                translate(-0.5f, -0.5f) {
                    drawMorph(morph.toComposePath(progress.value, path), color, filled, strokePx / (radius * 2f))
                }
            }
        }
    }
}

private fun DrawScope.drawMorph(path: Path, color: Color, filled: Boolean, strokeWidth: Float) {
    if (filled) drawPath(path, color, style = Fill) else drawPath(path, color, style = Stroke(width = strokeWidth))
}

enum class GlyphKind { Square, Circle, Hexagon, Star }

private val glyphPolygons: Map<GlyphKind, RoundedPolygon> by lazy {
    mapOf(
        GlyphKind.Square to RoundedPolygon(4, rounding = CornerRounding(0.22f)).normalized(),
        GlyphKind.Circle to RoundedPolygon.circle(numVertices = 10).normalized(),
        GlyphKind.Hexagon to RoundedPolygon(6, rounding = CornerRounding(0.2f)).normalized(),
        GlyphKind.Star to RoundedPolygon.star(8, innerRadius = 0.62f, rounding = CornerRounding(0.12f)).normalized()
    )
}

@Composable
fun Glyph(
    kind: GlyphKind,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    filled: Boolean = true
) {
    val path = remember(kind) {
        Path().apply {
            val cubics = glyphPolygons.getValue(kind).cubics
            moveTo(cubics.first().anchor0X, cubics.first().anchor0Y)
            cubics.forEach { cubicTo(it.control0X, it.control0Y, it.control1X, it.control1Y, it.anchor1X, it.anchor1Y) }
            close()
        }
    }
    Canvas(modifier = modifier.size(size)) {
        val side = this.size.minDimension
        val strokePx = 1.5.dp.toPx()
        scale(side, side, pivot = Offset.Zero) {
            if (filled) drawPath(path, color, style = Fill) else drawPath(path, color, style = Stroke(width = strokePx / side))
        }
    }
}

object EmblemShapes {
    fun androidPath(id: TraditionId?, size: Float): android.graphics.Path {
        val cubics = polygons.getValue(id).cubics
        return android.graphics.Path().apply {
            moveTo(cubics.first().anchor0X * size, cubics.first().anchor0Y * size)
            cubics.forEach {
                cubicTo(
                    it.control0X * size, it.control0Y * size,
                    it.control1X * size, it.control1Y * size,
                    it.anchor1X * size, it.anchor1Y * size
                )
            }
            close()
        }
    }
}
