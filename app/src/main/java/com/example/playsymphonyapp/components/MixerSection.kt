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
import java.util.Locale
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
 * Interfaz visual: un canal por pista, más la salida Master.
 * No procesa ni reproduce audio.
 */
@Composable
fun MixerSection(
    pistas: List<TimelineTrack>,
    modifier: Modifier = Modifier
) {
    val settings = remember {
        mutableStateMapOf<String, VisualMixerChannel>()
    }
    var selectedKey by remember {
        mutableStateOf<String?>(null)
    }

    val occurrences = mutableMapOf<String, Int>()

    val keys = pistas.map { track ->
        val occurrence = occurrences.getOrDefault(track.name, 0)
        occurrences[track.name] = occurrence + 1
        "track:${track.name}:$occurrence"
    } + "master"

    val channels = pistas.mapIndexed { index, track ->
        val saved = settings[keys[index]]
            ?: VisualMixerChannel(track.name, track.color)

        saved.copy(
            name = track.name,
            color = track.color
        )
    } + (
            settings["master"]
                ?: VisualMixerChannel(
                    name = "Master",
                    color = Color(0xFFE6E2F3),
                    volume = 1f
                )
            )

    val selected = selectedKey
        ?.let { keys.indexOf(it) }
        ?.takeIf { it >= 0 }
        ?: 0

    LaunchedEffect(keys) {
        settings.keys
            .toList()
            .filter { it !in keys }
            .forEach { settings.remove(it) }
    }

    fun update(index: Int, channel: VisualMixerChannel) {
        settings[keys[index]] = channel
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Mezcla de pistas",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${pistas.size} pistas · controles visuales",
            color = MixerMuted,
            style = MaterialTheme.typography.labelMedium
        )

        // Calcula el ancho usando el espacio disponible de esta sección.
        // Master también cuenta como mixer.
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val channelSpacing = 12.dp
            val visibleChannels = channels.size.coerceAtMost(2)

            val channelWidth = (
                    (maxWidth - channelSpacing * (visibleChannels - 1)) /
                            visibleChannels
                    ).coerceAtLeast(0.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        state = rememberScrollState(),
                        enabled = channels.size > 2
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    channelSpacing
                )
            ) {
                channels.forEachIndexed { index, channel ->
                    Surface(
                        modifier = Modifier
                            .width(channelWidth)
                            .clickable {
                                selectedKey = keys[index]
                            },
                        color = MixerCard,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (selected == index) {
                                channel.color
                            } else {
                                Color(0xFF35364A)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = channel.name,
                                color = channel.color,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = if (keys[index] == "master") {
                                    "Salida general"
                                } else {
                                    "Pista ${index + 1}"
                                },
                                color = MixerMuted,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Text(
                                text = "Panorama",
                                color = MixerMuted
                            )

                            val panLabel = when {
                                abs(channel.pan) < 0.01f -> "Centro"

                                channel.pan < 0 -> {
                                    "Izq. ${(abs(channel.pan) * 100).roundToInt()} %"
                                }

                                else -> {
                                    "Der. ${(channel.pan * 100).roundToInt()} %"
                                }
                            }

                            Text(
                                text = panLabel,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )

                            Slider(
                                value = channel.pan,
                                onValueChange = {
                                    update(
                                        index,
                                        channel.copy(pan = it)
                                    )
                                },
                                valueRange = -1f..1f,
                                modifier = Modifier.semantics {
                                    contentDescription = "Panorama ${channel.name}"
                                },
                                colors = SliderDefaults.colors(
                                    thumbColor = channel.color,
                                    activeTrackColor = channel.color
                                )
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = channel.mute,
                                    onClick = {
                                        update(
                                            index,
                                            channel.copy(mute = !channel.mute)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "M",
                                            color = Color.White
                                        )
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Silenciar ${channel.name}"
                                    }
                                )

                                if (keys[index] != "master") {
                                    FilterChip(
                                        selected = channel.solo,
                                        onClick = {
                                            update(
                                                index,
                                                channel.copy(solo = !channel.solo)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "S",
                                                color = Color.White
                                            )
                                        },
                                        modifier = Modifier.semantics {
                                            contentDescription = "Solo ${channel.name}"
                                        }
                                    )
                                }
                            }

                            MixerVolumeFader(
                                value = channel.volume,
                                color = channel.color,
                                label = "Volumen ${channel.name}",
                                onValueChange = {
                                    update(
                                        index,
                                        channel.copy(volume = it)
                                    )
                                }
                            )

                            val gain = if (channel.volume <= 0f) {
                                "−∞ dB"
                            } else {
                                String.format(
                                    Locale.ROOT,
                                    "%.1f dB",
                                    20.0 * log10(channel.volume.toDouble())
                                )
                            }

                            Text(
                                text = gain,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = if (channel.mute) {
                                    "Silencio activado"
                                } else {
                                    "Volumen"
                                },
                                color = if (channel.mute) {
                                    Color(0xFFFF7A87)
                                } else {
                                    MixerMuted
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        val current = channels[selected]

        Surface(
            color = MixerCard,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Efectos · ${current.name}",
                    color = current.color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                MixerEffectSlider(
                    title = "Graves",
                    value = current.low,
                    range = -12f..12f,
                    unit = "dB"
                ) {
                    update(selected, current.copy(low = it))
                }

                MixerEffectSlider(
                    title = "Medios",
                    value = current.mid,
                    range = -12f..12f,
                    unit = "dB"
                ) {
                    update(selected, current.copy(mid = it))
                }

                MixerEffectSlider(
                    title = "Agudos",
                    value = current.high,
                    range = -12f..12f,
                    unit = "dB"
                ) {
                    update(selected, current.copy(high = it))
                }

                MixerEffectSlider(
                    title = "Reverb",
                    value = current.reverb * 100,
                    range = 0f..100f,
                    unit = "%"
                ) {
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
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MixerMuted,
            modifier = Modifier.width(64.dp)
        )

        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = title
                },
            colors = SliderDefaults.colors(
                thumbColor = MixerPurple,
                activeTrackColor = MixerPurple
            )
        )

        Text(
            text = "${value.roundToInt()} $unit",
            color = Color.White,
            modifier = Modifier.width(56.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun MixerVolumeFader(
    value: Float,
    color: Color,
    label: String,
    onValueChange: (Float) -> Unit
) {
    val onChange by rememberUpdatedState(onValueChange)

    Canvas(
        modifier = Modifier
            .width(76.dp)
            .height(180.dp)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(
                    value,
                    0f..1f
                )
                setProgress { requested ->
                    onChange(requested.coerceIn(0f, 1f))
                    true
                }
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()

                detectTapGestures { point ->
                    onChange(
                        (
                                1f - (point.y - edge) /
                                        (size.height - 2 * edge)
                                ).coerceIn(0f, 1f)
                    )
                }
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()

                detectVerticalDragGestures { change, _ ->
                    change.consume()

                    onChange(
                        (
                                1f - (change.position.y - edge) /
                                        (size.height - 2 * edge)
                                ).coerceIn(0f, 1f)
                    )
                }
            }
    ) {
        val edge = 16.dp.toPx()
        val travel = size.height - 2 * edge
        val x = size.width / 2
        val y = edge + (1 - value) * travel

        for (step in 0..10) {
            val tick = edge + travel * step / 10

            drawLine(
                color = Color(0xFF55566E),
                start = Offset(x - 25.dp.toPx(), tick),
                end = Offset(x - 17.dp.toPx(), tick)
            )
        }

        drawLine(
            color = Color(0xFF45465D),
            start = Offset(x, edge),
            end = Offset(x, size.height - edge),
            strokeWidth = 6.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(x, y),
            end = Offset(x, size.height - edge),
            strokeWidth = 6.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        drawRoundRect(
            color = Color(0xFFEAE6F5),
            topLeft = Offset(
                x - 16.dp.toPx(),
                y - 8.dp.toPx()
            ),
            size = Size(
                32.dp.toPx(),
                16.dp.toPx()
            ),
            cornerRadius = CornerRadius(5.dp.toPx())
        )
    }
}
