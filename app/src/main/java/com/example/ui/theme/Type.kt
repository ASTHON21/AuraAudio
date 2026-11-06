package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.R

val PlusJakartaSansFontFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold),
)

object AuraTypeTokens {
  val HeadlineXl = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = (-0.03).em,
  )

  val HeadlineXlMobile = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.025).em,
  )

  val HeadlineLg = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.02).em,
  )

  val HeadlineSm = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.015).em,
  )

  val BodyLg = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.01).em,
  )

  val BodyMd = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
  )

  val BodySm = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.005.em,
  )

  // Tabular numerals for timestamps and real-time scrub metrics
  val BodySmTabular = BodySm.copy(
    fontFeatureSettings = "tnum"
  )

  val LabelMd = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.04.em,
  )

  val LabelSm = TextStyle(
    fontFamily = PlusJakartaSansFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.08.em,
  )
}

val Typography = Typography(
  displayLarge = AuraTypeTokens.HeadlineXl,
  headlineLarge = AuraTypeTokens.HeadlineXlMobile,
  headlineMedium = AuraTypeTokens.HeadlineLg,
  headlineSmall = AuraTypeTokens.HeadlineSm,
  titleLarge = AuraTypeTokens.HeadlineLg,
  titleMedium = AuraTypeTokens.HeadlineSm,
  titleSmall = AuraTypeTokens.LabelMd,
  bodyLarge = AuraTypeTokens.BodyLg,
  bodyMedium = AuraTypeTokens.BodyMd,
  bodySmall = AuraTypeTokens.BodySm,
  labelLarge = AuraTypeTokens.LabelMd,
  labelMedium = AuraTypeTokens.LabelMd,
  labelSmall = AuraTypeTokens.LabelSm,
)
