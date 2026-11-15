package com.masum.fathom.ui.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomMotion

private const val StaggerLimit = 8

@Composable
fun Modifier.staggerIn(
    index: Int,
    rise: Dp = 12.dp,
    step: Int = 32,
    baseDelay: Int = 0
): Modifier {
    val enabled = index in 0..StaggerLimit
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (enabled) progress.animateTo(1f, tween(320, baseDelay + index * step, FathomMotion.Emphasized))
    }
    return graphicsLayer {
        val p = progress.value
        alpha = p
        translationY = (1f - p) * rise.toPx()
    }
}

@Composable
fun StaggerItem(index: Int, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.staggerIn(index), content = content)
}

@Composable
fun rememberDrift(period: Int = 9000): State<Float> {
    val transition = rememberInfiniteTransition(label = "drift")
    val t = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(period, easing = LinearEasing)),
        label = "drift_t"
    )
    return t
}
