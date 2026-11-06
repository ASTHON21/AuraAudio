package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {
  @Query("SELECT * FROM audio_tracks ORDER BY addedAt DESC")
  fun getAllTracks(): Flow<List<AudioTrackEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrack(track: AudioTrackEntity): Long

  @Update
  suspend fun updateTrack(track: AudioTrackEntity)

  @Query("DELETE FROM audio_tracks WHERE id = :id")
  suspend fun deleteTrackById(id: Long)

  @Query("UPDATE audio_tracks SET isFavorite = :isFavorite WHERE id = :id")
  suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
