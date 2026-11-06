package com.example.playsymphonyapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.roundToInt
import java.util.Locale

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
 * Interfaz de mixer: un canal por cada TimelineTrack, más la salida Master.
 * No procesa ni reproduce audio.
 * Los controles conservan sus valores durante las recomposiciones;
 * al salir de la sección se reinician. Master tampoco modifica otras pistas.
 */
@Composable
fun MixerSection(
    pistas: List<TimelineTrack>,
    modifier: Modifier = Modifier
) {
    val settings = remember { mutableStateMapOf<String, VisualMixerChannel>() }
    var selectedKey by remember { mutableStateOf<String?>(null) }

    // Nombre y ocurrencia distinguen incluso las pistas con nombres repetidos.
    // Un identificador estable por pista permitiría conservar ajustes al renombrar.
    val occurrences = mutableMapOf<String, Int>()
    val keys = pistas.map { track ->
        val occurrence = occurrences.getOrDefault(track.name, 0)
        occurrences[track.name] = occurrence + 1
        "track:${track.name}:$occurrence"
    } + "master"
    val channels = pistas.mapIndexed { index, track ->
        val saved = settings[keys[index]] ?: VisualMixerChannel(track.name, track.color)
        saved.copy(name = track.name, color = track.color)
    } + (settings["master"] ?: VisualMixerChannel("Master", Color(0xFFE6E2F3), volume = 1f))
    val selected = selectedKey?.let { keys.indexOf(it) }?.takeIf { it >= 0 } ?: 0

    // Quita los ajustes de pistas eliminadas; conserva los de las restantes.
    LaunchedEffect(keys) {
        settings.keys.toList().filter { it !in keys }.forEach { settings.remove(it) }
    }

    fun update(index: Int, channel: VisualMixerChannel) {
        settings[keys[index]] = channel
    }

    Column(
        modifier = modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Mezcla de pistas", color = Color.White,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${pistas.size} pistas · controles visuales", color = MixerMuted,
            style = MaterialTheme.typography.labelMedium)

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            channels.forEachIndexed { index, channel ->
                Surface(
                    modifier = Modifier.width(172.dp).clickable { selectedKey = keys[index] },
                    color = MixerCard,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, if (selected == index) channel.color else Color(0xFF35364A))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(channel.name, color = channel.color,
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (keys[index] == "master") "Salida general" else "Pista ${index + 1}",
                            color = MixerMuted, style = MaterialTheme.typography.labelSmall)

                        Text("Panorama", color = MixerMuted)
                        val panLabel = when {
                            abs(channel.pan) < 0.01f -> "Centro"
                            channel.pan < 0 -> "Izq. ${(abs(channel.pan) * 100).roundToInt()} %"
                            else -> "Der. ${(channel.pan * 100).roundToInt()} %"
                        }
                        Text(panLabel, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = channel.pan,
                            onValueChange = { update(index, channel.copy(pan = it)) },
                            valueRange = -1f..1f,
                            modifier = Modifier.semantics { contentDescription = "Panorama ${channel.name}" },
                            colors = SliderDefaults.colors(thumbColor = channel.color, activeTrackColor = channel.color)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = channel.mute,
                                onClick = { update(index, channel.copy(mute = !channel.mute)) },
                                label = { Text("M", color = Color.White) },
                                modifier = Modifier.semantics { contentDescription = "Silenciar ${channel.name}" }
                            )
                            if (keys[index] != "master") FilterChip(
                                selected = channel.solo,
                                onClick = { update(index, channel.copy(solo = !channel.solo)) },
                                label = { Text("S", color = Color.White) },
                                modifier = Modifier.semantics { contentDescription = "Solo ${channel.name}" }
                            )
                        }
                        MixerVolumeFader(
                            value = channel.volume,
                            color = channel.color,
                            label = "Volumen ${channel.name}",
                            onValueChange = { update(index, channel.copy(volume = it)) }
                        )
                        val gain = if (channel.volume <= 0f) "−∞ dB"
                        else String.format(Locale.ROOT, "%.1f dB", 20.0 * log10(channel.volume.toDouble()))
                        Text(gain, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (channel.mute) "Silencio activado" else "Volumen",
                            color = if (channel.mute) Color(0xFFFF7A87) else MixerMuted,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        val current = channels[selected]
        Surface(color = MixerCard, shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Efectos · ${current.name}", color = current.color,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                MixerEffectSlider("Graves", current.low, -12f..12f, "dB") {
                    update(selected, current.copy(low = it))
                }
                MixerEffectSlider("Medios", current.mid, -12f..12f, "dB") {
                    update(selected, current.copy(mid = it))
                }
                MixerEffectSlider("Agudos", current.high, -12f..12f, "dB") {
                    update(selected, current.copy(high = it))
                }
                MixerEffectSlider("Reverb", current.reverb * 100, 0f..100f, "%") {
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MixerMuted, modifier = Modifier.width(64.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f).semantics { contentDescription = title },
            colors = SliderDefaults.colors(thumbColor = MixerPurple, activeTrackColor = MixerPurple)
        )
        Text("${value.roundToInt()} $unit", color = Color.White,
            modifier = Modifier.width(56.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MixerVolumeFader(value: Float, color: Color, label: String, onValueChange: (Float) -> Unit) {
    // El callback actualizado permite arrastrar sin reiniciar el gesto al recomponer.
    val onChange by rememberUpdatedState(onValueChange)
    Canvas(
        Modifier.width(76.dp).height(180.dp)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(value, 0f..1f)
                setProgress { requested -> onChange(requested.coerceIn(0f, 1f)); true }
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()
                detectTapGestures { point ->
                    onChange((1f - (point.y - edge) / (size.height - 2 * edge)).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                val edge = 16.dp.toPx()
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    onChange((1f - (change.position.y - edge) / (size.height - 2 * edge)).coerceIn(0f, 1f))
                }
            }
    ) {
        val edge = 16.dp.toPx()
        val travel = size.height - 2 * edge
        val x = size.width / 2
        val y = edge + (1 - value) * travel
        for (step in 0..10) {
            val tick = edge + travel * step / 10
            drawLine(Color(0xFF55566E), Offset(x - 25.dp.toPx(), tick), Offset(x - 17.dp.toPx(), tick))
        }
        drawLine(Color(0xFF45465D), Offset(x, edge), Offset(x, size.height - edge),
            strokeWidth = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawLine(color, Offset(x, y), Offset(x, size.height - edge),
            strokeWidth = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        drawRoundRect(Color(0xFFEAE6F5), Offset(x - 16.dp.toPx(), y - 8.dp.toPx()),
            Size(32.dp.toPx(), 16.dp.toPx()), CornerRadius(5.dp.toPx()))
    }
}
