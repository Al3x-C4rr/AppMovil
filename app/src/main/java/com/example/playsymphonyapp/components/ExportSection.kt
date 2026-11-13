package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.playsymphonyapp.ui.theme.AccentColor

@Composable
fun ExportSection(modifier: Modifier = Modifier) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var artista by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = artista,
            onValueChange = { artista = it },
            label = { Text("Artista") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Formato: MP3")

        Button(
            onClick = { /* Aquí irá la exportación */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar MP3")
        }
    }
}