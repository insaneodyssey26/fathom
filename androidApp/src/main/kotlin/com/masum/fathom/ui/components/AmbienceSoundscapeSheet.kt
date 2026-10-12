package com.masum.fathom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.LockSimple
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.audio.SoundscapeTrack
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.Equalizer
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Seekline
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import java.util.Locale

@Composable
fun AmbienceSoundscapeSheet(onDismiss: () -> Unit) {
    val engine = LocalSanctuaryAmbienceEngine.current
    val haptics = LocalFathomHaptics.current
    val subscription = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current
    val state by engine.state.collectAsState()
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone

    FathomSheet(onDismiss = onDismiss) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(
            overline = "Soundscapes",
            title = "Set the room",
            subtitle = "A quiet backdrop for reading and prayer.",
            onClose = onDismiss
        )
        if (state.currentTrack != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Equalizer(color = tone.hue, size = 13.dp, active = state.isPlaying)
                    Column {
                        Text(
                            text = state.currentTrack?.title.orEmpty(),
                            style = FathomTheme.typography.title,
                            color = colors.textPrimary
                        )
                        MonoLabel(
                            text = if (state.isPlaying) "Currently playing" else "Paused",
                            color = tone.hue
                        )
                    }
                }
                FathomButton(
                    text = "Turn off",
                    onClick = {
                        haptics.light()
                        engine.stop()
                        closeSheet()
                    },
                    style = ButtonStyle.Ghost
                )
            }
            Spacer(Modifier.height(10.dp))
        }
        Column(modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Hairline()
            SoundscapeTrack.entries.forEach { track ->
                val selected = state.currentTrack == track
                val locked = track != SoundscapeTrack.TEMPLE_RAIN && track != SoundscapeTrack.DISTANT_STORM && !subscription.isPro
                val ink by animateColorAsState(if (selected) colors.textPrimary else colors.textSecondary, tween(220), label = "track_ink")
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .tappable(scale = 0.985f, haptic = false) {
                                if (locked) {
                                    haptics.medium()
                                    openPaywall()
                                } else {
                                    haptics.light()
                                    if (selected) engine.togglePlayPause() else engine.selectTrack(track)
                                }
                            }
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(Modifier.padding(horizontal = 2.dp)) {
                            if (locked) {
                                Icon(PhosphorIcons.Light.LockSimple, null, tint = tone.hue, modifier = Modifier.size(16.dp))
                            } else {
                                Equalizer(color = if (selected) tone.hue else colors.textTertiary, active = selected && state.isPlaying)
                            }
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = track.title, style = FathomTheme.typography.title, color = ink)
                                if (locked) {
                                    MonoLabel(text = "Patron", color = tone.hue)
                                }
                            }
                            MonoLabel(text = track.subtitle, color = colors.textTertiary, maxLines = 2)
                        }
                        if (selected) MonoLabel(text = if (state.isPlaying) "Playing" else "Paused", color = tone.hue)
                    }
                    Hairline()
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MonoLabel(text = "Volume")
                MonoLabel(text = "${(state.volume * 100).toInt()}%", color = tone.hue)
            }
            Seekline(progress = state.volume, onSeek = engine::setVolume)

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MonoLabel(text = "Sleep timer")
                state.sleepTimerSecondsRemaining?.let {
                    MonoLabel(text = String.format(Locale.US, "%02d:%02d", it / 60, it % 60), color = tone.hue)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    val locked = minutes > 30 && !subscription.isPro
                    val active = state.sleepTimerMinutes == minutes && state.sleepTimerSecondsRemaining != null
                    val container by animateColorAsState(if (active) tone.hue else Color.Transparent, tween(220), label = "timer_bg")
                    val content by animateColorAsState(if (active) tone.onHue else colors.textSecondary, tween(220), label = "timer_fg")
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(container)
                            .border(0.75.dp, if (active) Color.Transparent else colors.hairline, RoundedCornerShape(50))
                            .tappable(scale = 0.93f) {
                                if (locked) {
                                    haptics.medium()
                                    openPaywall()
                                } else {
                                    if (active) engine.cancelSleepTimer() else engine.setSleepTimer(minutes)
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "${minutes}m", style = FathomTheme.typography.label, color = content)
                            if (locked) {
                                Icon(PhosphorIcons.Light.LockSimple, null, tint = colors.textTertiary, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }

            if (state.currentTrack != null) {
                Spacer(Modifier.height(28.dp))
                FathomButton(
                    text = "Stop soundscape",
                    onClick = {
                        engine.stop()
                        closeSheet()
                    },
                    style = ButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
