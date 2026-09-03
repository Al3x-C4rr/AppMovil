// Archivo: LibrarySongs.kt
// Este componente muestra la lista de canciones en formato de tarjetas horizontales.
// Implementa el diseño oscuro con iconos de colores y menú de opciones.

package com.example.playsymphonyapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.playsymphonyapp.songDatabase.*
import com.example.playsymphonyapp.ui.theme.*
import java.util.Locale

@Composable
fun LibrarySongs(onSongsCounted: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val database = remember { Room.databaseBuilder(context, SongDatabase::class.java, "song_database").build() }
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }

    // Carga inicial y sincronización de datos con el código (songList.kt)
    LaunchedEffect(Unit) {
        // En fase de desarrollo, limpiamos y volvemos a cargar para reflejar cambios en songList.kt
        database.songDao().deleteAllSongs()
        database.songDao().insertSongs(song1, song2, song3, song4, song5, song6, song7)
        
        // Obtenemos la lista actualizada
        songs = database.songDao().getAllSongs()
        
        // Notifica el conteo total para el encabezado dinámico
        onSongsCounted(songs.size)
    }

    // Lista desplazable de canciones
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(songs.indices.toList()) { index ->
            val song = songs[index]
            // Selecciona un color de icono basado en el índice
            val iconColor = when (index % 4) {
                0 -> IconPurple
                1 -> IconBlue
                2 -> IconOrange
                else -> IconGold
            }
            SongCard(song, iconColor)
            
            // Línea divisoria sutil entre tarjetas
            if (index < songs.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 0.5.dp,
                    color = Color.Gray.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// Componente para la tarjeta individual de cada canción
@Composable
fun SongCard(song: Song, iconColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de nota musical con fondo circular de color
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = iconColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Nota",
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información de la canción (Nombre y Artista)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.name,
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.artist,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }

            // Duración y menú de opciones
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Convierte duración de segundos a formato MM:SS
                val minutes = song.duration / 60
                val seconds = song.duration % 60
                val timeString = String.format(java.util.Locale.getDefault(), "%d:%02d", minutes, seconds)
                
                Text(
                    text = timeString,
                    color = Color.Gray,
                    fontSize = 12.sp,
                )
                
                IconButton(onClick = { /* Opciones */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LibrarySongsPreview() {
    LibrarySongs()
}
