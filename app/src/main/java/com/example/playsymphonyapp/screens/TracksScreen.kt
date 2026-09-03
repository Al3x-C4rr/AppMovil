// Archivo: TracksScreen.kt
// Este archivo contiene el rediseño profesional del editor de audio ("AudioCraft").
// Incluye barra lateral, métricas de audio y controles de edición avanzados.

package com.example.playsymphonyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

// Función Composable principal para la pantalla del Editor
@Composable
fun TracksScreen(navController: NavController) {
    // Silenciamos advertencia si no se usa directamente
    val _nav = navController
    // Estado para controlar la visibilidad del menú lateral (Sidebar)
    var isMenuVisible by remember { mutableStateOf(value = false) }
    // Estado para la pestaña de instrumentos seleccionada
    var selectedTab by remember { mutableIntStateOf(value = 0) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. BARRA SUPERIOR (Métricas y reproducción rápida)
            AudioCraftTopBar()

            Row(modifier = Modifier.fillMaxSize()) {
                
                // 2. BARRA LATERAL (Samples, Timeline, etc.) - Ocultable
                if (isMenuVisible) {
                    AudioCraftSidebar()
                }

                // 3. CONTENIDO CENTRAL DEL EDITOR
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    
                    // Botón tipo "hamburguesa" para mostrar/ocultar el sidebar lateral
                    IconButton(onClick = { isMenuVisible = !isMenuVisible }) {
                        Icon(
                            imageVector = if (isMenuVisible) Icons.Default.Close else Icons.Default.Menu,
                            contentDescription = "Alternar Menú",
                            tint = LightText,
                        )
                    }

                    // Pestañas centrales de selección de instrumento
                    AudioCraftTabs(selectedTab) { selectedTab = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Área de edición de onda de audio y controles de región
                    TrackEditorContent()
                }
            }
        }
    }
}

// Componente para la barra superior con el nombre del audio y métricas
@Composable
fun AudioCraftTopBar() {
    Surface(
        color = Color(0xFF1E1E1E),
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("AudioCraft", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Noche Infinita", color = LightText, fontSize = 12.sp)
                Text("120 BPM", color = Color(0xFFFF9800), fontSize = 10.sp)
            }

            // Muestra el tiempo actual de reproducción
            Text("01:02:03", color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Medium)

            Row {
                Icon(Icons.Default.PlayArrow, "Play", tint = Color.Green, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Stop, "Stop", tint = Color.Red, modifier = Modifier.size(28.dp))
            }
        }
    }
}

// Menú lateral con opciones de flujo de trabajo
@Composable
fun AudioCraftSidebar() {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SidebarItem(Icons.AutoMirrored.Filled.QueueMusic, "Samples")
        SidebarItem(Icons.Default.Timeline, "Timeline")
        SidebarItem(Icons.Default.GraphicEq, "Tracks", isSelected = true)
        SidebarItem(Icons.Default.Tune, "Mixer")
        SidebarItem(Icons.Default.FileUpload, "Export")
    }
}

// Item individual de la barra lateral
@Composable
fun SidebarItem(icon: ImageVector, label: String, isSelected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFBB86FC) else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Text(label, color = if (isSelected) Color(0xFFBB86FC) else Color.Gray, fontSize = 10.sp)
    }
}

// Sistema de pestañas para cambiar entre capas de audio (Drums, Synth, etc.)
@Composable
fun AudioCraftTabs(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("Drums", "Bass", "Synth", "Voz", "FX")
    SecondaryScrollableTabRow(
        selectedTabIndex = selected,
        containerColor = Color.Transparent,
        contentColor = Color(0xFFBB86FC),
        edgePadding = 0.dp,
        divider = {},
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selected == index,
                onClick = { onSelect(index) },
                text = { Text(title, fontSize = 12.sp, fontWeight = if (selected == index) FontWeight.Bold else FontWeight.Normal) },
            )
        }
    }
}

// Contenido principal que muestra la onda y los controles de recorte/velocidad/tono
@Composable
fun TrackEditorContent() {
    Card(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252525)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Track Editor - Mezcla Principal", color = LightText, fontWeight = FontWeight.Bold)
            
            // Representación visual de la onda de audio (Simulada)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(vertical = 12.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(30) {
                        Box(modifier = Modifier
                            .width(3.dp)
                            .height((20..70).random().dp)
                            .background(Color(0xFFBB86FC))
                            .clip(RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }

            // Controles de Recorte de Región
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Recortar región", color = Color.Gray, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("IN: 12", color = LightText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("OUT: 80", color = LightText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Métrica de velocidad y tono
                Column(horizontalAlignment = Alignment.End) {
                    Text("Velocidad: 1x", color = Color(0xFFFF9800), fontSize = 12.sp)
                    Text("Tono (semitonos): 0", color = Color.Cyan, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de efectos Fade In / Fade Out
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {}, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3700B3)),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("Fade In", fontSize = 12.sp)
                }
                Button(
                    onClick = {}, 
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3700B3)),
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                ) {
                    Text("Fade Out", fontSize = 12.sp)
                }
            }
        }
    }
}

// Vista previa para el entorno de desarrollo
@Preview(showBackground = true)
@Composable
fun TracksScreenPreview() {
    TracksScreen(rememberNavController())
}
