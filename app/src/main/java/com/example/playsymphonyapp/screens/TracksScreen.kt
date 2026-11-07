package com.example.playsymphonyapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.playsymphonyapp.components.*
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.viewmodels.MusicViewModel

@Composable
fun TracksScreen(
    navController: NavController,
    viewModel: MusicViewModel
) {
    var isMenuVisible by remember {
        mutableStateOf(false)
    }

    var selectedSection by remember {
        mutableStateOf("Tracks")
    }

    val recorder = remember {
        Recorder()
    }

    val pistas = remember {
        mutableStateListOf<TimelineTrack>()
    }

    val timelinePlayer = rememberPianoTimelinePlayer()

    val song by viewModel.currentSong

    var inPosition by viewModel.inPosition
    var outPosition by viewModel.outPosition
    var playheadPosition by viewModel.playheadPosition
    var zoomScale by viewModel.zoomScale

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        AudioCraftTopBar(
            title = song?.title ?: "Sin pista",
            artist = song?.artist ?: "",
            isPlaying = timelinePlayer.isPlaying,

            onPlayToggle = {
                // Pausa la canción importada.
                if (viewModel.isPlaying.value) {
                    viewModel.togglePlayPause()
                }

                // Reproduce o detiene las grabaciones.
                if (timelinePlayer.isPlaying) {
                    timelinePlayer.stop()
                } else {
                    timelinePlayer.play(pistas)
                }
            },

            onResetRequested = {
                timelinePlayer.stop()
                viewModel.resetEditor()
            },

            onUndoRequested = {
                viewModel.undoLastEdit()
            },

            recorder = recorder,

            onTrackRecorded = { pista ->
                timelinePlayer.stop()
                pistas.add(pista)
            }
        )

        timelinePlayer.errorMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFFFF9090)
            )
        }

        TracksWorkspace(
            isMenuVisible = isMenuVisible,
            selectedSection = selectedSection,
            hasSong = song != null,

            onMenuToggle = {
                isMenuVisible = !isMenuVisible
            },

            onSectionSelected = {
                selectedSection = it
            },

            recorder = recorder,
            pistas = pistas,

            posicion = timelinePlayer.positionSeconds,

            onPositionChange = { seconds ->
                timelinePlayer.seekTo(seconds, pistas)
            }
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