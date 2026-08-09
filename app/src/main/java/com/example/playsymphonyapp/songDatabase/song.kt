package com.example.playsymphonyapp.songDatabase

import androidx.room3.PrimaryKey
import androidx.room3.Entity

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val artist: String,
    val duration: Int,
    val icon: Int,
)