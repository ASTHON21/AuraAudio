package com.example.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class AudioRepository(
  private val context: Context,
  private val dao: AudioTrackDao,
) {
  val allTracks: Flow<List<AudioTrackEntity>> = dao.getAllTracks()

  suspend fun toggleFavorite(track: AudioTrackEntity) {
    dao.setFavorite(track.id, !track.isFavorite)
  }

  suspend fun updateTrackMetadata(
    track: AudioTrackEntity,
    newTitle: String,
    newArtist: String,
    newAlbum: String,
    newCoverUri: String?,
  ) {
    dao.updateTrack(
      track.copy(
        title = newTitle.trim().ifEmpty { track.title },
        artist = newArtist.trim().ifEmpty { track.artist },
        album = newAlbum.trim().ifEmpty { track.album },
        coverUriString = newCoverUri?.trim()?.ifEmpty { null },
      )
    )
  }

  suspend fun deleteTrack(track: AudioTrackEntity) {
    withContext(Dispatchers.IO) {
      // Clean up internal synthesized files or cached cover art if owned by app
      track.coverUriString?.let { coverPath ->
        if (coverPath.startsWith("file://")) {
          runCatching { File(Uri.parse(coverPath).path ?: "").delete() }
        }
      }
      if (track.uriString.startsWith("file://")) {
        runCatching { File(Uri.parse(track.uriString).path ?: "").delete() }
      }
      dao.deleteTrackById(track.id)
    }
  }

  suspend fun importLocalAudioUri(uri: Uri): AudioTrackEntity? = withContext(Dispatchers.IO) {
    runCatching {
      try {
        context.contentResolver.takePersistableUriPermission(
          uri,
          Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
      } catch (_: SecurityException) {
        // Not all providers support persistable permissions; safe to ignore
      }

      val fallbackName = queryDisplayName(uri) ?: uri.lastPathSegment ?: "Untitled Audio"
      val cleanFallbackTitle = fallbackName.substringBeforeLast(".")

      val retriever = MediaMetadataRetriever()
      var title = cleanFallbackTitle
      var artist = "Unknown Artist"
      var album = "Local Sanctuary"
      var durationMs = 0L
      var mimeType = context.contentResolver.getType(uri) ?: "audio/mpeg"
      var bitrateBps = 0
      var sampleRateHz = 44100
      var bitDepth = 16
      var channels = 2
      var coverUriString: String? = null
      var dominantColorHex = 0xFF4F75FFL

      try {
        retriever.setDataSource(context, uri)
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
          ?.takeIf { it.isNotBlank() }
          ?.let { title = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
          ?.takeIf { it.isNotBlank() }
          ?.let { artist = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
          ?.takeIf { it.isNotBlank() }
          ?.let { album = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
          ?.toLongOrNull()
          ?.let { durationMs = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
          ?.takeIf { it.isNotBlank() }
          ?.let { mimeType = it }
        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
          ?.toIntOrNull()
          ?.let { bitrateBps = it }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)
            ?.toIntOrNull()
            ?.let { sampleRateHz = it }
          retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)
            ?.toIntOrNull()
            ?.let { bitDepth = it }
        }

        val embeddedPic = retriever.embeddedPicture
        if (embeddedPic != null && embeddedPic.isNotEmpty()) {
          val coversDir = File(context.filesDir, "covers").apply { mkdirs() }
          val coverFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
          FileOutputStream(coverFile).use { it.write(embeddedPic) }
          coverUriString = Uri.fromFile(coverFile).toString()

          val bitmap = BitmapFactory.decodeByteArray(embeddedPic, 0, embeddedPic.size)
          if (bitmap != null) {
            dominantColorHex = extractDominantColor(bitmap)
            bitmap.recycle()
          }
        }
      } finally {
        runCatching { retriever.release() }
      }

      val lowerMime = mimeType.lowercase()
      val lowerName = fallbackName.lowercase()
      val isLossless = lowerMime.contains("flac") ||
        lowerMime.contains("wav") ||
        lowerMime.contains("raw") ||
        lowerMime.contains("alac") ||
        lowerMime.contains("aiff") ||
        lowerName.endsWith(".flac") ||
        lowerName.endsWith(".wav") ||
        lowerName.endsWith(".alac") ||
        lowerName.endsWith(".aiff")

      val formatShort = when {
        lowerMime.contains("flac") || lowerName.endsWith(".flac") -> "FLAC"
        lowerMime.contains("wav") || lowerName.endsWith(".wav") -> "WAV"
        lowerMime.contains("alac") || lowerName.endsWith(".alac") -> "ALAC"
        lowerMime.contains("aac") || lowerName.endsWith(".m4a") -> "AAC"
        lowerMime.contains("ogg") || lowerName.endsWith(".ogg") -> "OGG"
        lowerMime.contains("opus") || lowerName.endsWith(".opus") -> "OPUS"
        else -> "MP3"
      }

      val bitrateKbps = if (bitrateBps > 0) {
        bitrateBps / 1000
      } else if (isLossless) {
        (sampleRateHz * bitDepth * channels) / 1000
      } else {
        320
      }

      val qualityBadge = when {
        isLossless && (sampleRateHz >= 88200 || bitDepth >= 24) -> "HI-RES"
        isLossless -> "LOSSLESS"
        else -> formatShort
      }

      val kHzString = if (sampleRateHz % 1000 == 0) {
        "${sampleRateHz / 1000}KHZ"
      } else {
        "${sampleRateHz / 1000.0}KHZ"
      }

      val technicalSpecLabel = if (isLossless) {
        "$kHzString / ${bitDepth}-BIT $formatShort"
      } else {
        "$kHzString / $bitrateKbps KBPS $formatShort"
      }

      val entity = AudioTrackEntity(
        title = title,
        artist = artist,
        album = album,
        uriString = uri.toString(),
        coverUriString = coverUriString,
        durationMs = durationMs,
        mimeType = mimeType,
        bitrateKbps = bitrateKbps,
        sampleRateHz = sampleRateHz,
        bitDepth = bitDepth,
        channels = channels,
        isLossless = isLossless,
        qualityBadge = qualityBadge,
        technicalSpecLabel = technicalSpecLabel,
        dominantColorHex = dominantColorHex,
        isStream = false,
      )
      val id = dao.insertTrack(entity)
      entity.copy(id = id)
    }.getOrNull()
  }

  suspend fun addStreamTrack(
    streamUrl: String,
    title: String,
    artist: String,
    album: String,
    coverUrl: String?,
  ): AudioTrackEntity = withContext(Dispatchers.IO) {
    val cleanUrl = streamUrl.trim()
    val lowerUrl = cleanUrl.lowercase()
    val isLossless = lowerUrl.endsWith(".flac") || lowerUrl.endsWith(".wav")
    val formatShort = when {
      lowerUrl.endsWith(".flac") -> "FLAC"
      lowerUrl.endsWith(".wav") -> "WAV"
      lowerUrl.endsWith(".aac") || lowerUrl.endsWith(".m4a") -> "AAC"
      lowerUrl.endsWith(".ogg") -> "OGG"
      else -> "STREAM"
    }
    val entity = AudioTrackEntity(
      title = title.trim(),
      artist = artist.trim(),
      album = album.trim().ifEmpty { "Direct Network Stream" },
      uriString = cleanUrl,
      coverUriString = coverUrl?.trim()?.takeIf { it.isNotEmpty() },
      durationMs = 0L,
      mimeType = if (isLossless) "audio/flac" else "audio/mpeg",
      bitrateKbps = if (isLossless) 1411 else 320,
      sampleRateHz = 48000,
      bitDepth = if (isLossless) 24 else 16,
      channels = 2,
      isLossless = isLossless,
      qualityBadge = if (isLossless) "LOSSLESS" else "DIRECT STREAM",
      technicalSpecLabel = if (isLossless) "48KHZ / 24-BIT $formatShort" else "48KHZ / DIRECT $formatShort",
      dominantColorHex = 0xFF4F75FFL,
      isStream = true,
    )
    val id = dao.insertTrack(entity)
    entity.copy(id = id)
  }

  /**
   * Synthesizes a real uncompressed 16-bit 44.1kHz stereo PCM WAV audio file from user-entered
   * acoustic parameters so users can test real hardware playback and scrubbing without external files.
   */
  suspend fun synthesizeCustomReferenceWav(
    title: String,
    artist: String,
    baseFrequencyHz: Double,
    durationSeconds: Int,
  ): AudioTrackEntity = withContext(Dispatchers.IO) {
    val sampleRate = 44100
    val channels = 2
    val bitsPerSample = 16
    val clampedDuration = durationSeconds.coerceIn(3, 120)
    val totalFrames = sampleRate * clampedDuration
    val dataSize = totalFrames * channels * (bitsPerSample / 8)

    val synthDir = File(context.filesDir, "synthesized").apply { mkdirs() }
    val outFile = File(synthDir, "aura_ref_${System.currentTimeMillis()}.wav")

    FileOutputStream(outFile).use { fos ->
      val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
      // RIFF chunk
      header.put("RIFF".toByteArray(Charsets.US_ASCII))
      header.putInt(36 + dataSize)
      header.put("WAVE".toByteArray(Charsets.US_ASCII))
      // fmt sub-chunk
      header.put("fmt ".toByteArray(Charsets.US_ASCII))
      header.putInt(16) // PCM chunk size
      header.putShort(1) // AudioFormat = 1 (PCM)
      header.putShort(channels.toShort())
      header.putInt(sampleRate)
      header.putInt(sampleRate * channels * (bitsPerSample / 8)) // ByteRate
      header.putShort((channels * (bitsPerSample / 8)).toShort()) // BlockAlign
      header.putShort(bitsPerSample.toShort())
      // data sub-chunk
      header.put("data".toByteArray(Charsets.US_ASCII))
      header.putInt(dataSize)
      fos.write(header.array())

      val chunkFrames = 2048
      val buffer = ByteBuffer.allocate(chunkFrames * channels * 2).order(ByteOrder.LITTLE_ENDIAN)
      val fadeFrames = (sampleRate * 0.4).toInt().coerceAtLeast(1)

      var frame = 0
      while (frame < totalFrames) {
        buffer.clear()
        val end = minOf(frame + chunkFrames, totalFrames)
        while (frame < end) {
          val t = frame.toDouble() / sampleRate.toDouble()
          val env = when {
            frame < fadeFrames -> frame.toDouble() / fadeFrames
            frame > totalFrames - fadeFrames -> (totalFrames - frame).toDouble() / fadeFrames
            else -> 1.0
          }
          // Warm fundamental + subtle fifth and octave harmonic with slow binaural spatial modulation
          val lfo = 0.85 + 0.15 * sin(2.0 * PI * 0.25 * t)
          val fundamentalL = sin(2.0 * PI * baseFrequencyHz * t)
          val fundamentalR = sin(2.0 * PI * (baseFrequencyHz + 1.5) * t)
          val harmonicfifth = 0.28 * sin(2.0 * PI * (baseFrequencyHz * 1.498) * t)
          val subHarmonic = 0.20 * sin(2.0 * PI * (baseFrequencyHz * 0.5) * t)

          val sampleL = ((fundamentalL * 0.52 + harmonicfifth + subHarmonic) * env * lfo * 18000.0)
            .toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()
          val sampleR = ((fundamentalR * 0.52 + harmonicfifth + subHarmonic) * env * lfo * 18000.0)
            .toInt()
            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            .toShort()

          buffer.putShort(sampleL)
          buffer.putShort(sampleR)
          frame++
        }
        fos.write(buffer.array(), 0, buffer.position())
      }
    }

    val uri = Uri.fromFile(outFile)
    val entity = AudioTrackEntity(
      title = title.trim(),
      artist = artist.trim(),
      album = "${baseFrequencyHz.toInt()} Hz Reference Signal",
      uriString = uri.toString(),
      coverUriString = null,
      durationMs = clampedDuration * 1000L,
      mimeType = "audio/wav",
      bitrateKbps = 1411,
      sampleRateHz = sampleRate,
      bitDepth = bitsPerSample,
      channels = channels,
      isLossless = true,
      qualityBadge = "LOSSLESS",
      technicalSpecLabel = "44.1KHZ / 16-BIT WAV",
      dominantColorHex = if (baseFrequencyHz > 400.0) 0xFFFFA877L else 0xFF4F75FFL,
      isStream = false,
    )
    val id = dao.insertTrack(entity)
    entity.copy(id = id)
  }

  private fun queryDisplayName(uri: Uri): String? {
    return runCatching {
      context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
          if (cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0) cursor.getString(idx) else null
          } else null
        }
    }.getOrNull()
  }

  private fun extractDominantColor(bitmap: android.graphics.Bitmap): Long {
    val cx = bitmap.width / 2
    val cy = bitmap.height / 2
    val pixel = bitmap.getPixel(cx.coerceAtLeast(0), cy.coerceAtLeast(0))
    val r = (pixel shr 16) and 0xFF
    val g = (pixel shr 8) and 0xFF
    val b = pixel and 0xFF
    if (r + g + b < 60) {
      return 0xFF4F75FFL
    }
    return (0xFF000000L) or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
  }
}
