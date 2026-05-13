package com.jitou.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class JitouColorsTest {
    @Test
    fun lightPaletteUsesWarmNeutralLuxuryTokens() {
        assertEquals(Color(0xFFF7F4EE), LightJitouColors.background)
        assertEquals(Color(0xFFFFFEFB), LightJitouColors.surface)
        assertEquals(Color(0xFFEEE7DA), LightJitouColors.surfaceMuted)
        assertEquals(Color(0xFF1C1A17), LightJitouColors.ink)
        assertEquals(Color(0xFF706A60), LightJitouColors.mutedInk)
        assertEquals(Color(0xFFD8B45A), LightJitouColors.accent)
        assertEquals(Color(0xFF8A651F), LightJitouColors.accentStrong)
        assertEquals(Color(0xFFDCE7DE), LightJitouColors.sage)
        assertEquals(Color(0xFFE8CDBD), LightJitouColors.clay)
        assertEquals(Color(0x1A1C1A17), LightJitouColors.line)
        assertEquals(Color(0xFF9D342E), LightJitouColors.danger)
    }

    @Test
    fun darkPaletteKeepsTheSameSemanticRoles() {
        assertEquals(Color(0xFF15120F), DarkJitouColors.background)
        assertEquals(Color(0xFF1D1915), DarkJitouColors.surface)
        assertEquals(Color(0xFF332D26), DarkJitouColors.surfaceMuted)
        assertEquals(Color(0xFFF1E8DC), DarkJitouColors.ink)
        assertEquals(Color(0xFFB8AA9A), DarkJitouColors.mutedInk)
        assertEquals(Color(0xFFD7B35A), DarkJitouColors.accent)
        assertEquals(Color(0xFFF0D889), DarkJitouColors.accentStrong)
        assertEquals(Color(0xFF26372F), DarkJitouColors.sage)
        assertEquals(Color(0xFF4A3028), DarkJitouColors.clay)
        assertEquals(Color(0x1FFFFFFF), DarkJitouColors.line)
        assertEquals(Color(0xFFFFB4A9), DarkJitouColors.danger)
    }

    @Test
    fun themeModeDefaultsToLightAndResolvesSystemOnlyWhenRequested() {
        assertEquals(JitouThemeMode.Light, JitouThemeMode.default)
        assertEquals(false, JitouThemeMode.Light.shouldUseDarkTheme(systemDarkTheme = true))
        assertEquals(true, JitouThemeMode.Dark.shouldUseDarkTheme(systemDarkTheme = false))
        assertEquals(false, JitouThemeMode.System.shouldUseDarkTheme(systemDarkTheme = false))
        assertEquals(true, JitouThemeMode.System.shouldUseDarkTheme(systemDarkTheme = true))
    }

    @Test
    fun themeModeParsesStoredPreferenceWithLightFallback() {
        assertEquals(JitouThemeMode.Light, JitouThemeMode.fromStorageValue(null))
        assertEquals(JitouThemeMode.Light, JitouThemeMode.fromStorageValue(""))
        assertEquals(JitouThemeMode.Light, JitouThemeMode.fromStorageValue("light"))
        assertEquals(JitouThemeMode.Dark, JitouThemeMode.fromStorageValue("dark"))
        assertEquals(JitouThemeMode.System, JitouThemeMode.fromStorageValue("system"))
        assertEquals(JitouThemeMode.Light, JitouThemeMode.fromStorageValue("unexpected"))
    }
}
