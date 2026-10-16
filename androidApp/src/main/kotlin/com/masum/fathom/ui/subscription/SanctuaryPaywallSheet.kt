package com.masum.fathom.ui.subscription

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.user.SubscriptionTier
import com.masum.fathom.core.subscription.LocalSubscriptionManager
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import androidx.compose.material3.CircularProgressIndicator
import com.masum.fathom.core.subscription.ActiveDeviceInfo

private data class PatronPerk(val number: String, val title: String, val body: String)

private val PatronPerks = listOf(
    PatronPerk("01", "Eight sacred reading themes", "Nocturne, Vellum, Abyss, Moss, Sahara, Twilight, Cinnabar, and Dawn."),
    PatronPerk("02", "Full sacred pathways immersion", "Sequential chapter progression across all 15 canonical texts with DepthDial radial progress tracking."),
    PatronPerk("03", "Unlimited reflections & vault", "Write unlimited personal verse reflections and export your private encrypted data archive."),
    PatronPerk("04", "Sanctuary streak ritual widget", "Live ritual counter and consecrated contemplation tracker on your home screen."),
    PatronPerk("05", "Sacred tradition morning alarms", "Tailored daily morning contemplation notifications delivering wisdom from your chosen path."),
    PatronPerk("06", "All ambient soundscapes & timers", "Continuous sacred audio soundscapes with unlimited extended contemplation timers."),
    PatronPerk("07", "Patron seal & zero ads forever", "Golden consecrated Patron emblem in your Sanctuary profile with guaranteed zero tracking.")
)

@Composable
fun SanctuaryPaywallSheet(onDismiss: () -> Unit) {
    val haptics = LocalFathomHaptics.current
    val manager = LocalSubscriptionManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val isPro = manager.subscriptionState.value.isPro
    var selected by remember { mutableStateOf(SubscriptionTier.ANNUAL) }
    var showRestoreSheet by remember { mutableStateOf(false) }
    var showDeviceSheet by remember { mutableStateOf(false) }

    if (showRestoreSheet) {
        RestorePurchaseSheet(
            onDismiss = { showRestoreSheet = false },
            onRestore = { input ->
                scope.launch {
                    val result = manager.activateWithKey(input.trim().uppercase(), null)
                    if (result.isValid) {
                        haptics.success()
                        Toast.makeText(context, "Welcome, Fathom Patron. Sanctuary unlocked.", Toast.LENGTH_LONG).show()
                        showRestoreSheet = false
                        onDismiss()
                    } else {
                        haptics.error()
                        Toast.makeText(context, result.errorMessage ?: "License key not found or expired.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
        return
    }

    if (showDeviceSheet) {
        DeviceManagementSheet(
            onDismiss = { showDeviceSheet = false },
            onDeactivatedCurrent = {
                showDeviceSheet = false
                onDismiss()
            }
        )
        return
    }

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = if (isPro) "PATRON CONSECRATED" else "FATHOM PATRON", color = tone.hue)
                Text(
                    text = if (isPro) "Sanctuary Is Active." else "Consecrate Your Sanctuary.",
                    style = typography.display,
                    color = colors.textPrimary
                )
                Text(
                    text = if (isPro) {
                        "Your Patron license is active on this device. You can manage connected devices or deactivate this phone anytime."
                    } else {
                        "Support independent sacred wisdom. Unlock all themes, pathways, reflections, and neural studio audio."
                    },
                    style = typography.commentaryBody,
                    color = colors.textSecondary
                )
            }

            if (isPro) {
                Spacer(Modifier.height(28.dp))
                FathomButton(
                    text = "Manage Connected Devices",
                    onClick = {
                        haptics.light()
                        showDeviceSheet = true
                    },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                FathomButton(
                    text = "Deactivate this Device",
                    onClick = {
                        haptics.medium()
                        scope.launch {
                            manager.deactivateCurrentDevice()
                            Toast.makeText(context, "License deactivated on this device.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    },
                    style = ButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(Modifier.padding(top = 28.dp)) {
                Hairline()
                PatronPerks.forEach { perk ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        MonoLabel(text = perk.number, color = tone.hue, modifier = Modifier.padding(top = 4.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = perk.title, style = typography.title, color = colors.textPrimary)
                            Text(text = perk.body, style = typography.body, color = colors.textSecondary)
                        }
                    }
                    Hairline()
                }
            }

            if (!isPro) {
                val countryCode = remember { java.util.Locale.getDefault().country.orEmpty() }

                Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf(SubscriptionTier.LIFETIME, SubscriptionTier.ANNUAL, SubscriptionTier.MONTHLY).forEach { tier ->
                        PatronTierRow(
                            tier = tier,
                            countryCode = countryCode,
                            selected = selected == tier,
                            onClick = {
                                haptics.light()
                                selected = tier
                            }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(0.75.dp, colors.hairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                 modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(tone.hue)
                            )
                            MonoLabel(text = "Secure Global Checkout", color = tone.hue)
                        }
                        Text(
                            text = "Prices are listed in USD. Handled securely with instant multi-device activation by Dodo Payments.",
                            style = FathomTheme.typography.caption,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                FathomButton(
                    text = "Become a Patron (${selected.title})",
                    onClick = {
                        haptics.heavy()
                        manager.launchDodoCheckout(selected)
                    },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .tappable(scale = 0.95f) {
                            haptics.light()
                            showRestoreSheet = true
                        }
                        .padding(vertical = 16.dp, horizontal = 24.dp)
                ) {
                    MonoLabel(text = "Already a Patron? Enter license key", color = colors.textTertiary)
                }
            }
        }
    }
}

@Composable
private fun PatronTierRow(tier: SubscriptionTier, countryCode: String, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val shape = RoundedCornerShape(22.dp)
    val border by animateColorAsState(if (selected) tone.hue else colors.hairline, tween(240), label = "tier_border")
    val fill by animateColorAsState(if (selected) tone.hue.copy(alpha = 0.10f) else androidx.compose.ui.graphics.Color.Transparent, tween(240), label = "tier_fill")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(if (selected) 1.5.dp else 0.75.dp, border, shape)
            .tappable(scale = 0.985f, haptic = false, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(1.5.dp, if (selected) tone.hue else colors.textTertiary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Box(Modifier.size(12.dp).clip(CircleShape).background(tone.hue))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = tier.title, style = FathomTheme.typography.title, color = colors.textPrimary)
            MonoLabel(text = tier.localizedBillingPeriod(countryCode), color = colors.textTertiary)
            tier.badgeLabel?.let {
                MonoLabel(
                    text = it,
                    color = tone.hue,
                    maxLines = 2
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = tier.localizedPriceDisplay(countryCode), style = FathomTheme.typography.headline.copy(fontSize = 20.sp), color = colors.textPrimary)
            MonoLabel(text = tier.localizedEquivalentMonthlyPrice(countryCode), color = colors.textTertiary)
        }
    }
}

@Composable
private fun RestorePurchaseSheet(onDismiss: () -> Unit, onRestore: (String) -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val manager = LocalSubscriptionManager.current
    val haptics = LocalFathomHaptics.current
    var emailOrKey by remember { mutableStateOf("") }

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(
            overline = "PATRON RESTORE",
            title = "Enter License Key",
            subtitle = "Paste the license key sent to your email after checkout to unlock Patron access.",
            onClose = onDismiss
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.hairline, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            if (emailOrKey.isEmpty()) {
                Text(text = "XXXX-XXXX-XXXX-XXXX", style = FathomTheme.typography.body, color = colors.textTertiary)
            }
            BasicTextField(
                value = emailOrKey,
                onValueChange = { emailOrKey = it },
                singleLine = true,
                textStyle = FathomTheme.typography.body.copy(fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(tone.hue),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(16.dp))
        FathomButton(
            text = "Activate License",
            onClick = { onRestore(emailOrKey) },
            enabled = emailOrKey.isNotBlank(),
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth()
        )
        if (manager.subscriptionState.value.isPro) {
            Spacer(Modifier.height(10.dp))
            FathomButton(
                text = "Reset to Free Tier",
                onClick = {
                    manager.resetToFree()
                    haptics.light()
                    onDismiss()
                },
                style = ButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeviceManagementSheet(
    onDismiss: () -> Unit,
    onDeactivatedCurrent: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    val manager = LocalSubscriptionManager.current
    val haptics = LocalFathomHaptics.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var devices by remember { mutableStateOf<List<ActiveDeviceInfo>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var actionInProgress by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        devices = manager.fetchActiveDevices()
        isLoading = false
    }

    FathomSheet(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState())
        ) {
            SheetHeader(
                overline = "PATRON LICENSING",
                title = "Connected Devices",
                subtitle = "Your Patron license supports up to 2 active devices simultaneously. Revoking frees up that device slot instantly.",
                onClose = onDismiss
            )

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = tone.hue, modifier = Modifier.size(28.dp))
                    }
                }
                devices.isNullOrEmpty() -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No active devices found.",
                            style = typography.body,
                            color = colors.textSecondary
                        )
                    }
                }
                else -> {
                    val list = devices!!
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        list.forEach { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(colors.surfaceElevated)
                                    .border(1.dp, colors.hairline, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = dev.deviceName,
                                        style = typography.title,
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (dev.isCurrentDevice) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(tone.hue.copy(alpha = 0.14f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                MonoLabel(text = "This device", color = tone.hue)
                                            }
                                        }
                                        MonoLabel(
                                            text = "ID: ${dev.deviceId.take(8)}…",
                                            color = colors.textTertiary
                                        )
                                    }
                                }

                                FathomButton(
                                    text = if (dev.isCurrentDevice) "Deactivate" else "Revoke",
                                    onClick = {
                                        if (actionInProgress) return@FathomButton
                                        actionInProgress = true
                                        haptics.medium()
                                        scope.launch {
                                            val ok = manager.revokeDevice(dev.deviceId)
                                            if (ok) {
                                                haptics.success()
                                                if (dev.isCurrentDevice) {
                                                    Toast.makeText(context, "Deactivated this device.", Toast.LENGTH_SHORT).show()
                                                    onDeactivatedCurrent()
                                                } else {
                                                    Toast.makeText(context, "Device revoked. Slot freed.", Toast.LENGTH_SHORT).show()
                                                    devices = manager.fetchActiveDevices()
                                                }
                                            } else {
                                                haptics.error()
                                                Toast.makeText(context, "Failed to revoke device.", Toast.LENGTH_SHORT).show()
                                            }
                                            actionInProgress = false
                                        }
                                    },
                                    style = ButtonStyle.Ghost,
                                    modifier = Modifier
                                        .height(38.dp)
                                        .padding(horizontal = 0.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            FathomButton(
                text = "Deactivate Current Phone",
                onClick = {
                    if (actionInProgress) return@FathomButton
                    actionInProgress = true
                    haptics.medium()
                    scope.launch {
                        manager.deactivateCurrentDevice()
                        haptics.success()
                        Toast.makeText(context, "License removed from this phone.", Toast.LENGTH_SHORT).show()
                        onDeactivatedCurrent()
                    }
                },
                style = ButtonStyle.Ghost,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

