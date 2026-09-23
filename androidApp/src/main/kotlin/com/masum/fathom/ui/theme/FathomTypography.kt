package com.masum.fathom.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.masum.fathom.R

val PlayfairFontFamily = FontFamily(
    Font(R.font.playfair_bold, FontWeight.Bold),
    Font(R.font.playfair_italic, FontWeight.Bold, FontStyle.Italic)
)

val InstrumentSerifFontFamily = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic)
)

val CinzelDecorativeFontFamily = FontFamily(
    Font(R.font.cinzel_decorative_bold, FontWeight.Bold)
)

val BodoniFontFamily = FontFamily(
    Font(R.font.bodoni_bold, FontWeight.Bold),
    Font(R.font.bodoni_italic, FontWeight.Bold, FontStyle.Italic)
)

val SyneFontFamily = FontFamily(
    Font(R.font.syne_bold, FontWeight.Bold)
)

val CormorantFontFamily = FontFamily(
    Font(R.font.cormorant_medium, FontWeight.Medium),
    Font(R.font.cormorant_bold, FontWeight.Bold),
    Font(R.font.cormorant_italic, FontWeight.Medium, FontStyle.Italic)
)

val JakartaFontFamily = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
    Font(R.font.jakarta_bold, FontWeight.Bold)
)

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel_bold, FontWeight.Bold)
)

val ItalianaFontFamily = FontFamily(
    Font(R.font.italiana_regular, FontWeight.Normal)
)


@Immutable
data class FathomTypography(
    val brandTitle: TextStyle = TextStyle(
        fontFamily = CinzelDecorativeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 2.2.sp
    ),
    val verseDisplay: TextStyle = TextStyle(



        fontFamily = CormorantFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.2.sp
    ),
    val verseOriginal: TextStyle = TextStyle(
        fontFamily = CormorantFontFamily,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic,
        fontSize = 19.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.4.sp
    ),

    val canonicalHeader: TextStyle = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.4.sp
    ),
    val traditionBadge: TextStyle = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.0.sp
    ),
    val commentaryTitle: TextStyle = TextStyle(
        fontFamily = PlayfairFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.1.sp
    ),
    val commentaryBody: TextStyle = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    val caption: TextStyle = TextStyle(
        fontFamily = JakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
)

val LocalFathomTypography = staticCompositionLocalOf { FathomTypography() }

