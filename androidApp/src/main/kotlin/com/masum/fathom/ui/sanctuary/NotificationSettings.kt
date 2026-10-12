package com.masum.fathom.ui.sanctuary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.Minus
import com.adamglin.phosphoricons.light.Plus
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.notification.DailyNotificationScheduler
import com.masum.fathom.core.notification.DailyVerseNotifier
import com.masum.fathom.core.notification.DailyVersePicker
import com.masum.fathom.core.notification.NotificationChannels
import com.masum.fathom.core.sanctuary.SanctuaryPreferences
import com.masum.fathom.core.sanctuary.SanctuarySettings
import com.masum.fathom.core.sanctuary.VerseSource
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomToggle
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Pill
import com.masum.fathom.ui.theme.FathomTheme
import java.time.LocalDate
import java.util.Locale

private data class TimePreset(val label: String, val hour: Int, val minute: Int)

private val Presets = listOf(
    TimePreset("Morning", 7, 0),
    TimePreset("Midday", 12, 0),
    TimePreset("Evening", 20, 0),
    TimePreset("Night", 22, 0)
)

private fun needsRuntimePermission(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

private fun openSystemSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Composable
fun NotificationSettings(
    preferences: SanctuaryPreferences,
    settings: SanctuarySettings
) {
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val haptics = LocalFathomHaptics.current

    var systemEnabled by remember { mutableStateOf(NotificationChannels.areEnabled(context)) }
    var denied by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        systemEnabled = NotificationChannels.areEnabled(context)
        if (systemEnabled) denied = false
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        systemEnabled = NotificationChannels.areEnabled(context)
        if (granted && systemEnabled) {
            pending?.invoke()
            denied = false
        } else {
            denied = true
        }
        pending = null
    }

    fun withPermission(action: () -> Unit) {
        if (systemEnabled || !needsRuntimePermission()) {
            action()
        } else {
            pending = action
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun setDaily(enabled: Boolean) {
        preferences.updateNotificationTime(enabled, settings.notificationHour, settings.notificationMinute)
        if (enabled) {
            DailyNotificationScheduler.scheduleDailyReminder(context, settings.notificationHour, settings.notificationMinute)
        } else {
            DailyNotificationScheduler.cancelDailyReminder(context)
        }
    }

    fun setTime(hour: Int, minute: Int) {
        preferences.updateNotificationTime(true, hour, minute)
        DailyNotificationScheduler.scheduleDailyReminder(context, hour, minute)
    }

    val wantsNotifications = settings.dailyNotificationEnabled || settings.updateAlertsEnabled

    Column {
        Hairline()
        ToggleRow(
            title = "Daily verse",
            body = "One verse a day, at the time you choose.",
            checked = settings.dailyNotificationEnabled && systemEnabled,
            onChange = { enabled ->
                haptics.light()
                if (enabled) withPermission { setDaily(true) } else setDaily(false)
            }
        )

        AnimatedVisibility(
            visible = settings.dailyNotificationEnabled && systemEnabled,
            enter = expandVertically(tween(320)) + fadeIn(tween(240)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(140))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(22.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MonoLabel(text = "Time")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Presets.forEach { preset ->
                            Pill(
                                text = preset.label,
                                selected = settings.notificationHour == preset.hour && settings.notificationMinute == preset.minute,
                                onClick = {
                                    haptics.light()
                                    setTime(preset.hour, preset.minute)
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Stepper(
                            value = String.format(Locale.US, "%02d", settings.notificationHour),
                            label = "Hour",
                            onMinus = { haptics.light(); setTime((settings.notificationHour + 23) % 24, settings.notificationMinute) },
                            onPlus = { haptics.light(); setTime((settings.notificationHour + 1) % 24, settings.notificationMinute) }
                        )
                        Text(text = ":", style = typography.headline, color = colors.textTertiary, modifier = Modifier.padding(bottom = 18.dp))
                        Stepper(
                            value = String.format(Locale.US, "%02d", settings.notificationMinute),
                            label = "Minute",
                            onMinus = { haptics.light(); setTime(settings.notificationHour, (settings.notificationMinute + 55) % 60) },
                            onPlus = { haptics.light(); setTime(settings.notificationHour, (settings.notificationMinute + 5) % 60) }
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MonoLabel(text = "Verses from")
                    val tradition = settings.userTradition
                    if (tradition != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill(
                                text = "Every tradition",
                                selected = settings.verseSource == VerseSource.ALL_TRADITIONS,
                                onClick = { haptics.light(); preferences.updateVerseSource(VerseSource.ALL_TRADITIONS) }
                            )
                            Pill(
                                text = "Mostly $tradition",
                                selected = settings.verseSource == VerseSource.MY_TRADITION,
                                onClick = {
                                    haptics.light()
                                    preferences.updateVerseSource(VerseSource.MY_TRADITION)
                                }
                            )
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill(
                                text = "Every tradition",
                                selected = true,
                                onClick = { haptics.light() }
                            )
                        }
                        Text(
                            text = "Universal path selected in your Journal profile. Verses will cycle across all sacred traditions.",
                            style = typography.caption,
                            color = colors.textTertiary
                        )
                    }
                }

                Hairline()
                ToggleRow(
                    title = "Show the verse on the lock screen",
                    body = "Off keeps the verse hidden until you unlock your phone.",
                    checked = settings.showVerseOnLockScreen,
                    onChange = { haptics.light(); preferences.updateLockScreenVerse(it) },
                    padded = false
                )

                FathomButton(
                    text = "Send a preview",
                    onClick = {
                        haptics.light()
                        val verse = DailyVersePicker.pickFor(context, LocalDate.now(), settings.verseSource, settings.userTradition)
                        DailyVerseNotifier.post(context, verse, settings)
                    },
                    style = ButtonStyle.Ghost,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }
        }

        Hairline()
        ToggleRow(
            title = "Update alerts",
            body = "A quiet note when a new version of Fathom is on Google Play.",
            checked = settings.updateAlertsEnabled && systemEnabled,
            onChange = { enabled ->
                haptics.light()
                if (enabled) withPermission { preferences.updateUpdateAlerts(true) } else preferences.updateUpdateAlerts(false)
            }
        )
        Hairline()

        AnimatedVisibility(
            visible = (!systemEnabled && wantsNotifications) || denied,
            enter = expandVertically(tween(320)) + fadeIn(tween(240)),
            exit = shrinkVertically(tween(220)) + fadeOut(tween(140))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.surfaceElevated)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonoLabel(text = "Notifications are off", color = FathomTheme.tone.hue)
                Text(
                    text = "Android is blocking notifications from Fathom, so reminders and update alerts can't arrive.",
                    style = typography.body,
                    color = colors.textSecondary
                )
                FathomButton(
                    text = "Open notification settings",
                    onClick = { openSystemSettings(context) },
                    style = ButtonStyle.Tone,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    body: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    padded: Boolean = true
) {
    val colors = FathomTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = if (padded) 18.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = FathomTheme.typography.title, color = colors.textPrimary)
            Text(text = body, style = FathomTheme.typography.body, color = colors.textSecondary)
        }
        FathomToggle(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun Stepper(value: String, label: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    val colors = FathomTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            StepButton(icon = PhosphorIcons.Light.Minus, description = "$label earlier", onClick = onMinus)
            Text(
                text = value,
                style = FathomTheme.typography.numeral.copy(fontSize = 38.sp, lineHeight = 40.sp),
                color = colors.textPrimary,
                modifier = Modifier.width(56.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            StepButton(icon = PhosphorIcons.Light.Plus, description = "$label later", onClick = onPlus)
        }
        MonoLabel(text = label)
    }
}
