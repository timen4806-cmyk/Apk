package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = NightBackground,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = GoldAccent,
    onSecondary = NightBackground,
    secondaryContainer = NightSurfaceVariant,
    onSecondaryContainer = GoldLight,
    tertiary = AmberAccent,
    background = NightBackground,
    onBackground = TextPrimaryDark,
    surface = NightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NightCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight.copy(alpha = 0.2f),
    onPrimaryContainer = EmeraldDark,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = GoldLight.copy(alpha = 0.3f),
    onSecondaryContainer = GoldDark,
    tertiary = AmberAccent,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MosqueAppTheme(
    darkTheme: Boolean = true, // Default to elegant dark theme for comfortable night viewing & mosque display
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
