package com.jitou.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jitou.app.ui.home.JitouHomeRoute
import com.jitou.app.ui.theme.JitouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JitouTheme {
                JitouHomeRoute()
            }
        }
    }
}
