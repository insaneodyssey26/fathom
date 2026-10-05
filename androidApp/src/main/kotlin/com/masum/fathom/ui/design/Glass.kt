package com.masum.fathom.ui.design

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.masum.fathom.ui.theme.FathomTheme

val LocalBackdrop = compositionLocalOf<LayerBackdrop?> { null }

@Composable
fun Modifier.glass(
    shape: Shape,
    tint: Color = FathomTheme.colors.background.copy(alpha = 0.38f),
    blur: Dp = 18.dp,
    outline: Boolean = true
): Modifier {
    val backdrop = LocalBackdrop.current
    val colors = FathomTheme.colors
    val edge = if (colors.isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
    return if (backdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blur.toPx())
            },
            onDrawSurface = { drawRect(tint) }
        ).then(if (outline) Modifier.border(0.75.dp, edge, shape) else Modifier)
    } else {
        this.clip(shape)
            .background(colors.surfaceElevated.copy(alpha = 0.94f))
            .then(if (outline) Modifier.border(0.75.dp, edge, shape) else Modifier)
    }
}

@Composable
fun GlassLayer(
    modifier: Modifier = Modifier,
    backdropColor: Color = FathomTheme.colors.background,
    background: @Composable BoxScope.() -> Unit,
    overlay: @Composable BoxScope.() -> Unit
) {
    val backdrop = rememberLayerBackdrop {
        drawRect(backdropColor)
        drawContent()
    }
    Box(modifier = modifier) {
        CompositionLocalProvider(LocalBackdrop provides null) {
            Box(modifier = Modifier.fillMaxSize().layerBackdrop(backdrop), content = background)
        }
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            overlay()
        }
    }
}
