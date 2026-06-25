package com.eztech.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PythonBlue,
    onPrimary = Color.White,
    primaryContainer = PythonBlueLight,
    onPrimaryContainer = Color(0xFF0B3551),
    secondary = PythonYellow,
    onSecondary = EditorInk,
    secondaryContainer = PythonYellowSoft,
    onSecondaryContainer = Color(0xFF3A2D00),
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDFBE8),
    onTertiaryContainer = Color(0xFF05391C),
    error = ErrorRed,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    background = SlateSurface,
    onBackground = EditorInk,
    surface = SlateCard,
    onSurface = EditorInk,
    surfaceVariant = PythonBlueMist,
    onSurfaceVariant = Color(0xFF465A69),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBFDFE),
    surfaceContainer = Color(0xFFF2F8FC),
    surfaceContainerHigh = Color(0xFFEAF3F9),
    surfaceContainerHighest = Color(0xFFE2EDF6),
    outline = Color(0xFF7C97AA),
    outlineVariant = SlateLine,
    inverseSurface = PythonBlueDark,
    inverseOnSurface = Color.White,
    inversePrimary = PythonYellow,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = PythonBlueDark,
    onPrimaryContainer = Color(0xFFDCEEFF),
    secondary = PythonYellow,
    onSecondary = EditorInk,
    secondaryContainer = Color(0xFF5B4500),
    onSecondaryContainer = Color(0xFFFFF2B8),
    tertiary = Color(0xFF86EFAC),
    onTertiary = Color(0xFF052E16),
    tertiaryContainer = Color(0xFF14532D),
    onTertiaryContainer = Color(0xFFD9FBE5),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFFDADA),
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF101923),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF172536),
    onSurfaceVariant = Color(0xFFC7D7E5),
    surfaceContainerLowest = Color(0xFF070B13),
    surfaceContainerLow = Color(0xFF111B27),
    surfaceContainer = Color(0xFF162232),
    surfaceContainerHigh = Color(0xFF1D2C3E),
    surfaceContainerHighest = Color(0xFF26384C),
    outline = Color(0xFF8CA6BA),
    outlineVariant = Color(0xFF324A60),
    inverseSurface = Color(0xFFE6F1FA),
    inverseOnSurface = Color(0xFF102333),
    inversePrimary = PythonBlue,
)

@Composable
fun EzTechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = EzTechTypography,
        shapes = EzTechShapes,
        content = content,
    )
}
