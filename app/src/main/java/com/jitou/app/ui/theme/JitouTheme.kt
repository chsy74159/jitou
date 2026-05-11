package com.jitou.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF171717),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD84D),
    onPrimaryContainer = Color(0xFF171717),
    secondary = Color(0xFFCFECE1),
    onSecondary = Color(0xFF171717),
    secondaryContainer = Color(0xFFFFD8C9),
    tertiary = Color(0xFF6F72FF),
    tertiaryContainer = Color(0xFFE9ECEF),
    background = Color(0xFFF8F7F2),
    onBackground = Color(0xFF171717),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0ECE2),
    onSurfaceVariant = Color(0xFF72706A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD6C9),
    onPrimary = Color(0xFF00372F),
    primaryContainer = Color(0xFF064E45),
    onPrimaryContainer = Color(0xFFD4EEE7),
    secondary = Color(0xFFFFB68C),
    onSecondary = Color(0xFF4F250C),
    secondaryContainer = Color(0xFF6E391E),
    tertiary = Color(0xFFBBC6FF),
    tertiaryContainer = Color(0xFF31477F),
    background = Color(0xFF171410),
    onBackground = Color(0xFFEAE1D8),
    surface = Color(0xFF171410),
    surfaceVariant = Color(0xFF4C463F),
    onSurfaceVariant = Color(0xFFD0C5BA),
)

@Composable
fun JitouTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
