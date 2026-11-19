package com.example.playsymphonyapp.Editor

import android.app.Application
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playsymphonyapp.R
import com.example.playsymphonyapp.components.TimelineTrack
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class ProjectViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application

    private val folder = File(
        app.filesDir,
        "project_audio"
    ).apply { mkdirs() }

    private val preferences = app.getSharedPreferences("project", 0)

    var tracks by mutableStateOf<List<ProjectTrack>>(emptyList())
        private set

    var master by mutableStateOf(1f)
        private set

    var positionMs by mutableStateOf(0L)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var isPreparing by mutableStateOf(false)
        private set

    var isBusy by mutableStateOf(false)
        private set

    var message by mutableStateOf<Int?>(null)
        private set

    private val liveMix = AtomicReference(ProjectMix(emptyList()))
    private var playbackJob: Job? = null
    private val history = ArrayDeque<ProjectMix>()

    val durationMs: Long
        get() = tracks.maxOfOrNull {
            it.startMs + it.durationMs
        } ?: 0L

    init {
        try {
            val saved = JSONObject(
                preferences.getString("snapshot", "{}") ?: "{}"
            )

            master = saved.optDouble("master", 1.0)
                .toFloat()
                .coerceIn(0f, 1f)

            val array = saved.optJSONArray("tracks") ?: JSONArray()

            tracks = (0 until array.length()).mapNotNull { index ->
                val value = array.getJSONObject(index)
                val filename = value.getString("file")

                if (File(filename).name != filename) {
                    return@mapNotNull null
                }

                val file = File(folder, filename)

                if (!file.isFile || file.length() < 4) {
                    return@mapNotNull null
                }

                ProjectTrack(
                    id = value.getString("id"),
                    name = value.getString("name"),
                    file = file,
                    durationMs = file.length() / 4 * 1000 / PcmMixer.RATE,
                    startMs = value.optLong("start")
                        .coerceIn(0, MAX_START_MS),
                    volume = value.optDouble("volume", 0.75)
                        .toFloat().coerceIn(0f, 1f),
                    pan = value.optDouble("pan", 0.0)
                        .toFloat().coerceIn(-1f, 1f),
                    mute = value.optBoolean("mute"),
                    solo = value.optBoolean("solo")
                )
            }
        } catch (error: Exception) {
            Log.e("Project", "Cannot restore project", error)
            message = R.string.project_restore_error
        }

        publish()
    }

    private fun publish() {
        liveMix.set(ProjectMix(tracks, master))

        val array = JSONArray()

        tracks.forEach { track ->
            array.put(
                JSONObject().apply {
                    put("id", track.id)
                    put("name", track.name)
                    put("file", track.file.name)
                    put("start", track.startMs)
                    put("volume", track.volume)
                    put("pan", track.pan)
                    put("mute", track.mute)
                    put("solo", track.solo)
                }
            )
        }

        preferences.edit()
            .putString(
                "snapshot",
                JSONObject().apply {
                    put("master", master)
                    put("tracks", array)
                }.toString()
            )
            .apply()
    }

    fun dismissMessage() {
        message = null
    }

    fun rememberEdit() {
        if (history.size >= 50) history.removeFirst()
        history.addLast(ProjectMix(tracks, master))
    }

    fun undo() {
        if (isBusy || history.isEmpty()) return

        pause()

        val previous = history.removeLast()
        tracks = previous.tracks
        master = previous.master
        positionMs = positionMs.coerceAtMost(durationMs)

        publish()
    }

    fun updateTrack(track: ProjectTrack) {
        if (isBusy) return

        tracks = tracks.map {
            if (it.id == track.id) track else it
        }

        publish()
    }

    fun moveTrack(id: String, startMs: Long) {
        pause()

        val track = tracks.find { it.id == id } ?: return

        updateTrack(
            track.copy(
                startMs = startMs.coerceIn(0, MAX_START_MS)
            )
        )
    }

    fun reorder(id: String, direction: Int) {
        if (isBusy) return

        val index = tracks.indexOfFirst { it.id == id }
        val next = index + direction

        if (index < 0 || next !in tracks.indices) return

        pause()
        rememberEdit()

        tracks = tracks.toMutableList().apply {
            add(next, removeAt(index))
        }

        publish()
    }

    fun updateMasterVolume(value: Float) {
        if (isBusy || !value.isFinite()) return

        master = value.coerceIn(0f, 1f)
        publish()
    }

    fun removeTrack(id: String) {
        if (isBusy) return

        pause()
        rememberEdit()

        // Conserva el archivo para permitir Deshacer.
        tracks = tracks.filterNot { it.id == id }
        positionMs = positionMs.coerceAtMost(durationMs)

        publish()
    }

    fun newProject() {
        if (isBusy) return

        pause()

        val previous = playbackJob
        val oldFiles = folder.listFiles().orEmpty().toList()

        tracks = emptyList()
        master = 1f
        positionMs = 0
        history.clear()

        publish()

        viewModelScope.launch(Dispatchers.IO) {
            previous?.join()
            oldFiles.forEach { it.delete() }
        }
    }

    fun seekTo(milliseconds: Long) {
        pause()
        positionMs = milliseconds.coerceIn(0, durationMs)
    }

    fun pause() {
        playbackJob?.cancel()
        isPlaying = false
        isPreparing = false
    }

    fun togglePlayback() {
        if (isPlaying || isPreparing) {
            pause()
            return
        }

        if (tracks.isEmpty() || isBusy) return

        val previous = playbackJob
        isPreparing = true

        playbackJob = viewModelScope.launch {
            previous?.join()

            try {
                if (positionMs >= durationMs) positionMs = 0

                val snapshot = liveMix.get()
                val channels = snapshot.channels()
                val endFrame = PcmMixer.durationFrames(channels)
                val firstFrame = positionMs * PcmMixer.RATE / 1000

                withContext(Dispatchers.IO) {
                    val format = AudioFormat.Builder()
                        .setSampleRate(PcmMixer.RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build()

                    val minimum = AudioTrack.getMinBufferSize(
                        PcmMixer.RATE,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )

                    check(minimum > 0)

                    val output = AudioTrack.Builder()
                        .setAudioFormat(format)
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(
                                    AudioAttributes.CONTENT_TYPE_MUSIC
                                )
                                .build()
                        )
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .setBufferSizeInBytes(maxOf(minimum, 8192))
                        .build()

                    try {
                        check(output.state == AudioTrack.STATE_INITIALIZED)

                        PcmMixer(channels).use { mixer ->
                            withContext(Dispatchers.Main) {
                                isPreparing = false
                                isPlaying = true
                            }

                            output.play()
                            var frame = firstFrame

                            while (frame < endFrame) {
                                ensureActive()

                                val count = min(
                                    1024L,
                                    endFrame - frame
                                ).toInt()

                                val live = liveMix.get()

                                val block = mixer.read(
                                    frame,
                                    count,
                                    live.channels(),
                                    live.master
                                )

                                var written = 0

                                while (written < block.size) {
                                    ensureActive()

                                    val size = output.write(
                                        block,
                                        written,
                                        block.size - written,
                                        AudioTrack.WRITE_NON_BLOCKING
                                    )

                                    check(size >= 0) {
                                        "AudioTrack write failed: $size"
                                    }

                                    if (size == 0) {
                                        delay(5)
                                    } else {
                                        written += size
                                    }
                                }

                                frame += count

                                val played = output.playbackHeadPosition
                                    .toLong() and 0xffffffffL

                                withContext(Dispatchers.Main) {
                                    positionMs = (firstFrame + played) *
                                            1000 / PcmMixer.RATE
                                }
                            }

                            while (
                                (output.playbackHeadPosition.toLong()
                                        and 0xffffffffL) <
                                endFrame - firstFrame
                            ) {
                                ensureActive()
                                delay(10)
                            }

                            withContext(Dispatchers.Main) {
                                positionMs = durationMs
                            }
                        }
                    } finally {
                        runCatching {
                            output.pause()
                            output.flush()
                        }

                        output.release()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("Project", "Playback failed", error)
                message = R.string.playback_error
            } finally {
                isPlaying = false
                isPreparing = false
            }
        }
    }

    fun importAudio(uri: Uri) {
        if (isBusy) return

        pause()
        isBusy = true

        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val pcm = File(folder, "$id.pcm")
            val source = File(app.cacheDir, "$id.input")

            try {
                val track = withContext(Dispatchers.IO) {
                    val name = if (uri.scheme == "content") {
                        app.contentResolver.query(
                            uri,
                            arrayOf(OpenableColumns.DISPLAY_NAME),
                            null,
                            null,
                            null
                        )?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                cursor.getString(0)
                            } else null
                        }
                    } else {
                        uri.lastPathSegment
                    }

                    val input = app.contentResolver.openInputStream(uri)
                        ?: error("Cannot open audio")

                    input.use { stream ->
                        source.outputStream().use {
                            stream.copyTo(it)
                        }
                    }

                    ffmpeg(
                        listOf(
                            "-y",
                            "-i", source.absolutePath,
                            "-vn",
                            "-map", "0:a:0",
                            "-ar", "44100",
                            "-ac", "2",
                            "-c:a", "pcm_s16le",
                            "-f", "s16le",
                            pcm.absolutePath
                        )
                    )

                    check(pcm.length() >= 4 && pcm.length() % 4 == 0L)

                    ProjectTrack(
                        id = id,
                        name = name ?: app.getString(R.string.audio_track),
                        file = pcm,
                        durationMs = pcm.length() / 4 * 1000 / PcmMixer.RATE
                    )
                }

                rememberEdit()
                tracks = tracks + track
                publish()
            } catch (cancelled: CancellationException) {
                pcm.delete()
                throw cancelled
            } catch (error: Exception) {
                pcm.delete()
                Log.e("Project", "Import failed", error)
                message = R.string.import_error
            } finally {
                source.delete()
                isBusy = false
            }
        }
    }

    fun addRecording(track: TimelineTrack) {
        if (isBusy) return

        pause()
        isBusy = true

        viewModelScope.launch {
            val pcm = File(folder, "${UUID.randomUUID()}.pcm")
            val temporary = mutableListOf<File>()

            try {
                val recorded = withContext(Dispatchers.IO) {
                    val cache = mutableMapOf<Int, File>()

                    val channels = track.clips.flatMap { clip ->
                        clip.notes.map { note ->
                            val octave = note.midi / 12 - 1

                            val names = arrayOf(
                                "c", "c", "d", "d", "e", "f",
                                "f", "g", "g", "a", "a", "b"
                            )

                            val sharp = note.midi % 12 in
                                    listOf(1, 3, 6, 8, 10)

                            val name = names[note.midi % 12] +
                                    octave +
                                    if (sharp) "$octave" else ""

                            val resource = app.resources.getIdentifier(
                                name,
                                "raw",
                                app.packageName
                            )

                            check(resource != 0) {
                                "Missing piano sample: $name"
                            }

                            val decoded = cache[resource] ?: run {
                                val input = File.createTempFile(
                                    "note-",
                                    ".mp3",
                                    app.cacheDir
                                )

                                val output = File.createTempFile(
                                    "note-",
                                    ".pcm",
                                    app.cacheDir
                                )

                                temporary.add(input)
                                temporary.add(output)

                                app.resources.openRawResource(resource)
                                    .use { source ->
                                        input.outputStream().use {
                                            source.copyTo(it)
                                        }
                                    }

                                ffmpeg(
                                    listOf(
                                        "-y",
                                        "-i", input.absolutePath,
                                        "-vn",
                                        "-ar", "44100",
                                        "-ac", "2",
                                        "-c:a", "pcm_s16le",
                                        "-f", "s16le",
                                        output.absolutePath
                                    )
                                )

                                cache[resource] = output
                                output
                            }

                            PcmMixer.Channel(
                                decoded,
                                ((clip.start + note.start) * 1000).toLong(),
                                1f,
                                0f,
                                false,
                                false
                            )
                        }
                    }

                    check(channels.isNotEmpty())

                    val end = maxOf(
                        PcmMixer.durationFrames(channels),
                        (
                                track.clips.maxOf {
                                    it.start + it.duration
                                } * PcmMixer.RATE
                                ).toLong()
                    )

                    PcmMixer(channels).use { mixer ->
                        pcm.outputStream().buffered().use { output ->
                            var frame = 0L

                            while (frame < end) {
                                ensureActive()

                                val count = min(
                                    4096L,
                                    end - frame
                                ).toInt()

                                output.write(
                                    PcmMixer.littleEndian(
                                        mixer.read(
                                            frame,
                                            count,
                                            channels,
                                            1f
                                        )
                                    )
                                )

                                frame += count
                            }
                        }
                    }

                    ProjectTrack(
                        id = UUID.randomUUID().toString(),
                        name = track.name,
                        file = pcm,
                        durationMs = end * 1000 / PcmMixer.RATE
                    )
                }

                rememberEdit()
                tracks = tracks + recorded
                publish()
            } catch (cancelled: CancellationException) {
                pcm.delete()
                throw cancelled
            } catch (error: Exception) {
                pcm.delete()
                Log.e("Project", "Piano render failed", error)
                message = R.string.import_error
            } finally {
                temporary.forEach { it.delete() }
                isBusy = false
            }
        }
    }

    fun exportMp3(destination: Uri) {
        if (isBusy || tracks.isEmpty()) return

        pause()

        val snapshot = ProjectMix(tracks.toList(), master)
        isBusy = true

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val mixed = File.createTempFile(
                        "mix-",
                        ".pcm",
                        app.cacheDir
                    )

                    var encoded: File? = null

                    try {
                        val mp3 = File.createTempFile(
                            "mix-",
                            ".mp3",
                            app.cacheDir
                        )

                        encoded = mp3

                        val channels = snapshot.channels()
                        val end = PcmMixer.durationFrames(channels)

                        PcmMixer(channels).use { mixer ->
                            mixed.outputStream().buffered().use { output ->
                                var frame = 0L

                                while (frame < end) {
                                    ensureActive()

                                    val count = min(
                                        4096L,
                                        end - frame
                                    ).toInt()

                                    output.write(
                                        PcmMixer.littleEndian(
                                            mixer.read(
                                                frame,
                                                count,
                                                channels,
                                                snapshot.master
                                            )
                                        )
                                    )

                                    frame += count
                                }
                            }
                        }

                        ffmpeg(
                            listOf(
                                "-y",
                                "-f", "s16le",
                                "-ar", "44100",
                                "-ac", "2",
                                "-i", mixed.absolutePath,
                                "-c:a", "libmp3lame",
                                "-b:a", "192k",
                                mp3.absolutePath
                            )
                        )

                        check(mp3.length() > 0)

                        val stream = app.contentResolver.openOutputStream(
                            destination,
                            "wt"
                        ) ?: error("Cannot write destination")

                        stream.use { output ->
                            mp3.inputStream().use { input ->
                                val buffer = ByteArray(65536)

                                while (true) {
                                    ensureActive()

                                    val count = input.read(buffer)
                                    if (count < 0) break

                                    output.write(buffer, 0, count)
                                }
                            }
                        }
                    } finally {
                        mixed.delete()
                        encoded?.delete()
                    }
                }

                message = R.string.export_success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e("Project", "Export failed", error)
                message = R.string.export_error
            } finally {
                isBusy = false
            }
        }
    }

    override fun onCleared() {
        playbackJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val MAX_START_MS = 24 * 60 * 60 * 1000L
    }
}