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

private val DarkColorScheme =
  darkColorScheme(
    primary = WaAccentGreen,
    secondary = WaTealGreenLight,
    background = WaDarkBg,
    surface = WaDarkSurface,
    onPrimary = WaDarkBg,
    onSecondary = WaDarkTextPrimary,
    onBackground = WaDarkTextPrimary,
    onSurface = WaDarkTextPrimary,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WaTealGreen,
    secondary = WaTealGreenLight,
    background = WaLightBg,
    surface = WaLightSurface,
    onPrimary = Color.White,
    onSecondary = WaLightTextPrimary,
    onBackground = WaLightTextPrimary,
    onSurface = WaLightTextPrimary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to enforce WhatsApp green branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
