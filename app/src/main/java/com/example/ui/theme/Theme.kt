package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Amber500,
    onPrimary = Slate900,
    primaryContainer = Slate800,
    onPrimaryContainer = Amber100,
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = Blue100,
    background = Color(0xFF0B1120),
    surface = Slate900,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Slate800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Slate700,
    outlineVariant = Slate700
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue800,
    secondary = Amber500,
    onSecondary = Slate900,
    secondaryContainer = Amber100,
    onSecondaryContainer = Amber600,
    tertiary = Green600,
    onTertiary = Color.White,
    background = Slate100,
    surface = Color.White,
    onBackground = Slate900,
    onSurface = Slate900,
    surfaceVariant = Slate50,
    onSurfaceVariant = Slate500,
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun ElektroTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep tailored brand styling matching the prompt
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
