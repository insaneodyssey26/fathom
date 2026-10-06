package com.masum.fathom.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Light
import com.adamglin.phosphoricons.light.ArrowRight
import com.adamglin.phosphoricons.light.LockSimple
import com.masum.fathom.core.haptics.LocalFathomHaptics
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.Atmosphere
import com.masum.fathom.ui.design.ButtonStyle
import com.masum.fathom.ui.design.FathomButton
import com.masum.fathom.ui.design.Hairline
import com.masum.fathom.ui.design.fadeEdges
import com.masum.fathom.ui.design.MonoLabel
import com.masum.fathom.ui.design.TraditionEmblem
import com.masum.fathom.ui.design.tappable
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.LocalTone
import com.masum.fathom.ui.theme.rememberTone
import androidx.compose.runtime.CompositionLocalProvider

private data class Lineage(val id: String, val title: String, val canon: String, val tradition: TraditionId?)

private val Lineages = listOf(
    Lineage("All Traditions", "All traditions", "Wander freely", null),
    Lineage("Sanatana Dharma", "Sanatana Dharma", "Bhagavad Gita · Upanishads", TraditionId.HINDUISM),
    Lineage("Islam", "Islam", "The Holy Quran", TraditionId.ISLAM),
    Lineage("Christianity", "Christianity", "The Holy Bible", TraditionId.CHRISTIANITY),
    Lineage("Buddhism", "Buddhism", "Dhammapada · Zen", TraditionId.BUDDHISM),
    Lineage("Taoism", "Taoism", "Tao Te Ching", TraditionId.TAOISM)
)

private val Headlines = listOf(
    "Welcome.\nWhat shall we\ncall you?",
    "Which path\nspeaks to\nyou?",
    "How should\ntoday's verse\nfind you?"
)

@Composable
fun OnboardingScreen(
    onComplete: (name: String, tradition: String?, personalizeFeed: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalFathomHaptics.current
    val colors = FathomTheme.colors
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf("All Traditions") }
    var personalize by remember { mutableStateOf(false) }

    val chosen = Lineages.first { it.id == selected }
    val tone = rememberTone(if (step >= 1) chosen.tradition else null)
    val lastStep = Headlines.lastIndex

    BackHandler(enabled = step > 0) {
        step -= 1
    }

    fun advance() {
        if (step < lastStep) {
            if (step == 0 && name.isBlank()) name = ""
            step += 1
        } else {
            val finalName = name.trim().ifBlank { "Seeker" }
            val finalTradition = if (selected == "All Traditions") null else selected
            onComplete(finalName, finalTradition, personalize && finalTradition != null)
        }
    }

    CompositionLocalProvider(LocalTone provides tone) {
        Atmosphere(tone = tone, modifier = modifier.fillMaxSize(), strength = 1.15f) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                val split = maxWidth >= 840.dp
                val short = maxHeight < 640.dp

                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .tappable(enabled = step > 0, scale = 0.94f, onClick = { step -= 1 }),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            MonoLabel(
                                text = if (step > 0) "← Back" else "fathom",
                                color = if (step > 0) colors.textSecondary else colors.textTertiary
                            )
                        }
                        MonoLabel(text = "0${step + 1} / 0${Headlines.size}", color = colors.textTertiary)
                    }

                    Progress(step = step, count = Headlines.size, modifier = Modifier.padding(horizontal = 28.dp))

                    if (split) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 56.dp)) {
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight().padding(end = 48.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Headline(step)
                            }
                            Column(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                StepBody(
                                    step = step,
                                    name = name,
                                    onName = { name = it },
                                    selected = selected,
                                    onSelect = {
                                        haptics.light()
                                        selected = it
                                        personalize = false
                                    },
                                    personalize = personalize,
                                    onPersonalize = { haptics.light(); personalize = it },
                                    chosen = chosen,
                                    onDone = ::advance,
                                    modifier = Modifier.widthIn(max = 520.dp)
                                )
                            }
                        }
                    } else {
                        val bodyScroll = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fadeEdges(bottom = if (bodyScroll.canScrollForward) 28.dp else 0.dp)
                                .verticalScroll(bodyScroll)
                                .padding(horizontal = 28.dp)
                        ) {
                            Spacer(Modifier.height(if (short) 8.dp else 28.dp))
                            Headline(step)
                            Spacer(Modifier.height(if (short) 16.dp else 32.dp))
                            StepBody(
                                step = step,
                                name = name,
                                onName = { name = it },
                                selected = selected,
                                onSelect = {
                                    haptics.light()
                                    selected = it
                                    personalize = false
                                },
                                personalize = personalize,
                                onPersonalize = { haptics.light(); personalize = it },
                                chosen = chosen,
                                onDone = ::advance
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 20.dp, top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FathomButton(
                            text = if (step < lastStep) "Continue" else "Begin reading",
                            onClick = ::advance,
                            trailing = PhosphorIcons.Light.ArrowRight,
                            style = if (step >= 1 && chosen.tradition != null) ButtonStyle.Tone else ButtonStyle.Primary,
                            modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Progress(step: Int, count: Int, modifier: Modifier = Modifier) {
    val tone = FathomTheme.tone
    val colors = FathomTheme.colors
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { index ->
            val fill by animateFloatAsState(if (index <= step) 1f else 0f, tween(520, easing = FathomMotion.Emphasized), label = "seg")
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(colors.hairline)
            ) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(fill).background(tone.hue))
            }
        }
    }
}

@Composable
private fun Headline(step: Int) {
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            val dir = if (targetState > initialState) 1 else -1
            (fadeIn(tween(420, 120)) + slideInHorizontally(tween(520, 0, FathomMotion.Emphasized)) { dir * it / 6 })
                .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(320, 0, FathomMotion.Emphasized)) { -dir * it / 8 })
        },
        label = "headline"
    ) { target ->
        Text(
            text = Headlines[target],
            style = FathomTheme.typography.display,
            color = FathomTheme.colors.textPrimary
        )
    }
}

@Composable
private fun StepBody(
    step: Int,
    name: String,
    onName: (String) -> Unit,
    selected: String,
    onSelect: (String) -> Unit,
    personalize: Boolean,
    onPersonalize: (Boolean) -> Unit,
    chosen: Lineage,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = step,
        transitionSpec = {
            val dir = if (targetState > initialState) 1 else -1
            (fadeIn(tween(380, 140)) + slideInHorizontally(tween(520, 0, FathomMotion.Emphasized)) { dir * it / 5 })
                .togetherWith(fadeOut(tween(160)) + slideOutHorizontally(tween(300, 0, FathomMotion.Emphasized)) { -dir * it / 8 })
        },
        label = "body",
        modifier = modifier
    ) { target ->
        when (target) {
            0 -> NameField(name = name, onName = onName, onDone = onDone)
            1 -> Column {
                Lineages.forEachIndexed { index, lineage ->
                    LineageRow(lineage = lineage, selected = selected == lineage.id, onClick = { onSelect(lineage.id) })
                    if (index < Lineages.lastIndex) Hairline()
                }
            }
            else -> FeedChoice(
                chosen = chosen,
                personalize = personalize,
                onPersonalize = onPersonalize
            )
        }
    }
}

@Composable
private fun NameField(name: String, onName: (String) -> Unit, onDone: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = FathomTheme.tone
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val line by animateColorAsState(if (focused) tone.hue else colors.hairline, tween(300), label = "line")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box {
            if (name.isEmpty()) {
                Text(
                    text = "Your name",
                    style = FathomTheme.typography.headline.copy(fontSize = 34.sp),
                    color = colors.textTertiary.copy(alpha = 0.7f)
                )
            }
            BasicTextField(
                value = name,
                onValueChange = { if (it.length <= 24) onName(it) },
                singleLine = true,
                textStyle = FathomTheme.typography.headline.copy(fontSize = 34.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(tone.hue),
                interactionSource = source,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(line))
        Text(
            text = "Only used to greet you. Nothing leaves your device.",
            style = FathomTheme.typography.body,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun LineageRow(lineage: Lineage, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val rowTone = rememberTone(lineage.tradition)
    val title by animateColorAsState(if (selected) colors.textPrimary else colors.textSecondary, tween(260), label = "t")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tappable(scale = 0.985f, haptic = false, onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TraditionEmblem(
            traditionId = lineage.tradition,
            color = if (selected) rowTone.hue else colors.textTertiary,
            size = 34.dp,
            filled = selected
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = lineage.title, style = FathomTheme.typography.headline.copy(fontSize = 25.sp), color = title)
            MonoLabel(text = lineage.canon, color = colors.textTertiary)
        }
    }
}

@Composable
private fun FeedChoice(chosen: Lineage, personalize: Boolean, onPersonalize: (Boolean) -> Unit) {
    val colors = FathomTheme.colors
    val specific = chosen.tradition != null
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceRow(
            tradition = null,
            title = "Every scripture",
            body = "A balanced mix drawn from all seven traditions.",
            selected = !personalize || !specific,
            onClick = { onPersonalize(false) }
        )
        if (specific) {
            Hairline()
            ChoiceRow(
                tradition = chosen.tradition,
                title = "Mainly ${chosen.title}",
                body = "Most verses from your tradition, with the occasional voice from elsewhere.",
                selected = personalize,
                onClick = { onPersonalize(true) }
            )
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(PhosphorIcons.Light.LockSimple, null, tint = colors.textTertiary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Text(
                text = "Everything stays on this phone. There is no server, so your name and preferences are never sent anywhere.",
                style = FathomTheme.typography.body,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun ChoiceRow(tradition: TraditionId?, title: String, body: String, selected: Boolean, onClick: () -> Unit) {
    val colors = FathomTheme.colors
    val tone = rememberTone(tradition)
    Row(
        modifier = Modifier.fillMaxWidth().tappable(scale = 0.985f, haptic = true, onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TraditionEmblem(traditionId = tradition, color = if (selected) tone.hue else colors.textTertiary, size = 30.dp, filled = selected)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = FathomTheme.typography.title, color = if (selected) colors.textPrimary else colors.textSecondary)
            Text(text = body, style = FathomTheme.typography.body, color = colors.textTertiary)
        }
    }
}
