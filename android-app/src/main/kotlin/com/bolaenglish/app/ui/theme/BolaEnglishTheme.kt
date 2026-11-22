package com.bolaenglish.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFFFF6B6B),
    background = Color(0xFFF8F9FA),
    surface = Color.White,
    onSurface = Color(0xFF21252D)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCEB5FF),
    secondary = Color(0xFFFF6B6B),
    background = Color(0xFF21252D),
    surface = Color(0xFF343B4F),
    onSurface = Color(0xFFF8F9FA)
)

@Composable
fun BolaEnglishTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
