package com.example.playsymphonyapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.playsymphonyapp.R
import com.example.playsymphonyapp.components.MixerSection
import com.example.playsymphonyapp.components.PianoAdaptable
import com.example.playsymphonyapp.components.Recorder
import com.example.playsymphonyapp.Editor.ProjectTrack
import com.example.playsymphonyapp.Editor.ProjectViewModel
import com.example.playsymphonyapp.viewmodels.MusicViewModel
import java.io.File
import java.util.Locale
import kotlin.math.roundToLong

private val EditorCard = Color(0xFF1A1B2E)
private val EditorAccent = Color(0xFFBB86FC)

private val ChannelColors = listOf(
    Color(0xFFBB86FC),
    Color(0xFF80FFE5),
    Color(0xFFFFB86C),
    Color(0xFFFF7A87)
)

@Composable
fun TracksScreen(
    navController: NavController,
    viewModel: MusicViewModel,
    project: ProjectViewModel
) {
    val recorder = remember { Recorder() }

    var section by rememberSaveable {
        mutableStateOf(
            if (project.tracks.isEmpty()) "piano" else "timeline"
        )
    }

    var confirmNew by remember { mutableStateOf(false) }
    val owner = LocalLifecycleOwner.current

    DisposableEffect(owner) {
        if (viewModel.isPlaying.value) {
            viewModel.togglePlayPause()
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                project.pause()
                recorder.stopAsTrack()?.let(project::addRecording)
            }
        }

        owner.lifecycle.addObserver(observer)

        onDispose {
            owner.lifecycle.removeObserver(observer)
            project.pause()
            recorder.stopAsTrack()?.let(project::addRecording)
        }
    }

    val importer = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            project.importAudio(it)
            section = "timeline"
        }
    }

    val exporter = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("audio/mpeg")
    ) { uri ->
        uri?.let(project::exportMp3)
    }

    val sections = listOf(
        "piano" to R.string.piano,
        "timeline" to R.string.timeline,
        "mixer" to R.string.mixer,
        "export" to R.string.export_audio
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1019))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "PlaySymphony",
            color = EditorAccent,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                enabled = !project.isBusy && !recorder.isRecording,
                onClick = {
                    if (project.tracks.isEmpty()) {
                        project.newProject()
                        section = "piano"
                    } else {
                        confirmNew = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.new_project))
            }

            Button(
                enabled = !project.isBusy && !recorder.isRecording,
                onClick = {
                    importer.launch(arrayOf("audio/*"))
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.add_track))
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                enabled = project.tracks.isNotEmpty() &&
                        !project.isBusy &&
                        !recorder.isRecording,
                onClick = project::togglePlayback
            ) {
                Text(
                    stringResource(
                        if (project.isPlaying || project.isPreparing) {
                            R.string.pause
                        } else {
                            R.string.play
                        }
                    )
                )
            }

            OutlinedButton(
                onClick = { project.seekTo(0) }
            ) {
                Text(stringResource(R.string.to_start))
            }

            Text(
                "${clock(project.positionMs)} / ${clock(project.durationMs)}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }

        val seekLabel = stringResource(R.string.seek)

        Slider(
            value = project.positionMs.toFloat().coerceIn(
                0f,
                project.durationMs.toFloat().coerceAtLeast(1f)
            ),
            valueRange = 0f..project.durationMs
                .toFloat()
                .coerceAtLeast(1f),
            onValueChange = {
                project.seekTo(it.toLong())
            },
            enabled = project.tracks.isNotEmpty() && !project.isBusy,
            modifier = Modifier.semantics {
                contentDescription = seekLabel
            }
        )

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEach { (id, label) ->
                FilterChip(
                    selected = section == id,
                    enabled = !recorder.isRecording,
                    onClick = {
                        project.pause()
                        section = id
                    },
                    label = {
                        Text(stringResource(label))
                    }
                )
            }
        }

        if (project.isBusy || project.isPreparing) {
            LinearProgressIndicator(Modifier.fillMaxWidth())

            Text(
                stringResource(R.string.processing_audio),
                color = Color.LightGray
            )
        }

        project.message?.let { resource ->
            Text(
                stringResource(resource),
                color = EditorAccent
            )

            TextButton(onClick = project::dismissMessage) {
                Text(stringResource(R.string.dismiss))
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (section == "piano") {
                Column(Modifier.fillMaxSize()) {
                    Button(
                        enabled = !project.isBusy,
                        onClick = {
                            project.pause()

                            if (recorder.isRecording) {
                                recorder.stopAsTrack()
                                    ?.let(project::addRecording)

                                section = "timeline"
                            } else {
                                recorder.start()
                            }
                        }
                    ) {
                        Text(
                            stringResource(
                                if (recorder.isRecording) {
                                    R.string.stop_recording
                                } else {
                                    R.string.record
                                }
                            )
                        )
                    }

                    PianoAdaptable(
                        recorder = recorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (section) {
                        "timeline" -> {
                            Text(
                                stringResource(R.string.timeline_help),
                                color = Color.LightGray
                            )

                            Row(
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    enabled = !project.isBusy,
                                    onClick = project::undo
                                ) {
                                    Text(stringResource(R.string.undo))
                                }

                                viewModel.currentSong.value?.let { song ->
                                    OutlinedButton(
                                        enabled = !project.isBusy,
                                        onClick = {
                                            val uri = if (
                                                song.filePath.contains("://")
                                            ) {
                                                Uri.parse(song.filePath)
                                            } else {
                                                Uri.fromFile(File(song.filePath))
                                            }

                                            project.importAudio(uri)
                                        }
                                    ) {
                                        Text(
                                            stringResource(
                                                R.string.add_current_song
                                            )
                                        )
                                    }
                                }
                            }

                            if (project.tracks.isEmpty()) {
                                EmptyProject()
                            } else {
                                Timeline(project)
                            }
                        }

                        "mixer" -> {
                            if (project.tracks.isEmpty()) {
                                EmptyProject()
                            }

                            MixerSection(
                                tracks = project.tracks,
                                master = project.master,
                                enabled = !project.isBusy,
                                onTrackChange = project::updateTrack,
                                onMasterChange = project::updateMasterVolume,
                                onEditStart = project::rememberEdit
                            )
                        }

                        "export" -> {
                            Text(
                                stringResource(R.string.export_description),
                                color = Color.White
                            )

                            Button(
                                enabled = project.tracks.isNotEmpty() &&
                                        !project.isBusy,
                                onClick = {
                                    exporter.launch("PlaySymphony.mp3")
                                }
                            ) {
                                Text(stringResource(R.string.save_mp3))
                            }

                            if (project.tracks.isEmpty()) {
                                EmptyProject()
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmNew) {
        AlertDialog(
            onDismissRequest = {
                confirmNew = false
            },
            title = {
                Text(stringResource(R.string.new_project))
            },
            text = {
                Text(
                    stringResource(
                        R.string.new_project_confirmation
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        project.newProject()
                        section = "piano"
                        confirmNew = false
                    }
                ) {
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmNew = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyProject() {
    Text(
        stringResource(R.string.empty_project),
        color = Color.LightGray
    )
}

@Composable
private fun Timeline(project: ProjectViewModel) {
    var spanMs by rememberSaveable {
        mutableLongStateOf(60000L)
    }

    LaunchedEffect(project.tracks.map { it.id }) {
        spanMs = maxOf(
            spanMs,
            project.durationMs + 30000
        )
    }

    OutlinedButton(
        enabled = spanMs < ProjectViewModel.MAX_START_MS,
        onClick = {
            spanMs = (spanMs + 60000)
                .coerceAtMost(ProjectViewModel.MAX_START_MS)
        }
    ) {
        Text(stringResource(R.string.extend_timeline))
    }

    val scroll = rememberScrollState()

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val timelineWidth = maxOf(
            maxWidth,
            (spanMs / 1000f * 24).dp.coerceAtMost(20000.dp)
        )

        val pixelsPerMs = with(LocalDensity.current) {
            timelineWidth.toPx()
        } / spanMs

        Column(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
        ) {
            Column(
                Modifier.width(timelineWidth),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..8).forEach { step ->
                        Text(
                            clock(spanMs * step / 8),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                project.tracks.forEachIndexed { index, track ->
                    key(track.id) {
                        TimelineClip(
                            track,
                            index,
                            project,
                            timelineWidth,
                            spanMs,
                            pixelsPerMs
                        )
                    }
                }
            }
        }
    }

    project.tracks.forEachIndexed { index, track ->
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                track.name,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                enabled = index > 0 && !project.isBusy,
                onClick = {
                    project.reorder(track.id, -1)
                }
            ) {
                Text("↑")
            }

            TextButton(
                enabled = index < project.tracks.lastIndex &&
                        !project.isBusy,
                onClick = {
                    project.reorder(track.id, 1)
                }
            ) {
                Text("↓")
            }

            TextButton(
                enabled = !project.isBusy,
                onClick = {
                    project.removeTrack(track.id)
                }
            ) {
                Text(stringResource(R.string.remove))
            }
        }
    }
}

@Composable
private fun TimelineClip(
    track: ProjectTrack,
    index: Int,
    project: ProjectViewModel,
    width: androidx.compose.ui.unit.Dp,
    spanMs: Long,
    pixelsPerMs: Float
) {
    val latest by rememberUpdatedState(track)
    val enabled by rememberUpdatedState(!project.isBusy)
    val color = ChannelColors[index % ChannelColors.size]

    Box(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(EditorCard, RoundedCornerShape(6.dp))
    ) {
        Box(
            Modifier
                .offset(
                    x = width * (track.startMs.toFloat() / spanMs)
                )
                .width(
                    (
                            width * (track.durationMs.toFloat() / spanMs)
                            ).coerceAtLeast(48.dp)
                )
                .fillMaxHeight()
                .background(
                    color.copy(alpha = 0.35f),
                    RoundedCornerShape(6.dp)
                )
                .border(
                    1.dp,
                    color,
                    RoundedCornerShape(6.dp)
                )
                .pointerInput(track.id, pixelsPerMs, spanMs) {
                    var start = 0L
                    var delta = 0f

                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            if (enabled) {
                                project.pause()
                                project.rememberEdit()
                                start = latest.startMs
                                delta = 0f
                            }
                        },
                        onDrag = { change, amount ->
                            if (enabled) {
                                change.consume()
                                delta += amount.x

                                val next = (
                                        start +
                                                (delta / pixelsPerMs).roundToLong()
                                        ).coerceIn(
                                        0L,
                                        (spanMs - latest.durationMs)
                                            .coerceAtLeast(0)
                                    )

                                project.moveTrack(latest.id, next)
                            }
                        }
                    )
                }
                .padding(8.dp)
        ) {
            Column {
                Text(
                    track.name,
                    color = Color.White,
                    maxLines = 1
                )

                Text(
                    clock(track.startMs),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Box(
            Modifier
                .offset(
                    x = width * (project.positionMs.toFloat() / spanMs)
                )
                .width(2.dp)
                .fillMaxHeight()
                .background(Color.Red)
        )
    }
}

private fun clock(ms: Long): String = String.format(
    Locale.ROOT,
    "%d:%02d.%01d",
    ms / 60000,
    ms / 1000 % 60,
    ms / 100 % 10
)