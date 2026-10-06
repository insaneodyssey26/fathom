package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.fill.Pause as PauseFill
import com.adamglin.phosphoricons.fill.Play as PlayFill
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.ui.design.Equalizer
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import java.util.Locale

@Composable
fun AmbienceFloatingPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val engine = LocalSanctuaryAmbienceEngine.current
    val state by engine.state.collectAsState()
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone

    AnimatedVisibility(
        visible = state.currentTrack != null,
        enter = fadeIn(tween(240)) + scaleIn(tween(360, easing = FathomMotion.Emphasized), initialScale = 0.88f),
        exit = fadeOut(tween(160)) + scaleOut(tween(200), targetScale = 0.90f),
        modifier = modifier
    ) {
        val track = state.currentTrack ?: return@AnimatedVisibility
        val pillShape = RoundedCornerShape(24.dp)
        val edgeColor = if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
        val innerGlow = if (colors.isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.35f)

        Row(
            modifier = Modifier
                .height(44.dp)
                .shadow(
                    elevation = if (colors.isDark) 12.dp else 8.dp,
                    shape = pillShape,
                    ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.10f),
                    spotColor = tone.hue.copy(alpha = 0.16f)
                )
                .glass(
                    shape = pillShape,
                    tint = colors.background.copy(alpha = if (colors.isDark) 0.76f else 0.86f),
                    blur = 24.dp,
                    outline = false
                )
                .border(width = 0.75.dp, color = edgeColor, shape = pillShape)
                .drawBehind {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(innerGlow, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.5f
                        ),
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }
                .tappable(scale = 0.96f, onClick = onClick)
                .padding(start = 14.dp, end = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Equalizer(color = tone.hue, size = 15.dp, active = state.isPlaying)

            Text(
                text = track.title,
                style = FathomTheme.typography.label.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (state.isPlaying) colors.textPrimary else colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            state.sleepTimerSecondsRemaining?.let {
                MonoLabel(
                    text = String.format(Locale.US, "%02d:%02d", it / 60, it % 60),
                    color = tone.hue
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tone.hue)
                    .tappable(scale = 0.90f, haptic = false) { engine.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isPlaying) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                    contentDescription = if (state.isPlaying) "Pause soundscape" else "Play soundscape",
                    tint = tone.onHue,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
