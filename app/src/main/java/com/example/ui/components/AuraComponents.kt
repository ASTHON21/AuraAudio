package com.example.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AudioTrackEntity
import com.example.ui.RepeatMode
import com.example.ui.theme.AuraColors
import com.example.ui.theme.AuraTypeTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LocalCoverArtwork(
  coverUriString: String?,
  contentDescription: String?,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = coverUriString) {
    value = if (coverUriString.isNullOrBlank()) {
      null
    } else {
      withContext(Dispatchers.IO) {
        runCatching {
          val uri = Uri.parse(coverUriString)
          val bitmap = if (uri.scheme == "file") {
            BitmapFactory.decodeFile(File(uri.path ?: "").absolutePath)
          } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
          }
          bitmap?.asImageBitmap()
        }.getOrNull()
      }
    }
  }

  val loadedBitmap = imageBitmap
  if (loadedBitmap != null) {
    Image(
      bitmap = loadedBitmap,
      contentDescription = contentDescription,
      contentScale = ContentScale.Crop,
      modifier = modifier
    )
  }
}

@Composable
fun AuraAmbientBackdrop(
  dominantColorHex: Long,
  isPlaying: Boolean,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "aura_breath")
  val breathPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
      repeatMode = AnimRepeatMode.Reverse
    ),
    label = "breath_phase"
  )

  val primaryGlowColor = Color(dominantColorHex).copy(alpha = if (isPlaying) 0.16f else 0.10f)
  val secondaryGlowColor = AuraColors.DawnAzure.copy(alpha = if (isPlaying) 0.14f else 0.08f)
  val apricotGlowColor = AuraColors.WarmApricot.copy(alpha = if (isPlaying) 0.08f else 0.04f)

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .background(AuraColors.BaseVoid)
  ) {
    val w = size.width
    val h = size.height
    val drift = (breathPhase - 0.5f) * 48f

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(primaryGlowColor, Color.Transparent),
        center = Offset(w * 0.5f + drift, h * 0.28f - drift * 0.5f),
        radius = minOf(w, h) * 0.72f
      ),
      radius = minOf(w, h) * 0.72f,
      center = Offset(w * 0.5f + drift, h * 0.28f - drift * 0.5f)
    )

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(secondaryGlowColor, Color.Transparent),
        center = Offset(w * 0.25f - drift * 0.6f, h * 0.55f),
        radius = minOf(w, h) * 0.60f
      ),
      radius = minOf(w, h) * 0.60f,
      center = Offset(w * 0.25f - drift * 0.6f, h * 0.55f)
    )

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(apricotGlowColor, Color.Transparent),
        center = Offset(w * 0.78f, h * 0.42f + drift * 0.4f),
        radius = minOf(w, h) * 0.48f
      ),
      radius = minOf(w, h) * 0.48f,
      center = Offset(w * 0.78f, h * 0.42f + drift * 0.4f)
    )
  }
}

@Composable
fun AuraMetadataBadge(
  text: String,
  isHighlight: Boolean = false,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val shape = RoundedCornerShape(8.dp)
  val backgroundModifier = if (isHighlight) {
    Modifier.background(
      brush = Brush.linearGradient(
        colors = listOf(
          AuraColors.WarmApricot.copy(alpha = 0.16f),
          AuraColors.DawnAzure.copy(alpha = 0.06f)
        )
      ),
      shape = shape
    )
  } else {
    Modifier.background(color = AuraColors.NeutralBadgeFill, shape = shape)
  }

  val borderColor = if (isHighlight) {
    AuraColors.WarmApricot.copy(alpha = 0.30f)
  } else {
    AuraColors.NeutralBadgeBorder
  }

  val textColor = if (isHighlight) {
    AuraColors.WarmApricot
  } else {
    AuraColors.OnSurfaceVariant
  }

  Box(
    modifier = modifier
      .clip(shape)
      .then(backgroundModifier)
      .border(width = 1.dp, color = borderColor, shape = shape)
      .then(
        if (onClick != null) {
          Modifier.clickable(onClick = onClick)
        } else {
          Modifier
        }
      )
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text.uppercase(Locale.ROOT),
      style = AuraTypeTokens.LabelSm,
      color = textColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
fun AuraFloatingFeatureGuideCard(
  title: String,
  subtitle: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(24.dp)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            Color(0xFFF4F5F7).copy(alpha = 0.06f),
            Color(0xFFF4F5F7).copy(alpha = 0.03f)
          )
        )
      )
      .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = shape)
      .padding(horizontal = 16.dp, vertical = 14.dp)
      .testTag("floating_feature_guide_card"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(AuraColors.DawnAzure.copy(alpha = 0.14f))
        .border(
          width = 1.dp,
          color = AuraColors.DawnAzure.copy(alpha = 0.30f),
          shape = RoundedCornerShape(12.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.AutoAwesome,
        contentDescription = null,
        tint = AuraColors.DawnAzure,
        modifier = Modifier.size(20.dp)
      )
    }

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = title,
        style = AuraTypeTokens.LabelMd,
        color = AuraColors.AuroraSilver,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = subtitle,
        style = AuraTypeTokens.BodySm,
        color = AuraColors.OnSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }

    Box(
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .clip(CircleShape)
        .background(AuraColors.NeutralBadgeFill)
        .border(width = 1.dp, color = AuraColors.HairlineBorder, shape = CircleShape)
        .clickable(onClick = onDismiss)
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("dismiss_feature_guide_button"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stringResource(R.string.dismiss).uppercase(Locale.ROOT),
        style = AuraTypeTokens.LabelSm,
        color = AuraColors.AuroraSilver
      )
    }
  }
}

@Composable
fun AuraHairlineScrubber(
  currentPositionMs: Long,
  durationMs: Long,
  bufferedFraction: Float,
  onSeekToFraction: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  var isDragging by remember { mutableStateOf(false) }
  var dragFraction by remember { mutableFloatStateOf(0f) }

  val computedProgress = if (durationMs > 0L) {
    (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }
  val displayFraction = if (isDragging) dragFraction else computedProgress

  val thumbDiameter: Dp by animateDpAsState(
    targetValue = if (isDragging) 14.dp else 8.dp,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
    label = "thumb_size"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("audio_scrubber")
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(36.dp)
        .pointerInput(durationMs) {
          detectTapGestures { offset ->
            if (size.width > 0) {
              val fraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
              onSeekToFraction(fraction)
            }
          }
        }
        .pointerInput(durationMs) {
          detectHorizontalDragGestures(
            onDragStart = { offset ->
              isDragging = true
              if (size.width > 0) {
                dragFraction = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
              }
            },
            onDragEnd = {
              isDragging = false
              onSeekToFraction(dragFraction)
            },
            onDragCancel = {
              isDragging = false
            },
            onHorizontalDrag = { change, _ ->
              change.consume()
              if (size.width > 0) {
                dragFraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
              }
            }
          )
        },
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val trackHeightPx = 3.dp.toPx()
        val centerY = size.height / 2f
        val topY = centerY - trackHeightPx / 2f
        val cornerRadius = CornerRadius(trackHeightPx / 2f, trackHeightPx / 2f)

        drawRoundRect(
          color = AuraColors.SeekTrackInactive,
          topLeft = Offset(0f, topY),
          size = Size(size.width, trackHeightPx),
          cornerRadius = cornerRadius
        )

        val bufWidth = (size.width * bufferedFraction.coerceIn(0f, 1f))
        if (bufWidth > 0f) {
          drawRoundRect(
            color = AuraColors.SeekTrackBuffered,
            topLeft = Offset(0f, topY),
            size = Size(bufWidth, trackHeightPx),
            cornerRadius = cornerRadius
          )
        }

        val activeWidth = (size.width * displayFraction.coerceIn(0f, 1f))
        if (activeWidth > 0f) {
          drawRoundRect(
            color = AuraColors.DawnAzure,
            topLeft = Offset(0f, topY),
            size = Size(activeWidth, trackHeightPx),
            cornerRadius = cornerRadius
          )
        }

        val thumbCenterX = activeWidth.coerceIn(0f, size.width)
        val glowRadiusPx = if (isDragging) 18.dp.toPx() else 12.dp.toPx()
        drawCircle(
          brush = Brush.radialGradient(
            colors = listOf(AuraColors.DawnAzureGlow, Color.Transparent),
            center = Offset(thumbCenterX, centerY),
            radius = glowRadiusPx
          ),
          radius = glowRadiusPx,
          center = Offset(thumbCenterX, centerY)
        )

        val thumbRadiusPx = thumbDiameter.toPx() / 2f
        drawCircle(
          color = AuraColors.AuroraSilver,
          radius = thumbRadiusPx,
          center = Offset(thumbCenterX, centerY)
        )
      }
    }

    val displayedPositionMs = if (isDragging && durationMs > 0L) {
      (durationMs * dragFraction).toLong()
    } else {
      currentPositionMs
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = formatDurationTabular(displayedPositionMs),
        style = AuraTypeTokens.BodySmTabular,
        color = if (isDragging) AuraColors.DawnAzure else AuraColors.OnSurfaceVariant,
        modifier = Modifier.testTag("elapsed_time_label")
      )
      Text(
        text = formatDurationTabular(durationMs),
        style = AuraTypeTokens.BodySmTabular,
        color = AuraColors.OnSurfaceVariant,
        modifier = Modifier.testTag("total_duration_label")
      )
    }
  }
}

@Composable
fun AuraTransportControls(
  isPlaying: Boolean,
  isShuffleEnabled: Boolean,
  repeatMode: RepeatMode,
  hasTracks: Boolean,
  onToggleShuffle: () -> Unit,
  onSkipPrevious: () -> Unit,
  onTogglePlayPause: () -> Unit,
  onSkipNext: () -> Unit,
  onCycleRepeat: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically
  ) {
    SecondaryTransportButton(
      icon = Icons.Filled.Shuffle,
      contentDescription = stringResource(R.string.toggle_shuffle),
      isActive = isShuffleEnabled,
      onClick = onToggleShuffle,
      testTag = "shuffle_button"
    )

    SecondaryTransportButton(
      icon = Icons.Filled.SkipPrevious,
      contentDescription = stringResource(R.string.skip_previous),
      isActive = false,
      iconSize = 28.dp,
      onClick = onSkipPrevious,
      testTag = "skip_previous_button"
    )

    Box(
      modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              AuraColors.AuroraSilver
            )
          )
        )
        .border(
          width = 1.dp,
          color = Color.White.copy(alpha = 0.85f),
          shape = CircleShape
        )
        .clickable(onClick = onTogglePlayPause)
        .testTag("play_pause_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        contentDescription = stringResource(R.string.play_pause),
        tint = if (hasTracks) AuraColors.BaseVoid else AuraColors.BaseVoid.copy(alpha = 0.55f),
        modifier = Modifier.size(30.dp)
      )
    }

    SecondaryTransportButton(
      icon = Icons.Filled.SkipNext,
      contentDescription = stringResource(R.string.skip_next),
      isActive = false,
      iconSize = 28.dp,
      onClick = onSkipNext,
      testTag = "skip_next_button"
    )

    SecondaryTransportButton(
      icon = if (repeatMode == RepeatMode.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
      contentDescription = stringResource(R.string.toggle_repeat),
      isActive = repeatMode != RepeatMode.OFF,
      onClick = onCycleRepeat,
      testTag = "repeat_button"
    )
  }
}

@Composable
private fun SecondaryTransportButton(
  icon: ImageVector,
  contentDescription: String,
  isActive: Boolean,
  iconSize: Dp = 22.dp,
  onClick: () -> Unit,
  testTag: String,
) {
  val tint by animateColorAsState(
    targetValue = if (isActive) AuraColors.AuroraSilver else AuraColors.AuroraSilver.copy(alpha = 0.50f),
    label = "transport_tint"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    IconButton(
      onClick = onClick,
      modifier = Modifier
        .size(48.dp)
        .testTag(testTag)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(iconSize)
      )
    }
    Box(
      modifier = Modifier
        .size(5.dp)
        .clip(CircleShape)
        .background(if (isActive) AuraColors.DawnAzure else Color.Transparent)
    )
  }
}

@Composable
fun AuraRecessedSearchField(
  query: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  val borderColor by animateColorAsState(
    targetValue = if (isFocused || query.isNotEmpty()) {
      AuraColors.PillInputActiveBorder
    } else {
      Color.Transparent
    },
    animationSpec = tween(220),
    label = "search_border"
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(48.dp)
      .clip(CircleShape)
      .background(AuraColors.PillInputFill)
      .border(width = 1.dp, color = borderColor, shape = CircleShape)
      .padding(horizontal = 16.dp)
      .testTag("recessed_search_field"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(
      imageVector = Icons.Filled.Search,
      contentDescription = null,
      tint = if (isFocused) AuraColors.DawnAzure else AuraColors.OnSurfaceVariant.copy(alpha = 0.65f),
      modifier = Modifier.size(18.dp)
    )

    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.CenterStart
    ) {
      if (query.isEmpty()) {
        Text(
          text = stringResource(R.string.search_placeholder),
          style = AuraTypeTokens.BodyMd,
          color = AuraColors.OnSurfaceVariant.copy(alpha = 0.50f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = AuraTypeTokens.BodyMd.copy(color = AuraColors.AuroraSilver),
        cursorBrush = SolidColor(AuraColors.DawnAzure),
        interactionSource = interactionSource,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_input_text")
      )
    }

    if (query.isNotEmpty()) {
      IconButton(
        onClick = { onQueryChange("") },
        modifier = Modifier
          .size(36.dp)
          .testTag("clear_search_button")
      ) {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = "Clear search",
          tint = AuraColors.OnSurfaceVariant,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun AuraTrackRowItem(
  index: Int,
  track: AudioTrackEntity,
  isCurrentTrack: Boolean,
  isPlaying: Boolean,
  onSelectTrack: () -> Unit,
  onOpenMenu: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val rowShape = RoundedCornerShape(16.dp)
  val bgColor by animateColorAsState(
    targetValue = when {
      isCurrentTrack -> AuraColors.GlassLevel1Active
      else -> Color.Transparent
    },
    label = "row_bg"
  )

  val borderColor = if (isCurrentTrack) {
    AuraColors.DawnAzure.copy(alpha = 0.30f)
  } else {
    Color.Transparent
  }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(rowShape)
      .background(bgColor)
      .border(width = 1.dp, color = borderColor, shape = rowShape)
      .clickable(onClick = onSelectTrack)
      .padding(horizontal = 12.dp, vertical = 10.dp)
      .testTag("track_row_${track.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(AuraColors.SurfaceContainerHigh)
        .border(
          width = 1.dp,
          color = AuraColors.HairlineBorder,
          shape = RoundedCornerShape(8.dp)
        ),
      contentAlignment = Alignment.Center
    ) {
      if (!track.coverUriString.isNullOrBlank()) {
        LocalCoverArtwork(
          coverUriString = track.coverUriString,
          contentDescription = track.title,
          modifier = Modifier.fillMaxSize()
        )
      } else if (isCurrentTrack && isPlaying) {
        Icon(
          imageVector = Icons.Filled.GraphicEq,
          contentDescription = null,
          tint = AuraColors.DawnAzure,
          modifier = Modifier.size(20.dp)
        )
      } else {
        Text(
          text = String.format(Locale.US, "%02d", index + 1),
          style = AuraTypeTokens.BodySmTabular,
          color = if (isCurrentTrack) AuraColors.DawnAzure else AuraColors.OnSurfaceVariant
        )
      }
    }

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Text(
        text = track.title,
        style = AuraTypeTokens.BodyLg,
        color = if (isCurrentTrack) AuraColors.AuroraSilver else AuraColors.OnSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AuraMetadataBadge(
          text = track.qualityBadge,
          isHighlight = track.isLossless
        )
        Text(
          text = "${track.artist} • ${track.technicalSpecLabel}",
          style = AuraTypeTokens.BodySm,
          color = AuraColors.OnSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    if (track.durationMs > 0L) {
      Text(
        text = formatDurationTabular(track.durationMs),
        style = AuraTypeTokens.BodySmTabular,
        color = AuraColors.OnSurfaceVariant.copy(alpha = 0.75f)
      )
    }

    IconButton(
      onClick = onOpenMenu,
      modifier = Modifier
        .size(48.dp)
        .testTag("track_overflow_${track.id}")
    ) {
      Icon(
        imageVector = Icons.Filled.MoreHoriz,
        contentDescription = stringResource(R.string.track_options),
        tint = AuraColors.AuroraSilver.copy(alpha = 0.40f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
fun AuraAcousticWaveCanvas(
  isPlaying: Boolean,
  dominantColorHex: Long,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "acoustic_wave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
      repeatMode = AnimRepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Canvas(modifier = modifier) {
    val barCount = 28
    val spacing = size.width / (barCount + 1)
    val centerY = size.height / 2f
    val maxBarHeight = size.height * 0.62f

    for (i in 0 until barCount) {
      val x = spacing * (i + 1)
      val normalizedIndex = i.toFloat() / barCount.toFloat()
      val envelope = sin(normalizedIndex * PI).toFloat()
      val animatedFactor = if (isPlaying) {
        0.25f + 0.75f * ((sin(wavePhase + i * 0.45f) + 1f) / 2f) * envelope
      } else {
        0.12f + 0.18f * envelope
      }
      val barHeight = (maxBarHeight * animatedFactor).coerceAtLeast(4.dp.toPx())
      val color = when {
        i % 7 == 0 -> AuraColors.WarmApricot.copy(alpha = if (isPlaying) 0.75f else 0.35f)
        isPlaying -> AuraColors.DawnAzure.copy(alpha = 0.80f)
        else -> Color(dominantColorHex).copy(alpha = 0.40f)
      }
      drawRoundRect(
        color = color,
        topLeft = Offset(x - 1.5.dp.toPx(), centerY - barHeight / 2f),
        size = Size(3.dp.toPx(), barHeight),
        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
      )
    }
  }
}

fun formatDurationTabular(durationMs: Long): String {
  if (durationMs <= 0L) return "00:00"
  val totalSeconds = durationMs / 1000L
  val minutes = totalSeconds / 60L
  val seconds = totalSeconds % 60L
  return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
