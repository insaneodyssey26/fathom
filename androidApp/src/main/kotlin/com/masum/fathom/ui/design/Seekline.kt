package com.masum.fathom.ui.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun Seekline(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = FathomTheme.tone.hue,
    track: Color = FathomTheme.colors.hairline
) {
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(progress) }
    val shown = if (dragging) dragValue else progress.coerceIn(0f, 1f)
    val thumb by animateFloatAsState(if (dragging) 1f else 0f, FathomMotion.press(), label = "seek_thumb")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset -> onSeek((offset.x / size.width).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        dragValue = (offset.x / size.width).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        onSeek(dragValue)
                        dragging = false
                    },
                    onDragCancel = { dragging = false }
                ) { change, _ ->
                    dragValue = (change.position.x / size.width).coerceIn(0f, 1f)
                }
            }
    ) {
        val y = size.height / 2f
        val stroke = 2.dp.toPx() + thumb * 1.dp.toPx()
        drawLine(track, Offset(0f, y), Offset(size.width, y), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(0f, y), Offset(size.width * shown, y), strokeWidth = stroke, cap = StrokeCap.Round)
        drawCircle(color, radius = (3.dp.toPx() + thumb * 5.dp.toPx()), center = Offset(size.width * shown, y))
    }
}
