package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AudioTrackEntity
import com.example.ui.HardwareAudioTelemetry
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStreamModalSheet(
  onDismiss: () -> Unit,
  onConnectStream: (url: String, title: String, artist: String, album: String, coverUrl: String?) -> Unit,
) {
  var streamUrl by remember { mutableStateOf("") }
  var title by remember { mutableStateOf("") }
  var artist by remember { mutableStateOf("") }
  var album by remember { mutableStateOf("") }
  var coverUrl by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AuraColors.ModalSheetSurface,
    scrimColor = Color.Black.copy(alpha = 0.68f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = { ModalSheetRimHandle() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("add_stream_modal_sheet"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "DIRECT NETWORK STREAM",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.DawnAzure
          )
          Text(
            text = "Connect Audio Stream",
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = AuraColors.OnSurfaceVariant
          )
        }
      }

      AuraSheetTextField(
        value = streamUrl,
        onValueChange = {
          streamUrl = it
          errorText = null
        },
        label = "Audio Stream URL (https://…)",
        testTag = "stream_url_input"
      )

      AuraSheetTextField(
        value = title,
        onValueChange = {
          title = it
          errorText = null
        },
        label = "Track Title",
        testTag = "stream_title_input"
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AuraSheetTextField(
          value = artist,
          onValueChange = { artist = it },
          label = "Artist",
          modifier = Modifier.weight(1f),
          testTag = "stream_artist_input"
        )
        AuraSheetTextField(
          value = album,
          onValueChange = { album = it },
          label = "Album / Source",
          modifier = Modifier.weight(1f),
          testTag = "stream_album_input"
        )
      }

      AuraSheetTextField(
        value = coverUrl,
        onValueChange = { coverUrl = it },
        label = "Artwork Image URL (Optional)",
        testTag = "stream_cover_input"
      )

      if (errorText != null) {
        Text(
          text = errorText!!,
          style = AuraTypeTokens.BodySm,
          color = AuraColors.Error
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      AuraPrimaryPillButton(
        text = "Engage Stream Signal",
        onClick = {
          val cleanUrl = streamUrl.trim()
          val cleanTitle = title.trim()
          if (cleanUrl.isEmpty() || (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://"))) {
            errorText = "Please enter a valid http:// or https:// audio stream URL."
          } else if (cleanTitle.isEmpty()) {
            errorText = "Please enter a title for the audio stream."
          } else {
            onConnectStream(
              cleanUrl,
              cleanTitle,
              artist.trim().ifEmpty { "Network Stream" },
              album.trim().ifEmpty { "Direct Stream" },
              coverUrl.trim().ifEmpty { null }
            )
          }
        },
        testTag = "submit_stream_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SynthesizeReferenceToneModalSheet(
  onDismiss: () -> Unit,
  onSynthesize: (title: String, artist: String, frequencyHz: Double, durationSec: Int) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var artist by remember { mutableStateOf("") }
  var frequencyText by remember { mutableStateOf("") }
  var durationText by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AuraColors.ModalSheetSurface,
    scrimColor = Color.Black.copy(alpha = 0.68f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = { ModalSheetRimHandle() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("synthesize_tone_modal_sheet"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "16-BIT / 44.1KHZ STEREO PCM GENERATOR",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.WarmApricot
          )
          Text(
            text = "Synthesize Acoustic WAV",
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = AuraColors.OnSurfaceVariant
          )
        }
      }

      Text(
        text = "Generate an uncompressed harmonic PCM WAV file directly onto local storage to calibrate and test the audio signal path.",
        style = AuraTypeTokens.BodySm,
        color = AuraColors.OnSurfaceVariant
      )

      AuraSheetTextField(
        value = title,
        onValueChange = {
          title = it
          errorText = null
        },
        label = "Track Title",
        testTag = "synth_title_input"
      )

      AuraSheetTextField(
        value = artist,
        onValueChange = {
          artist = it
          errorText = null
        },
        label = "Artist / Acoustic Label",
        testTag = "synth_artist_input"
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        AuraSheetTextField(
          value = frequencyText,
          onValueChange = {
            frequencyText = it
            errorText = null
          },
          label = "Frequency (Hz, 40–2000)",
          keyboardType = KeyboardType.Number,
          modifier = Modifier.weight(1f),
          testTag = "synth_frequency_input"
        )

        AuraSheetTextField(
          value = durationText,
          onValueChange = {
            durationText = it
            errorText = null
          },
          label = "Duration (Sec, 3–120)",
          keyboardType = KeyboardType.Number,
          modifier = Modifier.weight(1f),
          testTag = "synth_duration_input"
        )
      }

      if (errorText != null) {
        Text(
          text = errorText!!,
          style = AuraTypeTokens.BodySm,
          color = AuraColors.Error
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      AuraPrimaryPillButton(
        text = "Render Lossless WAV & Play",
        onClick = {
          val freq = frequencyText.trim().toDoubleOrNull()
          val dur = durationText.trim().toIntOrNull()
          val cleanTitle = title.trim()
          if (cleanTitle.isEmpty()) {
            errorText = "Please enter a title for your synthesized WAV track."
          } else if (freq == null || freq !in 40.0..2000.0) {
            errorText = "Enter a frequency between 40 Hz and 2000 Hz."
          } else if (dur == null || dur !in 3..120) {
            errorText = "Enter a duration between 3 and 120 seconds."
          } else {
            onSynthesize(
              cleanTitle,
              artist.trim().ifEmpty { "Aura Acoustic Lab" },
              freq,
              dur
            )
          }
        },
        testTag = "submit_synthesize_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalTelemetryModalSheet(
  track: AudioTrackEntity?,
  hardwareTelemetry: HardwareAudioTelemetry,
  playbackSpeed: Float,
  outputGain: Float,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AuraColors.ModalSheetSurface,
    scrimColor = Color.Black.copy(alpha = 0.68f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = { ModalSheetRimHandle() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("signal_telemetry_modal_sheet"),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "ACOUSTIC ARCHITECTURE",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.WarmApricot
          )
          Text(
            text = "Signal Path Telemetry",
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Close",
            tint = AuraColors.OnSurfaceVariant
          )
        }
      }

      TelemetryNodeCard(
        stageLabel = "STAGE 01 • SOURCE DECODER",
        title = track?.title ?: "No Active Signal",
        subtitle = if (track != null) {
          "${track.mimeType.uppercase(Locale.ROOT)} • ${track.technicalSpecLabel}"
        } else {
          "Awaiting local file or network stream input"
        },
        badgeText = track?.qualityBadge ?: "STANDBY",
        isHighlight = track?.isLossless == true
      )

      TelemetryNodeCard(
        stageLabel = "STAGE 02 • DSP & TRANSPORT ENGINE",
        title = "Android MediaPlayer Direct Pipeline",
        subtitle = String.format(
          Locale.US,
          "Clock Speed: %.2fx • Output Gain: %d%% • %d Channels",
          playbackSpeed,
          (outputGain * 100).toInt(),
          track?.channels ?: 2
        ),
        badgeText = "32-BIT FLOAT MIX",
        isHighlight = false
      )

      TelemetryNodeCard(
        stageLabel = "STAGE 03 • HARDWARE OUTPUT STAGE",
        title = hardwareTelemetry.outputDeviceName,
        subtitle = "${hardwareTelemetry.nativeSampleRateHz} Hz Native Clock • ${hardwareTelemetry.framesPerBuffer} Frames/Buffer",
        badgeText = "ACTIVE OUTPUT",
        isHighlight = true
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun TelemetryNodeCard(
  stageLabel: String,
  title: String,
  subtitle: String,
  badgeText: String,
  isHighlight: Boolean,
) {
  val shape = RoundedCornerShape(20.dp)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stageLabel,
        style = AuraTypeTokens.LabelSm,
        color = AuraColors.DawnAzure
      )
      AuraMetadataBadge(text = badgeText, isHighlight = isHighlight)
    }
    Text(
      text = title,
      style = AuraTypeTokens.HeadlineSm,
      color = AuraColors.AuroraSilver
    )
    Text(
      text = subtitle,
      style = AuraTypeTokens.BodySmTabular,
      color = AuraColors.OnSurfaceVariant
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackContextModalSheet(
  track: AudioTrackEntity,
  onDismiss: () -> Unit,
  onPlayNow: () -> Unit,
  onToggleFavorite: () -> Unit,
  onInspectSignal: () -> Unit,
  onEditMetadata: () -> Unit,
  onDeleteTrack: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AuraColors.ModalSheetSurface,
    scrimColor = Color.Black.copy(alpha = 0.68f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = { ModalSheetRimHandle() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("track_context_modal_sheet"),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AuraMetadataBadge(text = track.qualityBadge, isHighlight = track.isLossless)
            Text(
              text = track.technicalSpecLabel,
              style = AuraTypeTokens.LabelSm,
              color = AuraColors.OnSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = track.title,
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
          Text(
            text = "${track.artist} • ${track.album}",
            style = AuraTypeTokens.BodyMd,
            color = AuraColors.OnSurfaceVariant
          )
        }
      }

      SheetActionRow(
        icon = Icons.Filled.PlayArrow,
        label = "Play in Sanctuary",
        tint = AuraColors.DawnAzure,
        onClick = {
          onPlayNow()
          onDismiss()
        },
        testTag = "sheet_action_play"
      )

      SheetActionRow(
        icon = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        label = if (track.isFavorite) "Remove from Favorites" else "Mark as Favorite",
        tint = if (track.isFavorite) AuraColors.WarmApricot else AuraColors.AuroraSilver,
        onClick = {
          onToggleFavorite()
          onDismiss()
        },
        testTag = "sheet_action_favorite"
      )

      SheetActionRow(
        icon = Icons.Filled.GraphicEq,
        label = "Inspect Signal Path Telemetry",
        tint = AuraColors.AuroraSilver,
        onClick = {
          onDismiss()
          onInspectSignal()
        },
        testTag = "sheet_action_telemetry"
      )

      SheetActionRow(
        icon = Icons.Filled.Edit,
        label = "Edit Track Metadata & Artwork",
        tint = AuraColors.AuroraSilver,
        onClick = onEditMetadata,
        testTag = "sheet_action_edit"
      )

      SheetActionRow(
        icon = Icons.Filled.DeleteOutline,
        label = "Remove from Sanctuary",
        tint = AuraColors.Error,
        onClick = onDeleteTrack,
        testTag = "sheet_action_delete"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetadataModalSheet(
  track: AudioTrackEntity,
  onDismiss: () -> Unit,
  onSave: (title: String, artist: String, album: String, coverUri: String?) -> Unit,
) {
  var title by remember(track.id) { mutableStateOf(track.title) }
  var artist by remember(track.id) { mutableStateOf(track.artist) }
  var album by remember(track.id) { mutableStateOf(track.album) }
  var coverUri by remember(track.id) { mutableStateOf(track.coverUriString ?: "") }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AuraColors.ModalSheetSurface,
    scrimColor = Color.Black.copy(alpha = 0.68f),
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    dragHandle = { ModalSheetRimHandle() }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .testTag("edit_metadata_modal_sheet"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Text(
        text = "Edit Track Metadata",
        style = AuraTypeTokens.HeadlineLg,
        color = AuraColors.AuroraSilver
      )

      AuraSheetTextField(
        value = title,
        onValueChange = { title = it },
        label = "Track Title",
        testTag = "edit_title_input"
      )

      AuraSheetTextField(
        value = artist,
        onValueChange = { artist = it },
        label = "Artist",
        testTag = "edit_artist_input"
      )

      AuraSheetTextField(
        value = album,
        onValueChange = { album = it },
        label = "Album",
        testTag = "edit_album_input"
      )

      AuraSheetTextField(
        value = coverUri,
        onValueChange = { coverUri = it },
        label = "Cover Artwork URL (Optional)",
        testTag = "edit_cover_input"
      )

      AuraPrimaryPillButton(
        text = "Save Metadata",
        onClick = { onSave(title, artist, album, coverUri) },
        testTag = "save_metadata_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@Composable
private fun SheetActionRow(
  icon: ImageVector,
  label: String,
  tint: Color,
  onClick: () -> Unit,
  testTag: String,
) {
  val shape = RoundedCornerShape(14.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(20.dp)
    )
    Text(
      text = label,
      style = AuraTypeTokens.BodyLg,
      color = tint
    )
  }
}

@Composable
private fun ModalSheetRimHandle() {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top rim light highlight: 1px solid rgba(244, 245, 247, 0.15)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(AuraColors.ModalRimLight)
    )
    Spacer(modifier = Modifier.height(12.dp))
    Box(
      modifier = Modifier
        .width(40.dp)
        .height(4.dp)
        .clip(CircleShape)
        .background(AuraColors.AuroraSilver.copy(alpha = 0.22f))
    )
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun AuraSheetTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  keyboardType: KeyboardType = KeyboardType.Text,
  testTag: String,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = {
      Text(
        text = label,
        style = AuraTypeTokens.BodySm
      )
    },
    singleLine = true,
    textStyle = AuraTypeTokens.BodyMd.copy(color = AuraColors.AuroraSilver),
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    shape = RoundedCornerShape(16.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = AuraColors.PillInputFill,
      unfocusedContainerColor = AuraColors.PillInputFill,
      focusedBorderColor = AuraColors.PillInputActiveBorder,
      unfocusedBorderColor = AuraColors.HairlineBorder,
      focusedLabelColor = AuraColors.DawnAzure,
      unfocusedLabelColor = AuraColors.OnSurfaceVariant,
      cursorColor = AuraColors.DawnAzure,
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
  )
}

@Composable
fun AuraPrimaryPillButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(CircleShape)
      .background(AuraColors.AuroraSilver)
      .clickable(onClick = onClick)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = AuraTypeTokens.LabelMd,
      color = AuraColors.BaseVoid
    )
  }
}
