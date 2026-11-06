package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_tracks")
data class AudioTrackEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val artist: String,
  val album: String,
  val uriString: String,
  val coverUriString: String? = null,
  val durationMs: Long,
  val mimeType: String,
  val bitrateKbps: Int,
  val sampleRateHz: Int,
  val bitDepth: Int,
  val channels: Int,
  val isLossless: Boolean,
  val qualityBadge: String,
  val technicalSpecLabel: String,
  val dominantColorHex: Long = 0xFF4F75FF,
  val isFavorite: Boolean = false,
  val isStream: Boolean = false,
  val addedAt: Long = System.currentTimeMillis(),
)
