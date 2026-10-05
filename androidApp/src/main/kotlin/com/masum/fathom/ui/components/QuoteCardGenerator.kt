package com.masum.fathom.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.masum.fathom.R
import com.masum.fathom.core.model.Passage
import com.masum.fathom.ui.design.EmblemShapes
import com.masum.fathom.ui.theme.FathomTypefaces
import com.masum.fathom.ui.theme.ReadingTheme
import com.masum.fathom.ui.theme.palette
import com.masum.fathom.ui.theme.toneFor
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Random

enum class CardFormat(
    val title: String,
    val width: Int,
    val height: Int,
    val aspectRatio: Float
) {
    STORY("Story 9:16", 1080, 1920, 9f / 16f),
    SQUARE("Square 1:1", 1080, 1080, 1f),
    PORTRAIT("Feed 4:5", 1080, 1350, 4f / 5f)
}

enum class CardTheme(val title: String, val reading: ReadingTheme) {
    OBSIDIAN("Nocturne", ReadingTheme.OBSIDIAN),
    SEPIA("Vellum", ReadingTheme.SEPIA),
    SLATE("Abyss", ReadingTheme.SLATE),
    FOREST("Moss", ReadingTheme.FOREST)
}

object QuoteCardRenderer {

    fun generateBitmap(
        context: Context,
        passage: Passage,
        format: CardFormat,
        theme: CardTheme,
        includeOriginal: Boolean = true,
        includeThemes: Boolean = true
    ): Bitmap {
        val width = format.width
        val height = format.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val unit = width / 1080f

        val palette = theme.reading.palette()
        val tone = toneFor(passage.traditionId, palette)
        val ink = palette.textPrimary.toArgb()
        val muted = palette.textSecondary.toArgb()
        val faint = palette.textTertiary.toArgb()
        val hairline = palette.hairline.toArgb()
        val hue = tone.hue.toArgb()

        val scripture = FathomTypefaces.load(context, R.font.newsreader, "'wght' 400, 'opsz' 36")
        val original = FathomTypefaces.load(context, R.font.newsreader, "'wght' 400, 'opsz' 16")
        val mono = FathomTypefaces.load(context, R.font.geist_mono, "'wght' 500")
        val display = FathomTypefaces.load(context, R.font.fraunces_italic, "'wght' 400, 'opsz' 144, 'SOFT' 60, 'WONK' 1")

        canvas.drawColor(palette.background.toArgb())
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        glowPaint.shader = RadialGradient(width * 0.12f, height * 0.08f, maxOf(width, height) * 0.95f, tone.glowPrimary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
        glowPaint.shader = RadialGradient(width * 0.96f, height * 0.94f, maxOf(width, height) * 0.8f, tone.glowSecondary.toArgb(), 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)
        glowPaint.shader = LinearGradient(0f, height * 0.55f, 0f, height.toFloat(), 0x00000000, (palette.background.toArgb() and 0x00FFFFFF) or 0x66000000, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), glowPaint)

        val grain = Paint().apply { color = if (palette.isDark) 0x0DFFFFFF else 0x0D000000 }
        val random = Random(passage.id.hashCode().toLong())
        val speck = 1.6f * unit
        repeat(width * height / 260) {
            val x = random.nextInt(width).toFloat()
            val y = random.nextInt(height).toFloat()
            canvas.drawRect(x, y, x + speck, y + speck, grain)
        }

        val edge = 56f * unit
        val frame = RectF(edge, edge, width - edge, height - edge)
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hairline
            style = Paint.Style.STROKE
            strokeWidth = 2f * unit
        }
        canvas.drawRoundRect(frame, 44f * unit, 44f * unit, framePaint)

        val padX = 84f * unit
        val left = frame.left + padX
        val right = frame.right - padX
        val contentWidth = (right - left).toInt()

        val emblemSize = 64f * unit
        val emblemPath = EmblemShapes.androidPath(passage.traditionId, emblemSize)
        val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hue
            style = Paint.Style.STROKE
            strokeWidth = 3f * unit
        }
        val topY = frame.top + 92f * unit
        canvas.save()
        canvas.translate(left, topY)
        canvas.drawPath(emblemPath, emblemPaint)
        canvas.restore()

        val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = mono
            textSize = 26f * unit
            letterSpacing = 0.14f
            color = muted
            textAlign = Paint.Align.RIGHT
        }
        val caption = "${passage.traditionId.displayName} · ${passage.canonicalReference}".uppercase()
        val captionLayoutWidth = contentWidth - emblemSize.toInt() - (24f * unit).toInt()
        var captionText = caption
        while (captionPaint.measureText(captionText) > captionLayoutWidth && captionText.length > 8) {
            captionText = captionText.dropLast(2)
        }
        if (captionText != caption) captionText = captionText.trimEnd() + "…"
        canvas.drawText(captionText, right, topY + emblemSize / 2f + 9f * unit, captionPaint)

        val footerTop = frame.bottom - 190f * unit
        val bodyTop = topY + emblemSize + 70f * unit
        val themesHeight = if (includeThemes && passage.themes.isNotEmpty()) 64f * unit else 0f
        val bodyBottom = footerTop - 40f * unit - themesHeight
        val bodyHeight = (bodyBottom - bodyTop).toInt()

        val hasOriginal = includeOriginal && !passage.originalText.isNullOrBlank() && passage.originalText != passage.translation
        val originalLayout = if (hasOriginal) {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = original
                textSize = 36f * unit
                color = faint
            }
            var layout = layout(passage.originalText.orEmpty(), paint, contentWidth, 1.2f)
            while (layout.lineCount > 3 && paint.textSize > 24f * unit) {
                paint.textSize -= 2f * unit
                layout = layout(passage.originalText.orEmpty(), paint, contentWidth, 1.2f)
            }
            layout
        } else null
        val reservedForOriginal = (originalLayout?.height ?: 0) + if (originalLayout != null) (56f * unit).toInt() else 0

        val translationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = scripture
            color = ink
        }
        var size = (if (format == CardFormat.STORY) 92f else if (format == CardFormat.PORTRAIT) 84f else 76f) * unit
        val minSize = 34f * unit
        var translationLayout: StaticLayout
        while (true) {
            translationPaint.textSize = size
            translationLayout = layout(passage.translation, translationPaint, contentWidth, 1.22f)
            if (translationLayout.height + reservedForOriginal <= bodyHeight || size <= minSize) break
            size -= 2f * unit
        }

        val blockHeight = translationLayout.height + reservedForOriginal
        var cursor = bodyTop + ((bodyHeight - blockHeight).coerceAtLeast(0) / 2f)
        if (originalLayout != null) {
            canvas.save()
            canvas.translate(left, cursor)
            originalLayout.draw(canvas)
            canvas.restore()
            cursor += originalLayout.height + 22f * unit
            val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = hue
                strokeWidth = 3f * unit
            }
            canvas.drawLine(left, cursor, left + 72f * unit, cursor, rule)
            cursor += 34f * unit
        }
        canvas.save()
        canvas.translate(left, cursor)
        translationLayout.draw(canvas)
        canvas.restore()

        if (themesHeight > 0f) {
            val themesPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = mono
                textSize = 24f * unit
                letterSpacing = 0.08f
                color = faint
            }
            canvas.drawText(passage.themes.take(3).joinToString("   ") { "#" + it.name.lowercase() }, left, footerTop - 34f * unit, themesPaint)
        }

        val footerRule = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = hairline
            strokeWidth = 2f * unit
        }
        canvas.drawLine(left, footerTop, right, footerTop, footerRule)

        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = display
            textSize = 68f * unit
            color = ink
        }
        canvas.drawText("fathom", left, footerTop + 104f * unit, brandPaint)

        val taglinePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = mono
            textSize = 20f * unit
            letterSpacing = 0.16f
            color = faint
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("SACRED WISDOM, OFFLINE", right, footerTop + 96f * unit, taglinePaint)

        return bitmap
    }

    private fun layout(text: CharSequence, paint: TextPaint, width: Int, spacing: Float): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, spacing)
            .setIncludePad(false)
            .build()

    fun shareQuoteCard(
        context: Context,
        bitmap: Bitmap,
        passage: Passage
    ) {
        try {
            val cacheDir = File(context.cacheDir, "quote_cards")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val fileName = "fathom_quote_${passage.id.replace(":", "_")}_${System.currentTimeMillis()}.png"
            val file = File(cacheDir, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "“${passage.translation}” — ${passage.canonicalReference} via Fathom")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Sacred Quote Card")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share quote card: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveToGallery(
        context: Context,
        bitmap: Bitmap,
        passage: Passage
    ) {
        try {
            val fileName = "Fathom_${passage.canonicalReference.replace(" ", "_").replace(":", "_")}_${System.currentTimeMillis()}.png"
            var outputStream: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "Fathom")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                if (imageUri != null) {
                    outputStream = resolver.openOutputStream(imageUri)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Fathom"
                val file = File(imagesDir)
                if (!file.exists()) {
                    file.mkdirs()
                }
                val imageFile = File(imagesDir, fileName)
                outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            outputStream?.flush()
            outputStream?.close()
            Toast.makeText(context, "Saved quote card to Pictures/Fathom", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save quote card: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
