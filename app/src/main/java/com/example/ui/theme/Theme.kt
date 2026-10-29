package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AuraPrimary,
    onPrimary = AuraOnPrimary,
    primaryContainer = AuraPrimaryContainer,
    onPrimaryContainer = AuraOnPrimaryContainer,
    secondary = AuraSecondary,
    onSecondary = AuraOnSecondary,
    secondaryContainer = AuraSecondaryContainer,
    onSecondaryContainer = AuraOnSecondaryContainer,
    tertiary = AuraTertiary,
    onTertiary = AuraOnTertiary,
    tertiaryContainer = AuraTertiaryContainer,
    onTertiaryContainer = AuraOnTertiaryContainer,
    background = AuraDarkBackground,
    surface = AuraDarkSurface,
    surfaceVariant = AuraDarkSurfaceVariant,
    onBackground = AuraDarkOnSurface,
    onSurface = AuraDarkOnSurface,
    onSurfaceVariant = AuraDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = AuraPrimaryContainer,
    onPrimary = AuraOnPrimaryContainer,
    primaryContainer = AuraPrimary,
    onPrimaryContainer = AuraOnPrimary,
    secondary = AuraSecondaryContainer,
    onSecondary = AuraOnSecondaryContainer,
    secondaryContainer = AuraSecondary,
    onSecondaryContainer = AuraOnSecondary,
    tertiary = AuraTertiaryContainer,
    onTertiary = AuraOnTertiaryContainer,
    tertiaryContainer = AuraTertiary,
    onTertiaryContainer = AuraOnTertiary,
    background = AuraLightBackground,
    surface = AuraLightSurface,
    surfaceVariant = AuraLightSurfaceVariant,
    onBackground = AuraLightOnSurface,
    onSurface = AuraLightOnSurface,
    onSurfaceVariant = AuraLightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize our companion aesthetic
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
