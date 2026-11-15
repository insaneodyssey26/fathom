package com.masum.fathom.core.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.ui.graphics.toArgb
import com.masum.fathom.R
import com.masum.fathom.ui.design.EmblemShapes
import com.masum.fathom.ui.theme.FathomColors
import com.masum.fathom.ui.theme.FathomTypefaces
import com.masum.fathom.ui.theme.Tone
import java.util.Calendar
import java.util.Locale

data class WidgetSize(val widthDp: Int, val heightDp: Int)

object WidgetSizing {
    private const val MaxPixels = 1_200_000f

    fun current(context: Context, options: Bundle, defaultWidth: Int, defaultHeight: Int): WidgetSize {
        val landscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaultWidth)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, minWidth)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, defaultHeight)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, minHeight)
        val width = if (landscape) maxWidth else minWidth
        val height = if (landscape) minHeight else maxHeight
        return WidgetSize(width.coerceAtLeast(40), height.coerceAtLeast(40))
    }

    fun unit(size: WidgetSize, density: Float): Float {
        val scale = minOf(1f, kotlin.math.sqrt(MaxPixels / (size.widthDp * density * size.heightDp * density)))
        return density * scale
    }
}

private class Frame(
    val context: Context,
    val size: WidgetSize,
    val u: Float,
    val colors: FathomColors,
    val tone: Tone
) {
    val width = (size.widthDp * u).toInt().coerceAtLeast(1)
    val height = (size.heightDp * u).toInt().coerceAtLeast(1)
    val w = size.widthDp.toFloat()
    val h = size.heightDp.toFloat()
    val hue = tone.hue.toArgb()
    val ink = colors.textPrimary.toArgb()
    val muted = colors.textSecondary.toArgb()
    val faint = colors.textTertiary.toArgb()
    val hairline = colors.hairline.toArgb()
    val scripture = FathomTypefaces.load(context, R.font.newsreader, "'wght' 400, 'opsz' 36")
    val scriptureItalic = FathomTypefaces.load(context, R.font.newsreader_italic, "'wght' 400, 'opsz' 24")
    val display = FathomTypefaces.load(context, R.font.fraunces, "'wght' 380, 'opsz' 144, 'SOFT' 40")
    val mono = FathomTypefaces.load(context, R.font.geist, "'wght' 500")
    val semibold = FathomTypefaces.load(context, R.font.geist, "'wght' 600")

    fun px(dp: Float) = dp * u

    fun text(typeface: android.graphics.Typeface, sizeDp: Float, color: Int, spacing: Float = 0f) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = px(sizeDp)
        this.color = color
        letterSpacing = spacing
    }

    fun layout(text: CharSequence, paint: TextPaint, widthDp: Float, lineSpacing: Float, maxLines: Int = Int.MAX_VALUE, ellipsize: Boolean = false): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, px(widthDp).toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, lineSpacing)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .apply { if (ellipsize) setEllipsize(TextUtils.TruncateAt.END) }
            .build()

    fun fit(text: CharSequence, paint: TextPaint, widthDp: Float, heightDp: Float, maxDp: Float, minDp: Float, lineSpacing: Float): StaticLayout {
        var current = maxDp
        while (true) {
            paint.textSize = px(current)
            val full = layout(text, paint, widthDp, lineSpacing)
            if (full.height <= px(heightDp)) return full
            if (current <= minDp) {
                val lineHeight = (full.getLineBottom(0) - full.getLineTop(0)).coerceAtLeast(1)
                val lines = (px(heightDp) / lineHeight).toInt().coerceAtLeast(1)
                return layout(text, paint, widthDp, lineSpacing, lines, true)
            }
            current -= 1f
        }
    }

    fun draw(canvas: Canvas, layout: StaticLayout, xDp: Float, yDp: Float) {
        canvas.save()
        canvas.translate(px(xDp), px(yDp))
        layout.draw(canvas)
        canvas.restore()
    }

    fun heightDp(layout: StaticLayout) = layout.height / u
}

object WidgetCanvas {
    const val ActionsMinWidth = 170
    const val ActionsMinHeight = 100
    private const val ButtonCenterFromBottom = 27f

    fun showsActions(size: WidgetSize) = size.widthDp >= ActionsMinWidth && size.heightDp >= ActionsMinHeight

    private fun base(frame: Frame, seed: Long): Bitmap {
        val bitmap = WidgetArt.background(frame.width, frame.height, frame.colors, frame.tone, 26f * frame.u, seed)
        return bitmap
    }

    fun daily(
        context: Context,
        size: WidgetSize,
        u: Float,
        passage: WidgetPassage,
        colors: FathomColors,
        tone: Tone,
        showInfo: Boolean
    ): Bitmap {
        val f = Frame(context, size, u, colors, tone)
        val bitmap = base(f, passage.id.hashCode().toLong())
        val canvas = Canvas(bitmap)

        val tiny = f.h < 90f
        val compact = f.h < 130f || f.w < 200f
        val actions = showsActions(size)
        val pad = if (compact) 14f else 20f
        val innerW = f.w - pad * 2

        if (!tiny && f.w >= 220f && f.h >= 130f) {
            val mark = f.h * 0.95f
            val path = EmblemShapes.androidPath(passage.traditionId, f.px(mark))
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = (f.hue and 0x00FFFFFF) or 0x1C000000
                style = Paint.Style.STROKE
                strokeWidth = f.px(1.4f)
            }
            canvas.save()
            canvas.translate(f.px(f.w - mark * 0.62f), f.px(-mark * 0.2f))
            canvas.drawPath(path, stroke)
            canvas.restore()
        }

        var top = pad
        if (!tiny) {
            val emblemSize = if (compact) 16f else 20f
            val emblem = WidgetArt.emblem(context, passage.traditionId, f.hue, f.px(emblemSize).toInt())
            canvas.drawBitmap(emblem, f.px(pad), f.px(top), null)
            val label = f.text(f.mono, if (compact) 9.5f else 10.5f, f.hue, 0.14f)
            val labelText = (if (showInfo) "Context" else passage.traditionId?.displayName ?: "Fathom").uppercase(Locale.getDefault())
            val baseline = f.px(top + emblemSize / 2f) - (label.fontMetrics.ascent + label.fontMetrics.descent) / 2f
            canvas.drawText(labelText, f.px(pad + emblemSize + 8f), baseline, label)
            top += emblemSize + if (compact) 10f else 14f
        }

        val footerReserve = if (actions) 46f else if (tiny) 0f else 22f
        val bodyBottom = f.h - pad * 0.4f - footerReserve
        val bodyHeight = (bodyBottom - top).coerceAtLeast(20f)

        if (showInfo) {
            val title = passage.explanationTitle?.takeIf { it.isNotBlank() } ?: "Context"
            val titlePaint = f.text(f.semibold, if (compact) 12.5f else 14f, f.ink)
            val titleLayout = f.layout(title, titlePaint, innerW, 1.1f, 2, true)
            f.draw(canvas, titleLayout, pad, top)
            val used = f.heightDp(titleLayout) + 6f
            val body = passage.explanationBody?.takeIf { it.isNotBlank() } ?: passage.spiritualInsight.orEmpty()
            val bodyPaint = f.text(f.scriptureItalic, 15f, f.muted)
            val bodyLayout = f.fit(body, bodyPaint, innerW, (bodyHeight - used).coerceAtLeast(16f), if (compact) 15f else 17f, 11f, 1.18f)
            f.draw(canvas, bodyLayout, pad, top + used)
        } else {
            val maxSize = when {
                f.h >= 260f -> 32f
                f.h >= 190f -> 27f
                f.h >= 130f -> 22f
                else -> 18f
            }
            val versePaint = f.text(f.scripture, maxSize, f.ink)
            val verse = f.fit(passage.translation, versePaint, innerW, bodyHeight, maxSize, 11f, 1.16f)
            val offset = ((bodyHeight - f.heightDp(verse)) / 2f).coerceAtLeast(0f)
            f.draw(canvas, verse, pad, top + offset * 0.7f)
        }

        if (!tiny) {
            if (actions && f.h >= 130f) {
                val rule = Paint().apply { color = f.hairline; strokeWidth = f.px(0.75f) }
                canvas.drawLine(f.px(pad), f.px(f.h - 48f), f.px(f.w - pad), f.px(f.h - 48f), rule)
            }
            val reference = f.text(f.mono, if (compact) 9.5f else 10.5f, f.muted, 0.12f)
            val maxRef = if (actions) f.w - pad - 118f else f.w - pad * 2
            var refText = passage.reference.uppercase(Locale.getDefault())
            while (reference.measureText(refText) > f.px(maxRef) && refText.length > 6) refText = refText.dropLast(2)
            if (refText != passage.reference.uppercase(Locale.getDefault())) refText = refText.trimEnd() + "…"
            val centerY = if (actions) f.h - ButtonCenterFromBottom else f.h - pad * 0.7f - 4f
            val baseline = f.px(centerY) - (reference.fontMetrics.ascent + reference.fontMetrics.descent) / 2f
            canvas.drawText(refText, f.px(pad), baseline, reference)
        }
        return bitmap
    }

    fun streak(
        context: Context,
        size: WidgetSize,
        u: Float,
        summary: StreakSummary,
        isPlus: Boolean,
        colors: FathomColors,
        tone: Tone
    ): Bitmap {
        val f = Frame(context, size, u, colors, tone)
        val bitmap = base(f, 108108108L)
        val canvas = Canvas(bitmap)
        val tiny = f.w < 150f || f.h < 95f
        val wide = f.w >= f.h * 1.65f && !tiny
        val pad = if (f.h < 130f) 14f else 20f

        val label = f.text(f.mono, 10f, f.hue, 0.14f)
        fun drawLabel(text: String, x: Float, y: Float) {
            val baseline = f.px(y) - (label.fontMetrics.ascent + label.fontMetrics.descent) / 2f
            canvas.drawText(text.uppercase(Locale.getDefault()), f.px(x), baseline, label)
        }

        if (!isPlus) {
            drawLockedStreak(f, canvas, pad, tiny)
            return bitmap
        }

        if (tiny) {
            val numberPaint = f.text(f.display, 56f, f.ink)
            val number = summary.streakDays.toString()
            numberPaint.textSize = f.px(minOf(f.h * 0.52f, 58f))
            val numberWidth = numberPaint.measureText(number)
            val numberBaseline = f.px(f.h * 0.58f)
            canvas.drawText(number, (f.px(f.w) - numberWidth) / 2f, numberBaseline, numberPaint)
            label.textAlign = Paint.Align.CENTER
            val caption = (if (summary.streakDays == 1) "Day streak" else "Day streak").uppercase(Locale.getDefault())
            canvas.drawText(caption, f.px(f.w) / 2f, f.px(f.h * 0.58f + 16f), label)
            return bitmap
        }

        val emblem = WidgetArt.emblem(context, com.masum.fathom.core.model.TraditionId.STOICISM, f.hue, f.px(16f).toInt())
        canvas.drawBitmap(emblem, f.px(pad), f.px(pad), null)
        drawLabel("Streak", pad + 24f, pad + 8f)

        val status = if (summary.hasReadToday) "You have read today" else "Read a verse to keep it going"
        val statusPaint = f.text(f.scriptureItalic, 14f, if (summary.hasReadToday) f.ink else f.muted)

        if (wide) {
            val numberSize = minOf(f.h * 0.46f, 76f)
            val numberPaint = f.text(f.display, numberSize, f.ink)
            val number = summary.streakDays.toString()
            val baseline = f.px(pad + 26f + numberSize * 0.82f)
            canvas.drawText(number, f.px(pad), baseline, numberPaint)
            val numberRight = pad + numberPaint.measureText(number) / f.u + 12f
            val caption = f.text(f.semibold, 14f, f.ink)
            canvas.drawText(if (summary.streakDays == 1) "day" else "days", f.px(numberRight), baseline - f.px(numberSize * 0.30f), caption)
            val sub = f.text(f.mono, 9.5f, f.hue, 0.14f)
            canvas.drawText("IN A ROW", f.px(numberRight), baseline - f.px(numberSize * 0.30f) + f.px(15f), sub)
            val statusLayout = f.layout(status, statusPaint, f.w * 0.42f - pad, 1.1f, 2, true)
            f.draw(canvas, statusLayout, pad, f.h - pad - f.heightDp(statusLayout))
            drawWeek(f, canvas, f.w * 0.48f, pad + 10f, f.w - pad, f.h - pad - 10f, summary, true)
        } else {
            val bandTop = pad + 24f
            val weekHeight = if (f.h >= 150f) 52f else 34f
            val numberSize = minOf((f.h - bandTop - weekHeight - pad - 10f) * 0.78f, 78f).coerceAtLeast(30f)
            val numberPaint = f.text(f.display, numberSize, f.ink)
            val number = summary.streakDays.toString()
            val baseline = f.px(bandTop + numberSize * 0.84f)
            canvas.drawText(number, f.px(pad), baseline, numberPaint)
            val numberRight = pad + numberPaint.measureText(number) / f.u + 10f
            val caption = f.text(f.semibold, 13f, f.ink)
            canvas.drawText(if (summary.streakDays == 1) "day in a row" else "days in a row", f.px(numberRight), baseline - f.px(numberSize * 0.12f), caption)
            drawWeek(f, canvas, pad, f.h - pad - weekHeight, f.w - pad, f.h - pad, summary, f.h >= 150f)
            if (f.h >= 190f) {
                val statusLayout = f.layout(status, statusPaint, f.w - pad * 2, 1.1f, 1, true)
                f.draw(canvas, statusLayout, pad, f.h - pad - weekHeight - 14f - f.heightDp(statusLayout))
            }
        }
        return bitmap
    }

    private fun drawWeek(f: Frame, canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, summary: StreakSummary, letters: Boolean) {
        val count = 7
        val slot = (right - left) / count
        val radius = minOf(slot * 0.34f, 13f)
        val letterPaint = f.text(f.mono, 9.5f, f.faint, 0.1f).apply { textAlign = Paint.Align.CENTER }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = f.px(1.5f) }
        val check = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = f.px(1.8f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = f.tone.onHue.toArgb()
        }
        val centerY = if (letters) top + radius + 2f else (top + bottom) / 2f
        val calendar = Calendar.getInstance()
        for (i in 0 until count) {
            val cx = left + slot * i + slot / 2f
            val done = summary.weeklyHistory.getOrElse(i) { false }
            val isToday = i == count - 1
            val x = f.px(cx)
            val y = f.px(centerY)
            val r = f.px(radius)
            if (done) {
                fill.color = f.hue
                canvas.drawCircle(x, y, r, fill)
                val path = Path().apply {
                    moveTo(x - r * 0.38f, y + r * 0.02f)
                    lineTo(x - r * 0.08f, y + r * 0.32f)
                    lineTo(x + r * 0.42f, y - r * 0.3f)
                }
                canvas.drawPath(path, check)
            } else {
                fill.color = (f.hairline and 0x00FFFFFF) or 0x66000000
                canvas.drawCircle(x, y, r, fill)
            }
            if (isToday) {
                ring.color = f.hue
                canvas.drawCircle(x, y, r + f.px(3f), ring)
            }
            if (letters) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, i - (count - 1))
                val letter = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())?.take(1)?.uppercase(Locale.getDefault()) ?: ""
                letterPaint.color = if (isToday) f.hue else f.faint
                canvas.drawText(letter, x, f.px(centerY + radius + 16f), letterPaint)
            }
        }
    }

    private fun drawLockedStreak(f: Frame, canvas: Canvas, pad: Float, tiny: Boolean) {
        val label = f.text(f.mono, 10f, f.hue, 0.14f)
        if (!tiny) {
            val emblem = WidgetArt.emblem(f.context, com.masum.fathom.core.model.TraditionId.STOICISM, f.hue, f.px(16f).toInt())
            canvas.drawBitmap(emblem, f.px(pad), f.px(pad), null)
            val baseline = f.px(pad + 8f) - (label.fontMetrics.ascent + label.fontMetrics.descent) / 2f
            canvas.drawText("FATHOM PLUS", f.px(pad + 24f), baseline, label)
        }
        val headline = f.text(f.display, 26f, f.ink)
        val top = if (tiny) pad else pad + 30f
        val bottomReserve = if (tiny) 0f else 28f
        val layout = f.fit("Your streak,\nat a glance.", headline, f.w - pad * 2, f.h - top - pad - bottomReserve, 28f, 13f, 1.08f)
        f.draw(canvas, layout, pad, top)
        if (!tiny) {
            val hint = f.text(f.mono, 9.5f, f.hue, 0.14f)
            canvas.drawText("TAP TO UNLOCK  →", f.px(pad), f.px(f.h - pad - 2f), hint)
        }
    }
}
