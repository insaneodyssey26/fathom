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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CloudRain
import com.adamglin.phosphoricons.regular.CloudLightning
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.Drop
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Waves
import com.adamglin.phosphoricons.regular.SpeakerSimpleHigh
import com.adamglin.phosphoricons.regular.SpeakerSimpleLow
import com.adamglin.phosphoricons.regular.Timer
import com.adamglin.phosphoricons.regular.X
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Stop
import com.masum.fathom.core.audio.LocalSanctuaryAmbienceEngine
import com.masum.fathom.core.audio.SoundscapeTrack
import com.masum.fathom.ui.theme.JakartaFontFamily

private val GoldAccent = Color(0xFFE5A93C)
private val CardBg = Color(0xFF161514)
private val ChipSelectedBg = Color(0xFF2C2214)

private fun getTrackIcon(track: SoundscapeTrack): ImageVector = when (track) {
    SoundscapeTrack.TEMPLE_RAIN -> PhosphorIcons.Regular.CloudRain
    SoundscapeTrack.DISTANT_STORM -> PhosphorIcons.Regular.CloudLightning
    SoundscapeTrack.SANCTUARY_NIGHT -> PhosphorIcons.Regular.Moon
    SoundscapeTrack.SACRED_RIVER -> PhosphorIcons.Regular.Drop
    SoundscapeTrack.SINGING_BOWL -> PhosphorIcons.Regular.Sparkle
    SoundscapeTrack.DEEP_OCEAN -> PhosphorIcons.Regular.Waves
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbienceSoundscapeSheet(
    onDismiss: () -> Unit
) {
    val ambienceEngine = LocalSanctuaryAmbienceEngine.current
    val state by ambienceEngine.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F0E0D),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sacred Soundscapes",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Atmospheric acoustic backdrop for meditation & prayer",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9B95)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E1D1B), CircleShape)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(SoundscapeTrack.values()) { track ->
                    val isSelected = state.currentTrack == track
                    val isCurrentPlaying = isSelected && state.isPlaying

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ChipSelectedBg else CardBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldAccent.copy(alpha = 0.6f) else Color(0x1AFFFFFF),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { ambienceEngine.selectTrack(track) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldAccent.copy(alpha = 0.2f) else Color(0xFF22201E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getTrackIcon(track),
                                contentDescription = track.title,
                                tint = if (isSelected) GoldAccent else Color(0xFFC7C5C0),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (isSelected) Color.White else Color(0xFFE6E4DF)
                            )
                            Text(
                                text = track.subtitle,
                                fontFamily = JakartaFontFamily,
                                fontSize = 12.sp,
                                color = if (isSelected) GoldAccent.copy(alpha = 0.9f) else Color(0xFF8C8983)
                            )
                        }

                        if (isSelected) {
                            IconButton(
                                onClick = { ambienceEngine.togglePlayPause() },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(GoldAccent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isCurrentPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                                    contentDescription = "Toggle",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.currentTrack != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF141312))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.SpeakerSimpleLow,
                            contentDescription = null,
                            tint = Color(0xFF8C8983),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = state.volume,
                            onValueChange = { ambienceEngine.setVolume(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = GoldAccent,
                                inactiveTrackColor = Color(0xFF2C2A28)
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = PhosphorIcons.Regular.SpeakerSimpleHigh,
                            contentDescription = null,
                            tint = Color(0xFF8C8983),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Timer,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sleep Timer",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }

                        if (state.sleepTimerSecondsRemaining != null) {
                            val rem = state.sleepTimerSecondsRemaining ?: 0L
                            val mins = rem / 60
                            val secs = rem % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GoldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val timerOptions = listOf(15, 30, 45, 60)
                        timerOptions.forEach { mins ->
                            val isTimerActive = state.sleepTimerMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isTimerActive) GoldAccent else Color(0xFF22201E))
                                    .clickable {
                                        if (isTimerActive) ambienceEngine.cancelSleepTimer()
                                        else ambienceEngine.setSleepTimer(mins)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (isTimerActive) Color.Black else Color(0xFFC7C5C0)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2C1E1E))
                                .clickable { ambienceEngine.stop() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Stop,
                                    contentDescription = "Stop",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Stop",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFF6B6B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
