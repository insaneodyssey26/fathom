package com.masum.fathom.ui.shell

import android.os.Build
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.Waves
import com.adamglin.phosphoricons.light.GearSix
import com.adamglin.phosphoricons.light.Lock
import com.adamglin.phosphoricons.light.Scroll
import com.adamglin.phosphoricons.light.Sparkle
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun MoreSheet(
    onDismiss: () -> Unit,
    onSoundscapes: () -> Unit,
    onSettings: () -> Unit,
    onWhatsNew: () -> Unit,
    onLegal: (LegalDoc) -> Unit
) {
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val versionName = remember(context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).versionName
            }
        }.getOrNull().orEmpty()
    }

    FathomSheet(onDismiss = onDismiss) {
        SheetHeader(overline = "fathom $versionName", title = "More", onClose = onDismiss)
        Hairline()
        MoreRow(PhosphorIcons.Light.Waves, "Soundscapes", "Rain, bowls and a sleep timer") { onDismiss(); onSoundscapes() }
        MoreRow(PhosphorIcons.Light.GearSix, "Settings", "Themes, reminders, backup and updates") { onDismiss(); onSettings() }
        MoreRow(PhosphorIcons.Light.Sparkle, "What's new", "Release notes") { onDismiss(); onWhatsNew() }
        MoreRow(PhosphorIcons.Light.Lock, "Privacy Policy", "How your data is handled") { onDismiss(); onLegal(LegalDoc.PRIVACY) }
        MoreRow(PhosphorIcons.Light.Scroll, "Terms of Use", "The ground rules") { onDismiss(); onLegal(LegalDoc.TERMS) }
        Spacer(Modifier.height(20.dp))
        MonoLabel(text = "Offline by design · no accounts · no tracking", color = colors.textTertiary)
    }
}

@Composable
private fun MoreRow(icon: ImageVector, title: String, caption: String, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val haptics = LocalFathomHaptics.current
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .tappable(scale = 0.985f, haptic = false) {
                    haptics.light()
                    onClick()
                }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Icon(icon, null, tint = FathomTheme.tone.hue, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(text = title, style = FathomTheme.typography.title, color = colors.textPrimary)
                Text(text = caption, style = FathomTheme.typography.caption, color = colors.textTertiary)
            }
            Icon(PhosphorIcons.Light.ArrowRight, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
        }
        Hairline()
    }
}
