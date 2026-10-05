package com.masum.fathom.ui.design

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.ui.theme.FathomTheme
import com.masum.fathom.ui.theme.Tone

private const val AtmosphereAgsl = """
uniform float2 size;
uniform float phase;
uniform float strength;
uniform float grain;
layout(color) uniform half4 base;
layout(color) uniform half4 glowA;
layout(color) uniform half4 glowB;

float hash(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

half4 main(float2 fc) {
    float2 uv = fc / size;
    float aspect = size.x / size.y;
    float2 p = float2(uv.x * aspect, uv.y);

    float2 a = float2(aspect * (0.18 + 0.12 * sin(phase * 0.9)), 0.12 + 0.10 * cos(phase * 0.7));
    float2 b = float2(aspect * (0.92 + 0.08 * cos(phase * 0.6)), 0.88 + 0.08 * sin(phase * 0.8));

    float da = exp(-dot(p - a, p - a) * 2.6);
    float db = exp(-dot(p - b, p - b) * 3.4);

    half3 col = base.rgb;
    col = mix(col, glowA.rgb, half(da * strength));
    col = mix(col, glowB.rgb, half(db * strength * 0.9));

    float v = smoothstep(1.15, 0.25, length(uv - 0.5));
    col = mix(col * half(0.82), col, half(v));

    float n = hash(fc) - 0.5;
    col += half3(half(n * grain));
    return half4(col, 1.0);
}
"""

@Composable
fun Atmosphere(
    tone: Tone,
    modifier: Modifier = Modifier,
    phase: () -> Float = { 0f },
    strength: Float = 1f,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = FathomTheme.colors
    val glowA = tone.glowPrimary
    val glowB = tone.glowSecondary
    val base = colors.background
    val grain = if (colors.isDark) 0.045f else 0.035f

    val canvas: Modifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val shader = remember { RuntimeShader(AtmosphereAgsl) }
        val brush = remember(shader) { ShaderBrush(shader) }
        Modifier.drawBehind {
            shader.setFloatUniform("size", size.width, size.height)
            shader.setFloatUniform("phase", phase())
            shader.setFloatUniform("strength", strength)
            shader.setFloatUniform("grain", grain)
            shader.setColorUniform("base", base.toArgb())
            shader.setColorUniform("glowA", glowA.toArgb())
            shader.setColorUniform("glowB", glowB.toArgb())
            drawRect(brush)
        }
    } else {
        Modifier.drawBehind {
            drawRect(base)
            drawRect(
                Brush.radialGradient(
                    colors = listOf(glowA.copy(alpha = strength.coerceIn(0f, 1f)), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.1f),
                    radius = size.maxDimension * 0.7f
                )
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(glowB.copy(alpha = strength.coerceIn(0f, 1f) * 0.8f), Color.Transparent),
                    center = Offset(size.width * 0.95f, size.height * 0.92f),
                    radius = size.maxDimension * 0.6f
                )
            )
        }
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .then(canvas)
        )
        content()
    }
}
