package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Waves
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import java.util.Locale

@Composable
fun AmbienceFloatingPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val ambienceEngine = LocalSanctuaryAmbienceEngine.current
    val state by ambienceEngine.state.collectAsState()

    AnimatedVisibility(
        visible = state.currentTrack != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        val track = state.currentTrack ?: return@AnimatedVisibility

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(FathomTheme.colors.surfaceElevated.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = if (state.isPlaying) FathomTheme.colors.accentGold.copy(alpha = 0.5f) else FathomTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable {
                    haptics.light()
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Waves,
                contentDescription = null,
                tint = if (state.isPlaying) FathomTheme.colors.accentGold else FathomTheme.colors.textTertiary,
                modifier = Modifier.size(15.dp)
            )

            Text(
                text = track.title,
                fontFamily = JakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp,
                color = if (state.isPlaying) FathomTheme.colors.textPrimary else FathomTheme.colors.textSecondary
            )

            if (state.sleepTimerSecondsRemaining != null) {
                val rem = state.sleepTimerSecondsRemaining ?: 0
                val min = rem / 60
                val sec = rem % 60
                Text(
                    text = String.format(Locale.US, "(%02d:%02d)", min, sec),
                    fontFamily = JakartaFontFamily,
                    fontSize = 10.sp,
                    color = FathomTheme.colors.accentGold
                )
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.isPlaying) FathomTheme.colors.accentGold
                        else FathomTheme.colors.surface
                    )
                    .clickable {
                        haptics.light()
                        ambienceEngine.togglePlayPause()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (state.isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                    contentDescription = null,
                    tint = if (state.isPlaying) Color.Black else FathomTheme.colors.accentGold,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
