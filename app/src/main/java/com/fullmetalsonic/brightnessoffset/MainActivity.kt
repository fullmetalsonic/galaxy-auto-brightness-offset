package com.fullmetalsonic.brightnessoffset

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fullmetalsonic.brightnessoffset.ui.BrightnessApp
import com.fullmetalsonic.brightnessoffset.ui.theme.BrightnessOffsetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrightnessOffsetTheme(darkTheme = true) {
                BrightnessApp()
            }
        }
    }
}
