package com.masum.fathom.ui.design

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.masum.fathom.ui.theme.FathomTheme

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun BoxScope.DockedTopPanel(
    onHeight: (Int) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = FathomTheme.colors
    val shell = LocalShell.current
    Column(
        modifier = modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .pointerInput(Unit) { detectTapGestures { } }
            .glass(
                shape = RectangleShape,
                tint = colors.background.copy(alpha = if (colors.isDark) 0.82f else 0.88f),
                blur = 24.dp,
                outline = false
            )
            .onSizeChanged { onHeight(it.height) }
            .padding(start = shell.startInset)
            .statusBarsPadding()
            .padding(bottom = bottomPadding),
        content = content
    )
}
