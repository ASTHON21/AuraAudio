package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val AuraColorScheme = darkColorScheme(
  primary = AuraColors.DawnAzure,
  onPrimary = AuraColors.OnPrimary,
  primaryContainer = AuraColors.PrimaryContainer,
  onPrimaryContainer = AuraColors.OnPrimaryContainer,
  inversePrimary = AuraColors.InversePrimary,
  secondary = AuraColors.AuroraSilver,
  onSecondary = AuraColors.OnSecondary,
  secondaryContainer = AuraColors.SecondaryContainer,
  onSecondaryContainer = AuraColors.OnSecondaryContainer,
  tertiary = AuraColors.WarmApricot,
  onTertiary = AuraColors.OnTertiary,
  tertiaryContainer = AuraColors.TertiaryContainer,
  onTertiaryContainer = AuraColors.OnTertiaryContainer,
  background = AuraColors.BaseVoid,
  onBackground = AuraColors.OnSurface,
  surface = AuraColors.Surface,
  onSurface = AuraColors.OnSurface,
  surfaceVariant = AuraColors.SurfaceVariant,
  onSurfaceVariant = AuraColors.OnSurfaceVariant,
  surfaceTint = AuraColors.SurfaceTint,
  inverseSurface = AuraColors.InverseSurface,
  inverseOnSurface = AuraColors.InverseOnSurface,
  error = AuraColors.Error,
  onError = AuraColors.OnError,
  errorContainer = AuraColors.ErrorContainer,
  onErrorContainer = AuraColors.OnErrorContainer,
  outline = AuraColors.Outline,
  outlineVariant = AuraColors.OutlineVariant,
  surfaceBright = AuraColors.SurfaceBright,
  surfaceContainer = AuraColors.SurfaceContainer,
  surfaceContainerHigh = AuraColors.SurfaceContainerHigh,
  surfaceContainerHighest = AuraColors.SurfaceContainerHighest,
  surfaceContainerLow = AuraColors.SurfaceContainerLow,
  surfaceContainerLowest = AuraColors.SurfaceContainerLowest,
  surfaceDim = AuraColors.SurfaceDim,
)

val AuraShapes = Shapes(
  extraSmall = RoundedCornerShape(4.dp), // sm: 0.25rem
  small = RoundedCornerShape(8.dp),      // DEFAULT: 0.5rem
  medium = RoundedCornerShape(12.dp),    // md: 0.75rem
  large = RoundedCornerShape(16.dp),     // lg: 1rem
  extraLarge = RoundedCornerShape(24.dp) // xl: 1.5rem
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = AuraColorScheme,
    typography = Typography,
    shapes = AuraShapes,
    content = content,
  )
}
