// Archivo: SongDao.kt
// Interfaz para el acceso a datos de las canciones con Room.

package com.example.playsymphonyapp.songDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface SongDao {

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongById(songId: Int): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(vararg songs: Song)

    @Query("UPDATE songs SET filePath = :newPath, isEdited = 1, editDetails = :details WHERE id = :songId")
    suspend fun updateEditedSong(songId: Int, newPath: String, details: String)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: Int)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}
