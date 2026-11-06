package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Aura Minimalist Audio Design System Palette
object AuraColors {
  // Base Neutral & Surface Elevation Levels
  val BaseVoid = Color(0xFF0D0E12)
  val Surface = Color(0xFF121317)
  val SurfaceDim = Color(0xFF121317)
  val SurfaceBright = Color(0xFF38393D)
  val SurfaceContainerLowest = Color(0xFF0D0E12)
  val SurfaceContainerLow = Color(0xFF1A1B20)
  val SurfaceContainer = Color(0xFF1F1F24)
  val SurfaceContainerHigh = Color(0xFF292A2E)
  val SurfaceContainerHighest = Color(0xFF343439)
  val SurfaceVariant = Color(0xFF343439)

  // Typography & Structural Anchors
  val AuroraSilver = Color(0xFFF4F5F7)
  val OnSurface = Color(0xFFE3E2E7)
  val OnSurfaceVariant = Color(0xFFC4C5D7)
  val InverseSurface = Color(0xFFE3E2E7)
  val InverseOnSurface = Color(0xFF2F3035)
  val Outline = Color(0xFF8E90A0)
  val OutlineVariant = Color(0xFF434655)

  // Primary - Dawn Azure
  val DawnAzure = Color(0xFF4F75FF)
  val Primary = Color(0xFFB7C4FF)
  val OnPrimary = Color(0xFF002583)
  val PrimaryContainer = Color(0xFF6A89FF)
  val OnPrimaryContainer = Color(0xFF002074)
  val InversePrimary = Color(0xFF2350DB)
  val SurfaceTint = Color(0xFFB7C4FF)

  // Secondary - Cold Silver
  val Secondary = Color(0xFFC5C6C8)
  val OnSecondary = Color(0xFF2E3132)
  val SecondaryContainer = Color(0xFF444749)
  val OnSecondaryContainer = Color(0xFFB4B5B7)

  // Tertiary - Warm Apricot (Lossless / High-Res Accents)
  val WarmApricot = Color(0xFFFFA877)
  val Tertiary = Color(0xFFFFB68E)
  val OnTertiary = Color(0xFF542200)
  val TertiaryContainer = Color(0xFFCC7D50)
  val OnTertiaryContainer = Color(0xFF491D00)

  // Error
  val Error = Color(0xFFFFB4AB)
  val OnError = Color(0xFF690005)
  val ErrorContainer = Color(0xFF93000A)
  val OnErrorContainer = Color(0xFFFFDAD6)

  // Glassmorphism & Optical Layers
  val GlassLevel1Surface = Color(0xFFF4F5F7).copy(alpha = 0.04f)
  val GlassLevel1Hover = Color(0xFFFFFFFF).copy(alpha = 0.02f)
  val GlassLevel1Active = Color(0xFFF4F5F7).copy(alpha = 0.07f)
  val HairlineBorder = Color(0xFFF4F5F7).copy(alpha = 0.08f)
  val ModalSheetSurface = Color(0xFF0D0E12).copy(alpha = 0.90f)
  val ModalRimLight = Color(0xFFF4F5F7).copy(alpha = 0.15f)
  val DawnAzureGlow = Color(0xFF4F75FF).copy(alpha = 0.45f)
  val SeekTrackInactive = Color(0xFFF4F5F7).copy(alpha = 0.12f)
  val SeekTrackBuffered = Color(0xFFF4F5F7).copy(alpha = 0.25f)
  val PillInputFill = Color(0xFFF4F5F7).copy(alpha = 0.05f)
  val PillInputActiveBorder = Color(0xFF4F75FF).copy(alpha = 0.40f)
  val NeutralBadgeFill = Color(0xFFF4F5F7).copy(alpha = 0.05f)
  val NeutralBadgeBorder = Color(0xFFF4F5F7).copy(alpha = 0.10f)
}
