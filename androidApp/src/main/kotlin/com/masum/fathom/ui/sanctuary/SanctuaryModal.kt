package com.masum.fathom.ui.sanctuary

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.bold.Check
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CheckCircle
import com.adamglin.phosphoricons.regular.ClockCounterClockwise
import com.adamglin.phosphoricons.regular.Copy
import com.adamglin.phosphoricons.regular.DownloadSimple
import com.adamglin.phosphoricons.regular.FloppyDisk
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.Sparkle
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.Crown
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.notification.DailyNotificationScheduler
import com.masum.fathom.core.sanctuary.ByokProvider
import com.masum.fathom.core.sanctuary.SanctuaryDataEngine
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuaryStorageStats
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.core.update.AppUpdateStatus
import com.masum.fathom.core.update.LocalAppUpdateEngine
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ForestColors
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.ObsidianColors
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.SepiaColors
import com.masum.fathom.ui.theme.SlateColors
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanctuaryModal(
    preferences: SanctuaryPreferences,
    dataEngine: SanctuaryDataEngine,
    onDismiss: () -> Unit
) {
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current
    val activity = context as? Activity
    val updateEngine = LocalAppUpdateEngine.current
    val updateStatus by updateEngine.updateStatus.collectAsState()
    val subscriptionState = LocalSubscriptionState.current
    val openPaywall = LocalOpenPaywall.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val settings by preferences.settings.collectAsState()
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.88f).dp

    var storageStats by remember { mutableStateOf(SanctuaryStorageStats()) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var byokKeyInput by remember { mutableStateOf(settings.byokApiKey) }
    var showKeyMask by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        storageStats = dataEngine.getStorageStats()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = FathomTheme.colors.borderSubtle,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SANCTUARY",
                        fontFamily = SyneFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = FathomTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Aesthetics, contemplation habit & private data vault",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.sp,
                        color = FathomTheme.colors.textTertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (subscriptionState.isPro) {
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    FathomTheme.colors.accentGold.copy(alpha = 0.22f),
                                    FathomTheme.colors.surfaceElevated
                                )
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                listOf(
                                    FathomTheme.colors.surfaceElevated,
                                    FathomTheme.colors.surface
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (subscriptionState.isPro) FathomTheme.colors.accentGold.copy(alpha = 0.5f) else FathomTheme.colors.borderSubtle,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        haptics.medium()
                        openPaywall()
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(FathomTheme.colors.accentGold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Crown,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (subscriptionState.isPro) "SANCTUARY+ PATRON" else "FATHOM SANCTUARY+",
                                    fontFamily = SyneFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp,
                                    color = if (subscriptionState.isPro) FathomTheme.colors.accentGold else FathomTheme.colors.textPrimary
                                )
                                if (!subscriptionState.isPro) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FathomTheme.colors.accentGold)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PRO",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (subscriptionState.isPro) "Full wisdom archives, 4 themes & soundscapes active" else "Unlock all 4 sacred themes, soundscapes & study paths",
                                fontFamily = JakartaFontFamily,
                                fontSize = 11.5.sp,
                                color = FathomTheme.colors.textTertiary
                            )
                        }
                    }

                    Icon(
                        imageVector = PhosphorIcons.Regular.Sparkle,
                        contentDescription = null,
                        tint = FathomTheme.colors.accentGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "READING THEMES",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ReadingTheme.entries.forEach { theme ->
                    val isSelected = settings.theme == theme
                    val isPremiumTheme = theme != ReadingTheme.OBSIDIAN
                    val isLocked = isPremiumTheme && !subscriptionState.isPro
                    val themeColors = when (theme) {
                        ReadingTheme.OBSIDIAN -> ObsidianColors
                        ReadingTheme.SEPIA -> SepiaColors
                        ReadingTheme.SLATE -> SlateColors
                        ReadingTheme.FOREST -> ForestColors
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) FathomTheme.colors.surfaceElevated else FathomTheme.colors.surface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                if (isLocked) {
                                    haptics.medium()
                                    openPaywall()
                                } else {
                                    haptics.light()
                                    preferences.updateTheme(theme)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.background)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.surfaceElevated)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(themeColors.accentGold)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = theme.displayName,
                                    fontFamily = JakartaFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isSelected) FathomTheme.colors.textPrimary else FathomTheme.colors.textSecondary
                                )

                                if (isLocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FathomTheme.colors.accentGold.copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "PLUS",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.4.sp,
                                            color = FathomTheme.colors.accentGold
                                        )
                                    }
                                }
                            }
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = PhosphorIcons.Bold.Check,
                                contentDescription = "Active Theme",
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (isLocked) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Crown,
                                contentDescription = "Sanctuary+ Theme",
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DAILY CONTEMPLATION HABIT",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Wisdom Reminder",
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Text(
                            text = "Receive a quiet, sacred verse for morning or evening reflection",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Switch(
                        checked = settings.dailyNotificationEnabled,
                        onCheckedChange = { enabled ->
                            haptics.light()
                            preferences.updateNotificationTime(
                                enabled = enabled,
                                hour = settings.notificationHour,
                                minute = settings.notificationMinute
                            )
                            if (enabled) {
                                DailyNotificationScheduler.scheduleDailyReminder(
                                    context = context,
                                    hour = settings.notificationHour,
                                    minute = settings.notificationMinute
                                )
                                Toast.makeText(context, "Daily contemplation scheduled", Toast.LENGTH_SHORT).show()
                            } else {
                                DailyNotificationScheduler.cancelDailyReminder(context)
                                Toast.makeText(context, "Daily reminder paused", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FathomTheme.colors.background,
                            checkedTrackColor = FathomTheme.colors.accentGold,
                            uncheckedThumbColor = FathomTheme.colors.textTertiary,
                            uncheckedTrackColor = FathomTheme.colors.surface
                        )
                    )
                }

                AnimatedVisibility(visible = settings.dailyNotificationEnabled) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Reminder Time",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.sp,
                                color = FathomTheme.colors.textSecondary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val hourFormatted = String.format(java.util.Locale.US, "%02d", settings.notificationHour)
                                val minFormatted = String.format(java.util.Locale.US, "%02d", settings.notificationMinute)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.surface)
                                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptics.light()
                                            val nextHour = (settings.notificationHour - 1 + 24) % 24
                                            preferences.updateNotificationTime(true, nextHour, settings.notificationMinute)
                                            DailyNotificationScheduler.scheduleDailyReminder(context, nextHour, settings.notificationMinute)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("−", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, color = FathomTheme.colors.textPrimary)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.accentGold.copy(alpha = 0.16f))
                                        .border(1.dp, FathomTheme.colors.accentGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "$hourFormatted:$minFormatted",
                                        fontFamily = JakartaFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = FathomTheme.colors.accentGold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.surface)
                                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptics.light()
                                            val nextHour = (settings.notificationHour + 1) % 24
                                            preferences.updateNotificationTime(true, nextHour, settings.notificationMinute)
                                            DailyNotificationScheduler.scheduleDailyReminder(context, nextHour, settings.notificationMinute)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, color = FathomTheme.colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "STUDIO SPEECH ENGINE (OPTIONAL BYOK)",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Speech Synthesis Provider",
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = FathomTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fathom uses 100% free offline Android voice synthesis by default. Power users can optionally connect their own API key.",
                    fontFamily = JakartaFontFamily,
                    fontSize = 12.sp,
                    color = FathomTheme.colors.textTertiary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ByokProvider.entries.forEach { prov ->
                        val isSel = settings.byokProvider == prov
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) FathomTheme.colors.accentGold.copy(alpha = 0.18f) else FathomTheme.colors.surface)
                                .border(
                                    width = if (isSel) 1.dp else 0.dp,
                                    color = if (isSel) FathomTheme.colors.accentGold else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    haptics.light()
                                    preferences.updateByokSettings(prov, settings.byokApiKey, settings.byokVoiceId)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (prov) {
                                    ByokProvider.NONE -> "OFFLINE"
                                    ByokProvider.ELEVENLABS -> "ELEVENLABS"
                                    ByokProvider.OPENAI -> "OPENAI"
                                },
                                fontFamily = JakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.6.sp,
                                color = if (isSel) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }
                }

                if (settings.byokProvider != ByokProvider.NONE) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "YOUR ${settings.byokProvider.name} API KEY",
                        style = FathomTheme.typography.caption.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
                        color = FathomTheme.colors.accentGold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = byokKeyInput,
                            onValueChange = { byokKeyInput = it },
                            singleLine = true,
                            visualTransformation = if (showKeyMask) PasswordVisualTransformation() else VisualTransformation.None,
                            textStyle = TextStyle(
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.sp,
                                color = FathomTheme.colors.textPrimary
                            ),
                            cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (byokKeyInput.isEmpty()) {
                                    Text(
                                        text = "Paste secret API key...",
                                        fontFamily = JakartaFontFamily,
                                        fontSize = 13.sp,
                                        color = FathomTheme.colors.textTertiary
                                    )
                                }
                                inner()
                            }
                        )

                        TextButton(
                            onClick = {
                                haptics.light()
                                preferences.updateByokSettings(settings.byokProvider, byokKeyInput, settings.byokVoiceId)
                                Toast.makeText(context, "API Key Saved Securely", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("SAVE", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FathomTheme.colors.accentGold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LOCAL-FIRST DATA SANCTUARY",
                style = FathomTheme.typography.canonicalHeader,
                color = FathomTheme.colors.accentGold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(FathomTheme.colors.surfaceElevated)
                    .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${storageStats.totalVerses} Verses",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FathomTheme.colors.textPrimary
                        )
                        Text(
                            text = "7 Canons • ${storageStats.totalBooks} Books",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = storageStats.contentDbSizeMb,
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = FathomTheme.colors.accentGold
                        )
                        Text(
                            text = "100% Offline SQLite",
                            fontFamily = JakartaFontFamily,
                            fontSize = 12.sp,
                            color = FathomTheme.colors.textTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                haptics.light()
                                coroutineScope.launch {
                                    val json = dataEngine.exportBackupJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Fathom Data Backup", json)
                                    clipboard?.setPrimaryClip(clip)

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Fathom Sanctuary Backup")
                                        putExtra(Intent.EXTRA_TEXT, json)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Export Sanctuary Backup"))
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = PhosphorIcons.Regular.ShareNetwork, contentDescription = null, tint = FathomTheme.colors.accentGold, modifier = Modifier.size(15.dp))
                            Text(text = "EXPORT BACKUP", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FathomTheme.colors.accentGold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FathomTheme.colors.surface)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                            .clickable {
                                haptics.light()
                                showRestoreDialog = true
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = PhosphorIcons.Regular.ClockCounterClockwise, contentDescription = null, tint = FathomTheme.colors.textPrimary, modifier = Modifier.size(15.dp))
                            Text(text = "RESTORE DATA", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = FathomTheme.colors.textPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "CLOSED TESTING & UPDATES",
                    style = FathomTheme.typography.commentaryTitle.copy(fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp),
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FathomTheme.colors.surfaceElevated)
                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val pkgInfo = remember {
                                try {
                                    context.packageManager.getPackageInfo(context.packageName, 0)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            val displayVersionName = pkgInfo?.versionName ?: "1.1.0"
                            val displayVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                pkgInfo?.longVersionCode?.toInt() ?: 2
                            } else {
                                @Suppress("DEPRECATION")
                                pkgInfo?.versionCode ?: 2
                            }

                            Text(
                                text = "Fathom for Android",
                                fontFamily = SyneFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Text(
                                text = "Version $displayVersionName (Build $displayVersionCode) • Closed Testing",
                                fontFamily = JakartaFontFamily,
                                fontSize = 12.sp,
                                color = FathomTheme.colors.textSecondary
                            )
                        }

                        when (updateStatus) {
                            is AppUpdateStatus.Checking -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = FathomTheme.colors.accentGold,
                                    strokeWidth = 2.dp
                                )
                            }
                            is AppUpdateStatus.Available -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.accentGold)
                                        .clickable {
                                            haptics.medium()
                                            if (activity != null) {
                                                val avail = updateStatus as AppUpdateStatus.Available
                                                updateEngine.startFlexibleUpdate(activity, avail.info)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "UPDATE",
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                            is AppUpdateStatus.Downloaded -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.accentGold)
                                        .clickable {
                                            haptics.success()
                                            updateEngine.completeUpdate()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "RESTART",
                                        fontFamily = SyneFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FathomTheme.colors.surface)
                                        .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptics.light()
                                            updateEngine.checkForUpdates(manual = true)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = PhosphorIcons.Regular.ArrowsClockwise,
                                            contentDescription = null,
                                            tint = FathomTheme.colors.accentGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "CHECK",
                                            fontFamily = JakartaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = FathomTheme.colors.accentGold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (updateStatus is AppUpdateStatus.UpToDate) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CheckCircle,
                                contentDescription = null,
                                tint = FathomTheme.colors.accentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "You are on the latest sacred build.",
                                fontFamily = JakartaFontFamily,
                                fontSize = 11.5.sp,
                                color = FathomTheme.colors.accentGold
                            )
                        }
                    } else if (updateStatus is AppUpdateStatus.Error) {
                        val errMsg = (updateStatus as AppUpdateStatus.Error).message
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errMsg,
                            fontFamily = JakartaFontFamily,
                            fontSize = 11.5.sp,
                            color = FathomTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = {
                haptics.light()
                showRestoreDialog = false
            },
            containerColor = FathomTheme.colors.surface,
            title = {
                Text(
                    text = "RESTORE BACKUP JSON",
                    style = FathomTheme.typography.commentaryTitle.copy(fontFamily = SyneFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp),
                    color = FathomTheme.colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste your exported Fathom backup JSON below to restore your bookmarks and reflections.",
                        fontFamily = JakartaFontFamily,
                        fontSize = 12.5.sp,
                        color = FathomTheme.colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BasicTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        textStyle = TextStyle(fontFamily = JakartaFontFamily, fontSize = 12.sp, color = FathomTheme.colors.textPrimary),
                        cursorBrush = SolidColor(FathomTheme.colors.accentGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FathomTheme.colors.surfaceElevated)
                            .border(1.dp, FathomTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        decorationBox = { inner ->
                            if (restoreJsonInput.isEmpty()) {
                                Text(text = "Paste JSON here...", fontFamily = JakartaFontFamily, fontSize = 12.sp, color = FathomTheme.colors.textTertiary)
                            }
                            inner()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics.light()
                        coroutineScope.launch {
                            val res = dataEngine.restoreBackupJson(restoreJsonInput)
                            if (res.isSuccess) {
                                haptics.success()
                                Toast.makeText(context, "Restored ${res.getOrNull()} items successfully!", Toast.LENGTH_SHORT).show()
                                showRestoreDialog = false
                                storageStats = dataEngine.getStorageStats()
                            } else {
                                haptics.error()
                                Toast.makeText(context, "Invalid backup JSON format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("RESTORE", fontFamily = JakartaFontFamily, fontWeight = FontWeight.Bold, color = FathomTheme.colors.accentGold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptics.light()
                    showRestoreDialog = false
                }) {
                    Text("CANCEL", fontFamily = JakartaFontFamily, color = FathomTheme.colors.textSecondary)
                }
            }
        )
    }
}
