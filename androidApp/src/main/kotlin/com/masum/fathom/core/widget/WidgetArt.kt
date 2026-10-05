package com.masum.fathom.core.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.EmblemShapes
import com.masum.fathom.ui.theme.FathomColors
import com.masum.fathom.ui.theme.Tone
import java.util.Random

object WidgetArt {

    fun background(width: Int, height: Int, colors: FathomColors, tone: Tone, radius: Float, seed: Long): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        canvas.drawColor(colors.background.toArgb())

        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        val reach = maxOf(width, height).toFloat()
        glow.shader = RadialGradient(width * 0.08f, height * 0.05f, reach * 0.9f, tone.glowPrimary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glow)
        glow.shader = RadialGradient(width * 0.98f, height * 0.98f, reach * 0.7f, tone.glowSecondary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glow)

        val grain = Paint().apply { color = if (colors.isDark) 0x0DFFFFFF else 0x0D000000 }
        val random = Random(seed)
        val speck = maxOf(1f, width / 360f)
        repeat(width * height / 320) {
            val x = random.nextInt(width).toFloat()
            val y = random.nextInt(height).toFloat()
            canvas.drawRect(x, y, x + speck, y + speck, grain)
        }

        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.hairline.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = maxOf(1f, width / 360f)
        }
        canvas.drawPath(clip, edge)
        return bitmap
    }

    fun emblem(context: Context, traditionId: TraditionId?, color: Int, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val stroke = sizePx / 14f
        val inset = stroke
        val path = EmblemShapes.androidPath(traditionId, sizePx - inset * 2)
        canvas.translate(inset, inset)
        canvas.drawPath(
            path,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = stroke
            }
        )
        return bitmap
    }
}
