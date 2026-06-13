package com.aivos.echovault.presentation.ui.theme

  import android.app.Activity
  import android.os.Build
  import androidx.compose.foundation.isSystemInDarkTheme
  import androidx.compose.material3.*
  import androidx.compose.runtime.Composable
  import androidx.compose.runtime.SideEffect
  import androidx.compose.ui.graphics.toArgb
  import androidx.compose.ui.platform.LocalView
  import androidx.core.view.WindowCompat

  private val DarkColorScheme = darkColorScheme(
      primary = EchoViolet,
      onPrimary = LightBackground,
      primaryContainer = EchoVioletDark,
      onPrimaryContainer = EchoVioletLight,
      secondary = EchoTeal,
      tertiary = EchoGreen,
      background = DarkBackground,
      surface = DarkSurface,
      surfaceVariant = DarkSurfaceVariant,
      outline = DarkOutline,
      error = EchoRed
  )

  private val LightColorScheme = lightColorScheme(
      primary = EchoViolet,
      onPrimary = LightBackground,
      primaryContainer = EchoVioletLight,
      onPrimaryContainer = EchoVioletDark,
      secondary = EchoTeal,
      tertiary = EchoGreen,
      background = LightBackground,
      surface = LightSurface,
      surfaceVariant = LightSurfaceVariant,
      outline = LightOutline,
      error = EchoRed
  )

  @Composable
  fun EchoVaultTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
      val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
      val view = LocalView.current
      if (!view.isInEditMode) {
          SideEffect {
              val window = (view.context as Activity).window
              window.statusBarColor = colorScheme.background.toArgb()
              WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
          }
      }
      MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }