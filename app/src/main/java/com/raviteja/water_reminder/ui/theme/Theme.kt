package com.raviteja.water_reminder.ui.theme

import android.app.Activity
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
    primary = WarmCoral,
    onPrimary = TextCream,
    primaryContainer = WarmCoralSubtle,
    onPrimaryContainer = WarmCoralLight,
    secondary = SageGreenLight,
    onSecondary = TextCream,
    secondaryContainer = SageGreen,
    onSecondaryContainer = TextCream,
    tertiary = EditorialNavyLight,
    onTertiary = TextCream,
    tertiaryContainer = EditorialNavy,
    onTertiaryContainer = EditorialNavyLight,
    background = CharcoalBg,
    onBackground = TextCream,
    surface = CharcoalSurface,
    onSurface = TextCream,
    surfaceVariant = CharcoalSurfaceElevated,
    onSurfaceVariant = TextMuted,
    outline = CharcoalBorder,
    outlineVariant = CharcoalBorderMedium
)

private val LightColorScheme = lightColorScheme(
    primary = WarmCoral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE8),
    onPrimaryContainer = WarmCoralMuted,
    secondary = SageGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2ECE5),
    onSecondaryContainer = SageGreen,
    tertiary = EditorialNavy,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5EDF5),
    onTertiaryContainer = EditorialNavy,
    background = PaperLightBg,
    onBackground = TextDark,
    surface = PaperLightSurface,
    onSurface = TextDark,
    surfaceVariant = PaperLightSurfaceElevated,
    onSurfaceVariant = TextDarkMuted,
    outline = PaperLightBorder,
    outlineVariant = Color(0xFFCCC7BD)
)

@Composable
fun Water_reminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
