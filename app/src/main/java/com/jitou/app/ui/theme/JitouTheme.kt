package com.jitou.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class JitouThemeMode(val storageValue: String) {
    Light("light"),
    Dark("dark"),
    System("system"),
    ;

    fun shouldUseDarkTheme(systemDarkTheme: Boolean): Boolean = when (this) {
        Light -> false
        Dark -> true
        System -> systemDarkTheme
    }

    companion object {
        val default: JitouThemeMode = Light

        fun fromStorageValue(value: String?): JitouThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: default
    }
}

@Immutable
data class JitouColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val ink: Color,
    val mutedInk: Color,
    val accent: Color,
    val accentStrong: Color,
    val sage: Color,
    val clay: Color,
    val line: Color,
    val danger: Color,
)

internal val LightJitouColors = JitouColors(
    background = Color(0xFFF7F4EE),
    surface = Color(0xFFFFFEFB),
    surfaceMuted = Color(0xFFEEE7DA),
    ink = Color(0xFF1C1A17),
    mutedInk = Color(0xFF706A60),
    accent = Color(0xFFD8B45A),
    accentStrong = Color(0xFF8A651F),
    sage = Color(0xFFDCE7DE),
    clay = Color(0xFFE8CDBD),
    line = Color(0x1A1C1A17),
    danger = Color(0xFF9D342E),
)

internal val DarkJitouColors = JitouColors(
    background = Color(0xFF15120F),
    surface = Color(0xFF1D1915),
    surfaceMuted = Color(0xFF332D26),
    ink = Color(0xFFF1E8DC),
    mutedInk = Color(0xFFB8AA9A),
    accent = Color(0xFFD7B35A),
    accentStrong = Color(0xFFF0D889),
    sage = Color(0xFF26372F),
    clay = Color(0xFF4A3028),
    line = Color(0x1FFFFFFF),
    danger = Color(0xFFFFB4A9),
)

private val LocalJitouColors = staticCompositionLocalOf { LightJitouColors }

val MaterialTheme.jitouColors: JitouColors
    @Composable
    @ReadOnlyComposable
    get() = LocalJitouColors.current

private val LightColors = lightColorScheme(
    primary = LightJitouColors.accent,
    onPrimary = LightJitouColors.ink,
    primaryContainer = LightJitouColors.accent,
    onPrimaryContainer = LightJitouColors.ink,
    secondary = LightJitouColors.sage,
    onSecondary = LightJitouColors.ink,
    secondaryContainer = LightJitouColors.clay,
    tertiary = LightJitouColors.accentStrong,
    onTertiary = LightJitouColors.surface,
    tertiaryContainer = LightJitouColors.surfaceMuted,
    background = LightJitouColors.background,
    onBackground = LightJitouColors.ink,
    surface = LightJitouColors.surface,
    onSurface = LightJitouColors.ink,
    surfaceVariant = LightJitouColors.surfaceMuted,
    onSurfaceVariant = LightJitouColors.mutedInk,
    outline = LightJitouColors.line,
    error = LightJitouColors.danger,
    onError = LightJitouColors.surface,
)

private val DarkColors = darkColorScheme(
    primary = DarkJitouColors.accent,
    onPrimary = DarkJitouColors.background,
    primaryContainer = DarkJitouColors.accentStrong,
    onPrimaryContainer = DarkJitouColors.background,
    secondary = DarkJitouColors.sage,
    onSecondary = DarkJitouColors.ink,
    secondaryContainer = DarkJitouColors.clay,
    tertiary = DarkJitouColors.accentStrong,
    onTertiary = DarkJitouColors.background,
    tertiaryContainer = DarkJitouColors.surfaceMuted,
    background = DarkJitouColors.background,
    onBackground = DarkJitouColors.ink,
    surface = DarkJitouColors.surface,
    onSurface = DarkJitouColors.ink,
    surfaceVariant = DarkJitouColors.surfaceMuted,
    onSurfaceVariant = DarkJitouColors.mutedInk,
    outline = DarkJitouColors.line,
    error = DarkJitouColors.danger,
    onError = DarkJitouColors.background,
)

@Composable
fun JitouTheme(
    themeMode: JitouThemeMode = JitouThemeMode.default,
    darkTheme: Boolean = themeMode.shouldUseDarkTheme(isSystemInDarkTheme()),
    content: @Composable () -> Unit,
) {
    val jitouColors = if (darkTheme) DarkJitouColors else LightJitouColors

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = {
            CompositionLocalProvider(
                LocalJitouColors provides jitouColors,
                content = content,
            )
        },
    )
}
