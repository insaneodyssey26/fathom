package com.masum.fathom.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 42.dp
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val haptics = LocalFathomHaptics.current
    val shape = RoundedCornerShape(50)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(colors.surfaceElevated)
            .padding(3.dp)
    ) {
        val segment = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(segment * selectedIndex, tween(180, easing = FathomMotion.Emphasized), label = "segment_offset")
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(segment)
                .height(height - 6.dp)
                .clip(shape)
                .background(tone.hue.copy(alpha = 0.18f))
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val ink by animateColorAsState(if (selected) tone.hue else colors.textSecondary, tween(150), label = "segment_ink")
                Box(
                    modifier = Modifier
                        .width(segment)
                        .height(height - 6.dp)
                        .clip(shape)
                        .tappable(scale = 0.97f, haptic = false, role = androidx.compose.ui.semantics.Role.Tab) {
                            if (!selected) {
                                haptics.light()
                                onSelect(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = FathomTheme.typography.label.copy(fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
                        color = ink,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
