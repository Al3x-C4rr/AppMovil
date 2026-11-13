package com.example.playsymphonyapp.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.playsymphonyapp.R
import com.example.playsymphonyapp.Editor.ProjectTrack

private data class MixerControls(
    val name: String,
    val volume: Float = 0.75f,
    val pan: Float = 0f,
    val mute: Boolean = false,
    val solo: Boolean = false
)

@Composable
fun MixerSection(
    pistas: List<TimelineTrack> = emptyList(),
    modifier: Modifier = Modifier,
    tracks: List<ProjectTrack> = emptyList(),
    master: Float = 1f,
    enabled: Boolean = true,
    onTrackChange: (ProjectTrack) -> Unit = {},
    onMasterChange: (Float) -> Unit = {},
    onEditStart: () -> Unit = {}
) {
    // Compatibilidad con la llamada anterior de TracksWorkspace.
    val preview = remember {
        mutableStateMapOf<String, MixerControls>()
    }

    val channels = if (tracks.isNotEmpty()) {
        tracks.map {
            MixerControls(
                it.name,
                it.volume,
                it.pan,
                it.mute,
                it.solo
            )
        }
    } else {
        pistas.mapIndexed { index, track ->
            preview["$index:${track.name}"]
                ?: MixerControls(track.name)
        }
    }

    fun update(index: Int, value: MixerControls) {
        tracks.getOrNull(index)?.let { track ->
            onTrackChange(
                track.copy(
                    volume = value.volume,
                    pan = value.pan,
                    mute = value.mute,
                    solo = value.solo
                )
            )
        } ?: run {
            preview["$index:${pistas[index].name}"] = value
        }
    }

    val count = channels.size + 1
    var masterEditing by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val gap = 12.dp
        val visible = minOf(count, 2)

        val width = (
                (maxWidth - gap * (visible - 1)) / visible
                ).coerceAtLeast(0.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState(),
                    enabled = count > 2
                ),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            channels.forEachIndexed { index, channel ->
                key(
                    tracks.getOrNull(index)?.id
                        ?: "$index:${channel.name}"
                ) {
                    var volumeEditing by remember {
                        mutableStateOf(false)
                    }

                    var panEditing by remember {
                        mutableStateOf(false)
                    }

                    MixerChannelCard(channel.name, width) {
                        Text(
                            "${(channel.volume * 100).toInt()} %",
                            color = Color.White
                        )

                        Slider(
                            value = channel.volume,
                            enabled = enabled,
                            onValueChange = {
                                if (!volumeEditing) {
                                    onEditStart()
                                    volumeEditing = true
                                }

                                update(index, channel.copy(volume = it))
                            },
                            onValueChangeFinished = {
                                volumeEditing = false
                            }
                        )

                        Text(
                            stringResource(R.string.pan),
                            color = Color.LightGray
                        )

                        Slider(
                            value = channel.pan,
                            valueRange = -1f..1f,
                            enabled = enabled,
                            onValueChange = {
                                if (!panEditing) {
                                    onEditStart()
                                    panEditing = true
                                }

                                update(index, channel.copy(pan = it))
                            },
                            onValueChangeFinished = {
                                panEditing = false
                            }
                        )

                        FilterChip(
                            selected = channel.mute,
                            enabled = enabled,
                            onClick = {
                                onEditStart()
                                update(
                                    index,
                                    channel.copy(mute = !channel.mute)
                                )
                            },
                            label = {
                                Text(stringResource(R.string.mute))
                            }
                        )

                        FilterChip(
                            selected = channel.solo,
                            enabled = enabled,
                            onClick = {
                                onEditStart()
                                update(
                                    index,
                                    channel.copy(solo = !channel.solo)
                                )
                            },
                            label = {
                                Text(stringResource(R.string.solo))
                            }
                        )
                    }
                }
            }

            MixerChannelCard(
                stringResource(R.string.master),
                width
            ) {
                Text(
                    "${(master * 100).toInt()} %",
                    color = Color.White
                )

                Slider(
                    value = master,
                    enabled = enabled,
                    onValueChange = {
                        if (!masterEditing) {
                            onEditStart()
                            masterEditing = true
                        }

                        onMasterChange(it)
                    },
                    onValueChangeFinished = {
                        masterEditing = false
                    }
                )

                Text(
                    stringResource(R.string.master_help),
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
private fun MixerChannelCard(
    name: String,
    width: androidx.compose.ui.unit.Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.width(width),
        color = Color(0xFF1A1B2E),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(name, color = Color(0xFFBB86FC))
            content()
        }
    }
}