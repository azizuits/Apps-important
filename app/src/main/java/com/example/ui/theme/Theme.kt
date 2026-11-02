package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimaryLight,
    onPrimary = Color(0xFF003830),
    primaryContainer = TealPrimaryDark,
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = AmberSecondaryLight,
    onSecondary = Color(0xFF452B00),
    secondaryContainer = Color(0xFF784D00),
    onSecondaryContainer = Color(0xFFFFDF9E),
    tertiary = Color(0xFF38BDF8),
    background = SlateBackgroundDark,
    onBackground = SlateTextDark,
    surface = SlateSurfaceDark,
    onSurface = SlateTextDark,
    surfaceVariant = SlateSurfaceCardDark,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF0F766E),
    secondary = AmberSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = Color(0xFF0284C7),
    background = SlateBackgroundLight,
    onBackground = SlateTextLight,
    surface = SlateSurfaceLight,
    onSurface = SlateTextLight,
    surfaceVariant = SlateSurfaceCardLight,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = Color(0xFFCBD5E1)
)

@Composable
fun YearVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent vault brand feel
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
