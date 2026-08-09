package com.example.playsymphonyapp.songDatabase

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [Song::class],
    version = 1
)
abstract class SongDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}