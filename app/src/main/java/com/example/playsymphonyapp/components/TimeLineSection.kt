package com.example.playsymphonyapp.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.ceil
import java.util.Locale
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
 * Regla fija con cursor arrastrable, nombres fijos en horizontal, zoom y selección de clips.
 * Deslizar el cuerpo de las pistas desplaza horizontalmente el tiempo.
 * El componente necesita altura acotada; sin altura explícita usa entre 300 y 620 dp.
 * No permite mover clips ni procesa audio.
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
    val latestOnPositionChange by rememberUpdatedState(onPositionChange)
    var selected by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val lastEnd = tracks.flatMap { it.clips }.map { it.start + it.duration }
        .filter { it.isFinite() && it > 0f }.maxOrNull() ?: 0f
    val seconds = maxOf(1, durationSeconds, ceil(lastEnd.toDouble()).toInt())
    val secondWidth = 72.dp * zoom
    val rulerHeight = 36.dp
    val rowHeight = 116.dp
    val totalHeight = rowHeight * tracks.size.coerceAtLeast(1)
    val background = Color(0xFF171820)
    val textPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    val labelPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }

    Column(
        modifier.fillMaxWidth().heightIn(min = 300.dp, max = 620.dp)
            .clip(RoundedCornerShape(14.dp)).background(background)
    ) {
        Row(Modifier.fillMaxWidth().background(Color(0xFF1A1B2E)).padding(horizontal = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text(
                text = "${tracks.size} pistas · ${formatTimelinePosition(positionSeconds)}",
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1, overflow = TextOverflow.Ellipsis, fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.labelMedium
            )
            TextButton(onClick = { zoom = (zoom / 1.5f).coerceAtLeast(0.5f) }) {
                Text("−")
            }
            TextButton(onClick = { zoom = (zoom * 1.5f).coerceAtMost(3f) }) {
                Text("+")
            }
        }

        Row(Modifier.fillMaxWidth().height(rulerHeight).background(Color(0xFF242630))) {
            Box(Modifier.width(108.dp).fillMaxHeight(), contentAlignment = androidx.compose.ui.Alignment.CenterStart) {
                Text("PISTAS", color = Color.LightGray,
                    modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.labelSmall)
            }
            TimelineFixedRuler(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                seconds = seconds,
                secondWidth = secondWidth,
                scroll = horizontal,
                position = positionSeconds,
                onSeek = { latestOnPositionChange(it) }
            )
        }

        // Nombres y pistas se desplazan juntos en vertical; la regla queda fija.
        Row(Modifier.fillMaxWidth().weight(1f).verticalScroll(vertical)) {
            Column(Modifier.width(108.dp)) {
                if (tracks.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(rowHeight).background(Color(0xFF20212B))) {
                        Text("Sin pistas", color = Color.LightGray, modifier = Modifier.padding(12.dp))
                    }
                }
                tracks.forEachIndexed { index, track ->
                    Column(
                        Modifier.fillMaxWidth().height(rowHeight)
                            .background(if (selected?.first == index) Color(0xFF302640) else Color(0xFF20212B))
                            .clickable { selected = index to -1 }.padding(10.dp)
                    ) {
                        Text("${index + 1}  ${track.name}", color = track.color,
                            style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(8.dp))
                        Text(if (track.type == TimelineTrackType.PIANO) "Notas de piano" else "Audio",
                            color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Box(Modifier.weight(1f).horizontalScroll(horizontal)) {
                Canvas(
                    Modifier.width(secondWidth * seconds).height(totalHeight).clipToBounds()
                        .semantics { contentDescription = "Pistas, clips y notas. Toca para seleccionar el tiempo." }
                        .pointerInput(seconds, secondWidth, tracks) {
                            detectTapGestures { point ->
                                val time = (point.x / secondWidth.toPx()).coerceIn(0f, seconds.toFloat())
                                val trackIndex = (point.y / rowHeight.toPx()).toInt()
                                val track = tracks.getOrNull(trackIndex)
                                val clipIndex = track?.clips?.indexOfLast {
                                    time >= it.start && time < it.start + it.duration
                                } ?: -1
                                selected = if (track != null) trackIndex to clipIndex else null
                                latestOnPositionChange(time)
                            }
                        }
                ) {
                    val unit = secondWidth.toPx()
                    val ruler = 0f
                    val row = rowHeight.toPx()
                    textPaint.textSize = 11.dp.toPx()
                    textPaint.color = Color(0xFFCACBD3).toArgb()
                    labelPaint.textSize = 11.dp.toPx()
                    labelPaint.color = Color.White.toArgb()

                    tracks.indices.forEach { i ->
                        drawRect(if (i % 2 == 0) Color(0xFF1C1D26) else Color(0xFF20212A),
                            Offset(0f, i * row), Size(size.width, row))
                    }
                    val gridStep = if (secondWidth >= 40.dp) 1 else 2
                    for (second in 0..seconds step gridStep) {
                        val x = second * unit
                        drawLine(Color(0xFF373943), Offset(x, 0f), Offset(x, size.height))
                        if (secondWidth >= 100.dp && second < seconds) {
                            drawLine(Color(0xFF292B38), Offset(x + unit / 2, 0f), Offset(x + unit / 2, size.height))
                        }
                    }
                    if (tracks.isEmpty()) {
                        drawContext.canvas.nativeCanvas.drawText("Las grabaciones aparecerán aquí", 12.dp.toPx(), 36.dp.toPx(), textPaint)
                    }
                    tracks.forEachIndexed { index, track ->
                        val y = ruler + index * row
                        drawLine(Color(0xFF42444F), Offset(0f, y), Offset(size.width, y))
                        val pitches = track.clips.flatMap { it.notes }.map { it.midi }
                        val low = (pitches.minOrNull() ?: 60) - 1
                        val high = maxOf((pitches.maxOrNull() ?: 72) + 1, low + 11)
                        track.clips.forEachIndexed clipLoop@ { clipIndex, clip ->
                            if (clip.duration <= 0f || !clip.duration.isFinite() || !clip.start.isFinite()) return@clipLoop
                            val left = clip.start * unit
                            val right = (clip.start + clip.duration) * unit
                            val top = y + 7.dp.toPx()
                            val bottom = y + row - 7.dp.toPx()
                            drawRoundRect(track.color.copy(alpha = if (selected == (index to clipIndex)) 0.35f else 0.2f), Offset(left, top),
                                Size(right - left, bottom - top), CornerRadius(4.dp.toPx()))
                            clipRect(left, top, right, bottom) {
                                drawRect(track.color.copy(alpha = 0.35f), Offset(left, top),
                                    Size(right - left, 23.dp.toPx()))
                                drawContext.canvas.nativeCanvas.drawText(timelineEllipsis(clip.name, labelPaint, right - left - 12.dp.toPx()),
                                    left + 6.dp.toPx(), top + 16.dp.toPx(), labelPaint)
                                val bodyTop = top + 27.dp.toPx()
                                val bodyHeight = bottom - bodyTop - 4.dp.toPx()
                                if (track.type == TimelineTrackType.PIANO) {
                                    val pitchHeight = bodyHeight / (high - low + 1)
                                    for (pitch in low..high) {
                                        val py = bodyTop + (high - pitch) * pitchHeight
                                        val black = pitch % 12 in listOf(1, 3, 6, 8, 10)
                                        drawRect(Color.White.copy(alpha = if (black) 0.025f else 0.07f),
                                            Offset(left, py), Size(right - left, (pitchHeight - 0.5f).coerceAtLeast(0.5f)))
                                    }
                                    clip.notes.forEach { note ->
                                        if (note.duration > 0f && note.duration.isFinite() && note.start.isFinite()) drawRoundRect(
                                            track.color,
                                            Offset(left + note.start * unit, bodyTop + (high - note.midi) * pitchHeight),
                                            Size(note.duration * unit, (pitchHeight * 0.8f).coerceAtLeast(1f)),
                                            CornerRadius(1.dp.toPx()))
                                    }
                                } else if (clip.peaks.isNotEmpty()) {
                                    val center = bodyTop + bodyHeight / 2
                                    val step = (right - left) / clip.peaks.size
                                    clip.peaks.forEachIndexed { p, peak ->
                                        val amplitude = abs(peak.takeIf { it.isFinite() } ?: 0f).coerceIn(0f, 1f) * bodyHeight / 2
                                        val x = left + (p + 0.5f) * step
                                        drawLine(track.color, Offset(x, center - amplitude),
                                            Offset(x, center + amplitude), (step * 0.65f).coerceAtLeast(1f))
                                    }
                                }
                            }
                            drawRoundRect(
                                if (selected == (index to clipIndex)) Color.White else track.color.copy(alpha = 0.65f),
                                Offset(left, top), Size(right - left, bottom - top), CornerRadius(4.dp.toPx()),
                                style = Stroke(if (selected == (index to clipIndex)) 2.dp.toPx() else 1.dp.toPx())
                            )
                        }
                    }
                    val cursor = (positionSeconds.takeIf { it.isFinite() } ?: 0f).coerceIn(0f, seconds.toFloat()) * unit
                    drawLine(Color(0xFFFF685D), Offset(cursor, 0f), Offset(cursor, size.height), 2.dp.toPx())
                    drawCircle(Color(0xFFFF685D), 5.dp.toPx(), Offset(cursor, 5.dp.toPx()))
                }
            }
        }
        Text(
            "Tocá un clip para seleccionarlo · arrastrá la regla para mover el cursor",
            modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1B2E)).padding(8.dp),
            color = Color(0xFF969AAE), style = MaterialTheme.typography.labelSmall,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
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

@Composable
private fun TimelineFixedRuler(
    modifier: Modifier,
    seconds: Int,
    secondWidth: androidx.compose.ui.unit.Dp,
    scroll: androidx.compose.foundation.ScrollState,
    position: Float,
    onSeek: (Float) -> Unit
) {
    val latestSeek by rememberUpdatedState(onSeek)
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG) }
    Canvas(
        modifier.clipToBounds()
            .semantics { contentDescription = "Regla de tiempo. Toca o arrastra para mover el cursor." }
            .pointerInput(seconds, secondWidth) {
                detectTapGestures { p ->
                    latestSeek(((p.x + scroll.value) / secondWidth.toPx()).coerceIn(0f, seconds.toFloat()))
                }
            }
            .pointerInput(seconds, secondWidth) {
                detectDragGestures(
                    onDragStart = { p ->
                        latestSeek(((p.x + scroll.value) / secondWidth.toPx()).coerceIn(0f, seconds.toFloat()))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        latestSeek(((change.position.x + scroll.value) / secondWidth.toPx()).coerceIn(0f, seconds.toFloat()))
                    }
                )
            }
    ) {
        val unit = secondWidth.toPx()
        val step = when {
            secondWidth >= 50.dp -> 1
            secondWidth >= 25.dp -> 2
            else -> 5
        }
        val first = ((scroll.value / unit).toInt() / step) * step
        val last = minOf(seconds, ceil((scroll.value + size.width) / unit).toInt())
        paint.color = Color(0xFFBEC2D3).toArgb()
        paint.textSize = 10.dp.toPx()
        for (s in first..last step step) {
            val x = s * unit - scroll.value
            val time = "${s / 60}:${(s % 60).toString().padStart(2, '0')}"
            drawLine(Color(0xFF7D8199), Offset(x, size.height - 8.dp.toPx()), Offset(x, size.height))
            drawContext.canvas.nativeCanvas.drawText(time, x + 4.dp.toPx(), 16.dp.toPx(), paint)
        }
        val cursor = (position.takeIf { it.isFinite() } ?: 0f).coerceIn(0f, seconds.toFloat()) * unit - scroll.value
        drawLine(Color(0xFFFF685D), Offset(cursor, 0f), Offset(cursor, size.height), 2.dp.toPx())
        val triangle = androidx.compose.ui.graphics.Path().apply {
            moveTo(cursor - 6.dp.toPx(), 0f)
            lineTo(cursor + 6.dp.toPx(), 0f)
            lineTo(cursor, 8.dp.toPx())
            close()
        }
        drawPath(triangle, Color(0xFFFF685D))
    }
}

private fun timelineEllipsis(text: String, paint: Paint, width: Float): String {
    if (width <= 0f) return ""
    if (paint.measureText(text) <= width) return text
    val dots = "…"
    if (paint.measureText(dots) > width) return ""
    val count = paint.breakText(text, true, width - paint.measureText(dots), null)
    return text.take(count) + dots
}

private fun formatTimelinePosition(seconds: Float): String {
    val safe = (seconds.takeIf { it.isFinite() } ?: 0f).coerceAtLeast(0f)
    val ms = (safe * 1000).toLong()
    return String.format(Locale.ROOT, "%02d:%02d.%03d", ms / 60000, (ms / 1000) % 60, ms % 1000)
}

