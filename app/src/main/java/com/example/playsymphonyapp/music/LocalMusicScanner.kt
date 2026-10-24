package com.example.playsymphonyapp.music

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.playsymphonyapp.songDatabase.Song

class LocalMusicScanner {

    fun scanSongs(context: Context): List<Song> {
        val songs = mutableListOf<Song>()

        val collection =
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )

        val selection =
            "${MediaStore.Audio.Media.SIZE} > 0"

        val sortOrder =
            "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            val artistColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val mediaId = cursor.getLong(idColumn)

                val title = cursor.getString(titleColumn)
                    ?.takeIf { it.isNotBlank() }
                    ?: "Título desconocido"

                val artist = cursor.getString(artistColumn)
                    ?.takeIf {
                        it.isNotBlank() &&
                                it != "<unknown>"
                    }
                    ?: "Artista desconocido"

                val durationMs = cursor.getLong(durationColumn)

                val duration = formatDuration(durationMs)

                val contentUri = ContentUris.withAppendedId(
                    collection,
                    mediaId
                ).toString()

                songs.add(
                    Song(
                        title = title,
                        artist = artist,
                        duration = duration,
                        filePath = contentUri,
                        icon = com.example.playsymphonyapp.R.drawable.deltarune
                    )
                )
            }
        }

        return songs
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return "%d:%02d".format(minutes, seconds)
    }
}