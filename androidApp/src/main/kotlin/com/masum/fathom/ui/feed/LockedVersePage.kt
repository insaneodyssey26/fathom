package com.masum.fathom.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.LockSimple
import com.masum.fathom.core.subscription.FreeAccess
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.theme.FathomTheme

@Composable
fun LockedVersePage(
    chapter: Int,
    contentPadding: PaddingValues,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    Column(
        modifier = modifier.fillMaxSize().padding(contentPadding).padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(PhosphorIcons.Light.LockSimple, null, tint = tone.hue, modifier = Modifier.size(30.dp))
        Spacer(Modifier.height(20.dp))
        MonoLabel(text = "Chapter $chapter", color = tone.hue)
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Continue with Fathom Plus",
            style = FathomTheme.typography.headline,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "The first ${FreeAccess.ChapterLimit} chapters of every scripture are free to read in order.",
            style = FathomTheme.typography.commentaryBody,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(26.dp))
        FathomButton(
            text = "Unlock with Plus",
            onClick = onUnlock,
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
        )
    }
}
