// Archivo: song.kt
// Entidad que representa una canción en la base de datos Room.
// Incluye soporte para rutas de archivos MP3 y metadatos de edición.

package com.example.playsymphonyapp.songDatabase

import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,        // Nombre de la canción
    val artist: String,       // Nombre del artista
    val duration: String,     // Duración formateada (ej: "3:42")
    val filePath: String,     // Ruta absoluta al archivo MP3 en el dispositivo
    val icon: Int,            // Recurso ID para el icono visual
    val isEdited: Boolean = false,    // Indica si ha sido procesada por FFmpeg
    val editDetails: String? = null   // Descripción de los cambios hechos
)
