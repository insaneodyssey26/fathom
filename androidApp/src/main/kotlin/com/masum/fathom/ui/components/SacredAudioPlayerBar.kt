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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
        enter = slideInVertically(tween(220, 0, FathomMotion.Emphasized)) { it / 3 } + fadeIn(tween(160)),
        exit = slideOutVertically(tween(160, 0, FathomMotion.EmphasizedOut)) { it / 3 } + fadeOut(tween(120)),
        modifier = modifier
    ) {
        if (current == null) return@AnimatedVisibility
        val shape = RoundedCornerShape(22.dp)
        val snippet = (if (original) current.originalText else current.translation) ?: current.translation
        val modeLabel = when (mode) {
            RecitationMode.TRANSLATION_ONLY -> "English"
            RecitationMode.ORIGINAL_ONLY -> "Original"
            RecitationMode.FULL -> "Both"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!isNavbarVisible) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.surfaceElevated)
                    .border(0.75.dp, colors.hairline, shape)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 6.dp, top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(tone.hue)
                            .tappable(scale = 0.92f, haptic = false) {
                                haptics.medium()
                                audio.toggle(current, mode)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                            contentDescription = if (playing) "Pause recitation" else "Play recitation",
                            tint = tone.onHue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Equalizer(color = tone.hue, size = 12.dp, active = playing)
                            MonoLabel(text = current.canonicalReference, color = tone.hue)
                        }
                        Text(
                            text = snippet,
                            style = FathomTheme.typography.commentaryBody.copy(
                                fontFamily = ScriptureFamily,
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                fontStyle = if (original) FontStyle.Italic else FontStyle.Normal
                            ),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MonoLabel(
                                text = modeLabel,
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .tappable(scale = 0.94f, haptic = false) {
                                        haptics.light()
                                        audio.setMode(
                                            when (mode) {
                                                RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                                                RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                                                RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                                            }
                                        )
                                    }
                                    .padding(vertical = 5.dp)
                            )
                            MonoLabel(text = "·", color = colors.textTertiary)
                            MonoLabel(
                                text = "${String.format(Locale.US, "%.1f", speed)}×",
                                color = colors.textSecondary,
                                modifier = Modifier
                                    .tappable(scale = 0.94f, haptic = false) {
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
                                    .padding(vertical = 5.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .tappable(scale = 0.88f, haptic = false) {
                                haptics.light()
                                audio.stop()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Light.X,
                            contentDescription = "Stop recitation",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Seekline(
                    progress = progress,
                    onSeek = audio::seekTo,
                    color = tone.hue,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 2.dp, bottom = 10.dp)
                )
            }
        }
    }
}
