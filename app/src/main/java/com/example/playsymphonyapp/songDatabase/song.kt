package com.example.playsymphonyapp.songDatabase

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val artist: String,
    val duration: Int,
    val icon: Int,
)