package com.masum.fathom.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.FloppyDisk
import com.adamglin.phosphoricons.regular.ShareNetwork
import com.adamglin.phosphoricons.regular.X
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.JakartaFontFamily
import com.masum.fathom.ui.theme.SyneFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteCardModal(
    passage: Passage,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedFormat by remember { mutableStateOf(CardFormat.PORTRAIT) }
    var selectedTheme by remember { mutableStateOf(CardTheme.OBSIDIAN) }
    var includeOriginal by remember { mutableStateOf(!passage.originalText.isNullOrBlank()) }
    var includeThemes by remember { mutableStateOf(passage.themes.isNotEmpty()) }
    var isProcessing by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.background,
        contentColor = FathomTheme.colors.textPrimary,
        dragHandle = null,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QUOTE CARD",
                        style = FathomTheme.typography.caption.copy(
                            fontSize = 11.sp,
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = FathomTheme.colors.accentGold
                    )
                    Text(
                        text = "Export Typographic Card",
                        style = FathomTheme.typography.commentaryTitle.copy(
                            fontFamily = JakartaFontFamily,
                            fontSize = 18.sp
                        ),
                        color = FathomTheme.colors.textPrimary
                    )
                }

                IconButton(
                    onClick = {
                        haptics.light()
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = FathomTheme.colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(selectedTheme.bgHex))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                QuoteCardPreview(
                    passage = passage,
                    format = selectedFormat,
                    theme = selectedTheme,
                    includeOriginal = includeOriginal,
                    includeThemes = includeThemes,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .aspectRatio(selectedFormat.aspectRatio)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ASPECT RATIO",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FathomTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardFormat.entries.forEach { format ->
                        val isSelected = selectedFormat == format
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.16f) else FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    haptics.light()
                                    selectedFormat = format
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = format.title,
                                fontFamily = JakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PALETTE THEME",
                    style = FathomTheme.typography.caption.copy(
                        fontSize = 10.5.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = FathomTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardTheme.entries.forEach { theme ->
                        val isSelected = selectedTheme == theme
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) FathomTheme.colors.accentGold.copy(alpha = 0.16f) else FathomTheme.colors.surfaceElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    haptics.light()
                                    selectedTheme = theme
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(theme.cardBgHex))
                                    .border(1.dp, Color(theme.accentGoldHex), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = theme.title,
                                fontFamily = JakartaFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }
                }

                if (!passage.originalText.isNullOrBlank() || passage.themes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ELEMENTS",
                        style = FathomTheme.typography.caption.copy(
                            fontSize = 10.5.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = FathomTheme.colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (!passage.originalText.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Include Original Script",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.5.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Switch(
                                checked = includeOriginal,
                                onCheckedChange = {
                                    haptics.light()
                                    includeOriginal = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FathomTheme.colors.accentGold,
                                    checkedTrackColor = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                                    uncheckedThumbColor = FathomTheme.colors.textTertiary,
                                    uncheckedTrackColor = FathomTheme.colors.surfaceElevated
                                )
                            )
                        }
                    }

                    if (passage.themes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Include Theme Tags",
                                fontFamily = JakartaFontFamily,
                                fontSize = 13.5.sp,
                                color = FathomTheme.colors.textPrimary
                            )
                            Switch(
                                checked = includeThemes,
                                onCheckedChange = {
                                    haptics.light()
                                    includeThemes = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = FathomTheme.colors.accentGold,
                                    checkedTrackColor = FathomTheme.colors.accentGold.copy(alpha = 0.35f),
                                    uncheckedThumbColor = FathomTheme.colors.textTertiary,
                                    uncheckedTrackColor = FathomTheme.colors.surfaceElevated
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics.light()
                        if (!isProcessing) {
                            isProcessing = true
                            coroutineScope.launch {
                                val bitmap = withContext(Dispatchers.Default) {
                                    QuoteCardRenderer.generateBitmap(
                                        context = context,
                                        passage = passage,
                                        format = selectedFormat,
                                        theme = selectedTheme,
                                        includeOriginal = includeOriginal,
                                        includeThemes = includeThemes
                                    )
                                }
                                QuoteCardRenderer.saveToGallery(context, bitmap, passage)
                                haptics.success()
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FathomTheme.colors.borderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = FathomTheme.colors.surfaceElevated,
                        contentColor = FathomTheme.colors.textPrimary
                    )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.FloppyDisk,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = FathomTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SAVE",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }

                Button(
                    onClick = {
                        haptics.light()
                        if (!isProcessing) {
                            isProcessing = true
                            coroutineScope.launch {
                                val bitmap = withContext(Dispatchers.Default) {
                                    QuoteCardRenderer.generateBitmap(
                                        context = context,
                                        passage = passage,
                                        format = selectedFormat,
                                        theme = selectedTheme,
                                        includeOriginal = includeOriginal,
                                        includeThemes = includeThemes
                                    )
                                }
                                QuoteCardRenderer.shareQuoteCard(context, bitmap, passage)
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FathomTheme.colors.accentGold,
                        contentColor = Color(0xFF09090B)
                    )
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.ShareNetwork,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF09090B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SHARE CARD",
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteCardPreview(
    passage: Passage,
    format: CardFormat,
    theme: CardTheme,
    includeOriginal: Boolean,
    includeThemes: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(theme.cardBgHex))
            .border(1.dp, Color(theme.borderHex), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(theme.badgeBgHex))
                            .border(1.dp, Color(theme.accentGoldHex), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = passage.traditionId.displayName.uppercase(),
                            fontFamily = JakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.8.sp,
                            color = Color(theme.accentGoldHex)
                        )
                    }

                    Text(
                        text = passage.canonicalReference.uppercase(),
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.8.sp,
                        color = Color(theme.textSecondaryHex),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(theme.borderHex))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                if (includeOriginal && !passage.originalText.isNullOrBlank()) {
                    Text(
                        text = passage.originalText ?: "",
                        fontFamily = JakartaFontFamily,
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        color = Color(theme.textTertiaryHex),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = passage.translation,
                    fontFamily = JakartaFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (format == CardFormat.STORY) 11.5.sp else 10.5.sp,
                    lineHeight = 15.sp,
                    color = Color(theme.textPrimaryHex),
                    maxLines = if (format == CardFormat.STORY) 7 else 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                if (includeThemes && passage.themes.isNotEmpty()) {
                    Text(
                        text = passage.themes.joinToString(" • ") { "#" + it.name.uppercase() },
                        fontFamily = JakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 7.5.sp,
                        color = Color(theme.accentGoldHex),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(theme.borderHex))
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "fathom",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(theme.textPrimaryHex)
                        )
                        Text(
                            text = ".",
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(theme.accentGoldHex)
                        )
                    }

                    Text(
                        text = "SACRED WISDOM",
                        fontFamily = JakartaFontFamily,
                        fontSize = 7.sp,
                        letterSpacing = 0.8.sp,
                        color = Color(theme.textTertiaryHex)
                    )
                }
            }
        }
    }
}
