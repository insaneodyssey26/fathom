package com.masum.fathom.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Flame
import com.adamglin.phosphoricons.regular.Globe
import com.adamglin.phosphoricons.regular.HandsPraying
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Moon
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.Sun
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily

private data class TraditionOption(
    val id: String,
    val title: String,
    val scriptureHint: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun OnboardingScreen(
    onComplete: (name: String, tradition: String?, personalizeFeed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    var currentStep by remember { mutableIntStateOf(1) }
    var enteredName by remember { mutableStateOf("") }
    var selectedTradition by remember { mutableStateOf<String?>("All Traditions") }
    var personalizeFeed by remember { mutableStateOf(false) }

    val traditions = remember {
        listOf(
            TraditionOption(
                id = "Sanatana Dharma",
                title = "Sanatana Dharma",
                scriptureHint = "Bhagavad Gita, Upanishads",
                icon = PhosphorIcons.Regular.Sun
            ),
            TraditionOption(
                id = "Islam",
                title = "Islam",
                scriptureHint = "The Holy Quran",
                icon = PhosphorIcons.Regular.Moon
            ),
            TraditionOption(
                id = "Christianity",
                title = "Christianity",
                scriptureHint = "The Holy Bible",
                icon = PhosphorIcons.Regular.HandsPraying
            ),
            TraditionOption(
                id = "Buddhism",
                title = "Buddhism",
                scriptureHint = "Dhammapada, Zen",
                icon = PhosphorIcons.Regular.Sparkle
            ),
            TraditionOption(
                id = "Taoism",
                title = "Taoism",
                scriptureHint = "Tao Te Ching",
                icon = PhosphorIcons.Regular.Flame
            ),
            TraditionOption(
                id = "All Traditions",
                title = "All Traditions",
                scriptureHint = "Explore all books freely",
                icon = PhosphorIcons.Regular.Globe
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FathomTheme.colors.surfaceElevated)
                            .clickable {
                                haptics.light()
                                currentStep -= 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ArrowLeft,
                            contentDescription = "Back",
                            tint = FathomTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val active = index + 1 == currentStep
                        val passed = index + 1 < currentStep
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (active) 28.dp else 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        active -> FathomTheme.colors.accentGold
                                        passed -> FathomTheme.colors.accentGold.copy(alpha = 0.5f)
                                        else -> FathomTheme.colors.surfaceElevated
                                    }
                                )
                        )
                    }
                }

                if (currentStep < 3) {
                    TextButton(
                        onClick = {
                            haptics.light()
                            if (currentStep == 1 && enteredName.isBlank()) {
                                enteredName = "Seeker"
                            }
                            currentStep += 1
                        }
                    ) {
                        Text(
                            text = "SKIP",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally(initialOffsetX = { it }) + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 2 }) + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally(targetOffsetX = { it / 2 }) + fadeOut(tween(200)))
                    }
                },
                label = "onboarding_step_transition",
                modifier = Modifier.weight(1f, fill = false)
            ) { step ->
                when (step) {
                    1 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                FathomTheme.colors.accentGold.copy(alpha = 0.25f),
                                                FathomTheme.colors.surfaceElevated
                                            )
                                        )
                                    )
                                    .border(1.5.dp, FathomTheme.colors.accentGold.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Sparkle,
                                    contentDescription = null,
                                    tint = FathomTheme.colors.accentGold,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Welcome to Fathom",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = FathomTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "What is your name?",
                                fontFamily = JakartaFontFamily,
                                fontSize = 14.sp,
                                color = FathomTheme.colors.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(FathomTheme.colors.surfaceElevated)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (enteredName.isNotBlank()) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 18.dp, vertical = 16.dp)
                            ) {
                                if (enteredName.isEmpty()) {
                                    Text(
                                        text = "Your Name",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 15.sp,
                                        color = FathomTheme.colors.textTertiary
                                    )
                                }
                                BasicTextField(
                                    value = enteredName,
                                    onValueChange = { enteredName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = FathomTheme.colors.textPrimary
                                    ),
                                    cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    2 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Scriptures of Interest",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = FathomTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Which scriptures or traditions are you most drawn to? (Optional)",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.5.sp,
                                color = FathomTheme.colors.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                traditions.forEach { trad ->
                                    val isSelected = selectedTradition == trad.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.14f)
                                                else FathomTheme.colors.surfaceElevated
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                haptics.light()
                                                selectedTradition = trad.id
                                                personalizeFeed = (trad.id != "All Traditions")
                                            }
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) FathomTheme.colors.accentGold
                                                        else FathomTheme.colors.surface
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = trad.icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.Black else FathomTheme.colors.textSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = trad.title,
                                                    fontFamily = JakartaFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                                                )
                                                Text(
                                                    text = trad.scriptureHint,
                                                    fontFamily = JakartaFontFamily,
                                                    fontSize = 11.5.sp,
                                                    color = FathomTheme.colors.textTertiary
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = PhosphorIcons.Regular.Check,
                                                contentDescription = "Selected",
                                                tint = FathomTheme.colors.accentGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Your Daily Feed",
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = FathomTheme.colors.textPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Choose how verses appear on your daily feed:",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.5.sp,
                                color = FathomTheme.colors.textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            val hasSpecificTradition = selectedTradition != null && selectedTradition != "All Traditions"

                            if (hasSpecificTradition) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (personalizeFeed) FathomTheme.colors.accentGold.copy(alpha = 0.14f)
                                            else FathomTheme.colors.surfaceElevated
                                        )
                                        .border(
                                            width = if (personalizeFeed) 1.5.dp else 1.dp,
                                            color = if (personalizeFeed) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            haptics.light()
                                            personalizeFeed = true
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (personalizeFeed) FathomTheme.colors.accentGold else Color.Transparent)
                                            .border(
                                                1.5.dp,
                                                if (personalizeFeed) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (personalizeFeed) {
                                            Icon(
                                                imageVector = PhosphorIcons.Regular.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Mainly $selectedTradition",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = FathomTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "Focus primarily on $selectedTradition scriptures, with occasional wisdom from other texts.",
                                            fontFamily = JakartaFontFamily,
                                            fontSize = 11.5.sp,
                                            color = FathomTheme.colors.textTertiary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (!personalizeFeed || !hasSpecificTradition) FathomTheme.colors.accentGold.copy(alpha = 0.14f)
                                        else FathomTheme.colors.surfaceElevated
                                    )
                                    .border(
                                        width = if (!personalizeFeed || !hasSpecificTradition) 1.5.dp else 1.dp,
                                        color = if (!personalizeFeed || !hasSpecificTradition) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        haptics.light()
                                        personalizeFeed = false
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (!personalizeFeed || !hasSpecificTradition) FathomTheme.colors.accentGold else Color.Transparent)
                                    .border(
                                        1.5.dp,
                                        if (!personalizeFeed || !hasSpecificTradition) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!personalizeFeed || !hasSpecificTradition) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All Scriptures",
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = FathomTheme.colors.textPrimary
                                )
                                Text(
                                    text = "A balanced mix of verses from all sacred texts and traditions.",
                                    fontFamily = JakartaFontFamily,
                                    fontSize = 11.5.sp,
                                    color = FathomTheme.colors.textTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "Your data stays on your phone. Because there is no server, your name and preferences are never sent anywhere.",
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = FathomTheme.colors.textTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                haptics.medium()
                if (currentStep < 3) {
                    if (currentStep == 1 && enteredName.isBlank()) {
                        enteredName = "Seeker"
                    }
                    currentStep += 1
                } else {
                    val finalName = enteredName.trim().ifBlank { "Seeker" }
                    val finalTrad = if (selectedTradition == "All Traditions") null else selectedTradition
                    onComplete(finalName, finalTrad, personalizeFeed)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = FathomTheme.colors.accentGold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (currentStep < 3) "CONTINUE" else "START READING",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.ArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
}
