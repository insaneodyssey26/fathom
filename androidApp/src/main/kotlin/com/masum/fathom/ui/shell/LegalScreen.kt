package com.masum.fathom.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowLeft
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.DockedTopPanel
import com.masum.fathom.ui.design.GlassIconButton
import com.masum.fathom.ui.design.GlassLayer
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.LocalShell
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun LegalScreen(doc: LegalDoc, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val tone = rememberTone(null)
    val colors = FathomTheme.colors
    val typography = FathomTheme.typography
    val shell = LocalShell.current
    val haptics = LocalFathomHaptics.current
    val density = LocalDensity.current
    var panelHeight by remember { mutableIntStateOf(0) }
    val sections = remember(doc) { LegalDocuments.sections(doc) }

    CompositionLocalProvider(LocalTone provides tone) {
        GlassLayer(
            modifier = modifier.fillMaxSize(),
            background = {
                Atmosphere(tone = tone, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().padding(start = shell.startInset), contentAlignment = Alignment.TopCenter) {
                        LazyColumn(
                            modifier = Modifier.fillMaxHeight().widthIn(max = 680.dp),
                            contentPadding = PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = with(density) { panelHeight.toDp() } + 24.dp,
                                bottom = shell.bottomInset + 48.dp
                            )
                        ) {
                            item(key = "intro") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                                    MonoLabel(text = "Last updated ${doc.updated}", color = tone.hue)
                                    Text(text = doc.title, style = typography.display, color = colors.textPrimary)
                                }
                            }
                            itemsIndexed(sections, key = { index, _ -> "section_$index" }) { index, section ->
                                Column(Modifier.fillMaxWidth().padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Hairline()
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 10.dp)) {
                                        MonoLabel(text = (index + 1).toString().padStart(2, '0'), color = tone.hue, modifier = Modifier.padding(top = 8.dp))
                                        Text(text = section.heading, style = typography.headline.copy(fontSize = 24.sp, lineHeight = 29.sp), color = colors.textPrimary)
                                    }
                                    section.paragraphs.forEach {
                                        Text(text = it, style = typography.commentaryBody, color = colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            overlay = {
                DockedTopPanel(onHeight = { panelHeight = it }, bottomPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        GlassIconButton(
                            icon = PhosphorIcons.Light.ArrowLeft,
                            contentDescription = "Back",
                            onClick = {
                                haptics.light()
                                onBack()
                            }
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            MonoLabel(text = doc.overline, color = tone.hue)
                            Text(text = doc.title, style = typography.title, color = colors.textPrimary)
                        }
                    }
                }
            }
        )
    }
}
