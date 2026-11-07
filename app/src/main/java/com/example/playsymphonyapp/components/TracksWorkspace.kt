package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playsymphonyapp.ui.theme.LightText
import androidx.compose.runtime.*

@Composable
fun TracksWorkspace(
    isMenuVisible: Boolean,
    selectedSection: String,
    hasSong: Boolean,
    onMenuToggle: () -> Unit,
    onSectionSelected: (String) -> Unit,
    recorder: Recorder,
    pistas: List<TimelineTrack>,
    tracksContent: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()
    var posicion by remember { mutableStateOf(0f) }

    val ultimoFinal = pistas
        .flatMap { it.clips }
        .maxOfOrNull { it.start + it.duration } ?: 0f

    val duracion = maxOf(15, ultimoFinal.toInt() + 1)

    Row(modifier = Modifier.fillMaxSize()) {
        if (isMenuVisible) {
            AudioCraftSidebar(
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color(0xFF0F1019))
                .then(
                    if (selectedSection == "Samples") {
                        Modifier
                    } else {
                        Modifier.verticalScroll(scrollState)
                    }
                )
        ) { Row(){IconButton(onClick = onMenuToggle) {
            Icon(
                imageVector = if (isMenuVisible) {
                    Icons.Default.Close
                } else {
                    Icons.Default.Menu
                },
                contentDescription = null,
                tint = LightText
            )
        }

            Text(
                text = selectedSection,
                color = LightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp).padding(top = 12.dp).padding(bottom = 13.dp)
            )}


            when (selectedSection) {
                "Samples" -> {
                    PianoAdaptable(
                        recorder = recorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                "Tracks" -> {
                    if (hasSong) {
                        tracksContent()
                    } else {
                        newProyectSuggestion()
                    }
                }

                "Timeline" -> {
                    TimeLineSection(
                        tracks = pistas,
                        durationSeconds = duracion,
                        positionSeconds = posicion,
                        onPositionChange = { posicion = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }

                "Mixer" -> {
                    if (hasSong) {
                        MixerSection(pistas = pistas)
                    } else {
                        newProyectSuggestion()
                    }
                }

                "Export" -> {
                    if (hasSong) {
                        ExportSection()
                    } else {
                        newProyectSuggestion()
                    }
                }

                else -> {
                    Text(
                        text = when (selectedSection) {
                            else -> ""
                        },
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}