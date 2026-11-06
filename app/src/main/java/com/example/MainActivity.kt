package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SpatialAudioOff
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.SpatialAudioOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AudioTrackEntity
import com.example.ui.AuraPlayerViewModel
import com.example.ui.AuraScreen
import com.example.ui.components.AuraAmbientBackdrop
import com.example.ui.components.AuraMetadataBadge
import com.example.ui.components.EditMetadataModalSheet
import com.example.ui.components.SignalTelemetryModalSheet
import com.example.ui.components.SynthesizeReferenceToneModalSheet
import com.example.ui.components.TrackContextModalSheet
import com.example.ui.screens.SanctuaryPlayerScreen
import com.example.ui.screens.SignalArchitectureScreen
import com.example.ui.screens.TrackIndexScreen
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AuraMinimalistAudioApp()
      }
    }
  }
}

@Composable
fun AuraMinimalistAudioApp(
  viewModel: AuraPlayerViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
  val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
  val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenMultipleDocuments()
  ) { uris ->
    if (uris.isNotEmpty()) {
      viewModel.importAudioUris(uris)
    }
  }

  LaunchedEffect(uiState.statusMessage) {
    val msg = uiState.statusMessage
    if (!msg.isNullOrBlank()) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  BackHandler(enabled = uiState.currentScreen != AuraScreen.SANCTUARY) {
    viewModel.navigateTo(AuraScreen.SANCTUARY)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    AuraAmbientBackdrop(
      dominantColorHex = currentTrack?.dominantColorHex ?: 0xFF4F75FFL,
      isPlaying = uiState.isPlaying
    )

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) { data ->
          Snackbar(
            containerColor = AuraColors.SurfaceContainerHigh,
            contentColor = AuraColors.AuroraSilver,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = data.visuals.message,
              style = AuraTypeTokens.BodySm
            )
          }
        }
      },
      bottomBar = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AnimatedVisibility(
            visible = uiState.currentScreen != AuraScreen.SANCTUARY && currentTrack != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            currentTrack?.let { active ->
              AuraGlassMiniPlayer(
                track = active,
                isPlaying = uiState.isPlaying,
                onOpenSanctuary = { viewModel.navigateTo(AuraScreen.SANCTUARY) },
                onTogglePlayPause = { viewModel.togglePlayPause() }
              )
            }
          }

          AuraGlassBottomNavigationBar(
            currentScreen = uiState.currentScreen,
            trackCount = allTracks.size,
            onSelectScreen = { viewModel.navigateTo(it) }
          )
        }
      }
    ) { innerPadding ->
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        AnimatedContent(
          targetState = uiState.currentScreen,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "aura_screen_transition"
        ) { screen ->
          when (screen) {
            AuraScreen.SANCTUARY -> {
              SanctuaryPlayerScreen(
                uiState = uiState,
                currentTrack = currentTrack,
                trackCount = allTracks.size,
                onImportLocalAudio = {
                  audioPickerLauncher.launch(arrayOf("audio/*"))
                },
                onOpenSynthesizeSheet = { viewModel.setShowSynthesizeSheet(true) },
                onOpenTelemetrySheet = { viewModel.setShowSignalTelemetrySheet(true) },
                onDismissFeatureGuide = { viewModel.dismissFeatureGuide() },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onOpenTrackMenu = { viewModel.openTrackMenu(it) },
                onSeekToFraction = { viewModel.seekToFraction(it) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onCycleRepeat = { viewModel.cycleRepeatMode() }
              )
            }

            AuraScreen.INDEX -> {
              TrackIndexScreen(
                uiState = uiState,
                tracks = filteredTracks,
                currentTrackId = currentTrack?.id,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSelectFilter = { viewModel.selectFilter(it) },
                onDismissFeatureGuide = { viewModel.dismissFeatureGuide() },
                onSelectTrack = { track ->
                  viewModel.playTrack(track)
                  viewModel.navigateTo(AuraScreen.SANCTUARY)
                },
                onOpenTrackMenu = { viewModel.openTrackMenu(it) },
                onImportLocalAudio = {
                  audioPickerLauncher.launch(arrayOf("audio/*"))
                },
                onOpenSynthesizeSheet = { viewModel.setShowSynthesizeSheet(true) }
              )
            }

            AuraScreen.SIGNAL -> {
              SignalArchitectureScreen(
                uiState = uiState,
                currentTrack = currentTrack,
                allTracks = allTracks,
                onPlaybackSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onOutputGainChange = { viewModel.setOutputGain(it) },
                onSelectTrack = { track ->
                  viewModel.playTrack(track)
                },
                onOpenTrackMenu = { viewModel.openTrackMenu(it) },
                onOpenSynthesizeSheet = { viewModel.setShowSynthesizeSheet(true) }
              )
            }
          }
        }
      }
    }

    if (uiState.showSynthesizeSheet) {
      SynthesizeReferenceToneModalSheet(
        onDismiss = { viewModel.setShowSynthesizeSheet(false) },
        onSynthesize = { title, artist, freq, dur ->
          viewModel.synthesizeCustomTone(title, artist, freq, dur)
        }
      )
    }

    if (uiState.showSignalTelemetrySheet) {
      SignalTelemetryModalSheet(
        track = currentTrack,
        hardwareTelemetry = uiState.hardwareTelemetry,
        playbackSpeed = uiState.playbackSpeed,
        outputGain = uiState.outputGain,
        onDismiss = { viewModel.setShowSignalTelemetrySheet(false) }
      )
    }

    uiState.selectedTrackForMenu?.let { menuTrack ->
      TrackContextModalSheet(
        track = menuTrack,
        onDismiss = { viewModel.openTrackMenu(null) },
        onPlayNow = {
          viewModel.playTrack(menuTrack)
          viewModel.navigateTo(AuraScreen.SANCTUARY)
        },
        onToggleFavorite = { viewModel.toggleFavorite(menuTrack) },
        onInspectSignal = { viewModel.setShowSignalTelemetrySheet(true) },
        onEditMetadata = { viewModel.openTrackEdit(menuTrack) },
        onDeleteTrack = { viewModel.deleteTrack(menuTrack) }
      )
    }

    uiState.selectedTrackForEdit?.let { editTrack ->
      EditMetadataModalSheet(
        track = editTrack,
        onDismiss = { viewModel.openTrackEdit(null) },
        onSave = { title, artist, album ->
          viewModel.saveEditedMetadata(editTrack, title, artist, album)
        }
      )
    }
  }
}

@Composable
private fun AuraGlassMiniPlayer(
  track: AudioTrackEntity,
  isPlaying: Boolean,
  onOpenSanctuary: () -> Unit,
  onTogglePlayPause: () -> Unit,
) {
  val shape = RoundedCornerShape(20.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AuraColors.ModalSheetSurface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .clickable(onClick = onOpenSanctuary)
      .padding(horizontal = 16.dp, vertical = 10.dp)
      .testTag("mini_player_bar"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    AuraMetadataBadge(
      text = track.qualityBadge,
      isHighlight = track.isLossless
    )
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = track.title,
        style = AuraTypeTokens.LabelMd,
        color = AuraColors.AuroraSilver,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = track.artist,
        style = AuraTypeTokens.BodySm,
        color = AuraColors.OnSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    IconButton(
      onClick = onTogglePlayPause,
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(AuraColors.AuroraSilver)
        .testTag("mini_player_play_pause")
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = stringResource(R.string.play_pause),
        tint = AuraColors.BaseVoid,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}

@Composable
private fun AuraGlassBottomNavigationBar(
  currentScreen: AuraScreen,
  trackCount: Int,
  onSelectScreen: (AuraScreen) -> Unit,
) {
  val navShape = RoundedCornerShape(24.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(64.dp)
      .clip(navShape)
      .background(AuraColors.GlassLevel1Surface)
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = navShape)
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("bottom_navigation_bar"),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    AuraNavPillItem(
      label = stringResource(R.string.nav_sanctuary),
      selected = currentScreen == AuraScreen.SANCTUARY,
      selectedIcon = Icons.Filled.SpatialAudioOff,
      unselectedIcon = Icons.Outlined.SpatialAudioOff,
      onClick = { onSelectScreen(AuraScreen.SANCTUARY) },
      modifier = Modifier.weight(1f),
      testTag = "nav_tab_sanctuary"
    )

    AuraNavPillItem(
      label = if (trackCount > 0) {
        "${stringResource(R.string.nav_library)} ($trackCount)"
      } else {
        stringResource(R.string.nav_library)
      },
      selected = currentScreen == AuraScreen.INDEX,
      selectedIcon = Icons.Filled.LibraryMusic,
      unselectedIcon = Icons.Outlined.LibraryMusic,
      onClick = { onSelectScreen(AuraScreen.INDEX) },
      modifier = Modifier.weight(1f),
      testTag = "nav_tab_index"
    )

    AuraNavPillItem(
      label = stringResource(R.string.nav_signal),
      selected = currentScreen == AuraScreen.SIGNAL,
      selectedIcon = Icons.Filled.GraphicEq,
      unselectedIcon = Icons.Outlined.GraphicEq,
      onClick = { onSelectScreen(AuraScreen.SIGNAL) },
      modifier = Modifier.weight(1f),
      testTag = "nav_tab_signal"
    )
  }
}

@Composable
private fun AuraNavPillItem(
  label: String,
  selected: Boolean,
  selectedIcon: ImageVector,
  unselectedIcon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String,
) {
  val pillShape = CircleShape
  Row(
    modifier = modifier
      .fillMaxHeight()
      .clip(pillShape)
      .background(
        if (selected) AuraColors.DawnAzure.copy(alpha = 0.16f) else Color.Transparent
      )
      .border(
        width = 1.dp,
        color = if (selected) AuraColors.DawnAzure.copy(alpha = 0.35f) else Color.Transparent,
        shape = pillShape
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = if (selected) selectedIcon else unselectedIcon,
      contentDescription = label,
      tint = if (selected) AuraColors.DawnAzure else AuraColors.AuroraSilver.copy(alpha = 0.50f),
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      style = AuraTypeTokens.LabelMd,
      color = if (selected) AuraColors.AuroraSilver else AuraColors.AuroraSilver.copy(alpha = 0.50f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}
