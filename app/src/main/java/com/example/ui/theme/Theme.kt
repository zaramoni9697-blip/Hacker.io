package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = Color(0xFF004D59),
    onPrimaryContainer = Color(0xFFB5F4FF),
    secondary = CyberPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3700B3),
    onSecondaryContainer = Color(0xFFE8DDFF),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF00220E),
    tertiaryContainer = Color(0xFF005324),
    onTertiaryContainer = Color(0xFF88FFA9),
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = CyberRed,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFF00838F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB4F1FA),
    onPrimaryContainer = Color(0xFF002024),
    secondary = Color(0xFF5E35B1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7DEFB),
    onSecondaryContainer = Color(0xFF1E0060),
    tertiary = Color(0xFF00897B),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFD32F2F),
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek cybersecurity dark mode for best contrast and tech look
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
