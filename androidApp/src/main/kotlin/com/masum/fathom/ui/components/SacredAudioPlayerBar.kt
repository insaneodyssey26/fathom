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
import androidx.compose.foundation.layout.navigationBarsPadding
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
    modifier: Modifier = Modifier,
    isNavbarVisible: Boolean = true
) {
    val audioEngine = LocalSacredAudioEngine.current
    val playbackState by audioEngine.playbackState.collectAsState()

    var cachedPassage by remember { mutableStateOf<Passage?>(null) }
    var cachedIsOriginalPart by remember { mutableStateOf(false) }
    var cachedMode by remember { mutableStateOf(RecitationMode.TRANSLATION_ONLY) }
    var cachedProgress by remember { mutableFloatStateOf(0f) }
    var cachedSpeed by remember { mutableFloatStateOf(1.0f) }

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

        if (currentPassage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isNavbarVisible) Modifier.navigationBarsPadding()
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = if (!isNavbarVisible) 12.dp else 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FathomTheme.colors.surfaceElevated.copy(alpha = 0.96f))
                    .border(
                        width = 1.dp,
                        color = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
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
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.SpeakerHigh,
                                    contentDescription = "Audio Recitation",
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(9.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentPassage.canonicalReference,
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = FathomTheme.colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FathomTheme.colors.accentGold.copy(alpha = 0.12f))
                                            .clickable {
                                                val nextMode = when (currentMode) {
                                                    RecitationMode.TRANSLATION_ONLY -> RecitationMode.ORIGINAL_ONLY
                                                    RecitationMode.ORIGINAL_ONLY -> RecitationMode.FULL
                                                    RecitationMode.FULL -> RecitationMode.TRANSLATION_ONLY
                                                }
                                                audioEngine.setMode(nextMode)
                                            }
                                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    ) {
                                        Text(
                                            text = if (isOriginalPart) "ORIGINAL" else "ENGLISH",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.4.sp,
                                            color = FathomTheme.colors.accentGold
                                        )
                                    }
                                }

                                Text(
                                    text = (if (isOriginalPart) currentPassage.originalText else currentPassage.translation) ?: currentPassage.translation,
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.sp,
                                    color = FathomTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(FathomTheme.colors.surface)
                                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val nextSpeed = when {
                                            currentSpeed <= 0.85f -> 1.0f
                                            currentSpeed <= 1.05f -> 1.2f
                                            currentSpeed <= 1.25f -> 1.5f
                                            else -> 0.8f
                                        }
                                        audioEngine.setSpeed(nextSpeed)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", currentSpeed)}×",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = FathomTheme.colors.accentGold
                                )
                            }

                            IconButton(
                                onClick = {
                                    audioEngine.toggle(currentPassage, currentMode)
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f))
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) PhosphorIcons.Regular.Pause else PhosphorIcons.Regular.Play,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = { audioEngine.stop() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.X,
                                    contentDescription = "Stop",
                                    tint = FathomTheme.colors.textTertiary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    SacredInteractiveSeekbar(
                        progress = currentProgress,
                        onSeek = { fraction ->
                            audioEngine.seekTo(fraction)
                        }
                    )
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
            .height(14.dp)
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
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(FathomTheme.colors.borderSubtle)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(activeFraction.coerceIn(0.001f, 1f))
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(FathomTheme.colors.accentGold)
            )
        }

        val thumbOffset = ((totalWidth - 9.dp) * activeFraction).coerceAtLeast(0.dp)
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(9.dp)
                .clip(CircleShape)
                .background(FathomTheme.colors.accentGold)
                .border(1.dp, FathomTheme.colors.surfaceElevated, CircleShape)
        )
    }
}
