package com.masum.fathom.ui.design

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.rememberTone

@Composable
fun StateFrame(
    modifier: Modifier = Modifier,
    traditionId: TraditionId? = null,
    content: @Composable () -> Unit
) {
    val tone = rememberTone(traditionId)
    Atmosphere(tone = tone, modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
fun BreathingMark(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FathomMotion.Settle), RepeatMode.Reverse),
        label = "breath"
    )
    TraditionEmblem(
        traditionId = null,
        color = FathomTheme.colors.textSecondary,
        size = 44.dp,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}

@Composable
fun QuietMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = title, style = FathomTheme.typography.headline, color = FathomTheme.colors.textPrimary, textAlign = TextAlign.Center)
        Text(text = body, style = FathomTheme.typography.body, color = FathomTheme.colors.textSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(10.dp))
            FathomButton(text = action, onClick = onAction)
        }
    }
}
