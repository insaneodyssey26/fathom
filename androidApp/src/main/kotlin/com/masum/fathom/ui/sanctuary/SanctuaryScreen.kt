package com.masum.fathom.ui.sanctuary

import androidx.compose.ui.graphics.graphicsLayer
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.design.StaggerItem
import com.adamglin.phosphoricons.light.ArrowLeft
import com.masum.fathom.ui.design.Whisper
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.adamglin.phosphoricons.light.Info
import com.adamglin.phosphoricons.light.LockSimple
import com.adamglin.phosphoricons.light.X
import com.masum.fathom.core.audio.LocalSacredAudioEngine
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.sanctuary.AppIcon
import com.masum.fathom.core.sanctuary.AppIconManager
import com.masum.fathom.core.sanctuary.ByokProvider
import com.masum.fathom.core.sanctuary.SanctuaryDataEngine
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.subscription.LocalOpenPaywall
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.core.subscription.LocalSubscriptionState
import com.masum.fathom.core.update.AppUpdateStatus
import com.masum.fathom.core.update.LocalAppUpdateEngine
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomToggle
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.fadeEdges
import com.masum.fathom.ui.design.LocalBackdrop
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Pill
import com.masum.fathom.ui.design.Seekline
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.shell.LegalDoc
import com.masum.fathom.ui.shell.LegalDocuments
import com.masum.fathom.ui.shell.LocalOpenLegal
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.launch

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
    val appIconManager = remember { AppIconManager(context) }

    var keyInput by remember { mutableStateOf(settings.byokApiKey) }
    var maskKey by remember { mutableStateOf(true) }
    var isTestingKey by remember { mutableStateOf(false) }

    var isRestoringVault by remember { mutableStateOf(false) }

    val savePicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { destinationUri ->
        if (destinationUri != null) {
            scope.launch {
                try {
                    val bytes = dataEngine.exportVaultBytes()
                    val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    if (outputStream != null) {
                        outputStream.use { it.write(bytes) }
                        haptics.success()
                        Whisper.show("Sacred vault saved successfully")
                    } else {
                        Whisper.problem("Could not write to destination")
                    }
                } catch (_: Exception) {
                    Whisper.problem("Could not export vault")
                }
            }
        }
    }

    val markdownPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/markdown")) { destinationUri ->
        if (destinationUri != null) {
            scope.launch {
                try {
                    val mdString = dataEngine.exportMarkdownArchive()
                    val outputStream = context.contentResolver.openOutputStream(destinationUri)
                    if (outputStream != null) {
                        outputStream.use { it.write(mdString.toByteArray(Charsets.UTF_8)) }
                        haptics.success()
                        Whisper.show("Markdown journal exported")
                    } else {
                        Whisper.problem("Could not write to destination")
                    }
                } catch (_: Exception) {
                    Whisper.problem("Could not export markdown")
                }
            }
        }
    }

    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                isRestoringVault = true
                val stream = context.contentResolver.openInputStream(uri)
                if (stream == null) {
                    isRestoringVault = false
                    Whisper.problem("Could not open vault archive")
                    return@launch
                }
                val outcome = dataEngine.restoreVaultArchive(
                    inputStream = stream,
                    isPro = subscription.isPro,
                    freeNotesLimit = subscription.freeNotesLimit
                )
                isRestoringVault = false
                outcome.onSuccess { summary ->
                    haptics.success()
                    Whisper.show("Restored ${summary.bookmarksRestored} bookmarks, ${summary.reflectionsRestored} reflections")
                }.onFailure {
                    haptics.error()
                    Whisper.problem("Invalid or corrupt .fathom archive")
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

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var page by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf<SettingsPage?>(null) }
    androidx.activity.compose.BackHandler(enabled = page != null) { page = null }
    androidx.compose.runtime.LaunchedEffect(page) { listState.scrollToItem(0) }

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalBackdrop provides null) {
            Atmosphere(tone = tone, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxHeight().widthIn(max = 640.dp).windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)).fadeEdges(top = 96.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 96.dp, bottom = 48.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    ) {
                        item { StaggerItem(0) {
                            if (page == null) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    MonoLabel(text = "Settings", color = tone.hue)
                                    Text(text = "Sanctuary", style = typography.display, color = colors.textPrimary)
                                    Text(
                                        text = "How Fathom looks, sounds and remembers.",
                                        style = typography.commentaryBody,
                                        color = colors.textSecondary
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .tappable(scale = 0.96f) { page = null }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(PhosphorIcons.Light.ArrowLeft, "Back", tint = tone.hue, modifier = Modifier.size(18.dp))
                                    MonoLabel(text = "Settings", color = tone.hue)
                                }
                            }
                         }}

                        if (page == null) {
                        item { StaggerItem(1) {
                            val membershipLabel = if (subscription.isPro) {
                                when (subscription.tier) {
                                    SubscriptionTier.LIFETIME -> "Plus Lifetime"
                                    SubscriptionTier.ANNUAL -> "Plus Annual"
                                    SubscriptionTier.MONTHLY -> "Plus Monthly"
                                    else -> "Fathom Plus"
                                }
                            } else {
                                "Fathom Plus"
                            }
                            val membershipSubtitle = if (subscription.isPro) {
                                "Active membership · Tap to manage subscription"
                            } else {
                                "Unlock every theme, soundscape and study path"
                            }

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
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(tone.hue.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = membershipLabel.uppercase(),
                                            style = typography.caption.copy(
                                                fontSize = 11.5.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                letterSpacing = 1.1.sp
                                            ),
                                            color = tone.hue
                                        )
                                    }
                                    Text(
                                        text = membershipSubtitle,
                                        style = typography.title.copy(fontSize = 16.sp),
                                        color = colors.textPrimary
                                    )
                                }
                                Icon(PhosphorIcons.Light.ArrowRight, null, tint = tone.hue, modifier = Modifier.size(22.dp))
                            }
                         }}

                        }

                        if (page == null) {
                            item(key = "hub_rows") { StaggerItem(2) {
                                Column(Modifier.padding(top = 28.dp)) {
                                    SettingsPage.entries.forEach { entry ->
                                        SettingsHubRow(entry = entry, onClick = {
                                            haptics.light()
                                            page = entry
                                        })
                                    }
                                    Hairline()
                                }
                             }}
                        }
                        if (page == SettingsPage.Appearance) {
                        item { StaggerItem(1) { Heading("Appearance", "Reading themes")  }}
                        item { StaggerItem(2) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ReadingTheme.entries.chunked(2).forEach { pair ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                         }}

                        item { StaggerItem(3) { Heading("App Icon", "Home screen appearance")  }}
                        item { StaggerItem(4) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                AppIcon.entries.chunked(3).forEach { rowIcons ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowIcons.forEach { icon ->
                                            val isLocked = icon.isPatronOnly && !subscription.isPro
                                            val isSelected = settings.appIcon == icon
                                            AppIconTile(
                                                icon = icon,
                                                selected = isSelected,
                                                locked = isLocked,
                                                modifier = Modifier.weight(1f),
                                                onClick = {
                                                    if (isLocked) {
                                                        haptics.medium()
                                                        openPaywall()
                                                    } else {
                                                        haptics.success()
                                                        appIconManager.setAppIcon(icon)
                                                        preferences.updateAppIcon(icon)
                                                    }
                                                }
                                            )
                                        }
                                        if (rowIcons.size < 3) {
                                            repeat(3 - rowIcons.size) {
                                                Spacer(Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                         }}

                        item { StaggerItem(5) {
                            Spacer(Modifier.height(22.dp))
                            Hairline()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .tappable(scale = 0.995f) {
                                        if (!subscription.isPro) {
                                            haptics.medium()
                                            openPaywall()
                                        } else {
                                            haptics.light()
                                            preferences.updateDynamicTraditionTones(!settings.dynamicTraditionTones)
                                        }
                                    }
                                    .padding(vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Adaptive Tradition Themes",
                                            style = typography.title.copy(fontSize = 15.sp),
                                            color = colors.textPrimary
                                        )
                                        if (!subscription.isPro) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(tone.hue.copy(alpha = 0.14f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "PLUS",
                                                    style = typography.caption.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = tone.hue
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (!subscription.isPro) {
                                            "Lock reading screen strictly to your chosen theme without tradition color shifts."
                                        } else if (settings.dynamicTraditionTones) {
                                            "Subtly adapt color tones to match the tradition of each passage."
                                        } else {
                                            "Consistent reading theme atmosphere without tradition color shifts."
                                        },
                                        style = typography.body.copy(fontSize = 13.sp),
                                        color = colors.textSecondary
                                    )
                                }
                                if (subscription.isPro) {
                                    FathomToggle(
                                        checked = settings.dynamicTraditionTones,
                                        onCheckedChange = {
                                            haptics.light()
                                            preferences.updateDynamicTraditionTones(it)
                                        }
                                    )
                                } else {
                                    Icon(
                                        imageVector = PhosphorIcons.Light.LockSimple,
                                        contentDescription = null,
                                        tint = colors.textTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                         }}

                        item { StaggerItem(6) {
                            Hairline()
                            CustomGradientStudioSection(
                                settings = settings,
                                preferences = preferences,
                                isPro = subscription.isPro,
                                onOpenPaywall = openPaywall
                            )
                            Hairline()
                         }}
                        }

                        if (page == SettingsPage.Alerts) {
                        item { StaggerItem(1) { Heading("Notifications", "Reminders and alerts")  }}
                        item { StaggerItem(2) { NotificationSettings(preferences = preferences, settings = settings)  }}
                        }

                        if (page == SettingsPage.Voice) {
                        item { StaggerItem(1) { Heading("Voice", "Narration")  }}
                        item { StaggerItem(2) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(tone.hue.copy(alpha = 0.08f))
                                        .border(0.75.dp, tone.hue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Light.Info,
                                        contentDescription = null,
                                        tint = tone.hue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "Experimental Feature",
                                            style = typography.caption.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "Voice recitation is actively being refined and tested. Offline speech quality depends on your device engine.",
                                            style = typography.caption.copy(fontSize = 11.5.sp),
                                            color = colors.textSecondary
                                        )
                                    }
                                }

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
                                                    Whisper.show(msg)
                                                }.onFailure { err ->
                                                    haptics.error()
                                                    Whisper.problem(err.message ?: "Connection failed")
                                                }
                                            }
                                        },
                                        onSave = {
                                            haptics.success()
                                            preferences.updateByokSettings(settings.byokProvider, keyInput.trim(), settings.byokVoiceId)
                                            Whisper.show("Key saved on this device")
                                        }
                                    )
                                }
                            }
                         }}
                        }

                        if (page == SettingsPage.Data) {
                        item { StaggerItem(1) { Heading("Backup & Export", "Journal and bookmarks")  }}
                        item { StaggerItem(2) {
                            Column {
                                Hairline()
                                Spacer(Modifier.height(18.dp))
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
                                        text = if (isRestoringVault) "Restoring..." else "Restore",
                                        onClick = {
                                            if (!subscription.isPro) {
                                                haptics.medium()
                                                openPaywall()
                                            } else if (!isRestoringVault) {
                                                haptics.light()
                                                restorePicker.launch(arrayOf("*/*"))
                                            }
                                        },
                                        style = ButtonStyle.Ghost,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(Modifier.height(10.dp))

                                FathomButton(
                                    text = "Export to Markdown (.md)",
                                    onClick = {
                                        if (!subscription.isPro) {
                                            haptics.medium()
                                            openPaywall()
                                        } else {
                                            haptics.light()
                                            val dateStamp = java.time.LocalDate.now().toString()
                                            markdownPicker.launch("fathom-journal-$dateStamp.md")
                                        }
                                    },
                                    style = ButtonStyle.Ghost,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    Modifier.padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(PhosphorIcons.Light.LockSimple, null, tint = colors.textTertiary, modifier = Modifier.size(16.dp))
                                    Text(text = "Your journal and bookmarks remain stored on this device.", style = typography.caption, color = colors.textTertiary)
                                }
                            }
                         }}
                        }

                        if (page == SettingsPage.About) {
                        item { StaggerItem(1) { Heading("About", "Fathom for Android")  }}
                        item { StaggerItem(2) {
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
                                    title = if (subscription.isPro) "Fathom Plus (${subscription.tier.title})" else "Membership: Free Plan",
                                    caption = if (subscription.isPro) "Active Plus license · Tap to manage" else "Unlock all sacred themes, audio & pathways"
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
                         }}
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
    val shape = RoundedCornerShape(18.dp)
    val ring by androidx.compose.animation.animateColorAsState(
        if (selected) palette.accent else palette.hairline,
        androidx.compose.animation.core.tween(150),
        label = "theme_ring"
    )
    Row(
        modifier = modifier
            .height(68.dp)
            .clip(shape)
            .background(palette.background)
            .border(if (selected) 1.5.dp else 0.75.dp, ring, shape)
            .tappable(scale = 0.98f, haptic = false, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Aa",
            style = FathomTheme.typography.headline.copy(fontSize = 24.sp, lineHeight = 28.sp),
            color = palette.textPrimary
        )
        Text(
            text = theme.displayName,
            style = FathomTheme.typography.label.copy(fontSize = 13.sp),
            color = palette.textSecondary,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        if (locked) {
            Icon(PhosphorIcons.Light.LockSimple, null, tint = palette.textTertiary, modifier = Modifier.size(14.dp))
        } else {
            Box(Modifier.size(9.dp).clip(CircleShape).background(palette.accent))
        }
    }
}

@Composable
private fun AppIconTile(
    icon: AppIcon,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(
                width = if (selected) 2.dp else 0.75.dp,
                color = if (selected) tone.hue else colors.hairline,
                shape = shape
            )
            .tappable(scale = 0.95f, haptic = false, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    width = 1.dp,
                    color = if (selected) tone.hue.copy(alpha = 0.5f) else colors.hairline,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon.previewResId),
                contentDescription = icon.displayName,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = icon.displayName,
            style = typography.caption.copy(fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (locked) {
                Icon(
                    PhosphorIcons.Light.LockSimple,
                    null,
                    tint = tone.hue,
                    modifier = Modifier.size(11.dp)
                )
            }
            Text(
                text = if (locked) "Plus" else if (selected) "Active" else icon.subtitle.split("&").first().trim(),
                style = typography.caption.copy(
                    fontSize = 10.sp,
                    color = if (selected) tone.hue else colors.textTertiary
                )
            )
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

private enum class SettingsPage(val title: String, val caption: String, val glyph: com.masum.fathom.ui.design.GlyphKind) {
    Appearance("Appearance", "Themes, app icon and feed gradient", com.masum.fathom.ui.design.GlyphKind.Star),
    Alerts("Notifications", "Daily verse and reminders", com.masum.fathom.ui.design.GlyphKind.Circle),
    Voice("Voice", "Narration and your own API key", com.masum.fathom.ui.design.GlyphKind.Hexagon),
    Data("Backup & Export", "Vault, journal and bookmarks", com.masum.fathom.ui.design.GlyphKind.Square),
    About("About", "Version, updates, privacy and feedback", com.masum.fathom.ui.design.GlyphKind.Circle)
}

@Composable
private fun SettingsHubRow(entry: SettingsPage, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    Column {
        Hairline()
        Row(
            modifier = Modifier.fillMaxWidth().tappable(scale = 0.99f, haptic = false, onClick = onClick).padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.masum.fathom.ui.design.Glyph(kind = entry.glyph, color = FathomTheme.tone.hue, size = 30.dp, filled = false)
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(text = entry.title, style = FathomTheme.typography.headline.copy(fontSize = 26.sp, lineHeight = 30.sp), color = colors.textPrimary)
                MonoLabel(text = entry.caption, color = colors.textTertiary)
            }
            Icon(PhosphorIcons.Light.ArrowRight, null, tint = colors.textTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
