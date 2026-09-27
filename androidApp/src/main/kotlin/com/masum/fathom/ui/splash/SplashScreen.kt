package com.masum.fathom.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val revealFraction = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        val writingDuration = 1200
        val steps = 6
        for (i in 1..steps) {
            launch {
                delay((writingDuration / steps.toFloat() * i).toLong())
                haptics.light()
            }
        }
        
        revealFraction.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = writingDuration, easing = FastOutSlowInEasing)
        )
        
        delay(100)
        haptics.medium()
        delay(800)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.drawWithContent {
                    clipRect(right = size.width * revealFraction.value) {
                        this@drawWithContent.drawContent()
                    }
                },
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "fathom",
                    style = FathomTheme.typography.brandTitle.copy(
                        fontSize = 42.sp,
                        lineHeight = 42.sp
                    ),
                    color = FathomTheme.colors.textPrimary
                )

                Text(
                    text = ".",
                    style = FathomTheme.typography.brandTitle.copy(
                        fontSize = 42.sp,
                        lineHeight = 42.sp
                    ),
                    color = FathomTheme.colors.accentGold
                )
            }
        }
    }
}
