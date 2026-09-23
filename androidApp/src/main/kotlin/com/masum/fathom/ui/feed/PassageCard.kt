package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.BookmarkBorder
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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

                Spacer(modifier = Modifier.height(32.dp))

                if (!passage.originalText.isNullOrBlank()) {
                    Text(
                        text = passage.originalText ?: "",
                        style = FathomTheme.typography.verseOriginal,
                        color = FathomTheme.colors.textTertiary
                    )
                    SacredDivider(modifier = Modifier.padding(vertical = 12.dp))
                }

                Text(
                    text = passage.translation,
                    style = FathomTheme.typography.verseDisplay,
                    color = FathomTheme.colors.textPrimary
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                SacredDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (passage.themes.isNotEmpty()) {
                        Text(
                            text = passage.themes.joinToString(" • ") { it.name.uppercase() },
                            style = FathomTheme.typography.caption,
                            color = FathomTheme.colors.accentGold
                        )
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenContext,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Context & Commentary",
                                tint = FathomTheme.colors.textSecondary
                            )
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(FathomTheme.colors.surfaceElevated)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) FathomTheme.colors.accentGold else FathomTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
