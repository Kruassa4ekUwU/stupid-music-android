package com.stupidmusic.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Fallback palette (deep green like Spotify but Material You)
private val DarkScheme = darkColorScheme(
    primary = Color(0xFF1DB954),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF003919),
    onPrimaryContainer = Color(0xFF57FF8C),
    secondary = Color(0xFFB3B3B3),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFFB3B3B3),
    outline = Color(0xFF535353)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF1DB954),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8F5CC),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF535353),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF535353)
)

@Composable
fun StupidMusicTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val ctx = LocalContext.current
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else {
        if (dark) DarkScheme else LightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val win = (view.context as Activity).window
            win.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(win, view).isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(colorScheme = scheme, content = content)
}
