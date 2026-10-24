package com.example.playsymphonyapp.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.room.Room
import com.example.playsymphonyapp.music.LocalMusicScanner
import com.example.playsymphonyapp.songDatabase.Song
import com.example.playsymphonyapp.songDatabase.SongDatabase
import com.example.playsymphonyapp.ui.theme.*
import com.example.playsymphonyapp.viewmodels.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun LibrarySongs(
    viewModel: MusicViewModel,
    onSongsCounted: (Int) -> Unit = {},
    onSongSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val database = remember {
        Room.databaseBuilder(
            context,
            SongDatabase::class.java,
            "song_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val dao = remember(database) {
        database.songDao()
    }

    val songs by viewModel.playlist

    /*
     * Busca canciones locales y registra solamente las nuevas.
     * No elimina automáticamente las canciones existentes.
     */
    val syncLibrary: suspend () -> Unit = {
        if (hasAudioPermission(context)) {
            try {
                val scanner = LocalMusicScanner()
                val scannedSongs = scanner.scanSongs(context)
                val existingSongs = dao.getAllSongs()

                val existingPaths = existingSongs
                    .map { it.filePath }
                    .toSet()

                val existingTitlesAndArtists = existingSongs
                    .map { song ->
                        "${song.title.trim().lowercase()}|" +
                                song.artist.trim().lowercase()
                    }
                    .toSet()

                val newSongs = scannedSongs.filter { song ->
                    val songKey =
                        "${song.title.trim().lowercase()}|" +
                                song.artist.trim().lowercase()

                    song.filePath !in existingPaths &&
                            songKey !in existingTitlesAndArtists
                }

                if (newSongs.isNotEmpty()) {
                    dao.insertSongs(*newSongs.toTypedArray())
                }

                viewModel.loadSongs(dao)

            } catch (exception: SecurityException) {
                // El permiso puede haber sido revocado.
                viewModel.loadSongs(dao)
            }
        } else {
            // Mostramos lo que ya esté guardado, si existe.
            viewModel.loadSongs(dao)
        }
    }

    /*
     * Primera carga de la biblioteca.
     */
    LaunchedEffect(Unit) {
        syncLibrary()
    }

    /*
     * Vuelve a comprobar el permiso y la biblioteca cuando
     * la aplicación regresa al primer plano.
     */
    val currentSyncLibrary by rememberUpdatedState(syncLibrary)

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    currentSyncLibrary()
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    /*
     * Informa cuántas canciones hay en la biblioteca.
     */
    LaunchedEffect(songs.size) {
        onSongsCounted(songs.size)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(
            items = songs,
            key = { song -> song.id }
        ) { song ->

            val isCurrent =
                viewModel.currentSong.value?.id == song.id

            SongCard(
                song = song,
                iconColor = if (isCurrent) AccentColor else IconPurple,
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

/*
 * Comprueba el permiso correspondiente a la versión de Android.
 */
private fun hasAudioPermission(context: Context): Boolean {
    val permission = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    ) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    return ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun SongCard(
    song: Song,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
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

            IconButton(
                onClick = {
                    // Opciones extra pendientes de implementar.
                }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones",
                    tint = Color.Gray
                )
            }
        }
    }
}