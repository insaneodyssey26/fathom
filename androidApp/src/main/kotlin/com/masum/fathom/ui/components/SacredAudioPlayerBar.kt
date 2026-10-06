package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.fill.Pause as PauseFill
import com.adamglin.phosphoricons.fill.Play as PlayFill
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.RecitationMode
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.Equalizer
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Seekline
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.rememberTone
import java.util.Locale

@Composable
fun SacredAudioPlayerBar(
    modifier: Modifier = Modifier,
    isNavbarVisible: Boolean = true
) {
    val audio = LocalSacredAudioEngine.current
    val haptics = LocalFathomHaptics.current
    val playback by audio.playbackState.collectAsState()

    var passage by remember { mutableStateOf<Passage?>(null) }
    var original by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(RecitationMode.TRANSLATION_ONLY) }
    var progress by remember { mutableFloatStateOf(0f) }
    var speed by remember { mutableFloatStateOf(1f) }

    when (val state = playback) {
        is SacredAudioState.Playing -> {
            passage = state.passage
            original = state.isOriginalPart
            mode = state.mode
            progress = state.progress
            speed = state.speed
        }
        is SacredAudioState.Paused -> {
            passage = state.passage
            original = state.isOriginalPart
            mode = state.mode
            progress = state.progress
            speed = state.speed
        }
        SacredAudioState.Idle -> Unit
    }

    val visible = playback !is SacredAudioState.Idle
    val playing = playback is SacredAudioState.Playing
    val current = passage
    val tone = rememberTone(current?.traditionId)
    val colors = FathomTheme.colors

    AnimatedVisibility(
        visible = visible && current != null,
        enter = slideInVertically(tween(460, 0, FathomMotion.Emphasized)) { it / 2 } +
                fadeIn(tween(260)) +
                scaleIn(tween(360, easing = FathomMotion.Emphasized), initialScale = 0.94f),
        exit = slideOutVertically(tween(280, 0, FathomMotion.EmphasizedOut)) { it / 2 } +
                fadeOut(tween(180)) +
                scaleOut(tween(220), targetScale = 0.94f),
        modifier = modifier
    ) {
        if (current == null) return@AnimatedVisibility
        val playerShape = RoundedCornerShape(28.dp)
        val edgeColor = if (colors.isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
        val innerGlow = if (colors.isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.35f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isNavbarVisible) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (colors.isDark) 16.dp else 12.dp,
                        shape = playerShape,
                        ambientColor = if (colors.isDark) Color.Black.copy(alpha = 0.6f) else Color.Black.copy(alpha = 0.12f),
                        spotColor = tone.hue.copy(alpha = 0.20f)
                    )
                    .glass(
                        shape = playerShape,
                        tint = colors.background.copy(alpha = if (colors.isDark) 0.76f else 0.86f),
                        blur = 28.dp,
                        outline = false
                    )
                    .border(width = 0.75.dp, color = edgeColor, shape = playerShape)
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(innerGlow, Color.Transparent),
                                startY = 0f,
                                endY = size.height * 0.4f
                            ),
                            cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                        )
                    }
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Equalizer(color = tone.hue, size = 18.dp, active = playing)

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        MonoLabel(
                            text = current.canonicalReference,
                            color = tone.hue
                        )
                        Text(
                            text = (if (original) current.originalText else current.translation) ?: current.translation,
                            style = FathomTheme.typography.commentaryBody.copy(
                                fontFamily = ScriptureFamily,
                                fontSize = 14.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    PlayerPillButton(text = if (original) "Orig" else "Eng") {
                        haptics.light()
                        audio.setMode(
                            when (mode) {
                                RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                                RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                                RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                            }
                        )
                    }

                    PlayerPillButton(text = "${String.format(Locale.US, "%.1f", speed)}×") {
                        haptics.light()
                        audio.setSpeed(
                            when {
                                speed <= 0.85f -> 1.0f
                                speed <= 1.05f -> 1.2f
                                speed <= 1.25f -> 1.5f
                                else -> 0.8f
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(tone.hue)
                            .tappable(scale = 0.90f, haptic = false) {
                                haptics.medium()
                                audio.toggle(current, mode)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                            contentDescription = if (playing) "Pause recitation" else "Play recitation",
                            tint = tone.onHue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .tappable(scale = 0.88f, haptic = false) {
                                haptics.light()
                                audio.stop()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.X,
                            contentDescription = "Stop",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Seekline(
                    progress = progress,
                    onSeek = audio::seekTo,
                    color = tone.hue,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayerPillButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = FathomTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.surfaceElevated.copy(alpha = 0.40f))
            .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
            .tappable(scale = 0.92f, haptic = false, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
            color = colors.textSecondary
        )
    }
}
