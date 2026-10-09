// Archivo: PlayerScreen.kt
// Pantalla de reproducción conectada al motor de audio real.

package com.example.playsymphonyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.playsymphonyapp.components.navigateSafely
import com.example.playsymphonyapp.ui.theme.AccentColor
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText
import com.example.playsymphonyapp.viewmodels.MusicViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun PlayerScreen(navController: NavController, viewModel: MusicViewModel) {
    val song by viewModel.currentSong
    val isPlaying by viewModel.isPlaying
    val shuffle by viewModel.shuffleEnabled
    val repeat by viewModel.repeatEnabled
    
    // Estado del scroll para permitir desplazamiento en pantallas pequeñas
    val scrollState = rememberScrollState()
    
    // Si no hay canción seleccionada, mostramos una pantalla vacía pero con navegación
    if (song == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(DarkBackground),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.MusicNote, null, tint = Color.Gray, modifier = Modifier.size(100.dp))
            Spacer(Modifier.height(16.dp))
            Text("Selecciona una canción en la biblioteca", color = Color.Gray)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { navController.navigate("library") }) {
                Text("Ir a Biblioteca")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
            .verticalScroll(scrollState), // HABILITAMOS EL SCROLL AQUÍ
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Reproduciendo",
            color = LightText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Imagen de carátula
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(150.dp),
                tint = AccentColor.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info de la canción
        Text(text = song!!.title, color = LightText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = song!!.artist, color = Color.Gray, fontSize = 16.sp)

        // --- CONEXIÓN CON EL PROGRESO REAL ---
        val currentPos by viewModel.currentPosition
        val currentTime by viewModel.currentTime
        val totalDur by viewModel.totalDuration

        Spacer(modifier = Modifier.height(32.dp))

        // El Slider ahora se mueve solo y permite saltar en la canción
        Slider(
            value = currentPos,
            onValueChange = { viewModel.seekTo(it) },
            colors = SliderDefaults.colors(thumbColor = AccentColor, activeTrackColor = AccentColor),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = currentTime, color = Color.Gray, fontSize = 12.sp)
            Text(text = totalDur, color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controles Multimedia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.shuffleEnabled.value = !shuffle }) {
                Icon(Icons.Default.Shuffle, null, tint = if (shuffle) AccentColor else Color.Gray)
            }
            IconButton(onClick = { viewModel.playPrevious() }) {
                Icon(Icons.Default.SkipPrevious, null, tint = LightText, modifier = Modifier.size(40.dp))
            }
            
            FilledIconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.size(70.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentColor)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = { viewModel.playNext() }) {
                Icon(Icons.Default.SkipNext, null, tint = LightText, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = { viewModel.repeatEnabled.value = !repeat }) {
                Icon(Icons.Default.Repeat, null, tint = if (repeat) AccentColor else Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { 
                // Pausamos la música antes de ir al editor
                if (isPlaying) viewModel.togglePlayPause()
                navigateSafely(navController, "tracks") 
            },
            modifier = Modifier.fillMaxWidth().height(45.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E45))
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("ABRIR EN EDITOR", fontWeight = FontWeight.Bold)
        }
    }
}
