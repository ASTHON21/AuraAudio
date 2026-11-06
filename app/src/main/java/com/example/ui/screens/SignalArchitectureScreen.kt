package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AudioTrackEntity
import com.example.ui.PlayerUiState
import com.example.ui.components.AuraMetadataBadge
import com.example.ui.components.AuraTrackRowItem
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens
import java.util.Locale

@Composable
fun SignalArchitectureScreen(
  uiState: PlayerUiState,
  currentTrack: AudioTrackEntity?,
  allTracks: List<AudioTrackEntity>,
  onPlaybackSpeedChange: (Float) -> Unit,
  onOutputGainChange: (Float) -> Unit,
  onSelectTrack: (AudioTrackEntity) -> Unit,
  onOpenTrackMenu: (AudioTrackEntity) -> Unit,
  onOpenSynthesizeSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .testTag("signal_architecture_screen"),
    contentAlignment = Alignment.TopCenter
  ) {
    val isTablet = maxWidth >= 600.dp
    val horizontalMargin = if (isTablet) 32.dp else 20.dp
    val cardShape = RoundedCornerShape(24.dp)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 640.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = horizontalMargin, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "HARDWARE & DSP STAGE",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.DawnAzure
          )
          Text(
            text = "Signal & Acoustic Lab",
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
        }
        AuraMetadataBadge(
          text = "SYNTHESIZE WAV",
          isHighlight = true,
          onClick = onOpenSynthesizeSheet,
          modifier = Modifier
            .height(36.dp)
            .testTag("signal_synthesize_button")
        )
      }

      // Live Hardware DAC & Buffer Card
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(cardShape)
          .background(AuraColors.GlassLevel1Surface)
          .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = cardShape)
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "OUTPUT SIGNAL CHAIN",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.WarmApricot
          )
          AuraMetadataBadge(
            text = "${uiState.hardwareTelemetry.nativeSampleRateHz} HZ NATIVE",
            isHighlight = true
          )
        }

        Text(
          text = uiState.hardwareTelemetry.outputDeviceName,
          style = AuraTypeTokens.HeadlineSm,
          color = AuraColors.AuroraSilver
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          TelemetryStatPill(
            label = "SAMPLE RATE",
            value = "${uiState.hardwareTelemetry.nativeSampleRateHz} Hz",
            modifier = Modifier.weight(1f)
          )
          TelemetryStatPill(
            label = "BUFFER SIZE",
            value = "${uiState.hardwareTelemetry.framesPerBuffer} f/buf",
            modifier = Modifier.weight(1f)
          )
          TelemetryStatPill(
            label = "SOURCE BITDEPTH",
            value = "${currentTrack?.bitDepth ?: 16}-bit",
            modifier = Modifier.weight(1f)
          )
        }
      }

      // Transport Clock & Attenuation Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(cardShape)
          .background(AuraColors.GlassLevel1Surface)
          .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = cardShape)
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "PLAYBACK CLOCK RATE",
              style = AuraTypeTokens.LabelSm,
              color = AuraColors.OnSurfaceVariant
            )
            Text(
              text = String.format(Locale.US, "%.2fx Speed", uiState.playbackSpeed),
              style = AuraTypeTokens.BodySmTabular,
              color = AuraColors.AuroraSilver
            )
          }
          if (uiState.playbackSpeed != 1.0f) {
            Text(
              text = "RESET 1.00X",
              style = AuraTypeTokens.LabelSm,
              color = AuraColors.DawnAzure,
              modifier = Modifier
                .clip(CircleShape)
                .background(AuraColors.PillInputFill)
                .clickable { onPlaybackSpeedChange(1.0f) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("reset_speed_button")
            )
          }
        }

        Slider(
          value = uiState.playbackSpeed,
          onValueChange = onPlaybackSpeedChange,
          valueRange = 0.5f..2.0f,
          colors = SliderDefaults.colors(
            thumbColor = AuraColors.AuroraSilver,
            activeTrackColor = AuraColors.DawnAzure,
            inactiveTrackColor = AuraColors.SeekTrackInactive
          ),
          modifier = Modifier.testTag("playback_speed_slider")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DIGITAL OUTPUT GAIN",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.OnSurfaceVariant
          )
          Text(
            text = String.format(Locale.US, "%d%%", (uiState.outputGain * 100).toInt()),
            style = AuraTypeTokens.BodySmTabular,
            color = AuraColors.AuroraSilver
          )
        }

        Slider(
          value = uiState.outputGain,
          onValueChange = onOutputGainChange,
          valueRange = 0.1f..1.0f,
          colors = SliderDefaults.colors(
            thumbColor = AuraColors.AuroraSilver,
            activeTrackColor = AuraColors.WarmApricot,
            inactiveTrackColor = AuraColors.SeekTrackInactive
          ),
          modifier = Modifier.testTag("output_gain_slider")
        )
      }

      // Active Queue Section
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "ACTIVE PLAYBACK QUEUE (${allTracks.size})",
          style = AuraTypeTokens.LabelSm,
          color = AuraColors.OnSurfaceVariant
        )

        if (allTracks.isEmpty()) {
          Text(
            text = "Queue is currently empty. Import local audio files, connect a stream URL, or synthesize a custom WAV reference tone.",
            style = AuraTypeTokens.BodySm,
            color = AuraColors.OnSurfaceVariant
          )
        } else {
          allTracks.forEachIndexed { index, track ->
            AuraTrackRowItem(
              index = index,
              track = track,
              isCurrentTrack = track.id == currentTrack?.id,
              isPlaying = uiState.isPlaying && track.id == currentTrack?.id,
              onSelectTrack = { onSelectTrack(track) },
              onOpenMenu = { onOpenTrackMenu(track) }
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun TelemetryStatPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(14.dp)
  Column(
    modifier = modifier
      .clip(shape)
      .background(AuraColors.PillInputFill)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Text(
      text = label,
      style = AuraTypeTokens.LabelSm,
      color = AuraColors.OnSurfaceVariant
    )
    Text(
      text = value,
      style = AuraTypeTokens.BodySmTabular,
      color = AuraColors.AuroraSilver
    )
  }
}
