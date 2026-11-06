package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AudioTrackEntity
import com.example.ui.PlayerUiState
import com.example.ui.TrackFilter
import com.example.ui.components.AuraFloatingFeatureGuideCard
import com.example.ui.components.AuraMetadataBadge
import com.example.ui.components.AuraRecessedSearchField
import com.example.ui.components.AuraTrackRowItem
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens

@Composable
fun TrackIndexScreen(
  uiState: PlayerUiState,
  tracks: List<AudioTrackEntity>,
  currentTrackId: Long?,
  onSearchQueryChange: (String) -> Unit,
  onSelectFilter: (TrackFilter) -> Unit,
  onDismissFeatureGuide: () -> Unit,
  onSelectTrack: (AudioTrackEntity) -> Unit,
  onOpenTrackMenu: (AudioTrackEntity) -> Unit,
  onImportLocalAudio: () -> Unit,
  onOpenSynthesizeSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .testTag("track_index_screen"),
    contentAlignment = Alignment.TopCenter
  ) {
    val isTablet = maxWidth >= 600.dp
    val horizontalMargin = if (isTablet) 32.dp else 20.dp

    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 640.dp)
        .padding(horizontal = horizontalMargin, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "OFFLINE ACOUSTIC ARCHIVE",
            style = AuraTypeTokens.LabelSm,
            color = AuraColors.WarmApricot
          )
          Text(
            text = "Audio Index",
            style = AuraTypeTokens.HeadlineLg,
            color = AuraColors.AuroraSilver
          )
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          SmallActionChip(
            icon = Icons.Filled.Add,
            label = "Import Local",
            onClick = onImportLocalAudio,
            testTag = "index_import_local_button"
          )
          SmallActionChip(
            icon = Icons.Filled.Tune,
            label = "Synthesize WAV",
            onClick = onOpenSynthesizeSheet,
            testTag = "index_synthesize_wav_button"
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      AuraRecessedSearchField(
        query = uiState.searchQuery,
        onQueryChange = onSearchQueryChange
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TrackFilter.entries.forEach { filter ->
          val isSelected = uiState.activeFilter == filter
          AuraMetadataBadge(
            text = filter.label,
            isHighlight = isSelected,
            onClick = { onSelectFilter(filter) },
            modifier = Modifier
              .height(32.dp)
              .testTag("filter_chip_${filter.name.lowercase()}")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (uiState.showFeatureGuideCard) {
        AuraFloatingFeatureGuideCard(
          title = stringResource(R.string.feature_guide_title),
          subtitle = stringResource(R.string.feature_guide_subtitle),
          onDismiss = onDismissFeatureGuide
        )
        Spacer(modifier = Modifier.height(16.dp))
      }

      if (tracks.isEmpty()) {
        EmptyArchiveCard(
          hasActiveSearchOrFilter = uiState.searchQuery.isNotEmpty() || uiState.activeFilter != TrackFilter.ALL,
          onImportLocalAudio = onImportLocalAudio,
          onOpenSynthesizeSheet = onOpenSynthesizeSheet,
          modifier = Modifier.fillMaxWidth()
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("track_lazy_column"),
          contentPadding = PaddingValues(bottom = 24.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          itemsIndexed(tracks, key = { _, item -> item.id }) { index, track ->
            AuraTrackRowItem(
              index = index,
              track = track,
              isCurrentTrack = track.id == currentTrackId,
              isPlaying = uiState.isPlaying && track.id == currentTrackId,
              onSelectTrack = { onSelectTrack(track) },
              onOpenMenu = { onOpenTrackMenu(track) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SmallActionChip(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  testTag: String,
) {
  Row(
    modifier = Modifier
      .height(40.dp)
      .clip(CircleShape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = CircleShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AuraColors.DawnAzure,
      modifier = Modifier.size(15.dp)
    )
    Text(
      text = label,
      style = AuraTypeTokens.LabelMd,
      color = AuraColors.AuroraSilver
    )
  }
}

@Composable
private fun EmptyArchiveCard(
  hasActiveSearchOrFilter: Boolean,
  onImportLocalAudio: () -> Unit,
  onOpenSynthesizeSheet: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(24.dp)
  Column(
    modifier = modifier
      .clip(shape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .padding(24.dp)
      .testTag("empty_archive_card"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Text(
      text = if (hasActiveSearchOrFilter) {
        "No Matching Tracks"
      } else {
        stringResource(R.string.empty_library_title)
      },
      style = AuraTypeTokens.HeadlineSm,
      color = AuraColors.AuroraSilver,
      textAlign = TextAlign.Center
    )
    Text(
      text = if (hasActiveSearchOrFilter) {
        "Adjust your search query or filter category to view tracks."
      } else {
        stringResource(R.string.empty_library_body)
      },
      style = AuraTypeTokens.BodySm,
      color = AuraColors.OnSurfaceVariant,
      textAlign = TextAlign.Center
    )

    if (!hasActiveSearchOrFilter) {
      Spacer(modifier = Modifier.height(6.dp))
      EmptyStateActionButton(
        icon = Icons.Filled.AudioFile,
        text = "Import Audio Files from Device",
        tint = AuraColors.DawnAzure,
        onClick = onImportLocalAudio,
        modifier = Modifier.fillMaxWidth(),
        testTag = "empty_import_files_button"
      )
      EmptyStateActionButton(
        icon = Icons.Filled.Tune,
        text = "Synthesize 16-Bit / 44.1kHz Reference WAV",
        tint = AuraColors.WarmApricot,
        onClick = onOpenSynthesizeSheet,
        modifier = Modifier.fillMaxWidth(),
        testTag = "empty_synthesize_wav_button"
      )
    }
  }
}

@Composable
private fun EmptyStateActionButton(
  icon: ImageVector,
  text: String,
  tint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  Row(
    modifier = modifier
      .height(48.dp)
      .clip(CircleShape)
      .background(AuraColors.PillInputFill)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = CircleShape)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(
      text = text,
      style = AuraTypeTokens.LabelMd,
      color = AuraColors.AuroraSilver
    )
  }
}
