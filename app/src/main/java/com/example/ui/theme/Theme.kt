package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldLight,
    onPrimaryContainer = EmeraldDeep,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = SkyTurquoise,
    onTertiary = Color.White,
    tertiaryContainer = SkySoft,
    onTertiaryContainer = Color(0xFF0C4A6E),
    background = CreamBackground,
    onBackground = TextPrimaryDark,
    surface = WarmSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryDark,
    error = CoralPlayful,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399),
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,
    secondary = GoldBright,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = GoldSoft,
    tertiary = Color(0xFF38BDF8),
    onTertiary = Color(0xFF082F49),
    background = Color(0xFF071E18),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0D2E26),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF134E4A),
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
