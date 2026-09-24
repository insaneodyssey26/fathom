package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Pause
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.audio.RecitationMode
import com.masum.fathom.core.audio.SacredAudioState
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily

@Composable
fun SacredAudioPlayerBar(
    modifier: Modifier = Modifier
) {
    val audioEngine = LocalSacredAudioEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()

    var cachedPassage by remember { mutableStateOf<Passage?>(null) }
    var cachedIsOriginalPart by remember { mutableStateOf(false) }
    var cachedMode by remember { mutableStateOf(RecitationMode.TRANSLATION_ONLY) }
    var cachedProgress by remember { mutableFloatStateOf(0f) }
    var cachedSpeed by remember { mutableFloatStateOf(0.95f) }

    when (val state = playbackState) {
        is SacredAudioState.Playing -> {
            cachedPassage = state.passage
            cachedIsOriginalPart = state.isOriginalPart
            cachedMode = state.mode
            cachedProgress = state.progress
            cachedSpeed = state.speed
        }
        is SacredAudioState.Paused -> {
            cachedPassage = state.passage
            cachedIsOriginalPart = state.isOriginalPart
            cachedMode = state.mode
            cachedProgress = state.progress
            cachedSpeed = state.speed
        }
        is SacredAudioState.Idle -> Unit
    }

    val isVisible = playbackState is SacredAudioState.Playing || playbackState is SacredAudioState.Paused

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(animationSpec = tween(280)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        val currentPassage = cachedPassage
        val isOriginalPart = cachedIsOriginalPart
        val currentMode = cachedMode
        val currentProgress = cachedProgress
        val currentSpeed = cachedSpeed
        val isPlaying = playbackState is SacredAudioState.Playing

        val speedSteps = listOf(0.7f, 0.8f, 1.0f, 1.2f, 1.5f, 1.7f, 2.0f)

        if (currentPassage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.SpeakerHigh,
                                    contentDescription = "Sacred Audio",
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentPassage.canonicalReference,
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = FathomTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                            .clickable {
                                                val nextMode = when (currentMode) {
                                                    RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                                                    RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                                                    RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                                                }
                                                audioEngine.setMode(nextMode)
                                            }
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isOriginalPart) "ORIGINAL" else "ENGLISH",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp,
                                            letterSpacing = 0.5.sp,
                                            color = FathomTheme.colors.accentGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = (if (isOriginalPart) currentPassage.originalText else currentPassage.translation) ?: currentPassage.translation,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.5.sp,
                                    color = FathomTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    audioEngine.toggle(currentPassage, currentMode)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.18f))
                                    .size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            IconButton(
                                onClick = { audioEngine.stop() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.X,
                                    contentDescription = "Stop",
                                    tint = FathomTheme.colors.textTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SacredInteractiveSeekbar(
                        progress = currentProgress,
                        onSeek = { fraction ->
                            audioEngine.seekTo(fraction)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(currentProgress * 100).toInt()}% RECITED",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            color = FathomTheme.colors.textTertiary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.surface)
                                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                    .clickable(enabled = currentSpeed > 0.55f) {
                                        val nextSpeed = ((currentSpeed - 0.1f) * 10f).let { kotlin.math.round(it) } / 10f
                                        audioEngine.setSpeed(nextSpeed.coerceIn(0.5f, 2.5f))
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "−",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (currentSpeed > 0.55f) FathomTheme.colors.textPrimary else FathomTheme.colors.textTertiary.copy(alpha = 0.4f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f))
                                    .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", currentSpeed)}×",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.surface)
                                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                    .clickable(enabled = currentSpeed < 2.45f) {
                                        val nextSpeed = ((currentSpeed + 0.1f) * 10f).let { kotlin.math.round(it) } / 10f
                                        audioEngine.setSpeed(nextSpeed.coerceIn(0.5f, 2.5f))
                                    }
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (currentSpeed < 2.45f) FathomTheme.colors.textPrimary else FathomTheme.colors.textTertiary.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SacredInteractiveSeekbar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val activeFraction = if (isDragging) dragFraction else progress.coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = if (size.width > 0) (offset.x / size.width.toFloat()).coerceIn(0f, 1f) else 0f
                    dragFraction = fraction
                    onSeek(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragFraction = if (size.width > 0) (offset.x / size.width.toFloat()).coerceIn(0f, 1f) else 0f
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        dragFraction = if (size.width > 0) (change.position.x / size.width.toFloat()).coerceIn(0f, 1f) else 0f
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeek(dragFraction)
                    },
                    onDragCancel = {
                        isDragging = false
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(FathomTheme.colors.borderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(activeFraction.coerceIn(0.001f, 1f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FathomTheme.colors.accentGold)
            )
        }

        val thumbOffset = ((totalWidth - 11.dp) * activeFraction).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(11.dp)
                .clip(CircleShape)
                .background(FathomTheme.colors.accentGold)
                .border(1.5.dp, FathomTheme.colors.surfaceElevated, CircleShape)
        )
    }
}
