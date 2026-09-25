package com.masum.fathom.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.masum.fathom.R
import com.masum.fathom.core.model.Passage
import com.masum.fathom.core.model.TraditionId
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

enum class CardFormat(
    val title: String,
    val width: Int,
    val height: Int,
    val aspectRatio: Float
) {
    STORY("9:16 Story", 1080, 1920, 9f / 16f),
    SQUARE("1:1 Square", 1080, 1080, 1f),
    PORTRAIT("4:5 Feed", 1080, 1350, 4f / 5f)
}

enum class CardTheme(
    val title: String,
    val bgHex: Int,
    val cardBgHex: Int,
    val borderHex: Int,
    val textPrimaryHex: Int,
    val textSecondaryHex: Int,
    val textTertiaryHex: Int,
    val accentGoldHex: Int,
    val badgeBgHex: Int
) {
    OBSIDIAN(
        title = "Obsidian",
        bgHex = 0xFF09090B.toInt(),
        cardBgHex = 0xFF121214.toInt(),
        borderHex = 0xFF27272A.toInt(),
        textPrimaryHex = 0xFFFAFAFA.toInt(),
        textSecondaryHex = 0xFFA1A1AA.toInt(),
        textTertiaryHex = 0xFF71717A.toInt(),
        accentGoldHex = 0xFFD4AF37.toInt(),
        badgeBgHex = 0x22D4AF37.toInt()
    ),
    SEPIA(
        title = "Sepia",
        bgHex = 0xFFFBF7EE.toInt(),
        cardBgHex = 0xFFF4EFE6.toInt(),
        borderHex = 0xFFE7E0D3.toInt(),
        textPrimaryHex = 0xFF1C1917.toInt(),
        textSecondaryHex = 0xFF57534E.toInt(),
        textTertiaryHex = 0xFF78716C.toInt(),
        accentGoldHex = 0xFFB45309.toInt(),
        badgeBgHex = 0x22B45309.toInt()
    ),
    SLATE(
        title = "Slate",
        bgHex = 0xFF0F172A.toInt(),
        cardBgHex = 0xFF1E293B.toInt(),
        borderHex = 0xFF334155.toInt(),
        textPrimaryHex = 0xFFF8FAFC.toInt(),
        textSecondaryHex = 0xFF94A3B8.toInt(),
        textTertiaryHex = 0xFF64748B.toInt(),
        accentGoldHex = 0xFFFBBF24.toInt(),
        badgeBgHex = 0x22FBBF24.toInt()
    )
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

        val syneBold = try {
            ResourcesCompat.getFont(context, R.font.syne_bold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val jakartaRegular = try {
            ResourcesCompat.getFont(context, R.font.jakarta_regular) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        val jakartaMedium = try {
            ResourcesCompat.getFont(context, R.font.jakarta_medium) ?: Typeface.DEFAULT
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        val jakartaSemiBold = try {
            ResourcesCompat.getFont(context, R.font.jakarta_semibold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val jakartaBold = try {
            ResourcesCompat.getFont(context, R.font.jakarta_bold) ?: Typeface.DEFAULT_BOLD
        } catch (e: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.bgHex
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val outerMarginX = 64f
        val outerMarginY = when (format) {
            CardFormat.STORY -> 160f
            CardFormat.PORTRAIT -> 90f
            CardFormat.SQUARE -> 64f
        }

        val cardLeft = outerMarginX
        val cardTop = outerMarginY
        val cardRight = width - outerMarginX
        val cardBottom = height - outerMarginY
        val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)
        val cornerRadius = 36f

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.cardBgHex
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, cardBgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.borderHex
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)

        val innerPaddingX = 64f
        val contentWidth = (cardRect.width() - (innerPaddingX * 2)).toInt()
        var currentY = cardTop + 64f

        val badgeText = passage.traditionId.displayName.uppercase()
        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentGoldHex
            typeface = jakartaBold
            textSize = 28f
            letterSpacing = 0.12f
        }
        val badgeTextWidth = badgePaint.measureText(badgeText)
        val badgePaddingH = 26f
        val badgePaddingV = 14f
        val badgeWidth = badgeTextWidth + (badgePaddingH * 2)
        val badgeHeight = 28f + (badgePaddingV * 2)
        val badgeRect = RectF(
            cardLeft + innerPaddingX,
            currentY,
            cardLeft + innerPaddingX + badgeWidth,
            currentY + badgeHeight
        )

        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.badgeBgHex
            style = Paint.Style.FILL
        }
        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentGoldHex
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(badgeRect, 18f, 18f, badgeBgPaint)
        canvas.drawRoundRect(badgeRect, 18f, 18f, badgeBorderPaint)

        val badgeFontMetrics = badgePaint.fontMetrics
        val badgeTextY = badgeRect.centerY() - (badgeFontMetrics.ascent + badgeFontMetrics.descent) / 2
        canvas.drawText(badgeText, badgeRect.left + badgePaddingH, badgeTextY, badgePaint)

        val refText = passage.canonicalReference.uppercase()
        val refPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textSecondaryHex
            typeface = jakartaSemiBold
            textSize = 28f
            letterSpacing = 0.1f
            textAlign = Paint.Align.RIGHT
        }
        val refFontMetrics = refPaint.fontMetrics
        val refTextY = badgeRect.centerY() - (refFontMetrics.ascent + refFontMetrics.descent) / 2
        canvas.drawText(refText, cardRight - innerPaddingX, refTextY, refPaint)

        currentY += badgeHeight + 36f

        drawDivider(canvas, cardLeft + innerPaddingX, currentY, cardRight - innerPaddingX, theme)
        currentY += 40f

        val hasOriginal = includeOriginal && !passage.originalText.isNullOrBlank()
        val originalLayout: StaticLayout? = if (hasOriginal) {
            val origPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.textTertiaryHex
                typeface = jakartaRegular
                textSize = 34f
                letterSpacing = 0.02f
            }
            createStaticLayout(passage.originalText ?: "", origPaint, contentWidth)
        } else null

        val availableHeightForTranslation = (cardBottom - currentY - 160f) - (if (originalLayout != null) originalLayout.height + 60f else 0f)

        val textLength = passage.translation.length
        val dynamicFontSize = when {
            textLength < 100 -> if (format == CardFormat.STORY) 52f else 46f
            textLength < 220 -> if (format == CardFormat.STORY) 44f else 38f
            textLength < 400 -> if (format == CardFormat.STORY) 38f else 32f
            else -> 30f
        }

        val transPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textPrimaryHex
            typeface = jakartaMedium
            textSize = dynamicFontSize
            letterSpacing = 0.02f
        }

        val translationLayout = createStaticLayout(passage.translation, transPaint, contentWidth)

        val totalContentHeight = (if (originalLayout != null) originalLayout.height + 60f else 0f) + translationLayout.height
        val spaceRemaining = (cardBottom - 160f) - currentY
        if (spaceRemaining > totalContentHeight) {
            currentY += ((spaceRemaining - totalContentHeight) * 0.35f)
        }

        if (originalLayout != null) {
            canvas.save()
            canvas.translate(cardLeft + innerPaddingX, currentY)
            originalLayout.draw(canvas)
            canvas.restore()
            currentY += originalLayout.height + 28f

            drawDivider(canvas, cardLeft + innerPaddingX, currentY, cardRight - innerPaddingX, theme)
            currentY += 36f
        }

        canvas.save()
        canvas.translate(cardLeft + innerPaddingX, currentY)
        translationLayout.draw(canvas)
        canvas.restore()

        currentY += translationLayout.height + 36f

        val hasThemes = includeThemes && passage.themes.isNotEmpty()
        if (hasThemes) {
            val themesText = passage.themes.joinToString("   •   ") { "#" + it.name.uppercase() }
            val themesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = theme.accentGoldHex
                typeface = jakartaSemiBold
                textSize = 24f
                letterSpacing = 0.08f
            }
            canvas.drawText(themesText, cardLeft + innerPaddingX, cardBottom - 110f, themesPaint)
        }

        val footerDividerY = cardBottom - 80f
        drawDivider(canvas, cardLeft + innerPaddingX, footerDividerY, cardRight - innerPaddingX, theme)

        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textPrimaryHex
            typeface = syneBold
            textSize = 34f
            letterSpacing = -0.02f
        }
        val brandDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentGoldHex
            typeface = syneBold
            textSize = 34f
        }
        val fathomText = "fathom"
        val fathomWidth = brandPaint.measureText(fathomText)
        val footerY = cardBottom - 34f

        canvas.drawText(fathomText, cardLeft + innerPaddingX, footerY, brandPaint)
        canvas.drawText(".", cardLeft + innerPaddingX + fathomWidth, footerY, brandDotPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.textTertiaryHex
            typeface = jakartaRegular
            textSize = 22f
            letterSpacing = 0.08f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("SACRED HUMAN WISDOM", cardRight - innerPaddingX, footerY, taglinePaint)

        return bitmap
    }

    private fun createStaticLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(14f, 1.25f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 14f, false)
        }
    }

    private fun drawDivider(canvas: Canvas, startX: Float, y: Float, endX: Float, theme: CardTheme) {
        val midX = (startX + endX) / 2
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.borderHex
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        canvas.drawLine(startX, y, midX - 24f, y, linePaint)
        canvas.drawLine(midX + 24f, y, endX, y, linePaint)

        val diamondPath = Path().apply {
            moveTo(midX, y - 6f)
            lineTo(midX + 6f, y)
            lineTo(midX, y + 6f)
            lineTo(midX - 6f, y)
            close()
        }
        val diamondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = theme.accentGoldHex
            style = Paint.Style.FILL
        }
        canvas.drawPath(diamondPath, diamondPaint)
    }

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
