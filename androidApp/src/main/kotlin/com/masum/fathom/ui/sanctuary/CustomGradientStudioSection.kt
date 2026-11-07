package com.masum.fathom.ui.sanctuary

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.sanctuary.FeedGradientPreset
import com.masum.fathom.core.sanctuary.FeedGradientPresets
import com.masum.fathom.core.sanctuary.GradientFlow
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuarySettings
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomGradientStudioSection(
    settings: SanctuarySettings,
    preferences: SanctuaryPreferences,
    isPro: Boolean
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = FathomTheme.tone
    val haptics = LocalFathomHaptics.current

    val enabled = settings.feedCustomGradientEnabled
    var rawColorA by remember(settings.feedGradientColorA) {
        mutableStateOf(settings.feedGradientColorA.removePrefix("#"))
    }
    var rawColorB by remember(settings.feedGradientColorB) {
        mutableStateOf(settings.feedGradientColorB.removePrefix("#"))
    }
    val currentFlow = settings.feedGradientFlow
    var currentAngle by remember(settings.feedGradientAngle) {
        mutableFloatStateOf(settings.feedGradientAngle)
    }

    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(colors.surfaceElevated)
            .border(0.75.dp, colors.hairline, cardShape)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Custom Feed Gradient",
                            style = typography.title.copy(fontSize = 15.sp),
                            color = colors.textPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tone.hue.copy(alpha = 0.16f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PLUS",
                                style = typography.caption.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tone.hue
                                )
                            )
                        }
                    }
                    Text(
                        text = "Customize your reading screen with custom hex palettes and interactive flow angles.",
                        style = typography.body.copy(fontSize = 13.sp),
                        color = colors.textSecondary
                    )
                }

                Spacer(Modifier.width(12.dp))

                StudioMiniToggle(
                    checked = enabled,
                    onCheckedChange = {
                        haptics.light()
                        preferences.updateFeedCustomGradientEnabled(it)
                    }
                )
            }

            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically(tween(260)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(140))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .border(1.dp, colors.hairline, RoundedCornerShape(18.dp))
                    ) {
                        val colA = FeedGradientPresets.parseHexColor(rawColorA)
                        val colB = FeedGradientPresets.parseHexColor(rawColorB)
                        val previewBrush = currentFlow.createBrush(
                            colors = listOf(colA, colB),
                            width = constraints.maxWidth.toFloat(),
                            height = constraints.maxHeight.toFloat(),
                            angleDegrees = currentAngle
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(previewBrush)
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "“Silence is not the absence of sound, but the stillness of the mind.”",
                                    style = typography.display.copy(
                                        fontSize = 18.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White.copy(alpha = 0.95f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Text(
                                    text = "— Marcus Aurelius · Meditations",
                                    style = typography.caption.copy(
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.35f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${currentFlow.displayName} ${if (currentFlow == GradientFlow.CUSTOM_ANGLE) "(${currentAngle.roundToInt()}°)" else ""} · #${rawColorA.uppercase()} → #${rawColorB.uppercase()}",
                                        style = typography.caption.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Hex Colors",
                            style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HexFieldCard(
                                label = "Origin Color",
                                rawHex = rawColorA,
                                onHexChange = { newRaw ->
                                    rawColorA = newRaw
                                    if (FeedGradientPresets.isValidHex(newRaw)) {
                                        preferences.updateFeedGradientConfig(
                                            colorA = "#$newRaw",
                                            colorB = "#$rawColorB",
                                            flow = currentFlow,
                                            angle = currentAngle
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .padding(top = 18.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                                    .border(0.75.dp, colors.hairline, CircleShape)
                                    .tappable(scale = 0.92f) {
                                        haptics.light()
                                        val tempA = rawColorA
                                        rawColorA = rawColorB
                                        rawColorB = tempA
                                        preferences.updateFeedGradientConfig(
                                            colorA = "#$rawColorB",
                                            colorB = "#$tempA",
                                            flow = currentFlow,
                                            angle = currentAngle
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⇄",
                                    style = typography.title.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
                                    color = tone.hue
                                )
                            }

                            HexFieldCard(
                                label = "Target Color",
                                rawHex = rawColorB,
                                onHexChange = { newRaw ->
                                    rawColorB = newRaw
                                    if (FeedGradientPresets.isValidHex(newRaw)) {
                                        preferences.updateFeedGradientConfig(
                                            colorA = "#$rawColorA",
                                            colorB = "#$newRaw",
                                            flow = currentFlow,
                                            angle = currentAngle
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Gradient Flow",
                            style = typography.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surface)
                                .border(0.75.dp, colors.hairline, RoundedCornerShape(14.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                GradientFlow.TOP_TO_BOTTOM to "Vertical",
                                GradientFlow.DIAGONAL to "Diagonal",
                                GradientFlow.RADIAL_ORB to "Radial",
                                GradientFlow.CUSTOM_ANGLE to "Custom"
                            ).forEach { (flow, label) ->
                                val isSelected = currentFlow == flow
                                val tabBg by animateColorAsState(
                                    targetValue = if (isSelected) tone.hue else Color.Transparent,
                                    animationSpec = tween(180),
                                    label = "flow_tab_bg"
                                )
                                val tabFg by animateColorAsState(
                                    targetValue = if (isSelected) tone.onHue else colors.textSecondary,
                                    animationSpec = tween(180),
                                    label = "flow_tab_fg"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tabBg)
                                        .tappable(scale = 0.97f) {
                                            haptics.light()
                                            preferences.updateFeedGradientConfig(
                                                colorA = "#$rawColorA",
                                                colorB = "#$rawColorB",
                                                flow = flow,
                                                angle = currentAngle
                                            )
                                        }
                                        .padding(vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = typography.caption.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        ),
                                        color = tabFg,
                                        maxLines = 1,
                                        softWrap = false,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = currentFlow == GradientFlow.CUSTOM_ANGLE,
                            enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                            exit = shrinkVertically(tween(180)) + fadeOut(tween(140))
                        ) {
                            InteractiveAngleDial(
                                angleDegrees = currentAngle,
                                onAngleChange = { newAngle ->
                                    currentAngle = newAngle
                                    preferences.updateFeedGradientConfig(
                                        colorA = "#$rawColorA",
                                        colorB = "#$rawColorB",
                                        flow = GradientFlow.CUSTOM_ANGLE,
                                        angle = newAngle
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioMiniToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val track by animateColorAsState(
        if (checked) tone.hue else colors.hairline,
        tween(200),
        label = "mini_toggle_track"
    )
    val knob by animateDpAsState(
        if (checked) 17.dp else 2.dp,
        FathomMotion.bounce(),
        label = "mini_toggle_knob"
    )

    Box(
        modifier = modifier
            .size(width = 38.dp, height = 22.dp)
            .clip(CircleShape)
            .background(track)
            .tappable(scale = 0.94f, role = Role.Switch) { onCheckedChange(!checked) }
    ) {
        Box(
            Modifier
                .offset(x = knob, y = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(if (checked) tone.onHue else colors.textSecondary)
        )
    }
}

@Composable
private fun InteractiveAngleDial(
    angleDegrees: Float,
    onAngleChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val view = androidx.compose.ui.platform.LocalView.current

    val dialSize = 136.dp
    var lastHapticAngleInt by remember { mutableStateOf(angleDegrees.roundToInt()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .border(0.75.dp, colors.hairline, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Flow Angle",
                style = typography.title.copy(fontSize = 14.5.sp),
                color = colors.textPrimary
            )
            Text(
                text = "${angleDegrees.roundToInt()}°",
                style = typography.headline.copy(
                    fontFamily = com.masum.fathom.ui.theme.InterfaceFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = tone.hue
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(tone.hue.copy(alpha = 0.14f))
                    .padding(horizontal = 7.dp, vertical = 2.5.dp)
            ) {
                Text(
                    text = when (angleDegrees.roundToInt()) {
                        in 0..22, in 338..360 -> "East · Right"
                        in 23..67 -> "Southeast · Bottom-Right"
                        in 68..112 -> "South · Down"
                        in 113..157 -> "Southwest · Bottom-Left"
                        in 158..202 -> "West · Left"
                        in 203..247 -> "Northwest · Top-Left"
                        in 248..292 -> "North · Up"
                        else -> "Northeast · Top-Right"
                    },
                    style = typography.caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tone.hue
                    ),
                    maxLines = 1,
                    softWrap = false
                )
            }
            Text(
                text = "Drag the dial to rotate the gradient direction.",
                style = typography.caption.copy(fontSize = 11.5.sp),
                color = colors.textTertiary
            )
        }

        Spacer(Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(dialSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.surfaceElevated,
                            colors.background
                        )
                    )
                )
                .border(1.dp, colors.hairline, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val angleRad = atan2(offset.y - cy, offset.x - cx)
                            var deg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            if (deg < 0f) deg += 360f
                            val rounded = deg.roundToInt()
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                            lastHapticAngleInt = rounded
                            onAngleChange(deg)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val angleRad = atan2(change.position.y - cy, change.position.x - cx)
                            var deg = Math.toDegrees(angleRad.toDouble()).toFloat()
                            if (deg < 0f) deg += 360f
                            val currentInt = deg.roundToInt()
                            if (currentInt != lastHapticAngleInt) {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK)
                                lastHapticAngleInt = currentInt
                            }
                            onAngleChange(deg)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val outerRadius = (size.minDimension / 2f) - 12.dp.toPx()
                val tickLength = 5.dp.toPx()

                for (i in 0 until 24) {
                    val tickAngle = Math.toRadians((i * 15).toDouble())
                    val isMajor = i % 6 == 0
                    val len = if (isMajor) tickLength * 1.6f else tickLength
                    val startX = (cx + (outerRadius - len) * cos(tickAngle)).toFloat()
                    val startY = (cy + (outerRadius - len) * sin(tickAngle)).toFloat()
                    val endX = (cx + outerRadius * cos(tickAngle)).toFloat()
                    val endY = (cy + outerRadius * sin(tickAngle)).toFloat()

                    drawLine(
                        color = if (isMajor) tone.hue.copy(alpha = 0.6f) else colors.hairline.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                val currentRad = Math.toRadians(angleDegrees.toDouble())
                val trackRadius = (size.minDimension / 2f) - 18.dp.toPx()
                val pointerX = (cx + trackRadius * cos(currentRad)).toFloat()
                val pointerY = (cy + trackRadius * sin(currentRad)).toFloat()

                drawLine(
                    color = tone.hue.copy(alpha = 0.35f),
                    start = Offset(cx, cy),
                    end = Offset(pointerX, pointerY),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(tone.hue)
                    .border(2.dp, colors.surface, CircleShape)
            )

            val rad = Math.toRadians(angleDegrees.toDouble())
            val r = (136 / 2 - 18).toFloat()
            val knobX = (cos(rad) * r).toFloat().dp
            val knobY = (sin(rad) * r).toFloat().dp

            Box(
                modifier = Modifier
                    .offset(x = knobX, y = knobY)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(tone.hue)
                    .border(2.5.dp, colors.surfaceElevated, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(tone.onHue)
                )
            }
        }
    }
}

@Composable
private fun PresetPill(
    preset: FeedGradientPreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val shape = RoundedCornerShape(12.dp)

    val colA = FeedGradientPresets.parseHexColor(preset.colorA)
    val colB = FeedGradientPresets.parseHexColor(preset.colorB)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) tone.hue.copy(alpha = 0.16f) else colors.surface)
            .border(
                width = if (selected) 1.5.dp else 0.75.dp,
                color = if (selected) tone.hue else colors.hairline,
                shape = shape
            )
            .tappable(scale = 0.96f, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(colA, colB)))
                .border(0.75.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        )
        Text(
            text = preset.name,
            style = typography.caption.copy(
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) tone.hue else colors.textPrimary
        )
    }
}

@Composable
private fun HexFieldCard(
    label: String,
    rawHex: String,
    onHexChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = FathomTheme.tone
    val focusManager = LocalFocusManager.current

    val parsedColor = remember(rawHex) { FeedGradientPresets.parseHexColor(rawHex) }
    val isValid = remember(rawHex) { FeedGradientPresets.isValidHex(rawHex) }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = rawHex, selection = TextRange(rawHex.length)))
    }

    androidx.compose.runtime.LaunchedEffect(rawHex) {
        if (textFieldValue.text != rawHex) {
            textFieldValue = TextFieldValue(text = rawHex, selection = TextRange(rawHex.length))
        }
    }

    val shape = RoundedCornerShape(14.dp)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = typography.caption.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
            color = colors.textSecondary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(
                    width = if (isValid) 0.75.dp else 1.dp,
                    color = if (isValid) colors.hairline else Color(0xFFEF4444),
                    shape = shape
                )
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            )

            Text(
                text = "#",
                style = typography.body.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = tone.hue
                )
            )

            BasicTextField(
                value = textFieldValue,
                onValueChange = { newVal ->
                    val cleanText = newVal.text.replace("#", "").filter {
                        it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
                    }.take(8)
                    textFieldValue = newVal.copy(
                        text = cleanText,
                        selection = TextRange(cleanText.length)
                    )
                    onHexChange(cleanText)
                },
                textStyle = typography.body.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                ),
                singleLine = true,
                cursorBrush = SolidColor(tone.hue),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
