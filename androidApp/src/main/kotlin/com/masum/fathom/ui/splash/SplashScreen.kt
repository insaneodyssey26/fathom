package com.masum.fathom.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.delay

private val Sequence = listOf(
    TraditionId.HINDUISM,
    TraditionId.BUDDHISM,
    TraditionId.TAOISM,
    TraditionId.ISLAM,
    TraditionId.CHRISTIANITY,
    TraditionId.JUDAISM,
    TraditionId.STOICISM,
    null
)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    var index by remember { mutableIntStateOf(0) }
    val tone = rememberTone(Sequence[index])
    val settled = index == Sequence.lastIndex
    val wordAlpha by animateFloatAsState(if (index >= 4) 1f else 0f, tween(700, easing = FathomMotion.Emphasized), label = "word")
    val tagAlpha by animateFloatAsState(if (settled) 1f else 0f, tween(600, 120, FathomMotion.Emphasized), label = "tag")

    LaunchedEffect(Unit) {
        delay(240)
        for (i in Sequence.indices) {
            index = i
            if (i < Sequence.lastIndex) haptics.light()
            delay(210)
        }
        haptics.medium()
        delay(820)
        onSplashFinished()
    }

    Atmosphere(tone = tone, modifier = modifier.fillMaxSize(), strength = 1.25f) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TraditionEmblem(
                traditionId = Sequence[index],
                color = tone.hue,
                size = 84.dp,
                filled = !settled
            )
            Spacer(Modifier.height(30.dp))
            Text(
                text = "fathom",
                style = FathomTheme.typography.brandTitle.copy(fontSize = 52.sp, lineHeight = 54.sp, letterSpacing = (-1.6).sp),
                color = FathomTheme.colors.textPrimary,
                modifier = Modifier.graphicsLayer {
                    alpha = wordAlpha
                    translationY = (1f - wordAlpha) * 16.dp.toPx()
                }
            )
            Spacer(Modifier.height(10.dp))
            MonoLabel(
                text = "Sacred wisdom, offline",
                modifier = Modifier.graphicsLayer { alpha = tagAlpha },
                color = FathomTheme.colors.textSecondary
            )
        }
    }
}
