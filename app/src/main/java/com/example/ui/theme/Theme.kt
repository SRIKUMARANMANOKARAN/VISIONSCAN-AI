package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

private val VisionScanLightColorScheme = lightColorScheme(
    primary = MedicalBlue,
    onPrimary = ClinicalWhite,
    primaryContainer = MedicalBlueLight,
    onPrimaryContainer = MedicalBlueDark,
    secondary = ClinicalTeal,
    onSecondary = ClinicalWhite,
    secondaryContainer = ClinicalTealLight,
    onSecondaryContainer = ClinicalTealDark,
    tertiary = SkyAccent,
    onTertiary = ClinicalWhite,
    tertiaryContainer = SkyAccentLight,
    onTertiaryContainer = MedicalBlueDark,
    background = ClinicalCanvas,
    onBackground = InkPrimary,
    surface = ClinicalWhite,
    onSurface = InkPrimary,
    surfaceVariant = ClinicalSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = ClinicalOutline,
    outlineVariant = ClinicalOutlineSubtle,
    error = StatusAlert,
    onError = ClinicalWhite,
    errorContainer = StatusAlertBg,
    onErrorContainer = StatusAlert
)

private val ColorErrorDark = androidx.compose.ui.graphics.Color(0xFFFF8A80)
private val ColorErrorContainerDark = androidx.compose.ui.graphics.Color(0xFF5F1414)
private val ColorOnErrorContainerDark = androidx.compose.ui.graphics.Color(0xFFFFDAD6)

private val VisionScanDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkSurfaceVariant,
    error = ColorErrorDark,
    onError = DarkCanvas,
    errorContainer = ColorErrorContainerDark,
    onErrorContainer = ColorOnErrorContainerDark
)

val VisionScanShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VisionScanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    largeTextMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VisionScanDarkColorScheme else VisionScanLightColorScheme
    val typography = remember(largeTextMode) { createVisionScanTypography(largeTextMode) }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = VisionScanShapes,
        content = content
    )
}
