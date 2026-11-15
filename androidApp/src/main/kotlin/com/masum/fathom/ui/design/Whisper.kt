package com.masum.fathom.ui.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomMotion
import com.masum.fathom.ui.theme.FathomTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class WhisperMessage(val id: Long, val text: String, val problem: Boolean)

object Whisper {
    private val stream = MutableSharedFlow<WhisperMessage>(extraBufferCapacity = 8)
    private var counter = 0L
    internal var sheetHosts by mutableIntStateOf(0)

    val messages = stream.asSharedFlow()

    fun show(text: String) {
        stream.tryEmit(WhisperMessage(counter++, text, false))
    }

    fun problem(text: String) {
        stream.tryEmit(WhisperMessage(counter++, text, true))
    }
}

private val ProblemHue = Color(0xFFE08A72)

@Composable
fun WhisperHost(modifier: Modifier = Modifier, insideSheet: Boolean = false) {
    var current by remember { mutableStateOf<WhisperMessage?>(null) }

    if (insideSheet) {
        DisposableEffect(Unit) {
            Whisper.sheetHosts += 1
            onDispose { Whisper.sheetHosts -= 1 }
        }
    }

    LaunchedEffect(insideSheet) {
        Whisper.messages.collect { message ->
            if (insideSheet || Whisper.sheetHosts == 0) current = message
        }
    }
    LaunchedEffect(current?.id) {
        val message = current ?: return@LaunchedEffect
        delay((1800 + message.text.length * 38L).coerceAtMost(5200L))
        if (current?.id == message.id) current = null
    }

    Box(
        modifier = modifier.fillMaxWidth().statusBarsPadding().padding(top = 10.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedContent(
            targetState = current,
            transitionSpec = {
                (fadeIn(tween(FathomMotion.Standard)) + slideInVertically(tween(420, 0, FathomMotion.Emphasized)) { -it })
                    .togetherWith(fadeOut(tween(FathomMotion.Quick)) + slideOutVertically(tween(240, 0, FathomMotion.EmphasizedOut)) { -it / 2 })
            },
            label = "whisper"
        ) { message ->
            if (message != null) WhisperCapsule(message)
        }
    }
}

@Composable
private fun WhisperCapsule(message: WhisperMessage) {
    val colors = FathomTheme.colors
    val hue = if (message.problem) ProblemHue else FathomTheme.tone.hue
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .widthIn(max = 420.dp)
            .shadow(18.dp, shape, ambientColor = Color.Black.copy(alpha = 0.4f), spotColor = Color.Black.copy(alpha = 0.4f))
            .clip(shape)
            .background(colors.surfaceElevated)
            .border(0.75.dp, hue.copy(alpha = 0.35f), shape)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(hue))
        Text(
            text = message.text,
            style = FathomTheme.typography.label,
            color = colors.textPrimary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
