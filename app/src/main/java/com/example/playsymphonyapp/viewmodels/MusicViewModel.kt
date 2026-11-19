package com.example.playsymphonyapp.viewmodels

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.example.playsymphonyapp.songDatabase.Song
import com.example.playsymphonyapp.songDatabase.SongDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val playlist = mutableStateOf<List<Song>>(emptyList())
    val currentSong = mutableStateOf<Song?>(null)

    val isPlaying = mutableStateOf(false)
    val shuffleEnabled = mutableStateOf(false)
    val repeatEnabled = mutableStateOf(false)

    // Progreso normalizado entre 0 y 1.
    val currentPosition = mutableStateOf(0f)
    val currentTime = mutableStateOf("0:00")
    val totalDuration = mutableStateOf("0:00")

    // Estado visual del editor.
    val inPosition = mutableStateOf(0f)
    val outPosition = mutableStateOf(1f)
    val playheadPosition = mutableStateOf(0f)
    val zoomScale = mutableStateOf(1f)
    val pitchValue = mutableStateOf(0f)

    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false

    private data class EditorState(
        val start: Float,
        val end: Float,
        val playhead: Float,
        val zoom: Float,
        val pitch: Float
    )

    private val editHistory = mutableListOf<EditorState>()

    init {
        viewModelScope.launch {
            while (isActive) {
                val player = mediaPlayer

                if (prepared && player != null) {
                    updateProgress(player.currentPosition, player.duration)
                }

                delay(200)
            }
        }
    }

    fun loadSongs(dao: SongDao) {
        viewModelScope.launch {
            playlist.value = dao.getAllSongs()
        }
    }

    fun playSong(song: Song) {
        releasePlayer()

        currentSong.value = song
        currentPosition.value = 0f
        currentTime.value = "0:00"
        totalDuration.value = "0:00"

        editHistory.clear()
        restoreEditorDefaults()

        val player = MediaPlayer()
        mediaPlayer = player

        try {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )

            player.setOnPreparedListener { readyPlayer ->
                if (mediaPlayer === readyPlayer) {
                    prepared = true
                    totalDuration.value = formatTime(readyPlayer.duration)
                    readyPlayer.start()
                    isPlaying.value = true
                }
            }

            player.setOnCompletionListener { completedPlayer ->
                if (mediaPlayer === completedPlayer) {
                    isPlaying.value = false

                    if (repeatEnabled.value) {
                        completedPlayer.seekTo(0)
                        completedPlayer.start()
                        isPlaying.value = true
                    } else {
                        playNext()
                    }
                }
            }

            player.setOnErrorListener { failedPlayer, what, extra ->
                if (mediaPlayer === failedPlayer) {
                    releasePlayer()
                }

                Log.e(
                    "MusicViewModel",
                    "Error de reproducción: what=$what, extra=$extra"
                )
                true
            }

            val source = song.filePath

            if (source.startsWith("content://") || source.startsWith("file://")) {
                player.setDataSource(
                    getApplication<Application>(),
                    Uri.parse(source)
                )
            } else {
                // Compatibilidad con las rutas de archivo anteriores.
                player.setDataSource(source)
            }
            player.prepareAsync()
        } catch (exception: Exception) {
            releasePlayer()
            Log.e(
                "MusicViewModel",
                "No se pudo abrir ${song.filePath}",
                exception
            )
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer

        if (player == null) {
            val song = currentSong.value ?: playlist.value.firstOrNull()
            song?.let { playSong(it) }
            return
        }

        if (!prepared) return

        if (player.isPlaying) {
            player.pause()
            isPlaying.value = false
        } else {
            player.start()
            isPlaying.value = true
        }
    }

    fun seekTo(position: Float) {
        if (!position.isFinite()) return

        val player = mediaPlayer ?: return
        if (!prepared) return

        val duration = player.duration.coerceAtLeast(0)
        val target = (position.coerceIn(0f, 1f) * duration).toInt()

        player.seekTo(target)
        updateProgress(target, duration)
    }

    fun playNext() {
        changeSong(1)
    }

    fun playPrevious() {
        changeSong(-1)
    }

    private fun changeSong(direction: Int) {
        val songs = playlist.value
        if (songs.isEmpty()) return

        val currentIndex = songs.indexOfFirst {
            it.id == currentSong.value?.id
        }

        val nextSong = when {
            shuffleEnabled.value && songs.size > 1 -> {
                songs.filterIndexed { index, _ ->
                    index != currentIndex
                }.random()
            }

            currentIndex == -1 -> songs.first()

            else -> {
                val nextIndex =
                    (currentIndex + direction + songs.size) % songs.size
                songs[nextIndex]
            }
        }

        playSong(nextSong)
    }

    private fun updateProgress(position: Int, duration: Int) {
        val progress = if (duration > 0) {
            (position.toFloat() / duration).coerceIn(0f, 1f)
        } else {
            0f
        }

        currentPosition.value = progress
        playheadPosition.value = progress
        currentTime.value = formatTime(position)
        totalDuration.value = formatTime(duration)
    }

    fun saveEditsToHistory() {
        if (editHistory.size >= 100) {
            editHistory.removeAt(0)
        }

        editHistory.add(
            EditorState(
                start = inPosition.value,
                end = outPosition.value,
                playhead = playheadPosition.value,
                zoom = zoomScale.value,
                pitch = pitchValue.value
            )
        )
    }

    fun undoLastEdit() {
        if (editHistory.isEmpty()) return

        val previous = editHistory.removeAt(editHistory.lastIndex)

        inPosition.value = previous.start
        outPosition.value = previous.end
        zoomScale.value = previous.zoom
        pitchValue.value = previous.pitch
        playheadPosition.value = previous.playhead
        seekTo(previous.playhead)
    }

    fun resetEditor() {
        saveEditsToHistory()
        restoreEditorDefaults()
        seekTo(0f)
    }

    private fun restoreEditorDefaults() {
        inPosition.value = 0f
        outPosition.value = 1f
        playheadPosition.value = 0f
        zoomScale.value = 1f
        pitchValue.value = 0f
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds.coerceAtLeast(0) / 1000
        val minutes = seconds / 60
        return "$minutes:${(seconds % 60).toString().padStart(2, '0')}"
    }

    private fun releasePlayer() {
        prepared = false
        isPlaying.value = false

        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}