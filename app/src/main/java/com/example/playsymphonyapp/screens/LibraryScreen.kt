// Archivo: LibraryScreen.kt
// Pantalla de biblioteca con soporte para reproducción real y ViewModel.

package com.example.playsymphonyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.playsymphonyapp.components.LibraryHeader
import com.example.playsymphonyapp.components.LibrarySongs
import com.example.playsymphonyapp.ui.theme.AccentColor
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.viewmodels.MusicViewModel

@Composable
fun LibraryScreen(navController: NavController, viewModel: MusicViewModel) {
    var songCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(DarkBackground),
    ) {
        LibraryHeader(navController = navController)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = "$songCount CANCIONES",
                color = AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // Pasamos el viewModel y navegamos al reproductor al seleccionar una canción
            LibrarySongs(
                viewModel = viewModel,
                onSongsCounted = { songCount = it },
                onSongSelected = {
                    navController.navigate("player") {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
