package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AudioTrackEntity
import com.example.ui.PlayerUiState
import com.example.ui.components.AuraAcousticWaveCanvas
import com.example.ui.components.AuraFloatingFeatureGuideCard
import com.example.ui.components.AuraHairlineScrubber
import com.example.ui.components.AuraMetadataBadge
import com.example.ui.components.AuraTransportControls
import com.example.ui.components.LocalCoverArtwork
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SanctuaryPlayerScreen(
  uiState: PlayerUiState,
  currentTrack: AudioTrackEntity?,
  trackCount: Int,
  onImportLocalAudio: () -> Unit,
  onOpenSynthesizeSheet: () -> Unit,
  onOpenTelemetrySheet: () -> Unit,
  onDismissFeatureGuide: () -> Unit,
  onToggleFavorite: (AudioTrackEntity) -> Unit,
  onOpenTrackMenu: (AudioTrackEntity) -> Unit,
  onSeekToFraction: (Float) -> Unit,
  onToggleShuffle: () -> Unit,
  onSkipPrevious: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onSkipNext: () -> Unit,
  onCycleRepeat: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .testTag("sanctuary_player_screen"),
    contentAlignment = Alignment.TopCenter
  ) {
    val isTablet = maxWidth >= 600.dp
    val horizontalMargin = if (isTablet) 32.dp else 20.dp
    val spaceXl = if (maxHeight >= 740.dp) 36.dp else 24.dp

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 560.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = horizontalMargin, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "AURA SANCTUARY",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.OnSurfaceVariant
          )
          Text(
            text = if (currentTrack != null) {
              if (uiState.isPlaying) "Signal Active" else "Signal Paused"
            } else {
              "Standby Mode"
            },
            style = AuraTypeTokens.LabelMd,
            color = if (uiState.isPlaying) AuraColors.DawnAzure else AuraColors.AuroraSilver
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          AuraMetadataBadge(
            text = if (currentTrack != null) currentTrack.qualityBadge else "24-BIT READY",
            isHighlight = currentTrack?.isLossless ?: true,
            onClick = onOpenTelemetrySheet,
            modifier = Modifier.testTag("header_signal_badge")
          )

          IconButton(
            onClick = onImportLocalAudio,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(AuraColors.GlassLevel1Surface)
              .border(1.dp, AuraColors.HairlineBorder, CircleShape)
              .testTag("top_import_audio_button")
          ) {
            Icon(
              imageVector = Icons.Filled.AudioFile,
              contentDescription = stringResource(R.string.import_local_audio),
              tint = AuraColors.AuroraSilver,
              modifier = Modifier.size(18.dp)
            )
          }

          if (currentTrack != null) {
            IconButton(
              onClick = { onOpenTrackMenu(currentTrack) },
              modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AuraColors.GlassLevel1Surface)
                .border(1.dp, AuraColors.HairlineBorder, CircleShape)
                .testTag("top_track_options_button")
            ) {
              Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = stringResource(R.string.track_options),
                tint = AuraColors.AuroraSilver,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (uiState.showFeatureGuideCard) {
        AuraFloatingFeatureGuideCard(
          title = stringResource(R.string.feature_guide_title),
          subtitle = stringResource(R.string.feature_guide_subtitle),
          onDismiss = onDismissFeatureGuide
        )
        Spacer(modifier = Modifier.height(20.dp))
      }

      val artworkShape = RoundedCornerShape(24.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 360.dp)
          .aspectRatio(1f)
          .clip(artworkShape)
          .background(
            brush = Brush.linearGradient(
              colors = listOf(
                Color(0xFFF4F5F7).copy(alpha = 0.05f),
                Color(0xFF0D0E12).copy(alpha = 0.85f)
              )
            )
          )
          .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = artworkShape)
          .testTag("album_artwork_canvas"),
        contentAlignment = Alignment.Center
      ) {
        if (currentTrack != null && !currentTrack.coverUriString.isNullOrBlank()) {
          LocalCoverArtwork(
            coverUriString = currentTrack.coverUriString,
            contentDescription = currentTrack.title,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(84.dp)
              .align(Alignment.BottomCenter)
              .background(
                Brush.verticalGradient(
                  colors = listOf(Color.Transparent, AuraColors.BaseVoid.copy(alpha = 0.82f))
                )
              )
              .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.BottomCenter
          ) {
            AuraAcousticWaveCanvas(
              isPlaying = uiState.isPlaying,
              dominantColorHex = currentTrack.dominantColorHex,
              modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
            )
          }
        } else if (currentTrack != null) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              AuraMetadataBadge(
                text = currentTrack.qualityBadge,
                isHighlight = currentTrack.isLossless
              )
              Text(
                text = "${currentTrack.sampleRateHz} HZ",
                style = AuraTypeTokens.LabelSm,
                color = AuraColors.OnSurfaceVariant
              )
            }

            AuraAcousticWaveCanvas(
              isPlaying = uiState.isPlaying,
              dominantColorHex = currentTrack.dominantColorHex,
              modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
            )

            Text(
              text = currentTrack.technicalSpecLabel,
              style = AuraTypeTokens.LabelSm,
              color = AuraColors.OnSurfaceVariant
            )
          }
        } else {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            AuraAcousticWaveCanvas(
              isPlaying = false,
              dominantColorHex = 0xFF4F75FFL,
              modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
              text = "ENGAGE LOCAL AUDIO SOURCE",
              style = AuraTypeTokens.LabelSm,
              color = AuraColors.WarmApricot
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Load audio files from device storage or synthesize a lossless 16-bit / 44.1kHz reference WAV.",
              style = AuraTypeTokens.BodySm,
              color = AuraColors.OnSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CanvasSourceActionPill(
                icon = Icons.Filled.AudioFile,
                label = stringResource(R.string.import_local_audio),
                accentColor = AuraColors.DawnAzure,
                onClick = onImportLocalAudio,
                testTag = "canvas_import_local_button"
              )
              CanvasSourceActionPill(
                icon = Icons.Filled.Tune,
                label = stringResource(R.string.synthesize_wav),
                accentColor = AuraColors.WarmApricot,
                onClick = onOpenSynthesizeSheet,
                testTag = "canvas_synthesize_wav_button"
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(spaceXl))

      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          AuraMetadataBadge(
            text = currentTrack?.qualityBadge ?: "SIGNAL STANDBY",
            isHighlight = currentTrack?.isLossless ?: false,
            onClick = onOpenTelemetrySheet
          )
          AuraMetadataBadge(
            text = currentTrack?.technicalSpecLabel ?: "OFFLINE PCM / FLAC / WAV",
            isHighlight = false,
            onClick = onOpenTelemetrySheet
          )
          if (uiState.playbackSpeed != 1.0f) {
            AuraMetadataBadge(
              text = "${uiState.playbackSpeed}X CLOCK",
              isHighlight = true,
              onClick = onOpenTelemetrySheet
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = currentTrack?.title ?: stringResource(R.string.awaiting_audio_signal),
              style = if (isTablet) AuraTypeTokens.HeadlineXl else AuraTypeTokens.HeadlineXlMobile,
              color = AuraColors.AuroraSilver,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("now_playing_title")
            )
            Text(
              text = if (currentTrack != null) {
                "${currentTrack.artist} • ${currentTrack.album}"
              } else {
                stringResource(R.string.awaiting_audio_subtitle)
              },
              style = AuraTypeTokens.BodyLg,
              color = AuraColors.OnSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.testTag("now_playing_artist")
            )
          }

          if (currentTrack != null) {
            IconButton(
              onClick = { onToggleFavorite(currentTrack) },
              modifier = Modifier
                .size(48.dp)
                .testTag("favorite_toggle_button")
            ) {
              Icon(
                imageVector = if (currentTrack.isFavorite) {
                  Icons.Filled.Favorite
                } else {
                  Icons.Filled.FavoriteBorder
                },
                contentDescription = "Toggle Favorite",
                tint = if (currentTrack.isFavorite) {
                  AuraColors.WarmApricot
                } else {
                  AuraColors.AuroraSilver.copy(alpha = 0.50f)
                }
              )
            }
          } else {
            IconButton(
              onClick = onOpenTelemetrySheet,
              modifier = Modifier
                .size(48.dp)
                .testTag("telemetry_info_button")
            ) {
              Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = stringResource(R.string.signal_telemetry),
                tint = AuraColors.DawnAzure
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      AuraHairlineScrubber(
        currentPositionMs = uiState.currentPositionMs,
        durationMs = uiState.durationMs,
        bufferedFraction = uiState.bufferedFraction,
        onSeekToFraction = onSeekToFraction
      )

      Spacer(modifier = Modifier.height(20.dp))

      AuraTransportControls(
        isPlaying = uiState.isPlaying,
        isShuffleEnabled = uiState.isShuffleEnabled,
        repeatMode = uiState.repeatMode,
        hasTracks = trackCount > 0,
        onToggleShuffle = onToggleShuffle,
        onSkipPrevious = onSkipPrevious,
        onTogglePlayPause = {
          if (currentTrack != null) {
            onTogglePlayPause()
          } else {
            onImportLocalAudio()
          }
        },
        onSkipNext = onSkipNext,
        onCycleRepeat = onCycleRepeat
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun CanvasSourceActionPill(
  icon: ImageVector,
  label: String,
  accentColor: Color,
  onClick: () -> Unit,
  testTag: String,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp)
      .clip(CircleShape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = CircleShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = accentColor,
      modifier = Modifier.size(18.dp)
    )
    Text(
      text = label,
      style = AuraTypeTokens.LabelMd,
      color = AuraColors.AuroraSilver,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}
