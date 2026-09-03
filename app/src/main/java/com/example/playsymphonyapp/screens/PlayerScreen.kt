// Archivo: PlayerScreen.kt
// Pantalla de reproducción de música con controles avanzados y diseño moderno.

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.playsymphonyapp.R
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

import com.example.playsymphonyapp.components.navigateSafely

@Composable
fun PlayerScreen(navController: NavController) {
    // Estado para controlar reproducción y progreso
    var isPlaying by remember { mutableStateOf(value = false) }
    var progress by remember { mutableFloatStateOf(value = 0.4f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Título de la pantalla
        Text(
            text = "Reproductor",
            color = LightText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Imagen de la carátula (Grande y redondeada)
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // Imagen de ejemplo (puedes usar una real de tus recursos)
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Carátula",
                modifier = Modifier.size(150.dp),
                tint = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Información de la canción
        Text(
            text = "Noche Infinita",
            color = LightText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "AudioCraft Studio",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Modulador de progreso (Slider)
        Slider(
            value = progress,
            onValueChange = { progress = it },
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFBB86FC),
                activeTrackColor = Color(0xFFBB86FC)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        // Tiempos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "01:25", color = Color.Gray, fontSize = 12.sp)
            Text(text = "03:45", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controles de reproducción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Shuffle */ }) {
                Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = Color.Gray)
            }
            IconButton(onClick = { /* Previous */ }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = LightText, modifier = Modifier.size(40.dp))
            }
            
            // Botón Pausa/Play
            FilledIconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.size(70.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFBB86FC))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir/Pausa",
                    modifier = Modifier.size(40.dp)
                )
            }

            IconButton(onClick = { /* Next */ }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = LightText, modifier = Modifier.size(40.dp))
            }
            IconButton(onClick = { /* Repeat */ }) {
                Icon(Icons.Default.Repeat, contentDescription = "Repetir", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón inferior delgado de "Editar" sincronizado
        Button(
            onClick = { navigateSafely(navController, "tracks") },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3700B3))
        ) {
            Text(text = "EDITAR", color = LightText, fontWeight = FontWeight.Bold)
        }
    }
}
