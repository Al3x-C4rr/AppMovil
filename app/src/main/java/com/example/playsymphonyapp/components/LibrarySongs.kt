// Archivo: LibrarySongs.kt
// Muestra la lista de canciones MP3 reales y gestiona la reproducción.

package com.example.playsymphonyapp.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.playsymphonyapp.songDatabase.*
import com.example.playsymphonyapp.ui.theme.*
import com.example.playsymphonyapp.viewmodels.MusicViewModel

@Composable
fun LibrarySongs(
    viewModel: MusicViewModel,
    onSongsCounted: (Int) -> Unit = {},
    onSongSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { 
        Room.databaseBuilder(context, SongDatabase::class.java, "song_database")
            .fallbackToDestructiveMigration()
            .build() 
    }
    val songs by viewModel.playlist

    // Carga inicial y sincronización limpia (Sin borrado automático forzado)
    LaunchedEffect(Unit) {
        val dao = database.songDao()
        val allSongs = dao.getAllSongs()
        
        // Si no hay canciones o estamos probando nuevas entradas (V6), insertamos los 14 ejemplos
        if (allSongs.size < 14) {
            dao.deleteAllSongs()
            dao.insertSongs(song1, song2, song3, song4, song5, song6, song7, song8, song9, song10, song11, song12, song13, song14)
        }
        
        // Sincronizamos la lista del ViewModel
        viewModel.loadSongs(dao)
    }
    
    // Notifica el conteo cuando cambie la lista
    LaunchedEffect(songs) {
        onSongsCounted(songs.size)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(songs) { song ->
            val isCurrent = viewModel.currentSong.value?.id == song.id
            
            SongCard(
                song = song,
                iconColor = if (isCurrent) AccentColor else IconPurple, // Resalta la actual
                onClick = {
                    viewModel.playSong(song)
                    onSongSelected()
                }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                thickness = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun SongCard(song: Song, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(25.dp),
                color = iconColor.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
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
                if (song.isEdited) {
                    Text(
                        text = "Editado",
                        color = AccentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = song.duration,
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            IconButton(onClick = { /* Opciones extra */ }) {
                Icon(Icons.Default.MoreVert, null, tint = Color.Gray)
            }
        }
    }
}
