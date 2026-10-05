package com.masum.fathom.ui.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.FathomSheet
import com.masum.fathom.ui.design.rememberSheetCloser
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.SheetHeader
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.shell.traditionIdForLabel
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.ScriptureFamily
import com.masum.fathom.ui.theme.toneFor

@Composable
fun NoteSheet(
    passage: Passage,
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    var text by remember { mutableStateOf(initial) }
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val rule by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(260), label = "note_rule")

    FathomSheet(onDismiss = onDismiss) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(
            overline = if (initial.isBlank()) "New reflection" else "Edit reflection",
            title = passage.canonicalReference,
            onClose = onDismiss
        )
        Text(
            text = passage.translation,
            style = typography.commentaryBody.copy(fontStyle = FontStyle.Italic, fontFamily = ScriptureFamily, fontSize = 17.sp, lineHeight = 25.sp),
            color = colors.textTertiary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().heightIn(min = 148.dp)) {
            Box(Modifier.padding(end = 16.dp).size(width = 2.dp, height = 148.dp).background(rule))
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text(
                        text = "What does this stir in you?",
                        style = typography.verseDisplay.copy(fontSize = 21.sp, lineHeight = 30.sp),
                        color = colors.textTertiary.copy(alpha = 0.7f)
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    interactionSource = source,
                    textStyle = typography.verseDisplay.copy(fontSize = 21.sp, lineHeight = 30.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(tone.hue),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        FathomButton(
            text = "Save reflection",
            onClick = { closeSheet { onSave(text.trim()) } },
            enabled = text.isNotBlank(),
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSheet(
    currentName: String,
    currentTradition: String?,
    onSave: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val typography = FathomTheme.typography
    var name by remember { mutableStateOf(currentName) }
    var tradition by remember { mutableStateOf(currentTradition) }
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val rule by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(260), label = "profile_rule")
    val options = remember { listOf<TraditionId?>(null) + TraditionId.entries }

    FathomSheet(onDismiss = onDismiss) {
        val closeSheet = rememberSheetCloser(onDismiss)
        SheetHeader(overline = "Profile", title = "Who is reading?", onClose = onDismiss)
        MonoLabel(text = "Your name")
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = name,
            onValueChange = { if (it.length <= 24) name = it },
            singleLine = true,
            interactionSource = source,
            textStyle = typography.headline.copy(fontSize = 28.sp, lineHeight = 34.sp, color = colors.textPrimary),
            cursorBrush = SolidColor(tone.hue),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(rule))

        Spacer(Modifier.height(28.dp))
        MonoLabel(text = "Spiritual path")
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { id ->
                val selected = traditionIdForLabel(tradition) == id
                val optionTone = remember(id, colors.isDark, colors.background) { toneFor(id, colors) }
                val container by animateColorAsState(if (selected) optionTone.hue else Color.Transparent, tween(240), label = "opt_bg")
                val content by animateColorAsState(if (selected) optionTone.onHue else colors.textSecondary, tween(240), label = "opt_fg")
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(container)
                        .border(0.75.dp, if (selected) Color.Transparent else colors.hairline, RoundedCornerShape(50))
                        .tappable(scale = 0.94f) { tradition = id?.displayName }
                        .padding(start = 12.dp, end = 16.dp)
                        .height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TraditionEmblem(traditionId = id, color = if (selected) optionTone.onHue else optionTone.hue, size = 16.dp, filled = selected)
                    Text(text = id?.displayName ?: "Universal", style = typography.label, color = content)
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        FathomButton(
            text = "Save",
            onClick = { closeSheet { onSave(name.trim().ifBlank { "Seeker" }, tradition) } },
            style = ButtonStyle.Tone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
