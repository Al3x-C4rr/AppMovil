// Archivo: TracksScreen.kt
// Rediseño profesional "AudioCraft Pro" con Waveform Naranja/Gris.
// Velocidad exclusiva de Línea de Tiempo y controles de reproducción sincronizados.

package com.example.playsymphonyapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.playsymphonyapp.songDatabase.SongDatabase
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText
import com.example.playsymphonyapp.viewmodels.MusicViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun TracksScreen(navController: NavController, viewModel: MusicViewModel) {
    var isMenuVisible by remember { mutableStateOf(false) }
    val song by viewModel.currentSong
    val isPlaying by viewModel.isPlaying
    val context = LocalContext.current
    val database = remember { Room.databaseBuilder(context, SongDatabase::class.java, "song_database").build() }
    
    // Estado del scroll para el contenido del editor
    val scrollState = rememberScrollState()
    
    // Estados del ViewModel
    var inPosition by viewModel.inPosition
    var outPosition by viewModel.outPosition
    var playheadPosition by viewModel.playheadPosition
    var zoomScale by viewModel.zoomScale

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // BARRA SUPERIOR (Control de reproducción sincronizado)
            AudioCraftTopBar(
                title = song?.title ?: "Sin pista", 
                artist = song?.artist ?: "",
                isPlaying = isPlaying,
                onPlayToggle = { viewModel.togglePlayPause() },
                onResetRequested = { viewModel.resetEditor() },
                onUndoRequested = { viewModel.undoLastEdit() }
            )

            Row(modifier = Modifier.fillMaxSize()) {
                if (isMenuVisible) { AudioCraftSidebar() }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .background(Color(0xFF0F1019))
                        .verticalScroll(scrollState) // HABILITAMOS EL SCROLL AQUÍ
                ) {
                    IconButton(onClick = { isMenuVisible = !isMenuVisible }) {
                        Icon(imageVector = if (isMenuVisible) Icons.Default.Close else Icons.Default.Menu, contentDescription = null, tint = LightText)
                    }

                    Text("LÍNEA DE TIEMPO", color = LightText, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    
                    if (song == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Selecciona una canción en la biblioteca", color = Color.Gray)
                        }
                    } else {
                        // SECCIÓN EDITOR PRINCIPAL (Zoom afecta visualmente aquí)
                        TimelineEditorSection(
                            inPos = inPosition,
                            outPos = outPosition,
                            playPos = playheadPosition,
                            zoom = zoomScale,
                            onInChange = { viewModel.saveEditsToHistory(); inPosition = it },
                            onOutChange = { viewModel.saveEditsToHistory(); outPosition = it },
                            onPlayChange = { playheadPosition = it },
                            onZoomChange = { zoomScale = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // SECCIÓN DE TONO (Independiente)
                        PitchControlSection(
                            pitch = viewModel.pitchValue.value,
                            onPitchChange = { viewModel.pitchValue.value = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        FadeButtons()
                    }
                }
            }
        }
    }
}

@Composable
fun AudioCraftTopBar(title: String, artist: String, isPlaying: Boolean, onPlayToggle: () -> Unit, onResetRequested: () -> Unit, onUndoRequested: () -> Unit) {
    var showOptions by remember { mutableStateOf(false) }

    Surface(color = Color(0xFF1A1B2E), modifier = Modifier.fillMaxWidth().height(120.dp), shadowElevation = 8.dp) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("AudioCraft", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("$title · $artist", color = Color.Gray, fontSize = 12.sp)
                }
                Row {
                    IconButton(onClick = onUndoRequested) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Deshacer", tint = LightText) }
                    IconButton(onClick = { showOptions = true }) { Icon(Icons.Default.MoreVert, "Menú", tint = LightText) }
                    
                    DropdownMenu(expanded = showOptions, onDismissRequest = { showOptions = false }, modifier = Modifier.background(Color(0xFF2D2E45))) {
                        DropdownMenuItem(
                            text = { Text("Reiniciar Edición", color = Color.Red) },
                            onClick = { showOptions = false; onResetRequested() },
                            leadingIcon = { Icon(Icons.Default.Refresh, null, tint = Color.Red) }
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null, tint = LightText) }
                    
                    // Botón Play/Stop dinámico del editor
                    IconButton(onClick = onPlayToggle) { 
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, 
                            contentDescription = "Play/Stop", 
                            tint = if (isPlaying) Color.Red else Color.Green 
                        ) 
                    }
                    
                    IconButton(onClick = {}) { Icon(Icons.Default.FiberManualRecord, null, tint = Color.Red) }
                }
                Text("01:02:03", color = LightText, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0xFF2D2E45), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
fun TimelineEditorSection(
    inPos: Float, outPos: Float, playPos: Float, zoom: Float,
    onInChange: (Float) -> Unit, onOutChange: (Float) -> Unit, onPlayChange: (Float) -> Unit, onZoomChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp)).padding(12.dp)) {
        // Zoom exclusivo para la línea de tiempo
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ZoomIn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Row(modifier = Modifier.background(Color(0xFF2D2E45), RoundedCornerShape(8.dp)).padding(2.dp)) {
                listOf(0.5f, 1f, 2f).forEach { z ->
                    Box(modifier = Modifier
                        .background(if (zoom == z) Color(0xFF6200EE) else Color.Transparent, RoundedCornerShape(6.dp))
                        .clickable { onZoomChange(z) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("${if(z < 1) "0.5" else z.toInt()}x", color = LightText, fontSize = 10.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.GridView, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Text(" Snap", color = Color.Gray, fontSize = 10.sp)
        }

        Spacer(Modifier.height(12.dp))

        // WAVEFORM EDITOR
        WaveformEditorPro(inPos, outPos, playPos, onInChange, onOutChange, onPlayChange)

        Spacer(Modifier.height(12.dp))

        // STRIP DE INFORMACIÓN
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("IN 0:31", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("2:22 seleccionado", color = Color.Gray, fontSize = 11.sp)
            Text("2:53 OUT", color = Color(0xFFFF9800), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // BOTONES ACCIÓN RÁPIDA
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onInChange(0f); onOutChange(1f) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E45))) {
                Icon(Icons.Default.SelectAll, null, modifier = Modifier.size(14.dp))
                Text(" Todo", fontSize = 10.sp)
            }
            Button(onClick = { onPlayChange(inPos) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E45))) {
                Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(14.dp))
                Text(" IN", fontSize = 10.sp)
            }
            Button(onClick = { onPlayChange(outPos) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2E45))) {
                Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(14.dp))
                Text(" OUT", fontSize = 10.sp)
            }
        }
        
        Spacer(Modifier.height(8.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))) {
            Icon(Icons.Default.ContentCut, null, modifier = Modifier.size(18.dp), tint = Color.Black)
            Spacer(Modifier.width(8.dp))
            Text("CORTAR REGIÓN", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun WaveformEditorPro(inPos: Float, outPos: Float, playPos: Float, onInChange: (Float) -> Unit, onOutChange: (Float) -> Unit, onPlayChange: (Float) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))) {
        val widthPx = constraints.maxWidth.toFloat()
        
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("0:00", "0:28", "0:56", "1:23", "1:51", "2:19", "2:47", "3:14").forEach {
                Text(it, color = Color.Gray, fontSize = 8.sp)
            }
        }

        Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) { detectDragGestures { change, _ -> onPlayChange(change.position.x / widthPx) } }) {
            val barWidth = 3f
            val spacing = 3f
            val centerY = size.height / 2
            
            for (i in 0 until (size.width / (barWidth + spacing)).toInt()) {
                val x = i * (barWidth + spacing)
                val h = (size.height * 0.2f) + (i % 5) * 15f
                val isSelected = (x / size.width) in inPos..outPos
                
                drawLine(
                    color = if (isSelected) Color(0xFFFF9800) else Color.DarkGray, // Naranja Pro
                    start = Offset(x, centerY - h/2),
                    end = Offset(x, centerY + h/2),
                    strokeWidth = barWidth
                )
            }
            
            drawRect(Color.Black.copy(alpha = 0.5f), size = Size(inPos * size.width, size.height))
            drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(outPos * size.width, 0f), size = Size((1f - outPos) * size.width, size.height))
        }

        // HANDLE IN
        Box(modifier = Modifier.offset(x = (maxWidth * inPos) - 15.dp).width(30.dp).fillMaxHeight()
            .pointerInput(Unit) { detectDragGestures { change, _ -> onInChange((change.position.x / widthPx) + inPos) } }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = Color.Green, shape = RoundedCornerShape(4.dp)) {
                    Text("IN", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Spacer(modifier = Modifier.width(3.dp).fillMaxHeight().background(Color.Green))
            }
        }

        // HANDLE OUT
        Box(modifier = Modifier.offset(x = (maxWidth * outPos) - 15.dp).width(30.dp).fillMaxHeight()
            .pointerInput(Unit) { detectDragGestures { change, _ -> onOutChange((change.position.x / widthPx) + outPos) } }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(color = Color(0xFFFF9800), shape = RoundedCornerShape(4.dp)) {
                    Text("OUT", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                }
                Spacer(modifier = Modifier.width(3.dp).fillMaxHeight().background(Color(0xFFFF9800)))
            }
        }

        // PLAYHEAD
        Box(modifier = Modifier.offset(x = (maxWidth * playPos) - 1.5.dp).width(3.dp).fillMaxHeight().background(Color.Red)) {
            Box(Modifier.size(10.dp).background(Color.Red, RoundedCornerShape(2.dp)).align(Alignment.TopCenter).offset(y = 20.dp))
        }
    }
}

@Composable
fun PitchControlSection(pitch: Float, onPitchChange: (Float) -> Unit) {
    // Colores dinámicos: Graves (Cian/Verde) - Neutral (Morado) - Agudos (Naranja)
    val trackColor = when {
        pitch < -2f -> Color(0xFF03DAC6)
        pitch > 2f -> Color(0xFFFF9800)
        else -> Color(0xFFBB86FC)
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Tune, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Text(
                text = " Tono: ${pitch.toInt()} semitonos", 
                color = LightText, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = if (pitch > 0) "AGUDO" else if (pitch < 0) "GRAVE" else "ORIGINAL",
                color = trackColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
        
        Slider(
            value = pitch,
            onValueChange = onPitchChange,
            valueRange = -12f..12f,
            steps = 24, 
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("-12 (Grave)", color = Color.Gray, fontSize = 9.sp)
            Text("0", color = Color.Gray, fontSize = 9.sp)
            Text("+12 (Agudo)", color = Color.Gray, fontSize = 9.sp)
        }
    }
}

@Composable
fun FadeButtons() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).padding(4.dp), border = BorderStroke(1.dp, Color(0xFFBB86FC))) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(16.dp))
            Text(" Fade In", color = LightText)
        }
        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).padding(4.dp), border = BorderStroke(1.dp, Color(0xFFBB86FC))) {
            Icon(Icons.AutoMirrored.Filled.TrendingDown, null, modifier = Modifier.size(16.dp))
            Text(" Fade Out", color = LightText)
        }
    }
}

@Composable
fun AudioCraftSidebar() {
    Column(modifier = Modifier.fillMaxHeight().width(80.dp).background(Color(0xFF121212)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        SidebarItem(Icons.AutoMirrored.Filled.QueueMusic, "Samples")
        SidebarItem(Icons.Default.Timeline, "Timeline")
        SidebarItem(Icons.Default.GraphicEq, "Tracks", isSelected = true)
        SidebarItem(Icons.Default.Tune, "Mixer")
        SidebarItem(Icons.Default.FileUpload, "Export")
    }
}

@Composable
fun SidebarItem(icon: ImageVector, label: String, isSelected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Color(0xFFBB86FC) else Color.Gray, modifier = Modifier.size(28.dp))
        Text(label, color = if (isSelected) Color(0xFFBB86FC) else Color.Gray, fontSize = 10.sp)
    }
}
