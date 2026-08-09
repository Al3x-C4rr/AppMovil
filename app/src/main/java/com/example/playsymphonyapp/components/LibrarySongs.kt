package com.example.playsymphonyapp.components

// Importacion de la base de datos de canciones creadas
import androidx.compose.foundation.layout.Column
import com.example.playsymphonyapp.songDatabase.Song

// Importaciones necesarias para el diseño y funcionalidad de la pantalla
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.room3.Room
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.playsymphonyapp.songDatabase.SongDatabase
import com.example.playsymphonyapp.songDatabase.song1
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

import com.example.playsymphonyapp.songDatabase.song2
import com.example.playsymphonyapp.songDatabase.song3
import com.example.playsymphonyapp.songDatabase.song4
import com.example.playsymphonyapp.songDatabase.song5
import com.example.playsymphonyapp.songDatabase.song6
import com.example.playsymphonyapp.songDatabase.song7
import com.example.playsymphonyapp.songDatabase.song8
import com.example.playsymphonyapp.songDatabase.song9
import com.example.playsymphonyapp.songDatabase.song10
import com.example.playsymphonyapp.songDatabase.song11
import com.example.playsymphonyapp.songDatabase.song12

// Función Composable que define el diseño del encabezado de la biblioteca
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibrarySongs() {

    val context = LocalContext.current

    val database = remember {Room.databaseBuilder(context, SongDatabase::class.java,"song_database").build()}

    var songs by remember { mutableStateOf<List<Song>>(emptyList())}

    LaunchedEffect(Unit) {

        database.songDao().insertSongs(song12)

        songs = database.songDao().getAllSongs() }

    LazyColumn { items(songs) {
        song -> Column(
        modifier = Modifier
            .fillMaxWidth()  // Ocupa todo el ancho disponible
            .background(DarkBackground)  // Fondo oscuro
            .padding(16.dp),  // Espacio de 16dp en todos los lados
        verticalArrangement = Arrangement.Top,  // Alinea los hijos en la parte superior
        horizontalAlignment = Alignment.Start  // Alinea los hijos a la izquierda

    ) {
            Row() {
                Text(
                    text = song.name,  // Texto a mostrar
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 15.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
                )
                Text(
                    text = song.artist,  // Texto a mostrar
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 15.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
                )
                Text(
                    text = song.duration.toString(),  // Texto a mostrar
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 15.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
                )
                Image(
                    painter = painterResource(song.icon),
                    contentDescription = song.name,
                    modifier = Modifier.width(50.dp).height(50.dp).aspectRatio(1f),

                )
            }
            }
        }
    }
}
@Preview(showBackground = true)  // Muestra el fondo en la vista previa
@Composable
fun LibrarySongsPreview() {
    LibrarySongs()  // Muestra el MediaControl en la vista previa
}