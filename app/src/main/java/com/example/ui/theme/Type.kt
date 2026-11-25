package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

fun createVisionScanTypography(largeTextMode: Boolean): Typography {
    val scale = if (largeTextMode) 1.15f else 1.0f
    return Typography(
        displayLarge = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * scale).sp,
            lineHeight = (44 * scale).sp,
            letterSpacing = (-0.5).sp
        ),
        displaySmall = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * scale).sp,
            lineHeight = (35 * scale).sp,
            letterSpacing = (-0.25).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * scale).sp,
            lineHeight = (31 * scale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = (27 * scale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (18 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * scale).sp,
            lineHeight = (22 * scale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (21 * scale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (18 * scale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.3.sp
        ),
        labelSmall = TextStyle(
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (15 * scale).sp,
            letterSpacing = 0.4.sp
        )
    )
}
