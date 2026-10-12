package com.masum.fathom.ui.sanctuary

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.Eye
import com.adamglin.phosphoricons.light.EyeSlash
import com.adamglin.phosphoricons.light.LockSimple
import com.adamglin.phosphoricons.light.Minus
import com.adamglin.phosphoricons.light.Plus
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.notification.DailyNotificationScheduler
import com.masum.fathom.core.sanctuary.ByokProvider
import com.masum.fathom.core.sanctuary.SanctuaryDataEngine
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuaryStorageStats
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.core.update.AppUpdateStatus
import com.masum.fathom.core.update.LocalAppUpdateEngine
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.FathomToggle
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.fadeEdges
import com.masum.fathom.ui.design.LocalBackdrop
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Pill
import com.masum.fathom.ui.design.Seekline
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.shell.LegalDoc
import com.masum.fathom.ui.shell.LegalDocuments
import com.masum.fathom.ui.shell.LocalOpenLegal
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SanctuaryScreen(
    preferences: SanctuaryPreferences,
    dataEngine: SanctuaryDataEngine,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val haptics = LocalFathomHaptics.current
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = rememberTone(null)
    val settings by preferences.settings.collectAsState()
    val subscription = LocalSubscriptionState.current
    val subManager = LocalSubscriptionManager.current
    val openPaywall = LocalOpenPaywall.current
    val updateEngine = LocalAppUpdateEngine.current
    val updateStatus by updateEngine.updateStatus.collectAsState()
    val scope = rememberCoroutineScope()
    val openLegal = LocalOpenLegal.current
    val audioEngine = LocalSacredAudioEngine.current

    var stats by remember { mutableStateOf(SanctuaryStorageStats()) }
    var keyInput by remember { mutableStateOf(settings.byokApiKey) }
    var maskKey by remember { mutableStateOf(true) }
    var isTestingKey by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { stats = dataEngine.getStorageStats() }

    val savePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { destinationUri ->
        if (destinationUri != null) {
            scope.launch {
                try {
                    val bytes = dataEngine.exportVaultBytes()
                    val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    if (outputStream != null) {
                        outputStream.use { it.write(bytes) }
                        haptics.success()
                        Toast.makeText(context, "Sacred vault saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Could not write to destination", Toast.LENGTH_SHORT).show()
                    }
                } catch (_: Exception) {
                    Toast.makeText(context, "Could not export vault", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    Toast.makeText(context, "Could not open vault archive", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val outcome = dataEngine.restoreVaultArchive(stream)
                outcome.onSuccess { count ->
                    haptics.success()
                    Toast.makeText(context, "Restored $count items into your vault", Toast.LENGTH_SHORT).show()
                    stats = dataEngine.getStorageStats()
                }.onFailure {
                    haptics.error()
                    Toast.makeText(context, "Invalid or corrupt .fathom archive", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val version = remember(context) {


        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        }.getOrNull()
    }
    val versionName = version?.versionName.orEmpty()
    val buildNumber = if (version != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) version.longVersionCode else 0L

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBackdrop provides null) {
            Atmosphere(tone = tone, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight().widthIn(max = 640.dp).windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)).fadeEdges(top = 96.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 96.dp, bottom = 48.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    ) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MonoLabel(text = "Settings", color = tone.hue)
                                Text(text = "Sanctuary", style = typography.display, color = colors.textPrimary)
                                Text(
                                    text = "How Fathom looks, sounds and remembers.",
                                    style = typography.commentaryBody,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 36.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Brush.linearGradient(listOf(tone.hue.copy(alpha = 0.26f), tone.hue.copy(alpha = 0.06f))))
                                    .border(0.75.dp, tone.hue.copy(alpha = 0.32f), RoundedCornerShape(26.dp))
                                    .tappable(scale = 0.98f) {
                                        haptics.medium()
                                        openPaywall()
                                    }
                                    .padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    MonoLabel(text = if (subscription.isPro) "Sanctuary+ patron" else "Sanctuary+", color = tone.hue)
                                    Text(
                                        text = if (subscription.isPro) "Every theme, soundscape and path is open to you." else "Unlock every theme, soundscape and study path.",
                                        style = typography.title,
                                        color = colors.textPrimary
                                    )
                                }
                                Icon(PhosphorIcons.Light.ArrowRight, null, tint = tone.hue, modifier = Modifier.size(22.dp))
                            }
                        }

                        item { Heading("Appearance", "Reading themes") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ReadingTheme.entries.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        pair.forEach { theme ->
                                            val locked = theme != ReadingTheme.OBSIDIAN && theme != ReadingTheme.SEPIA && !subscription.isPro
                                            ThemeTile(
                                                theme = theme,
                                                selected = settings.theme == theme,
                                                locked = locked,
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    if (locked) {
                                                        haptics.medium()
                                                        openPaywall()
                                                    } else {
                                                        haptics.light()
                                                        preferences.updateTheme(theme)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item { Heading("Notifications", "Reminders and alerts") }
                        item { NotificationSettings(preferences = preferences, settings = settings) }

                        item { Heading("Voice", "Narration") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                    text = "Recitation uses your phone's free offline voice. Power users may connect their own neural voice key.",
                                    style = typography.body,
                                    color = colors.textSecondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ByokProvider.entries.forEach { provider ->
                                        Pill(
                                            text = when (provider) {
                                                ByokProvider.NONE -> "Offline"
                                                ByokProvider.ELEVENLABS -> "ElevenLabs"
                                                ByokProvider.OPENAI -> "OpenAI"
                                            },
                                            selected = settings.byokProvider == provider,
                                            onClick = {
                                                haptics.light()
                                                preferences.updateByokSettings(provider, settings.byokApiKey, settings.byokVoiceId)
                                            }
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = settings.byokProvider != ByokProvider.NONE,
                                    enter = expandVertically(tween(320)) + fadeIn(tween(240)),
                                    exit = shrinkVertically(tween(220)) + fadeOut(tween(140))
                                ) {
                                    KeyField(
                                        value = keyInput,
                                        onValueChange = { keyInput = it },
                                        masked = maskKey,
                                        isTesting = isTestingKey,
                                        onToggleMask = { maskKey = !maskKey },
                                        onTest = {
                                            haptics.light()
                                            isTestingKey = true
                                            scope.launch {
                                                val res = audioEngine.neuralSynthesizer.testConnection(
                                                    settings.byokProvider,
                                                    keyInput.trim(),
                                                    settings.byokVoiceId
                                                )
                                                isTestingKey = false
                                                res.onSuccess { msg ->
                                                    haptics.success()
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                }.onFailure { err ->
                                                    haptics.error()
                                                    Toast.makeText(context, err.message ?: "Connection failed", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        onSave = {
                                            haptics.success()
                                            preferences.updateByokSettings(settings.byokProvider, keyInput.trim(), settings.byokVoiceId)
                                            Toast.makeText(context, "Key saved on this device", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }

                        item { Heading("Data", "Your private vault") }
                        item {
                            Column {
                                Hairline()
                                Row(Modifier.fillMaxWidth().padding(vertical = 20.dp)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = stats.totalVerses.toString(), style = typography.numeral, color = colors.textPrimary)
                                        MonoLabel(text = "Verses · ${stats.totalBooks} books")
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(text = stats.contentDbSizeMb, style = typography.numeral, color = colors.textPrimary)
                                        MonoLabel(text = "On device · offline")
                                    }
                                }
                                Hairline()
                                Spacer(Modifier.height(20.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FathomButton(
                                        text = "Export backup",
                                        onClick = {
                                            if (!subscription.isPro) {
                                                haptics.medium()
                                                openPaywall()
                                            } else {
                                                haptics.light()
                                                val dateStamp = java.time.LocalDate.now().toString()
                                                savePicker.launch("fathom-vault-$dateStamp.fathom")
                                            }
                                        },
                                        style = ButtonStyle.Tone,
                                        modifier = Modifier.weight(1f)

                                    )

                                    FathomButton(
                                        text = "Restore",
                                        onClick = {
                                            haptics.light()
                                            restorePicker.launch(arrayOf("*/*"))
                                        },
                                        style = ButtonStyle.Ghost,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    Modifier.padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(PhosphorIcons.Light.LockSimple, null, tint = colors.textTertiary, modifier = Modifier.size(16.dp))
                                    Text(text = "Bookmarks, notes and history never leave this phone.", style = typography.caption, color = colors.textTertiary)
                                }
                            }
                        }

                        item { Heading("About", "Fathom for Android") }
                        item {
                            Column {
                                Hairline()
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "Version $versionName", style = typography.title, color = colors.textPrimary)
                                        MonoLabel(text = "Build $buildNumber · production release")
                                    }
                                    when (val status = updateStatus) {
                                        is AppUpdateStatus.Checking -> MonoLabel(text = "Checking", color = tone.hue)
                                        is AppUpdateStatus.Available -> FathomButton(
                                            text = "Update",
                                            onClick = {
                                                haptics.medium()
                                                activity?.let { updateEngine.startFlexibleUpdate(it, status.info) }
                                            },
                                            style = ButtonStyle.Tone,
                                            modifier = Modifier.height(44.dp)
                                        )
                                        is AppUpdateStatus.Downloaded -> FathomButton(
                                            text = "Restart",
                                            onClick = {
                                                haptics.success()
                                                updateEngine.completeUpdate()
                                            },
                                            style = ButtonStyle.Tone,
                                            modifier = Modifier.height(44.dp)
                                        )
                                        is AppUpdateStatus.Downloading -> Column(Modifier.width(120.dp)) {
                                            val fraction = if (status.totalBytes > 0) status.bytesDownloaded.toFloat() / status.totalBytes else 0f
                                            Seekline(progress = fraction, onSeek = {}, color = tone.hue)
                                        }
                                        else -> FathomButton(
                                            text = "Check",
                                            onClick = {
                                                haptics.light()
                                                updateEngine.checkForUpdates(manual = true)
                                            },
                                            style = ButtonStyle.Ghost,
                                            modifier = Modifier.height(44.dp)
                                        )
                                    }
                                }
                                when (val status = updateStatus) {
                                    is AppUpdateStatus.UpToDate -> MonoLabel(text = "You are on the latest build", color = tone.hue, modifier = Modifier.padding(bottom = 16.dp))
                                    is AppUpdateStatus.Error -> Text(text = status.message, style = typography.caption, color = colors.textSecondary, modifier = Modifier.padding(bottom = 16.dp))
                                    else -> Unit
                                }
                                AboutRow(
                                    title = if (subscription.isPro) "Patron Status: Consecrated (${subscription.tier.title})" else "Patron Status: Free Tier",
                                    caption = if (subscription.isPro) "Active Sanctuary license · Tap to manage" else "Unlock all sacred themes, audio & pathways"
                                ) {
                                    haptics.light()
                                    openPaywall()
                                }
                                AboutRow("Privacy Policy", "How your data is handled") { openLegal(LegalDoc.PRIVACY) }
                                AboutRow("Terms of Use", "The ground rules") { openLegal(LegalDoc.TERMS) }
                                AboutRow("Send feedback", LegalDocuments.CONTACT_EMAIL) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("mailto:${LegalDocuments.CONTACT_EMAIL}")
                                                putExtra(Intent.EXTRA_SUBJECT, "Fathom $versionName feedback")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                }
                                AboutRow("Rate Fathom", "Opens Google Play") {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.masum.fathom")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }.onFailure {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.masum.fathom")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 12.dp, end = 20.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceElevated)
                            .tappable(scale = 0.9f, haptic = false) {
                                haptics.light()
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(PhosphorIcons.Light.X, "Close settings", tint = colors.textPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun Heading(overline: String, title: String) {
    Column(Modifier.fillMaxWidth().padding(top = 52.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MonoLabel(text = overline, color = FathomTheme.tone.hue)
        Text(text = title, style = FathomTheme.typography.headline, color = FathomTheme.colors.textPrimary)
    }
}

@Composable
private fun ThemeTile(
    theme: ReadingTheme,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = theme.palette()
    val shape = RoundedCornerShape(24.dp)
    val subtitle = when (theme) {
        ReadingTheme.OBSIDIAN -> "OLED Gold"
        ReadingTheme.SEPIA -> "Warm Parchment"
        ReadingTheme.SLATE -> "Midnight Indigo"
        ReadingTheme.FOREST -> "Forest Emerald"
        ReadingTheme.SAHARA -> "Terracotta Clay"
        ReadingTheme.TWILIGHT -> "Mystic Amethyst"
        ReadingTheme.CINNABAR -> "Imperial Lacquer"
        ReadingTheme.DAWN -> "Crisp Alabaster"
    }

    Box(
        modifier = modifier
            .aspectRatio(1.22f)
            .clip(shape)
            .background(palette.background)
            .border(if (selected) 2.5.dp else 0.75.dp, if (selected) palette.accent else palette.hairline, shape)
            .tappable(scale = 0.97f, haptic = false, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(palette.surface)
                .border(0.75.dp, palette.hairline, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aa",
                style = FathomTheme.typography.headline.copy(fontSize = 26.sp, lineHeight = 28.sp),
                color = palette.textPrimary
            )
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(palette.accent)
                    .border(1.dp, palette.surface, CircleShape)
            )
        }

        Column(
            Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = theme.displayName,
                style = FathomTheme.typography.title.copy(fontSize = 15.sp),
                color = palette.textPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subtitle,
                    style = FathomTheme.typography.caption.copy(fontSize = 11.sp),
                    color = palette.textSecondary
                )
                if (locked) {
                    Icon(
                        PhosphorIcons.Light.LockSimple,
                        null,
                        tint = palette.accent,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(0.75.dp, FathomTheme.colors.hairline, CircleShape)
            .tappable(scale = 0.9f, haptic = false, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = FathomTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun KeyField(
    value: String,
    onValueChange: (String) -> Unit,
    masked: Boolean,
    isTesting: Boolean,
    onToggleMask: () -> Unit,
    onTest: () -> Unit,
    onSave: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val rule by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(240), label = "key_rule")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MonoLabel(text = "API key")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) Text(text = "Paste your secret key", style = FathomTheme.typography.body, color = colors.textTertiary)
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    interactionSource = source,
                    visualTransformation = if (masked) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    textStyle = FathomTheme.typography.body.copy(fontSize = 16.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(tone.hue),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                )
            }
            Box(Modifier.tappable(scale = 0.9f, haptic = false, onClick = onToggleMask).padding(6.dp)) {
                Icon(
                    imageVector = if (masked) PhosphorIcons.Light.Eye else PhosphorIcons.Light.EyeSlash,
                    contentDescription = if (masked) "Show key" else "Hide key",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Box(Modifier.tappable(scale = 0.94f, onClick = onTest).padding(8.dp)) {
                MonoLabel(text = if (isTesting) "Testing..." else "Test", color = colors.textSecondary)
            }
            Box(Modifier.tappable(scale = 0.94f, onClick = onSave).padding(8.dp)) {
                MonoLabel(text = "Save", color = tone.hue)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(rule))
    }
}

@Composable
private fun AboutRow(title: String, caption: String, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val haptics = LocalFathomHaptics.current
    Column {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tappable(scale = 0.99f, haptic = false) {
                    haptics.light()
                    onClick()
                }
                .padding(vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = FathomTheme.typography.title, color = colors.textPrimary)
                Text(text = caption, style = FathomTheme.typography.caption, color = colors.textTertiary)
            }
            Icon(PhosphorIcons.Light.ArrowRight, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
    }
}
