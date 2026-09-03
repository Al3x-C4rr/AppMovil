package com.example.playsymphonyapp.songDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Insert

@Dao
interface SongDao{

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<Song>

    @Insert
    suspend fun insertSongs(vararg songs: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSongById(songId: Int)

    @Query("DELETE FROM songs WHERE name = :songName")
    suspend fun deleteSongByName(songName: String)

    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
}