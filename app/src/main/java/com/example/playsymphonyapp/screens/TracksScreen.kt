// Archivo: TracksScreen.kt
// Rediseño profesional "AudioCraft Pro" con Waveform Naranja/Gris.
// Velocidad exclusiva de Línea de Tiempo y controles de reproducción sincronizados.

package com.example.playsymphonyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.playsymphonyapp.components.AudioCraftTopBar
import com.example.playsymphonyapp.components.TracksEditorContent
import com.example.playsymphonyapp.components.TracksWorkspace
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.viewmodels.MusicViewModel
import com.example.playsymphonyapp.components.Recorder
import com.example.playsymphonyapp.components.TimelineTrack

@Composable
fun TracksScreen(
    navController: NavController,
    viewModel: MusicViewModel
) {
    var isMenuVisible by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf("Tracks") }

    val song by viewModel.currentSong
    val isPlaying by viewModel.isPlaying

    var inPosition by viewModel.inPosition
    var outPosition by viewModel.outPosition
    var playheadPosition by viewModel.playheadPosition
    var zoomScale by viewModel.zoomScale
    val recorder = remember { Recorder() }
    val pistas = remember { mutableStateListOf<TimelineTrack>() }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AudioCraftTopBar(
            title = song?.title ?: "Sin pista",
            artist = song?.artist ?: "",
            isPlaying = isPlaying,
            onPlayToggle = { viewModel.togglePlayPause() },
            onResetRequested = { viewModel.resetEditor() },
            onUndoRequested = { viewModel.undoLastEdit() },
            recorder = recorder,
            onTrackRecorded = { pista ->
                pistas.add(pista)
            }
        )

        TracksWorkspace(
            isMenuVisible = isMenuVisible,
            selectedSection = selectedSection,
            hasSong = song != null,
            onMenuToggle = { isMenuVisible = !isMenuVisible },
            onSectionSelected = { selectedSection = it },
            recorder = recorder,
            pistas = pistas
        ) {
            TracksEditorContent(
                inPos = inPosition,
                outPos = outPosition,
                playPos = playheadPosition,
                zoom = zoomScale,
                pitch = viewModel.pitchValue.value,
                viewModel = viewModel,
                onInChange = {
                    viewModel.saveEditsToHistory()
                    inPosition = it
                },
                onOutChange = {
                    viewModel.saveEditsToHistory()
                    outPosition = it
                },
                onPlayChange = {
                    playheadPosition = it
                },
                onZoomChange = {
                    zoomScale = it
                },
                onPitchChange = {
                    viewModel.pitchValue.value = it
                }
            )
        }
    }
}