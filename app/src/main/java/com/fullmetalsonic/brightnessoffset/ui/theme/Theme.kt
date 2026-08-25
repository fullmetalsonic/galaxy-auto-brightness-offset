package com.fullmetalsonic.brightnessoffset.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Navy800,
    onPrimary = Cloud50,
    primaryContainer = Navy100,
    onPrimaryContainer = Navy950,
    secondary = Warning,
    onSecondary = Cloud50,
    secondaryContainer = ColorTokens.AmberContainer,
    onSecondaryContainer = Navy950,
    tertiary = Success,
    background = Cloud50,
    onBackground = Navy950,
    surface = ColorTokens.White,
    onSurface = Navy950,
    surfaceVariant = Cloud100,
    onSurfaceVariant = Slate700,
    error = Error,
)

private val DarkColors = darkColorScheme(
    primary = Amber300,
    onPrimary = Navy950,
    primaryContainer = Navy800,
    onPrimaryContainer = Navy100,
    secondary = Amber500,
    onSecondary = Navy950,
    secondaryContainer = ColorTokens.AmberDarkContainer,
    onSecondaryContainer = Amber300,
    tertiary = ColorTokens.SuccessLight,
    background = Navy950,
    onBackground = Cloud100,
    surface = Navy900,
    onSurface = Cloud100,
    surfaceVariant = Navy800,
    onSurfaceVariant = Navy100,
    error = ColorTokens.ErrorLight,
)

private object ColorTokens {
    val White = androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val AmberContainer = androidx.compose.ui.graphics.Color(0xFFFFE5B5)
    val AmberDarkContainer = androidx.compose.ui.graphics.Color(0xFF5D4200)
    val SuccessLight = androidx.compose.ui.graphics.Color(0xFF68DAB2)
    val ErrorLight = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
}

@Composable
fun BrightnessOffsetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
