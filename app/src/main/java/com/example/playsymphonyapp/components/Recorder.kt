package com.example.playsymphonyapp.components

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class NotaGrabada(
    val resource: Int,
    val timeMs: Long,
    val midi: Int,
    val durationMs: Long
)

class Recorder {
    private val notes = mutableListOf<NotaGrabada>()
    private var begin = 0L
    private var recordingDurationMs = 0L
    private var takeNumber = 0

    var isRecording by mutableStateOf(false)
        private set

    fun start() {
        if (isRecording) return
        notes.clear()
        recordingDurationMs = 0L
        begin = SystemClock.elapsedRealtime()
        isRecording = true
    }

    // El piano actual reproduce el MP3 completo por cada clic.
    // Por eso la duración corresponde al archivo, no al tiempo de pulsación.
    fun register(recurso: Int, midi: Int, durationMs: Long) {
        if (!isRecording || durationMs <= 0L) return
        require(midi in 0..127) { "Nota MIDI fuera de rango" }
        notes.add(NotaGrabada(
            resource = recurso,
            timeMs = SystemClock.elapsedRealtime() - begin,
            midi = midi,
            durationMs = durationMs
        ))
    }

    fun stop(): List<NotaGrabada> {
        if (isRecording) {
            recordingDurationMs = SystemClock.elapsedRealtime() - begin
            isRecording = false
        }
        return notes.toList()
    }

    /**
     * Detiene la grabación y construye una pista para TimeLineSection.
     * Cada toma crea una pista nueva desde el segundo 0.
     * Incluye el final de las muestras que siguen sonando después de detener.
     * No genera un archivo de audio ni reproduce la pista.
     */
    fun stopAsTrack(): TimelineTrack? {
        if (!isRecording) return null
        val recorded = stop()
        if (recorded.isEmpty()) return null
        takeNumber++
        val endOfSounds = recorded.maxOf { it.timeMs + it.durationMs }
        val clipDuration = maxOf(recordingDurationMs, endOfSounds) / 1000f
        return TimelineTrack(
            name = "Piano $takeNumber",
            type = TimelineTrackType.PIANO,
            color = Color(0xFFB59BFF),
            clips = listOf(TimelineClip(
                name = "Toma $takeNumber",
                start = 0f,
                duration = clipDuration,
                notes = recorded.map {
                    TimelineNote(
                        midi = it.midi,
                        start = it.timeMs / 1000f,
                        duration = it.durationMs / 1000f
                    )
                }
            ))
        )
    }
}
