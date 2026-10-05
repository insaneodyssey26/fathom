package com.masum.fathom.ui.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import androidx.compose.material3.Icon

@Composable
fun Modifier.tappable(
    enabled: Boolean = true,
    scale: Float = 0.96f,
    haptic: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit
): Modifier {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val haptics = LocalFathomHaptics.current
    val s by animateFloatAsState(if (pressed) scale else 1f, FathomMotion.press(), label = "tap_scale")
    return this
        .graphicsLayer {
            scaleX = s
            scaleY = s
        }
        .clickable(
            interactionSource = source,
            indication = null,
            enabled = enabled,
            role = role
        ) {
            if (haptic) haptics.light()
            onClick()
        }
}

@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = FathomTheme.colors.hairline) {
    Box(modifier = modifier.fillMaxWidth().height(0.75.dp).background(color))
}

@Composable
fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = FathomTheme.colors.textTertiary,
    maxLines: Int = 1
) {
    Text(
        text = text.uppercase(),
        style = FathomTheme.typography.mono,
        color = color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun FathomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    trailing: ImageVector? = null,
    style: ButtonStyle = ButtonStyle.Primary,
    enabled: Boolean = true
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val (container, content, stroke) = when (style) {
        ButtonStyle.Primary -> Triple(colors.textPrimary, colors.background, null)
        ButtonStyle.Tone -> Triple(tone.hue, tone.onHue, null)
        ButtonStyle.Ghost -> Triple(Color.Transparent, colors.textPrimary, BorderStroke(0.75.dp, colors.textTertiary.copy(alpha = 0.55f)))
        ButtonStyle.Quiet -> Triple(colors.surfaceElevated, colors.textPrimary, null)
    }
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(container.copy(alpha = container.alpha * if (enabled) 1f else 0.35f))
            .then(if (stroke != null) Modifier.border(stroke, shape) else Modifier)
            .tappable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) Icon(leading, null, tint = content, modifier = Modifier.size(18.dp))
        Text(text = text, style = FathomTheme.typography.label.copy(letterSpacing = 0.2.sp), color = content)
        if (trailing != null) Icon(trailing, null, tint = content, modifier = Modifier.size(18.dp))
    }
}

enum class ButtonStyle { Primary, Tone, Ghost, Quiet }

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = FathomTheme.colors.textPrimary,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .glass(CircleShape, tint = FathomTheme.colors.background.copy(alpha = 0.32f))
            .tappable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = FathomTheme.colors
    val shape = RoundedCornerShape(50)
    val container = if (selected) colors.textPrimary else Color.Transparent
    val content = if (selected) colors.background else colors.textSecondary
    Row(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(0.75.dp, if (selected) Color.Transparent else colors.hairline, shape)
            .then(if (onClick != null) Modifier.tappable(scale = 0.94f, onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        Text(text = text, style = FathomTheme.typography.label, color = content, maxLines = 1)
    }
}

@Composable
fun FathomToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val track by androidx.compose.animation.animateColorAsState(
        if (checked) tone.hue else colors.hairline,
        androidx.compose.animation.core.tween(220),
        label = "toggle_track"
    )
    val knob by androidx.compose.animation.core.animateDpAsState(if (checked) 22.dp else 2.dp, FathomMotion.bounce(), label = "toggle_knob")
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(CircleShape)
            .background(track)
            .tappable(scale = 0.94f, role = Role.Switch) { onCheckedChange(!checked) }
    ) {
        Box(
            Modifier
                .offset(x = knob, y = 2.dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(if (checked) tone.onHue else colors.textSecondary)
        )
    }
}
