package com.masum.fathom.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.DownloadSimple
import com.adamglin.phosphoricons.light.ShareFat
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.FathomToggle
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.Pill
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.rememberTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QuoteCardModal(
    passage: Passage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = FathomTheme.colors
    val scope = rememberCoroutineScope()
    val tone = rememberTone(passage.traditionId)
    val hasOriginal = !passage.originalText.isNullOrBlank()
    val hasThemes = passage.themes.isNotEmpty()

    var format by remember { mutableStateOf(CardFormat.PORTRAIT) }
    var theme by remember { mutableStateOf(CardTheme.OBSIDIAN) }
    var includeOriginal by remember { mutableStateOf(hasOriginal) }
    var includeThemes by remember { mutableStateOf(hasThemes) }
    var busy by remember { mutableStateOf(false) }
    var card by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(passage.id, format, theme, includeOriginal, includeThemes) {
        card = withContext(Dispatchers.Default) {
            QuoteCardRenderer.generateBitmap(context, passage, format, theme, includeOriginal, includeThemes)
        }
    }

    val short = LocalShell.current.isShort

    val preview: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .heightIn(max = if (short) 260.dp else 400.dp)
                    .aspectRatio(format.aspectRatio)
                    .clip(RoundedCornerShape(20.dp))
                    .border(0.75.dp, colors.hairline, RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated)
            ) {
                Crossfade(targetState = card, animationSpec = tween(260), label = "card_preview") { bitmap ->
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Card preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth().aspectRatio(format.aspectRatio)
                        )
                    }
                }
            }
        }
    }

    val actions: @Composable (Modifier) -> Unit = { modifier ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
            FathomButton(
                text = "Save",
                onClick = {
                    val bitmap = card ?: return@FathomButton
                    if (busy) return@FathomButton
                    busy = true
                    scope.launch {
                        withContext(Dispatchers.IO) { QuoteCardRenderer.saveToGallery(context, bitmap, passage) }
                        busy = false
                    }
                },
                leading = PhosphorIcons.Light.DownloadSimple,
                style = ButtonStyle.Ghost,
                enabled = card != null && !busy,
                modifier = Modifier.weight(1f)
            )
            FathomButton(
                text = "Share",
                onClick = {
                    val bitmap = card ?: return@FathomButton
                    if (busy) return@FathomButton
                    QuoteCardRenderer.shareQuoteCard(context, bitmap, passage)
                },
                leading = PhosphorIcons.Light.ShareFat,
                style = ButtonStyle.Tone,
                enabled = card != null && !busy,
                modifier = Modifier.weight(1.4f)
            )
        }
    }

    val controls: @Composable () -> Unit = {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabel(text = "Format")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardFormat.entries.forEach { option ->
                        Pill(text = option.title, selected = format == option, onClick = { format = option })
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MonoLabel(text = "Palette")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    CardTheme.entries.forEach { option ->
                        val palette = option.reading.palette()
                        val selected = theme == option
                        Column(
                            modifier = Modifier.tappable(scale = 0.92f) { theme = option },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(palette.background)
                                    .border(if (selected) 2.dp else 0.75.dp, if (selected) tone.hue else colors.hairline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(Modifier.size(16.dp).clip(CircleShape).background(palette.textPrimary))
                            }
                            MonoLabel(text = option.title, color = if (selected) colors.textPrimary else colors.textTertiary)
                        }
                    }
                }
            }

            if (hasOriginal) ToggleRow(label = "Original script", checked = includeOriginal, onChange = { includeOriginal = it })
            if (hasThemes) ToggleRow(label = "Theme tags", checked = includeThemes, onChange = { includeThemes = it })

            if (!short) actions(Modifier.fillMaxWidth())
            Spacer(Modifier.height(4.dp))
        }
    }

    CompositionLocalProvider(LocalTone provides tone) {
        FathomSheet(onDismiss = onDismiss) {
            SheetHeader(overline = "Share", title = "Make a card", onClose = onDismiss, trailing = { if (short) actions(Modifier.width(300.dp)) })
            if (short) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    preview(Modifier.weight(0.9f))
                    Box(Modifier.weight(1.1f).verticalScroll(rememberScrollState())) { controls() }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    preview(Modifier)
                    controls()
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = FathomTheme.typography.label.copy(fontSize = 15.sp), color = FathomTheme.colors.textPrimary)
        FathomToggle(checked = checked, onCheckedChange = onChange)
    }
}
