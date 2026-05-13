package com.jitou.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.jitou.app.notifications.HaircutNotificationScheduler
import com.jitou.app.ui.home.JitouHomeRoute
import com.jitou.app.ui.theme.JitouTheme
import com.jitou.app.ui.theme.JitouThemeMode

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        HaircutNotificationScheduler.scheduleNext(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        HaircutNotificationScheduler.ensureChannel(this)
        requestNotificationPermissionIfNeeded()
        HaircutNotificationScheduler.scheduleNext(this)

        setContent {
            var themeMode by remember { mutableStateOf(loadThemeMode()) }

            JitouTheme(themeMode = themeMode) {
                JitouHomeRoute(
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        saveThemeMode(mode)
                    },
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permissionState = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        )
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun loadThemeMode(): JitouThemeMode =
        JitouThemeMode.fromStorageValue(
            getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(THEME_MODE_KEY, null),
        )

    private fun saveThemeMode(mode: JitouThemeMode) {
        getSharedPreferences(THEME_PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(THEME_MODE_KEY, mode.storageValue)
            .apply()
    }

    private companion object {
        const val THEME_PREFS_NAME = "jitou_theme"
        const val THEME_MODE_KEY = "theme_mode"
    }
}
