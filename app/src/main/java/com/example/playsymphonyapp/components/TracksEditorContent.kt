package com.example.playsymphonyapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.playsymphonyapp.viewmodels.MusicViewModel

@Composable
fun TracksEditorContent(
    inPos: Float,
    outPos: Float,
    playPos: Float,
    zoom: Float,
    pitch: Float,
    onInChange: (Float) -> Unit,
    onOutChange: (Float) -> Unit,
    onPlayChange: (Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    viewModel: MusicViewModel
) {


    Column(modifier = Modifier.fillMaxWidth()) {
        TimelineEditorSection(
            inPos = inPos,
            outPos = outPos,
            playPos = playPos,
            zoom = zoom,
            onInChange = onInChange,
            onOutChange = onOutChange,
            onPlayChange = onPlayChange,
            onPitchChange = onPitchChange,
            onZoomChange = onZoomChange,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(16.dp))

        PitchControlSection(
            pitch = pitch,
            onPitchChange = onPitchChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        FadeButtons()
    }
}