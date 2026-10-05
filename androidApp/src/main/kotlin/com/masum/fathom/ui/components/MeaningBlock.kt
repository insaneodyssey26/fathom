package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.CaretDown
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun MeaningBlock(
    passage: Passage,
    modifier: Modifier = Modifier,
    hue: Color = FathomTheme.tone.hue
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current
    var open by remember(passage.id) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, FathomMotion.glide(), label = "meaning_turn")
    val entry = passage.explanations.first()

    Column(modifier = modifier.fillMaxWidth().animateContentSize(FathomMotion.glide())) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tappable(scale = 0.99f, haptic = false) {
                    haptics.light()
                    open = !open
                }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MonoLabel(text = if (open) "Meaning" else "Reveal the meaning", color = hue)
            Icon(PhosphorIcons.Light.CaretDown, null, tint = hue, modifier = Modifier.size(16.dp).rotate(turn))
        }
        AnimatedVisibility(
            visible = open,
            enter = expandVertically(tween(420, easing = FathomMotion.Emphasized)) + fadeIn(tween(300, 80)),
            exit = shrinkVertically(tween(260)) + fadeOut(tween(160))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                entry.title?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = typography.title, color = colors.textPrimary)
                }
                Text(text = entry.body, style = typography.commentaryBody, color = colors.textSecondary)
            }
        }
    }
}
