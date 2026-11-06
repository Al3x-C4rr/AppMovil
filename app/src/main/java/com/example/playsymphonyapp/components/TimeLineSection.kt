package com.example.playsymphonyapp.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sin

// Todos los tiempos están expresados en segundos.
// Los tiempos de las notas son relativos al inicio de su clip.
data class TimelineNote(val midi: Int, val start: Float, val duration: Float)

data class TimelineClip(
    val name: String,
    val start: Float,
    val duration: Float,
    val notes: List<TimelineNote> = emptyList(),
    // Picos normalizados entre 0 y 1; vacíos si aún no se analizó el audio.
    val peaks: List<Float> = emptyList()
)

enum class TimelineTrackType { AUDIO, PIANO }

data class TimelineTrack(
    val name: String,
    val type: TimelineTrackType,
    val color: Color,
    val clips: List<TimelineClip>
)

/**
 * Editor VISUAL: no reproduce, graba ni exporta audio.
 * Admite desplazamiento, zoom y selección de posición mediante un toque.
 * onPositionChange entrega segundos, no la posición normalizada de TracksScreen.
 */
@Composable
fun TimeLineSection(
    tracks: List<TimelineTrack>,
    modifier: Modifier = Modifier,
    durationSeconds: Int = 20,
    positionSeconds: Float = 0f,
    onPositionChange: (Float) -> Unit = {}
) {
    var zoom by remember { mutableStateOf(1f) }
    val horizontal = rememberScrollState()
    val vertical = rememberScrollState()
    val seconds = durationSeconds.coerceAtLeast(1)
    val secondWidth = 72.dp * zoom
    val rulerHeight = 36.dp
    val rowHeight = 116.dp
    val totalHeight = rulerHeight + rowHeight * tracks.size
    val background = Color(0xFF171820)
    val textPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    val labelPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }

    Column(modifier.background(background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(
                text = "Línea de tiempo · ${"%.2f".format(positionSeconds)} s",
                color = Color.White,
                modifier = Modifier.weight(1f).padding(top = 12.dp),
                style = MaterialTheme.typography.labelMedium
            )
            TextButton(onClick = { zoom = (zoom / 1.5f).coerceAtLeast(0.5f) }) {
                Text("−")
            }
            TextButton(onClick = { zoom = (zoom * 1.5f).coerceAtMost(3f) }) {
                Text("+")
            }
        }

        // Un solo scroll vertical mantiene alineados los nombres y las pistas.
        Row(Modifier.fillMaxWidth().weight(1f).verticalScroll(vertical)) {
            Column(Modifier.width(108.dp)) {
                Box(Modifier.fillMaxWidth().height(rulerHeight).background(Color(0xFF242630))) {
                    Text("PISTAS", color = Color.LightGray, modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelSmall)
                }
                tracks.forEachIndexed { index, track ->
                    Column(
                        Modifier.fillMaxWidth().height(rowHeight)
                            .background(Color(0xFF20212B)).padding(10.dp)
                    ) {
                        Text("${index + 1}  ${track.name}", color = track.color,
                            style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(if (track.type == TimelineTrackType.PIANO) "Notas de piano" else "Audio",
                            color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Box(Modifier.weight(1f).horizontalScroll(horizontal)) {
                Canvas(
                    Modifier.width(secondWidth * seconds).height(totalHeight)
                        .semantics { contentDescription = "Pistas, clips y notas. Toca para seleccionar el tiempo." }
                        .pointerInput(seconds, secondWidth, onPositionChange) {
                            detectTapGestures { point ->
                                onPositionChange((point.x / secondWidth.toPx()).coerceIn(0f, seconds.toFloat()))
                            }
                        }
                ) {
                    val unit = secondWidth.toPx()
                    val ruler = rulerHeight.toPx()
                    val row = rowHeight.toPx()
                    textPaint.textSize = 11.dp.toPx()
                    textPaint.color = Color(0xFFCACBD3).toArgb()
                    labelPaint.textSize = 11.dp.toPx()
                    labelPaint.color = Color.White.toArgb()

                    drawRect(Color(0xFF242630), size = Size(size.width, ruler))
                    tracks.indices.forEach { i ->
                        drawRect(if (i % 2 == 0) Color(0xFF1C1D26) else Color(0xFF20212A),
                            Offset(0f, ruler + i * row), Size(size.width, row))
                    }
                    for (second in 0..seconds) {
                        val x = second * unit
                        drawLine(Color(0xFF373943), Offset(x, ruler - 8.dp.toPx()), Offset(x, size.height))
                        if (second < seconds) drawContext.canvas.nativeCanvas.drawText(
                            "${second / 60}:${(second % 60).toString().padStart(2, '0')}",
                            x + 4.dp.toPx(), 19.dp.toPx(), textPaint)
                    }
                    tracks.forEachIndexed { index, track ->
                        val y = ruler + index * row
                        drawLine(Color(0xFF42444F), Offset(0f, y), Offset(size.width, y))
                        val pitches = track.clips.flatMap { it.notes }.map { it.midi }
                        val low = (pitches.minOrNull() ?: 60) - 1
                        val high = (pitches.maxOrNull() ?: 72) + 1
                        track.clips.forEach { clip ->
                            if (clip.duration <= 0f || !clip.duration.isFinite() || !clip.start.isFinite()) return@forEach
                            val left = clip.start * unit
                            val right = (clip.start + clip.duration) * unit
                            val top = y + 7.dp.toPx()
                            val bottom = y + row - 7.dp.toPx()
                            drawRoundRect(track.color.copy(alpha = 0.28f), Offset(left, top),
                                Size(right - left, bottom - top), CornerRadius(4.dp.toPx()))
                            clipRect(left, top, right, bottom) {
                                drawRect(track.color.copy(alpha = 0.35f), Offset(left, top),
                                    Size(right - left, 23.dp.toPx()))
                                drawContext.canvas.nativeCanvas.drawText(clip.name,
                                    left + 6.dp.toPx(), top + 16.dp.toPx(), labelPaint)
                                val bodyTop = top + 27.dp.toPx()
                                val bodyHeight = bottom - bodyTop - 4.dp.toPx()
                                if (track.type == TimelineTrackType.PIANO) {
                                    val pitchHeight = bodyHeight / (high - low + 1)
                                    for (pitch in low..high) {
                                        val py = bodyTop + (high - pitch) * pitchHeight
                                        drawLine(Color.White.copy(alpha = 0.07f), Offset(left, py), Offset(right, py))
                                    }
                                    clip.notes.forEach { note ->
                                        if (note.duration > 0f) drawRoundRect(
                                            track.color,
                                            Offset(left + note.start * unit, bodyTop + (high - note.midi) * pitchHeight),
                                            Size(note.duration * unit, (pitchHeight * 0.8f).coerceAtLeast(1f)),
                                            CornerRadius(1.dp.toPx()))
                                    }
                                } else if (clip.peaks.isNotEmpty()) {
                                    val center = bodyTop + bodyHeight / 2
                                    val step = (right - left) / clip.peaks.size
                                    clip.peaks.forEachIndexed { p, peak ->
                                        val amplitude = abs(peak).coerceIn(0f, 1f) * bodyHeight / 2
                                        val x = left + (p + 0.5f) * step
                                        drawLine(track.color, Offset(x, center - amplitude),
                                            Offset(x, center + amplitude), (step * 0.65f).coerceAtLeast(1f))
                                    }
                                }
                            }
                        }
                    }
                    val cursor = positionSeconds.coerceIn(0f, seconds.toFloat()) * unit
                    drawLine(Color(0xFFFF685D), Offset(cursor, 0f), Offset(cursor, size.height), 2.dp.toPx())
                    drawCircle(Color(0xFFFF685D), 5.dp.toPx(), Offset(cursor, 5.dp.toPx()))
                }
            }
        }
    }
}

// Datos DEMO: estas ondas no corresponden a archivos analizados.
fun demoTimelineTracks(): List<TimelineTrack> {
    val demoPeaks = List(140) { i -> (abs(sin(i * 0.71)) * (0.2 + 0.7 * abs(sin(i * 0.13)))).toFloat() }
    val melody = listOf(
        TimelineNote(60, 0f, 0.7f), TimelineNote(64, 0.8f, 0.5f),
        TimelineNote(67, 1.4f, 0.8f), TimelineNote(65, 2.4f, 0.6f),
        TimelineNote(64, 3.2f, 0.6f), TimelineNote(60, 4f, 1.4f),
        TimelineNote(64, 4f, 1.4f), TimelineNote(67, 4f, 1.4f)
    )
    return listOf(
        TimelineTrack("Piano", TimelineTrackType.PIANO, Color(0xFFB59BFF),
            listOf(TimelineClip("Piano · toma 1", 1f, 6f, notes = melody),
                TimelineClip("Piano · toma 2", 9f, 6f, notes = melody))),
        TimelineTrack("Base", TimelineTrackType.AUDIO, Color(0xFF74D6A0),
            listOf(TimelineClip("Base.mp3", 0f, 16f, peaks = demoPeaks))),
        TimelineTrack("Audio", TimelineTrackType.AUDIO, Color(0xFFFFB566),
            listOf(TimelineClip("Grabación 01", 5f, 4f, peaks = demoPeaks),
                TimelineClip("Grabación 02", 12f, 5f, peaks = demoPeaks)))
    )
}

@Composable
fun TimelineDemo(modifier: Modifier = Modifier) {
    val tracks = remember { demoTimelineTracks() }
    var position by remember { mutableStateOf(3.5f) }
    TimeLineSection(tracks, modifier, positionSeconds = position, onPositionChange = { position = it })
}

@Preview(showBackground = true, widthDp = 900, heightDp = 440)
@Composable
private fun TimelineDemoPreview() {
    MaterialTheme { TimelineDemo(Modifier.fillMaxSize()) }
}
