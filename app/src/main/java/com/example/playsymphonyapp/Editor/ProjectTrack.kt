package com.example.playsymphonyapp.Editor

import java.io.File

data class ProjectTrack(
    val id: String,
    val name: String,
    val file: File,
    val durationMs: Long,
    val startMs: Long = 0,
    val volume: Float = 0.75f,
    val pan: Float = 0f,
    val mute: Boolean = false,
    val solo: Boolean = false
) {
    fun pcmChannel() = PcmMixer.Channel(
        file,
        startMs,
        volume,
        pan,
        mute,
        solo
    )
}

data class ProjectMix(
    val tracks: List<ProjectTrack>,
    val master: Float = 1f
) {
    fun channels() = tracks.map { it.pcmChannel() }
}