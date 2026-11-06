package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AudioRepository
import com.example.data.AudioTrackEntity
import com.example.data.AuraDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AuraScreen {
  SANCTUARY,
  INDEX,
  SIGNAL,
}

enum class RepeatMode {
  OFF,
  ALL,
  ONE,
}

enum class TrackFilter(val label: String) {
  ALL("ALL TRACKS"),
  FAVORITES("FAVORITES"),
  LOSSLESS("LOSSLESS"),
  STREAMS("STREAMS"),
}

data class HardwareAudioTelemetry(
  val outputDeviceName: String = "Internal DAC / Speaker",
  val nativeSampleRateHz: String = "48000",
  val framesPerBuffer: String = "256",
)

data class PlayerUiState(
  val currentScreen: AuraScreen = AuraScreen.SANCTUARY,
  val currentTrackId: Long? = null,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val currentPositionMs: Long = 0L,
  val durationMs: Long = 0L,
  val bufferedFraction: Float = 0f,
  val isShuffleEnabled: Boolean = false,
  val repeatMode: RepeatMode = RepeatMode.OFF,
  val playbackSpeed: Float = 1.0f,
  val outputGain: Float = 1.0f,
  val searchQuery: String = "",
  val activeFilter: TrackFilter = TrackFilter.ALL,
  val showFeatureGuideCard: Boolean = true,
  val showAddStreamSheet: Boolean = false,
  val showSynthesizeSheet: Boolean = false,
  val showSignalTelemetrySheet: Boolean = false,
  val selectedTrackForMenu: AudioTrackEntity? = null,
  val selectedTrackForEdit: AudioTrackEntity? = null,
  val statusMessage: String? = null,
  val hardwareTelemetry: HardwareAudioTelemetry = HardwareAudioTelemetry(),
)

class AuraPlayerViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: AudioRepository
  private var mediaPlayer: MediaPlayer? = null
  private var progressJob: Job? = null

  private val _uiState = MutableStateFlow(PlayerUiState())
  val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

  val allTracks: StateFlow<List<AudioTrackEntity>>

  val filteredTracks: StateFlow<List<AudioTrackEntity>>

  val currentTrack: StateFlow<AudioTrackEntity?>

  init {
    val dao = AuraDatabase.getDatabase(application).audioTrackDao()
    repository = AudioRepository(application, dao)

    allTracks = repository.allTracks.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

    filteredTracks = combine(allTracks, _uiState) { tracks, state ->
      val query = state.searchQuery.trim().lowercase()
      tracks.filter { track ->
        val matchesFilter = when (state.activeFilter) {
          TrackFilter.ALL -> true
          TrackFilter.FAVORITES -> track.isFavorite
          TrackFilter.LOSSLESS -> track.isLossless
          TrackFilter.STREAMS -> track.isStream
        }
        val matchesQuery = if (query.isEmpty()) {
          true
        } else {
          track.title.lowercase().contains(query) ||
            track.artist.lowercase().contains(query) ||
            track.album.lowercase().contains(query) ||
            track.qualityBadge.lowercase().contains(query) ||
            track.technicalSpecLabel.lowercase().contains(query)
        }
        matchesFilter && matchesQuery
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

    currentTrack = combine(allTracks, _uiState) { tracks, state ->
      tracks.find { it.id == state.currentTrackId } ?: tracks.firstOrNull()
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = null
    )

    refreshHardwareTelemetry()
  }

  fun refreshHardwareTelemetry() {
    runCatching {
      val audioManager =
        getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as? AudioManager
      val sampleRate =
        audioManager?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) ?: "48000"
      val bufferFrames =
        audioManager?.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER) ?: "256"
      val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS).orEmpty()
      val activeDevice = devices.firstOrNull {
        it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
          it.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
          it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
          it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
          it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
          it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
      } ?: devices.firstOrNull()

      val deviceLabel = when (activeDevice?.type) {
        AudioDeviceInfo.TYPE_USB_HEADSET, AudioDeviceInfo.TYPE_USB_DEVICE -> "External USB Audio DAC"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Analog 3.5mm Headphone Out"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLE_HEADSET -> "Wireless Bluetooth Audio"
        else -> "Internal Direct PCM Speaker"
      }

      _uiState.update {
        it.copy(
          hardwareTelemetry = HardwareAudioTelemetry(
            outputDeviceName = deviceLabel,
            nativeSampleRateHz = sampleRate,
            framesPerBuffer = bufferFrames,
          )
        )
      }
    }
  }

  fun navigateTo(screen: AuraScreen) {
    _uiState.update { it.copy(currentScreen = screen) }
    if (screen == AuraScreen.SIGNAL) {
      refreshHardwareTelemetry()
    }
  }

  fun dismissFeatureGuide() {
    _uiState.update { it.copy(showFeatureGuideCard = false) }
  }

  fun updateSearchQuery(query: String) {
    _uiState.update { it.copy(searchQuery = query) }
  }

  fun selectFilter(filter: TrackFilter) {
    _uiState.update { it.copy(activeFilter = filter) }
  }

  fun setShowAddStreamSheet(show: Boolean) {
    _uiState.update { it.copy(showAddStreamSheet = show) }
  }

  fun setShowSynthesizeSheet(show: Boolean) {
    _uiState.update { it.copy(showSynthesizeSheet = show) }
  }

  fun setShowSignalTelemetrySheet(show: Boolean) {
    if (show) refreshHardwareTelemetry()
    _uiState.update { it.copy(showSignalTelemetrySheet = show) }
  }

  fun openTrackMenu(track: AudioTrackEntity?) {
    _uiState.update { it.copy(selectedTrackForMenu = track) }
  }

  fun openTrackEdit(track: AudioTrackEntity?) {
    _uiState.update {
      it.copy(
        selectedTrackForMenu = null,
        selectedTrackForEdit = track
      )
    }
  }

  fun clearStatusMessage() {
    _uiState.update { it.copy(statusMessage = null) }
  }

  fun importAudioUris(uris: List<Uri>) {
    if (uris.isEmpty()) return
    viewModelScope.launch {
      var firstImported: AudioTrackEntity? = null
      var count = 0
      for (uri in uris) {
        val imported = repository.importLocalAudioUri(uri)
        if (imported != null) {
          count++
          if (firstImported == null) firstImported = imported
        }
      }
      if (firstImported != null) {
        _uiState.update {
          it.copy(
            statusMessage = "Imported $count audio ${if (count == 1) "track" else "tracks"} into Sanctuary"
          )
        }
        if (_uiState.value.currentTrackId == null) {
          playTrack(firstImported)
        }
      } else {
        _uiState.update {
          it.copy(statusMessage = "Unable to decode selected audio file")
        }
      }
    }
  }

  fun addStreamUrl(
    streamUrl: String,
    title: String,
    artist: String,
    album: String,
    coverUrl: String?,
  ) {
    viewModelScope.launch {
      val added = repository.addStreamTrack(
        streamUrl = streamUrl,
        title = title,
        artist = artist,
        album = album,
        coverUrl = coverUrl,
      )
      _uiState.update {
        it.copy(
          showAddStreamSheet = false,
          statusMessage = "Connected stream: ${added.title}"
        )
      }
      playTrack(added)
    }
  }

  fun synthesizeCustomTone(
    title: String,
    artist: String,
    frequencyHz: Double,
    durationSec: Int,
  ) {
    viewModelScope.launch {
      val created = repository.synthesizeCustomReferenceWav(
        title = title,
        artist = artist,
        baseFrequencyHz = frequencyHz,
        durationSeconds = durationSec,
      )
      _uiState.update {
        it.copy(
          showSynthesizeSheet = false,
          statusMessage = "Synthesized 16-bit/44.1kHz WAV: ${created.title}"
        )
      }
      playTrack(created)
    }
  }

  fun toggleFavorite(track: AudioTrackEntity) {
    viewModelScope.launch {
      repository.toggleFavorite(track)
    }
  }

  fun saveEditedMetadata(
    track: AudioTrackEntity,
    title: String,
    artist: String,
    album: String,
    coverUri: String?,
  ) {
    viewModelScope.launch {
      repository.updateTrackMetadata(track, title, artist, album, coverUri)
      _uiState.update {
        it.copy(
          selectedTrackForEdit = null,
          statusMessage = "Updated track metadata"
        )
      }
    }
  }

  fun deleteTrack(track: AudioTrackEntity) {
    viewModelScope.launch {
      val wasCurrent = _uiState.value.currentTrackId == track.id
      if (wasCurrent) {
        stopAndReleasePlayer()
      }
      repository.deleteTrack(track)
      _uiState.update {
        it.copy(
          selectedTrackForMenu = null,
          currentTrackId = if (wasCurrent) null else it.currentTrackId,
          statusMessage = "Removed ${track.title}"
        )
      }
    }
  }

  fun playTrack(track: AudioTrackEntity) {
    stopAndReleasePlayer()
    _uiState.update {
      it.copy(
        currentTrackId = track.id,
        isBuffering = true,
        currentPositionMs = 0L,
        durationMs = track.durationMs,
        bufferedFraction = if (track.isStream) 0.15f else 1.0f,
      )
    }

    runCatching {
      val player = MediaPlayer()
      mediaPlayer = player
      player.setAudioAttributes(
        AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_MEDIA)
          .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
          .build()
      )

      val uri = Uri.parse(track.uriString)
      if (track.isStream || track.uriString.startsWith("http")) {
        player.setDataSource(track.uriString)
      } else {
        player.setDataSource(getApplication(), uri)
      }

      player.setOnBufferingUpdateListener { _, percent ->
        _uiState.update {
          it.copy(bufferedFraction = (percent / 100f).coerceIn(0f, 1f))
        }
      }

      player.setOnPreparedListener { mp ->
        val resolvedDuration = mp.duration.toLong().coerceAtLeast(track.durationMs)
        val gain = _uiState.value.outputGain
        mp.setVolume(gain, gain)
        applyPlaybackSpeedInternal(mp, _uiState.value.playbackSpeed)
        mp.start()
        _uiState.update {
          it.copy(
            isPlaying = true,
            isBuffering = false,
            durationMs = if (resolvedDuration > 0) resolvedDuration else track.durationMs,
            bufferedFraction = if (track.isStream) it.bufferedFraction.coerceAtLeast(0.35f) else 1f,
          )
        }
        startProgressTicker()
      }

      player.setOnCompletionListener {
        handleTrackCompletion()
      }

      player.setOnErrorListener { _, _, _ ->
        _uiState.update {
          it.copy(
            isPlaying = false,
            isBuffering = false,
            statusMessage = "Signal interrupted: unable to play audio source"
          )
        }
        true
      }

      player.prepareAsync()
    }.onFailure {
      _uiState.update {
        it.copy(
          isPlaying = false,
          isBuffering = false,
          statusMessage = "Could not initialize audio source"
        )
      }
    }
  }

  fun togglePlayPause() {
    val activeTrack = currentTrack.value ?: return
    val player = mediaPlayer
    if (player == null) {
      playTrack(activeTrack)
      return
    }
    runCatching {
      if (player.isPlaying) {
        player.pause()
        _uiState.update { it.copy(isPlaying = false) }
        progressJob?.cancel()
      } else {
        player.start()
        _uiState.update { it.copy(isPlaying = true) }
        startProgressTicker()
      }
    }.onFailure {
      playTrack(activeTrack)
    }
  }

  fun seekToFraction(fraction: Float) {
    val clamped = fraction.coerceIn(0f, 1f)
    val duration = _uiState.value.durationMs.takeIf { it > 0L }
      ?: currentTrack.value?.durationMs
      ?: return
    val targetMs = (duration * clamped).toLong()
    _uiState.update { it.copy(currentPositionMs = targetMs) }
    runCatching {
      mediaPlayer?.seekTo(targetMs.toInt())
    }
  }

  fun skipNext() {
    val tracks = allTracks.value
    if (tracks.isEmpty()) return
    val current = currentTrack.value ?: tracks.first()
    val nextTrack = if (_uiState.value.isShuffleEnabled && tracks.size > 1) {
      tracks.filter { it.id != current.id }.random()
    } else {
      val idx = tracks.indexOfFirst { it.id == current.id }
      if (idx in 0 until tracks.lastIndex) {
        tracks[idx + 1]
      } else {
        tracks.first()
      }
    }
    playTrack(nextTrack)
  }

  fun skipPrevious() {
    val tracks = allTracks.value
    if (tracks.isEmpty()) return
    if (_uiState.value.currentPositionMs > 3500L) {
      seekToFraction(0f)
      return
    }
    val current = currentTrack.value ?: tracks.first()
    val idx = tracks.indexOfFirst { it.id == current.id }
    val prevTrack = if (idx > 0) {
      tracks[idx - 1]
    } else {
      tracks.last()
    }
    playTrack(prevTrack)
  }

  fun toggleShuffle() {
    _uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
  }

  fun cycleRepeatMode() {
    _uiState.update { state ->
      val next = when (state.repeatMode) {
        RepeatMode.OFF -> RepeatMode.ALL
        RepeatMode.ALL -> RepeatMode.ONE
        RepeatMode.ONE -> RepeatMode.OFF
      }
      state.copy(repeatMode = next)
    }
  }

  fun setPlaybackSpeed(speed: Float) {
    val clamped = speed.coerceIn(0.5f, 2.0f)
    _uiState.update { it.copy(playbackSpeed = clamped) }
    mediaPlayer?.let { mp ->
      if (mp.isPlaying) {
        applyPlaybackSpeedInternal(mp, clamped)
      }
    }
  }

  fun setOutputGain(gain: Float) {
    val clamped = gain.coerceIn(0.1f, 1.0f)
    _uiState.update { it.copy(outputGain = clamped) }
    runCatching {
      mediaPlayer?.setVolume(clamped, clamped)
    }
  }

  private fun applyPlaybackSpeedInternal(mp: MediaPlayer, speed: Float) {
    runCatching {
      val params = PlaybackParams().setSpeed(speed)
      mp.playbackParams = params
    }
  }

  private fun handleTrackCompletion() {
    progressJob?.cancel()
    val tracks = allTracks.value
    val state = _uiState.value
    val active = currentTrack.value

    when (state.repeatMode) {
      RepeatMode.ONE -> {
        if (active != null) playTrack(active)
      }
      RepeatMode.ALL -> {
        skipNext()
      }
      RepeatMode.OFF -> {
        if (tracks.size > 1 && active != null) {
          val idx = tracks.indexOfFirst { it.id == active.id }
          if (idx in 0 until tracks.lastIndex) {
            playTrack(tracks[idx + 1])
          } else {
            _uiState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
          }
        } else {
          _uiState.update { it.copy(isPlaying = false, currentPositionMs = 0L) }
        }
      }
    }
  }

  private fun startProgressTicker() {
    progressJob?.cancel()
    progressJob = viewModelScope.launch {
      while (isActive) {
        val mp = mediaPlayer
        if (mp != null) {
          runCatching {
            if (mp.isPlaying) {
              val pos = mp.currentPosition.toLong().coerceAtLeast(0L)
              val dur = mp.duration.toLong()
              _uiState.update { state ->
                state.copy(
                  currentPositionMs = pos,
                  durationMs = if (dur > 0) dur else state.durationMs
                )
              }
            }
          }
        }
        delay(50L)
      }
    }
  }

  private fun stopAndReleasePlayer() {
    progressJob?.cancel()
    runCatching {
      mediaPlayer?.stop()
    }
    runCatching {
      mediaPlayer?.release()
    }
    mediaPlayer = null
    _uiState.update { it.copy(isPlaying = false, isBuffering = false) }
  }

  override fun onCleared() {
    super.onCleared()
    stopAndReleasePlayer()
  }
}
