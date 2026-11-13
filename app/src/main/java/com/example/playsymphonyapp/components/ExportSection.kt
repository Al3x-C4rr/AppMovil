package com.example.playsymphonyapp.components

import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.playsymphonyapp.export.AudioExportRequest
import com.example.playsymphonyapp.export.AudioExporter
import com.example.playsymphonyapp.export.AudioFileSaver
import com.example.playsymphonyapp.songDatabase.Song
import com.example.playsymphonyapp.ui.theme.AccentColor
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val pendingExportSaver = mapSaver<AudioExportRequest?>(
    save = { request ->
        if (request == null) {
            emptyMap()
        } else {
            mapOf(
                "sourcePath" to request.sourcePath,
                "title" to request.title,
                "artist" to request.artist,
                "startFraction" to request.startFraction,
                "endFraction" to request.endFraction,
                "pitchSemitones" to request.pitchSemitones
            )
        }
    },
    restore = { values ->
        if (values.isEmpty()) {
            null
        } else {
            AudioExportRequest(
                sourcePath = values["sourcePath"] as String,
                title = values["title"] as String,
                artist = values["artist"] as String,
                startFraction = values["startFraction"] as Float,
                endFraction = values["endFraction"] as Float,
                pitchSemitones = values["pitchSemitones"] as Float
            )
        }
    }
)

@Composable
fun ExportSection(
    modifier: Modifier = Modifier,
    song: Song? = null,
    inPosition: Float = 0f,
    outPosition: Float = 1f,
    pitchSemitones: Float = 0f
) {
    var nombre by rememberSaveable(song?.id, song?.filePath) {
        mutableStateOf(
            song?.title?.let { "${it}_editada" } ?: ""
        )
    }

    var artista by rememberSaveable(song?.id, song?.filePath) {
        mutableStateOf(song?.artist ?: "")
    }

    var mensaje by remember(song?.id, song?.filePath) {
        mutableStateOf<String?>(null)
    }

    var exportando by remember {
        mutableStateOf(false)
    }

    var pendingRequest by rememberSaveable(
        stateSaver = pendingExportSaver
    ) {
        mutableStateOf<AudioExportRequest?>(null)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exporter = remember(context) {
        AudioExporter(context)
    }

    val fileSaver = remember(context) {
        AudioFileSaver(context)
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(
            "audio/mp4"
        )
    ) { destination ->
        val request = pendingRequest
        pendingRequest = null

        if (destination == null) {
            mensaje = "Guardado cancelado."
        } else if (request == null) {
            mensaje =
                "No se recuperaron los datos de exportación. " +
                        "Inténtalo nuevamente."

            scope.launch(Dispatchers.IO) {
                runCatching {
                    DocumentsContract.deleteDocument(
                        context.contentResolver,
                        destination
                    )
                }
            }
        } else {
            exportando = true
            mensaje = "Procesando el audio…"

            scope.launch {
                var temporaryFile: File? = null
                var saved = false

                try {
                    val generatedFile = exporter.export(request)
                    temporaryFile = generatedFile

                    mensaje = "Guardando el archivo…"

                    fileSaver.save(
                        sourceFile = generatedFile,
                        destination = destination
                    )

                    saved = true
                    mensaje =
                        "Audio guardado correctamente " +
                                "en la ubicación elegida."
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    mensaje =
                        "No se pudo exportar el audio: " +
                                (exception.message ?: "Error desconocido.")
                } finally {
                    withContext(
                        NonCancellable + Dispatchers.IO
                    ) {
                        temporaryFile?.delete()

                        if (!saved) {
                            runCatching {
                                DocumentsContract.deleteDocument(
                                    context.contentResolver,
                                    destination
                                )
                            }
                        }
                    }

                    exportando = false
                }
            }
        }
    }

    val ocupado = exportando || pendingRequest != null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(150.dp),
                tint = AccentColor.copy(alpha = 0.5f)
            )
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del archivo") },
            singleLine = true,
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = artista,
            onValueChange = { artista = it },
            label = { Text("Artista") },
            singleLine = true,
            enabled = !ocupado,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Formato de salida: M4A (AAC)")

        Text(
            "Por ahora, el artista no se incorpora " +
                    "como etiqueta interna del archivo."
        )

        Button(
            enabled = song != null && !ocupado,
            onClick = {
                try {
                    val selectedSong = requireNotNull(song) {
                        "Selecciona una canción para exportar."
                    }

                    val request = AudioExportRequest(
                        sourcePath = selectedSong.filePath,
                        title = nombre.trim(),
                        artist = artista.trim(),
                        startFraction = inPosition,
                        endFraction = outPosition,
                        pitchSemitones = pitchSemitones
                    )

                    val safeName = request.title
                        .map { character ->
                            if (
                                character.isLetterOrDigit() ||
                                character == ' ' ||
                                character == '_' ||
                                character == '-' ||
                                character == '.'
                            ) {
                                character
                            } else {
                                '_'
                            }
                        }
                        .joinToString("")
                        .trim(' ', '.')
                        .take(80)
                        .ifBlank { "audio_editado" }

                    pendingRequest = request
                    mensaje = null

                    saveLauncher.launch("$safeName.m4a")
                } catch (exception: Exception) {
                    pendingRequest = null
                    mensaje = exception.message
                        ?: "No se pudo iniciar la exportación."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (exportando) {
                    "Exportando…"
                } else {
                    "Exportar audio"
                }
            )
        }

        mensaje?.let { texto ->
            Text(text = texto)
        }
    }
}