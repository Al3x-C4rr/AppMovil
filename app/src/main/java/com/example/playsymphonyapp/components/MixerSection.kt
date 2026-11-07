package com.example.playsymphonyapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt

private val MixerPurple = Color(0xFFBB86FC)
private val MixerCard = Color(0xFF1A1B2E)
private val MixerMuted = Color(0xFFB0B1C4)

private data class VisualMixerChannel(
    val name: String,
    val color: Color,
    val volume: Float = 0.75f,
    val pan: Float = 0f,
    val mute: Boolean = false,
    val solo: Boolean = false,
    val low: Float = 0f,
    val mid: Float = 0f,
    val high: Float = 0f,
    val reverb: Float = 0.25f
)

/**
 * No procesa ni reproduce audio.
 */
@Composable
fun MixerSection(
    pistas: List<TimelineTrack>,
    modifier: Modifier = Modifier
) {

    val occurrences = mutableMapOf<String, Int>()
    val keys = pistas.map { track ->
        val occurrence = occurrences.getOrDefault(track.name, 0)
        occurrences[track.name] = occurrence + 1
        "track:${track.name}:$occurrence"
    } + "master"
    val channels = pistas.mapIndexed { index, track ->

    LaunchedEffect(keys) {
    }

    fun update(index: Int, channel: VisualMixerChannel) {
        settings[keys[index]] = channel
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

            Row(
            ) {
                channels.forEachIndexed { index, channel ->
                    Surface(
                        color = MixerCard,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            val panLabel = when {
                                abs(channel.pan) < 0.01f -> "Centro"
                                }
                            Slider(
                                value = channel.pan,
                                valueRange = -1f..1f,
                                )
                                FilterChip(
                                    selected = channel.mute,
                                        )
                                        selected = channel.solo,
                                    )
                                }
                            MixerVolumeFader(
                                value = channel.volume,
                                color = channel.color,
                                label = "Volumen ${channel.name}",
                            )
                    }
                }
            }
        }

        val current = channels[selected]
                Spacer(Modifier.height(8.dp))
                    update(selected, current.copy(low = it))
                }
                    update(selected, current.copy(mid = it))
                }
                    update(selected, current.copy(high = it))
                }
                    update(selected, current.copy(reverb = it / 100))
                }
            }
        }
    }
}

@Composable
private fun MixerEffectSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onChange: (Float) -> Unit
) {
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
        )
    }
}

@Composable
    val onChange by rememberUpdatedState(onValueChange)
    Canvas(
            .semantics {
                contentDescription = label
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()
                detectTapGestures { point ->
                }
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                }
            }
    ) {
        val edge = 16.dp.toPx()
        val travel = size.height - 2 * edge
        val x = size.width / 2
        val y = edge + (1 - value) * travel
        for (step in 0..10) {
            val tick = edge + travel * step / 10
        }
    }
}
