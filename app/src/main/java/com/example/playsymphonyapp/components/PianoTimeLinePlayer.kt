package com.example.playsymphonyapp.components

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.playsymphonyapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Reproduce las pistas PIANO a partir de sus notas y las muestras de res/raw.
 * Usa la convención de Piano.kt: c1 = Do1 (MIDI 24), a1 = La1 (MIDI 33).
 * No reproduce TimelineTrackType.AUDIO, no procesa el mixer ni exporta archivos.
 * Llamar a sus métodos desde el hilo principal, como los onClick de Compose.
 */
class PianoTimelinePlayer(context: Context) {
    private val appContext = context.applicationContext

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private var playbackJob: Job? = null
    private var generation = 0

    private val voices = mutableMapOf<MediaPlayer, Long>()

    var isPlaying by mutableStateOf(false)
        private set

    var positionSeconds by mutableStateOf(0f)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private data class NoteEvent(
        val resource: Int,
        val startMs: Long,
        val endMs: Long
    )

    fun play(tracks: List<TimelineTrack>) {
        val pianoTracks = tracks.filter {
            it.type == TimelineTrackType.PIANO
        }

        val events = pianoTracks.flatMap { track ->
            track.clips.flatMap clipLoop@ { clip ->
                if (
                    !clip.start.isFinite() ||
                    !clip.duration.isFinite() ||
                    clip.duration <= 0
                ) {
                    return@clipLoop emptyList()
                }

                val clipEnd =
                    ((clip.start + clip.duration) * 1000).toLong()

                clip.notes.mapNotNull { note ->
                    val resource = timelinePianoResources[note.midi]

                    if (
                        resource == null ||
                        !note.start.isFinite() ||
                        !note.duration.isFinite() ||
                        note.duration <= 0
                    ) {
                        null
                    } else {
                        val start =
                            ((clip.start + note.start) * 1000).toLong()

                        val end = minOf(
                            clipEnd,
                            start + (note.duration * 1000).toLong()
                        )

                        if (end <= start || end <= 0) {
                            null
                        } else {
                            NoteEvent(resource, start, end)
                        }
                    }
                }
            }
        }.sortedBy {
            it.startMs
        }

        val endMs = pianoTracks
            .flatMap { it.clips }
            .map { it.start + it.duration }
            .filter { it.isFinite() && it > 0 }
            .maxOrNull()
            ?.let { (it * 1000).toLong() }
            ?: 0L

        cancelPlayback()
        errorMessage = null

        if (events.isEmpty() || endMs <= 0) {
            positionSeconds = 0f
            errorMessage =
                "No hay notas de piano grabadas para reproducir."
            return
        }

        // Después de llegar al final, un nuevo Play vuelve al comienzo.
        val fromMs = (positionSeconds * 1000).toLong().let {
            if (it >= endMs) {
                0L
            } else {
                it.coerceAtLeast(0L)
            }
        }

        val pending = events.filter {
            it.endMs > fromMs
        }

        val token = generation

        positionSeconds = fromMs / 1000f
        isPlaying = true

        playbackJob = scope.launch {
            val began = SystemClock.elapsedRealtime()
            var index = 0

            fun currentMs(): Long {
                return fromMs +
                        SystemClock.elapsedRealtime() - began
            }

            try {
                while (isActive && generation == token) {
                    var now = currentMs()

                    positionSeconds = minOf(now, endMs) / 1000f

                    if (now >= endMs) break

                    voices
                        .filterValues { it <= now }
                        .keys
                        .toList()
                        .forEach { releaseVoice(it) }

                    while (
                        index < pending.size &&
                        pending[index].startMs <= now
                    ) {
                        val event = pending[index++]

                        if (event.endMs > now) {
                            startVoice(
                                event,
                                token,
                                ::currentMs
                            )
                        }

                        now = currentMs()
                    }

                    delay(10)
                }
            } finally {
                if (generation == token) {
                    releaseVoices()
                    isPlaying = false
                }
            }
        }
    }

    fun pause() {
        cancelPlayback()
    }

    fun stop() {
        cancelPlayback()
        positionSeconds = 0f
    }

    fun seekTo(
        seconds: Float,
        tracks: List<TimelineTrack>
    ) {
        val resume = isPlaying

        cancelPlayback()

        positionSeconds = (
                seconds.takeIf { it.isFinite() } ?: 0f
                ).coerceAtLeast(0f)

        if (resume) {
            play(tracks)
        }
    }

    fun release() {
        cancelPlayback()
        scope.cancel()
    }

    private fun startVoice(
        event: NoteEvent,
        token: Int,
        currentMs: () -> Long
    ) {
        var created: MediaPlayer? = null

        try {
            val player = MediaPlayer.create(
                appContext,
                event.resource
            ) ?: error("No se pudo abrir la muestra.")

            created = player
            voices[player] = event.endMs

            player.setOnCompletionListener {
                releaseVoice(it)
            }

            player.setOnErrorListener { failed, _, _ ->
                errorMessage =
                    "No se pudo reproducir una de las notas."

                releaseVoice(failed)
                true
            }

            val offset = (
                    currentMs() - event.startMs
                    ).coerceAtLeast(0)

            if (
                currentMs() >= event.endMs ||
                offset >= player.duration
            ) {
                releaseVoice(player)
                return
            }

            // Si se comienza dentro de una nota, busca esa parte.
            if (offset > 30) {
                player.setOnSeekCompleteListener { prepared ->
                    if (
                        generation == token &&
                        isPlaying &&
                        voices.containsKey(prepared) &&
                        currentMs() < event.endMs
                    ) {
                        try {
                            prepared.start()
                        } catch (_: IllegalStateException) {
                            releaseVoice(prepared)
                        }
                    } else {
                        releaseVoice(prepared)
                    }
                }

                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O
                ) {
                    player.seekTo(
                        offset,
                        MediaPlayer.SEEK_CLOSEST
                    )
                } else {
                    @Suppress("DEPRECATION")
                    player.seekTo(
                        offset
                            .coerceAtMost(Int.MAX_VALUE.toLong())
                            .toInt()
                    )
                }
            } else {
                player.start()
            }
        } catch (_: Exception) {
            created?.let {
                releaseVoice(it)
            }

            errorMessage =
                "No se pudo cargar una de las muestras del piano."
        }
    }

    private fun cancelPlayback() {
        generation++

        playbackJob?.cancel()
        playbackJob = null

        isPlaying = false
        releaseVoices()
    }

    private fun releaseVoice(player: MediaPlayer) {
        if (voices.remove(player) != null) {
            runCatching {
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.setOnSeekCompleteListener(null)
                player.release()
            }
        }
    }

    private fun releaseVoices() {
        voices.keys.toList().forEach {
            releaseVoice(it)
        }
    }
}

@Composable
fun rememberPianoTimelinePlayer(): PianoTimelinePlayer {
    val context = LocalContext.current.applicationContext

    val player = remember(context) {
        PianoTimelinePlayer(context)
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    return player
}

// Mismo mapa de notas que usa el Piano.kt compartido.
private val timelinePianoResources: Map<Int, Int> = mapOf(
    33 to R.raw.a1,
    35 to R.raw.b1,
    24 to R.raw.c1,
    26 to R.raw.d1,
    28 to R.raw.e1,
    29 to R.raw.f1,
    31 to R.raw.g1,
    34 to R.raw.a11,
    25 to R.raw.c11,
    27 to R.raw.d11,
    30 to R.raw.f11,
    32 to R.raw.g11,

    45 to R.raw.a2,
    47 to R.raw.b2,
    36 to R.raw.c2,
    38 to R.raw.d2,
    40 to R.raw.e2,
    41 to R.raw.f2,
    43 to R.raw.g2,
    46 to R.raw.a22,
    37 to R.raw.c22,
    39 to R.raw.d22,
    42 to R.raw.f22,
    44 to R.raw.g22,

    57 to R.raw.a3,
    59 to R.raw.b3,
    48 to R.raw.c3,
    50 to R.raw.d3,
    52 to R.raw.e3,
    53 to R.raw.f3,
    55 to R.raw.g3,
    58 to R.raw.a33,
    49 to R.raw.c33,
    51 to R.raw.d33,
    54 to R.raw.f33,
    56 to R.raw.g33,

    69 to R.raw.a4,
    71 to R.raw.b4,
    60 to R.raw.c4,
    62 to R.raw.d4,
    64 to R.raw.e4,
    65 to R.raw.f4,
    67 to R.raw.g4,
    70 to R.raw.a44,
    61 to R.raw.c44,
    63 to R.raw.d44,
    66 to R.raw.f44,
    68 to R.raw.g44,

    81 to R.raw.a5,
    83 to R.raw.b5,
    72 to R.raw.c5,
    74 to R.raw.d5,
    76 to R.raw.e5,
    77 to R.raw.f5,
    79 to R.raw.g5,
    82 to R.raw.a55,
    73 to R.raw.c55,
    75 to R.raw.d55,
    78 to R.raw.f55,
    80 to R.raw.g55,

    93 to R.raw.a6,
    95 to R.raw.b6,
    84 to R.raw.c6,
    86 to R.raw.d6,
    88 to R.raw.e6,
    89 to R.raw.f6,
    91 to R.raw.g6,
    94 to R.raw.a66,
    85 to R.raw.c66,
    87 to R.raw.d66,
    90 to R.raw.f66,
    92 to R.raw.g66,

    105 to R.raw.a7,
    107 to R.raw.b7,
    96 to R.raw.c7,
    98 to R.raw.d7,
    100 to R.raw.e7,
    101 to R.raw.f7,
    103 to R.raw.g7,
    106 to R.raw.a77,
    97 to R.raw.c77,
    99 to R.raw.d77,
    102 to R.raw.f77,
    104 to R.raw.g77
)