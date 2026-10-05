package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
        enter = slideInVertically(tween(480, 0, FathomMotion.Emphasized)) { it } + fadeIn(tween(260)),
        exit = slideOutVertically(tween(300, 0, FathomMotion.EmphasizedOut)) { it } + fadeOut(tween(180)),
        modifier = modifier
    ) {
        if (current == null) return@AnimatedVisibility
        val shape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .then(if (!isNavbarVisible) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 12.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .glass(shape, tint = colors.surfaceElevated.copy(alpha = 0.78f), blur = 24.dp)
                .padding(start = 16.dp, end = 10.dp, top = 12.dp, bottom = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Equalizer(color = tone.hue, active = playing)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MonoLabel(text = current.canonicalReference, color = tone.hue)
                    Text(
                        text = (if (original) current.originalText else current.translation) ?: current.translation,
                        style = FathomTheme.typography.commentaryBody.copy(fontFamily = ScriptureFamily, fontSize = 16.sp, lineHeight = 21.sp),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                PlayerChip(text = if (original) "Orig" else "Eng") {
                    haptics.light()
                    audio.setMode(
                        when (mode) {
                            RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                            RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                            RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                        }
                    )
                }
                PlayerChip(text = "${String.format(Locale.US, "%.1f", speed)}×") {
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tone.hue)
                        .tappable(scale = 0.9f, haptic = false) {
                            haptics.medium()
                            audio.toggle(current, mode)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playing) PhosphorIcons.Fill.PauseFill else PhosphorIcons.Fill.PlayFill,
                        contentDescription = if (playing) "Pause" else "Play",
                        tint = tone.onHue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).tappable(scale = 0.88f, haptic = false) {
                        haptics.light()
                        audio.stop()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(PhosphorIcons.Light.X, "Stop", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                }
            }
            Seekline(progress = progress, onSeek = audio::seekTo, color = tone.hue, modifier = Modifier.padding(end = 6.dp))
        }
    }
}

@Composable
private fun PlayerChip(text: String, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(0.75.dp, colors.hairline, RoundedCornerShape(50))
            .tappable(scale = 0.92f, haptic = false, onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = FathomTheme.typography.caption, color = colors.textSecondary)
    }
}
