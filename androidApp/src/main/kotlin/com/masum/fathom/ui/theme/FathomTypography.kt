package com.masum.fathom.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.sp
import com.masum.fathom.R

@OptIn(ExperimentalTextApi::class)
private fun axis(
    res: Int,
    weight: Int,
    style: FontStyle = FontStyle.Normal,
    vararg settings: FontVariation.Setting
): Font = Font(
    resId = res,
    weight = FontWeight(weight),
    style = style,
    variationSettings = FontVariation.Settings(FontWeight(weight), style, *settings)
)

@OptIn(ExperimentalTextApi::class)
private fun fraunces(weight: Int, style: FontStyle, opsz: Float, soft: Float, wonk: Float): Font = axis(
    if (style == FontStyle.Italic) R.font.fraunces_italic else R.font.fraunces,
    weight,
    style,
    FontVariation.Setting("opsz", opsz),
    FontVariation.Setting("SOFT", soft),
    FontVariation.Setting("WONK", wonk)
)

@OptIn(ExperimentalTextApi::class)
private fun newsreader(weight: Int, style: FontStyle, opsz: Float): Font = axis(
    if (style == FontStyle.Italic) R.font.newsreader_italic else R.font.newsreader,
    weight,
    style,
    FontVariation.Setting("opsz", opsz)
)

val DisplayFamily = FontFamily(
    fraunces(300, FontStyle.Normal, 144f, 40f, 1f),
    fraunces(400, FontStyle.Normal, 144f, 40f, 1f),
    fraunces(500, FontStyle.Normal, 144f, 40f, 1f),
    fraunces(600, FontStyle.Normal, 144f, 40f, 1f),
    fraunces(300, FontStyle.Italic, 144f, 60f, 1f),
    fraunces(400, FontStyle.Italic, 144f, 60f, 1f),
    fraunces(500, FontStyle.Italic, 144f, 60f, 1f)
)

val HeadingFamily = FontFamily(
    fraunces(400, FontStyle.Normal, 48f, 30f, 0f),
    fraunces(500, FontStyle.Normal, 48f, 30f, 0f),
    fraunces(600, FontStyle.Normal, 48f, 30f, 0f),
    fraunces(400, FontStyle.Italic, 48f, 50f, 0f),
    fraunces(500, FontStyle.Italic, 48f, 50f, 0f)
)

val ScriptureFamily = FontFamily(
    newsreader(300, FontStyle.Normal, 36f),
    newsreader(400, FontStyle.Normal, 36f),
    newsreader(500, FontStyle.Normal, 36f),
    newsreader(300, FontStyle.Italic, 36f),
    newsreader(400, FontStyle.Italic, 36f),
    newsreader(500, FontStyle.Italic, 36f)
)

val ReadingFamily = FontFamily(
    newsreader(400, FontStyle.Normal, 16f),
    newsreader(500, FontStyle.Normal, 16f),
    newsreader(600, FontStyle.Normal, 16f),
    newsreader(400, FontStyle.Italic, 16f),
    newsreader(500, FontStyle.Italic, 16f)
)

val InterfaceFamily = FontFamily(
    axis(R.font.geist, 400),
    axis(R.font.geist, 500),
    axis(R.font.geist, 600),
    axis(R.font.geist, 700)
)

val MonoFamily = FontFamily(
    axis(R.font.geist_mono, 400),
    axis(R.font.geist_mono, 500),
    axis(R.font.geist_mono, 600)
)

@Immutable
data class FathomTypography(
    val brandTitle: TextStyle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 26.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.6).sp
    ),
    val displayLarge: TextStyle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 56.sp,
        lineHeight = 58.sp,
        letterSpacing = (-1.4).sp
    ),
    val display: TextStyle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp
    ),
    val headline: TextStyle = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.7).sp
    ),
    val title: TextStyle = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.3).sp
    ),
    val verseDisplay: TextStyle = TextStyle(
        fontFamily = ScriptureFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
        lineBreak = LineBreak.Paragraph
    ),
    val verseOriginal: TextStyle = TextStyle(
        fontFamily = ScriptureFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    val canonicalHeader: TextStyle = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.1.sp
    ),
    val traditionBadge: TextStyle = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp
    ),
    val commentaryTitle: TextStyle = TextStyle(
        fontFamily = HeadingFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp
    ),
    val commentaryBody: TextStyle = TextStyle(
        fontFamily = ReadingFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.05.sp
    ),
    val body: TextStyle = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp
    ),
    val label: TextStyle = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.05.sp
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = InterfaceFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.1.sp
    ),
    val mono: TextStyle = TextStyle(
        fontFamily = MonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.4.sp
    ),
    val numeral: TextStyle = TextStyle(
        fontFamily = DisplayFamily,
        fontWeight = FontWeight.Light,
        fontSize = 44.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.6).sp
    )
)

val LocalFathomTypography = staticCompositionLocalOf { FathomTypography() }
