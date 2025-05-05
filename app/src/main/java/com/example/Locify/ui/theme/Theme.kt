package com.example.Locify.ui.theme

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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PurplePrimary,
    onPrimary = White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = PurpleSecondary,
    onSecondary = Black,
    secondaryContainer = PurpleLight,
    onSecondaryContainer = PurpleDark,
    tertiary = Success,
    onTertiary = Black,
    background = Black800,
    onBackground = White,
    surface = Black700,
    onSurface = White,
    surfaceVariant = Black600,
    onSurfaceVariant = Gray300,
    error = Error,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = PurpleDark,
    secondary = PurpleSecondary,
    onSecondary = White,
    secondaryContainer = PurpleLight,
    onSecondaryContainer = PurpleDark,
    tertiary = Success,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = Gray100,
    onSurface = Black,
    surfaceVariant = Gray200,
    onSurfaceVariant = Gray700,
    error = Error,
    onError = White
)

@Composable
fun LocifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}