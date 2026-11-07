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

@Composable
fun TracksWorkspace(
    isMenuVisible: Boolean,
    selectedSection: String,
    hasSong: Boolean,
    onMenuToggle: () -> Unit,
    onSectionSelected: (String) -> Unit,
    recorder: Recorder,
    pistas: List<TimelineTrack>,
    posicion: Float,
    onPositionChange: (Float) -> Unit,
    tracksContent: @Composable () -> Unit
) {
    val scrollState = rememberScrollState()

    val ultimoFinal = pistas
        .flatMap { it.clips }
        .maxOfOrNull { it.start + it.duration }
        ?: 0f

    val duracion = maxOf(
        15,
        ultimoFinal.toInt() + 1
    )

    Row(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    if (
                        selectedSection == "Samples" ||
                        selectedSection == "Timeline"
                    ) {
                        Modifier
                    } else {
                        Modifier.verticalScroll(scrollState)
                    }
                )
        ) {
            IconButton(
                onClick = onMenuToggle
            ) {
                Icon(
                    imageVector = if (isMenuVisible) {
                        Icons.Default.Close
                    } else {
                        Icons.Default.Menu
                    },
                    contentDescription =
                        "Mostrar u ocultar secciones",
                    tint = LightText
                )
            }

            Text(
                text = selectedSection,
                color = LightText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )

            when (selectedSection) {
                "Samples" -> {
                    PianoAdaptable(
                        recorder = recorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                "Timeline" -> {
                    TimeLineSection(
                        tracks = pistas,
                        durationSeconds = duracion,
                        positionSeconds = posicion,
                        onPositionChange = onPositionChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }

                "Mixer" -> {
                    MixerSection(
                        pistas = pistas
                    )
                }

                "Export" -> {
                    if (hasSong) {
                        ExportSection()
                    } else {
                        newProyectSuggestion()
                    }
                }

                "Tracks" -> {
                    if (hasSong) {
                        tracksContent()
                    } else {
                        newProyectSuggestion()
                    }
                }
            }
        }
    }
}