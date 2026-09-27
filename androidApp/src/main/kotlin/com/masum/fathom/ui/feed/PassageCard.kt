package com.masum.fathom.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.components.SacredDivider
import com.masum.fathom.ui.components.TraditionBadge
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun PassageCard(
    passage: Passage,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FathomTheme.colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 136.dp),
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

            if (passage.themes.isNotEmpty()) {
                Text(
                    text = passage.themes.joinToString(" • ") { it.name.uppercase() },
                    style = FathomTheme.typography.caption,
                    color = FathomTheme.colors.accentGold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
