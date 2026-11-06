package com.example.playsymphonyapp.components

import android.os.SystemClock

data class NotaGrabada(
    val resource: Int,
    val timeMs: Long
)

class Recorder {
    private val notes = mutableListOf<NotaGrabada>()
    private var begin = 0L
    private var recorder = false

    fun start() {
        notes.clear()
        begin = SystemClock.elapsedRealtime()
        recorder = true
    }

    fun register(recurso: Int) {
        if (!recorder) return

        notes.add(
            NotaGrabada(
                recurso,
                SystemClock.elapsedRealtime() - begin
            )
        )
    }

    fun stop(): List<NotaGrabada> {
        recorder = false
        return notes.toList()
    }
}