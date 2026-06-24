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
    secondary = PythonYellow,
    onSecondary = EditorInk,
    tertiary = SuccessGreen,
    error = ErrorRed,
    background = SlateSurface,
    onBackground = EditorInk,
    surface = Color.White,
    onSurface = EditorInk,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = Color(0xFF0F172A),
    secondary = PythonYellow,
    onSecondary = EditorInk,
    tertiary = Color(0xFF86EFAC),
    error = Color(0xFFFCA5A5),
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFE5E7EB),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
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

