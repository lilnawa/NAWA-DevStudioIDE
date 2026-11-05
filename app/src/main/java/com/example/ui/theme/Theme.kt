package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NawaDarkColorScheme = darkColorScheme(
    primary = NeonCrimson,
    onPrimary = Color.White,
    primaryContainer = RedDark,
    onPrimaryContainer = Color.White,
    secondary = NeonRedAccent,
    onSecondary = Color.White,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = Charcoal,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderColor,
    error = Color(0xFFFF453A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Red & Black dark theme for DevStudio
    // Dynamic color is disabled by default to maintain signature Red & Black theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = NawaDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.hashCode()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
