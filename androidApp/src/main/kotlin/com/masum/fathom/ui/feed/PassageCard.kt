package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.BookmarkSimple as BookmarkSimpleFill
import com.adamglin.phosphoricons.regular.BookmarkSimple
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.CaretUp
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.ShareNetwork
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun PassageCard(
    passage: Passage,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onOpenContext: () -> Unit,
    onShare: () -> Unit,
    onScrollPrevious: (() -> Unit)? = null,
    onScrollNext: (() -> Unit)? = null,
    hasPrevious: Boolean = true,
    hasNext: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 76.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TraditionBadge(traditionId = passage.traditionId)

                    Text(
                        text = passage.canonicalReference.uppercase(),
                        style = FathomTheme.typography.canonicalHeader,
                        color = FathomTheme.colors.textSecondary
                    )
                }

                SacredDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!passage.originalText.isNullOrBlank()) {
                        Text(
                            text = passage.originalText ?: "",
                            style = FathomTheme.typography.verseOriginal,
                            color = FathomTheme.colors.textTertiary
                        )
                        SacredDivider(modifier = Modifier.padding(vertical = 14.dp))
                    }

                    Text(
                        text = passage.translation,
                        style = FathomTheme.typography.verseDisplay,
                        color = FathomTheme.colors.textPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp)
            ) {
                SacredDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (passage.themes.isNotEmpty()) {
                        Text(
                            text = passage.themes.joinToString(" • ") { it.name.uppercase() },
                            style = FathomTheme.typography.caption,
                            color = FathomTheme.colors.accentGold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onScrollPrevious?.invoke() },
                            enabled = hasPrevious,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretUp,
                                contentDescription = "Previous Verse",
                                tint = if (hasPrevious) FathomTheme.colors.textSecondary else FathomTheme.colors.textTertiary.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onScrollNext?.invoke() },
                            enabled = hasNext,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.CaretDown,
                                contentDescription = "Next Verse",
                                tint = if (hasNext) FathomTheme.colors.textSecondary else FathomTheme.colors.textTertiary.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.ShareNetwork,
                                contentDescription = "Share Quote Card",
                                tint = FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenContext,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Info,
                                contentDescription = "Context & Commentary",
                                tint = FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) PhosphorIcons.Fill.BookmarkSimpleFill else PhosphorIcons.Regular.BookmarkSimple,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
