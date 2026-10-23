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
    primary = HydraPrimary,
    secondary = HydraSecondary,
    tertiary = HydraTertiary,
    background = HydraBg,
    surface = HydraCard,
    onBackground = HydraOnSurface,
    onSurface = HydraOnSurface,
    surfaceVariant = HydraSurface,
    onSurfaceVariant = HydraTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = HydraPrimary,
    secondary = HydraSecondary,
    tertiary = HydraTertiary,
    background = Color.White,
    surface = Color(0xFFF1F5F9),
    onBackground = HydraBg,
    onSurface = HydraBg,
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = HydraSecondary
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
