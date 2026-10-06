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
import com.masum.fathom.R
import com.masum.fathom.core.model.TraditionId
import com.masum.fathom.ui.design.EmblemShapes
import com.masum.fathom.ui.theme.FathomColors
import com.masum.fathom.ui.theme.FathomTypefaces
import com.masum.fathom.ui.theme.Tone
import java.util.Random

object WidgetArt {

    fun background(width: Int, height: Int, colors: FathomColors, tone: Tone, radius: Float, seed: Long): Bitmap {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val clip = Path().apply { addRoundRect(RectF(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat()), radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        canvas.drawColor(colors.background.toArgb())

        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        val reach = maxOf(safeWidth, safeHeight).toFloat()
        glow.shader = RadialGradient(safeWidth * 0.08f, safeHeight * 0.05f, reach * 0.9f, tone.glowPrimary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat(), glow)
        glow.shader = RadialGradient(safeWidth * 0.98f, safeHeight * 0.98f, reach * 0.7f, tone.glowSecondary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, safeWidth.toFloat(), safeHeight.toFloat(), glow)

        val grain = Paint().apply { color = if (colors.isDark) 0x0DFFFFFF else 0x0D000000 }
        val random = Random(seed)
        val speck = maxOf(1f, safeWidth / 360f)
        repeat(safeWidth * safeHeight / 320) {
            val x = random.nextInt(safeWidth).toFloat()
            val y = random.nextInt(safeHeight).toFloat()
            canvas.drawRect(x, y, x + speck, y + speck, grain)
        }

        val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colors.hairline.toArgb()
            style = Paint.Style.STROKE
            strokeWidth = maxOf(1f, safeWidth / 360f)
        }
        canvas.drawPath(clip, edge)
        return bitmap
    }

    fun emblem(context: Context, traditionId: TraditionId?, color: Int, sizePx: Int): Bitmap {
        val safeSize = sizePx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val stroke = safeSize / 14f
        val inset = stroke
        val path = EmblemShapes.androidPath(traditionId, safeSize - inset * 2)
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

    fun wordmark(context: Context, color: Int, heightPx: Int): Bitmap {
        val safeHeight = heightPx.coerceAtLeast(1)
        val typeface = FathomTypefaces.load(context, R.font.fraunces_italic, "'wght' 400, 'opsz' 48, 'SOFT' 60")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.typeface = typeface
            textSize = safeHeight * 0.82f
        }
        val text = "fathom"
        val textWidth = paint.measureText(text)
        val safeWidth = (textWidth + safeHeight * 0.2f).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val metrics = paint.fontMetrics
        val y = (safeHeight - (metrics.descent + metrics.ascent)) / 2f
        canvas.drawText(text, 0f, y, paint)
        return bitmap
    }
}
