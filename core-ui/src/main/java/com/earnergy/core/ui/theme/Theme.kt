package com.earnergy.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.Black,
    secondary = GreenDark,
    onSecondary = androidx.compose.ui.graphics.Color.White
)

private val DarkColors = darkColorScheme(
    primary = GreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = GreenDark,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = GreenPrimary,
    onSecondary = androidx.compose.ui.graphics.Color.Black
)

@Composable
fun EarnergyTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
