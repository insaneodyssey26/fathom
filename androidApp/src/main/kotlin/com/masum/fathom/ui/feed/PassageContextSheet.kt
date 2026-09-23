package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.theme.FathomTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassageContextSheet(
    passage: Passage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FathomTheme.colors.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TraditionBadge(traditionId = passage.traditionId)

                Text(
                    text = passage.canonicalReference,
                    style = FathomTheme.typography.canonicalHeader,
                    color = FathomTheme.colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!passage.originalText.isNullOrBlank()) {
                Text(
                    text = "ORIGINAL TEXT",
                    style = FathomTheme.typography.caption,
                    color = FathomTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = passage.originalText ?: "",
                    style = FathomTheme.typography.verseOriginal,
                    color = FathomTheme.colors.textSecondary
                )
                SacredDivider(modifier = Modifier.padding(vertical = 12.dp))
            }

            if (passage.explanations.isNotEmpty()) {
                Text(
                    text = "COMMENTARY & CONTEXT",
                    style = FathomTheme.typography.caption,
                    color = FathomTheme.colors.accentGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                passage.explanations.forEach { explanation ->
                    if (!explanation.title.isNullOrBlank()) {
                        Text(
                            text = explanation.title ?: "",
                            style = FathomTheme.typography.commentaryTitle,
                            color = FathomTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Text(
                        text = explanation.body,
                        style = FathomTheme.typography.commentaryBody,
                        color = FathomTheme.colors.textSecondary
                    )

                    if (!explanation.author.isNullOrBlank() || !explanation.source.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val attribution = listOfNotNull(explanation.author, explanation.source).joinToString(" • ")
                        Text(
                            text = "— $attribution",
                            style = FathomTheme.typography.caption,
                            color = FathomTheme.colors.textTertiary
                        )
                    }

                    SacredDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
            } else {
                Text(
                    text = "No secondary commentary attached to this passage.",
                    style = FathomTheme.typography.commentaryBody,
                    color = FathomTheme.colors.textTertiary
                )
            }
        }
    }
}
