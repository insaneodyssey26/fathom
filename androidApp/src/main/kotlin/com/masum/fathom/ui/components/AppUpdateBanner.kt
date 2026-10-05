package com.masum.fathom.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.update.AppUpdateEngine
import com.masum.fathom.core.update.AppUpdateStatus
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Seekline
import com.masum.fathom.ui.design.glass
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun AppUpdateBanner(
    updateEngine: AppUpdateEngine,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val haptics = LocalFathomHaptics.current
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val status by updateEngine.updateStatus.collectAsState()
    val visible = status is AppUpdateStatus.Available || status is AppUpdateStatus.Downloading || status is AppUpdateStatus.Downloaded

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(480, 0, FathomMotion.Emphasized)) { -it } + fadeIn(tween(240)),
        exit = slideOutVertically(tween(300, 0, FathomMotion.EmphasizedOut)) { -it } + fadeOut(tween(180)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .glass(RoundedCornerShape(26.dp), tint = colors.surfaceElevated.copy(alpha = 0.86f), blur = 24.dp)
                .padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimatedContent(
                targetState = status::class,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                label = "update_copy",
                modifier = Modifier.weight(1f)
            ) {
                when (val current = status) {
                    is AppUpdateStatus.Downloading -> {
                        val fraction = if (current.totalBytes > 0) current.bytesDownloaded.toFloat() / current.totalBytes else 0f
                        Column {
                            MonoLabel(text = "Downloading · ${(fraction * 100).toInt()}%", color = tone.hue)
                            Seekline(progress = fraction, onSeek = {}, color = tone.hue)
                        }
                    }
                    is AppUpdateStatus.Downloaded -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MonoLabel(text = "Ready", color = tone.hue)
                        Text(text = "Restart to finish updating", style = FathomTheme.typography.label, color = colors.textPrimary)
                    }
                    else -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MonoLabel(text = "Update available", color = tone.hue)
                        Text(text = "New verses and improvements", style = FathomTheme.typography.label, color = colors.textPrimary)
                    }
                }
            }
            when (val current = status) {
                is AppUpdateStatus.Available -> UpdateAction(text = "Update") {
                    haptics.medium()
                    activity?.let { updateEngine.startFlexibleUpdate(it, current.info) }
                }
                is AppUpdateStatus.Downloaded -> UpdateAction(text = "Restart") {
                    haptics.success()
                    updateEngine.completeUpdate()
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun UpdateAction(text: String, onClick: () -> Unit) {
    val tone = FathomTheme.tone
    Row(
        modifier = Modifier
            .tappable(scale = 0.94f, haptic = false, onClick = onClick)
            .background(tone.hue, RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, style = FathomTheme.typography.label, color = tone.onHue)
    }
}


