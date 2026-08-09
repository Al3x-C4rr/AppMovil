package com.example.playsymphonyapp.songDatabase

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Insert

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
}