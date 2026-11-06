package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playsymphonyapp.ui.theme.LightText

@Composable
fun AudioCraftTopBar(
    title: String,
    artist: String,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onResetRequested: () -> Unit,
    onUndoRequested: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF1A1B2E),
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AudioCraft",
                        color = Color(0xFFBB86FC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Text(
                        text = "$title · $artist",
                        modifier = Modifier.widthIn(max = 200.dp),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = onUndoRequested) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Deshacer",
                            tint = LightText
                        )
                    }

                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menú",
                            tint = LightText
                        )
                    }

                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false },
                        modifier = Modifier.background(Color(0xFF2D2E45))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Reiniciar Edición",
                                    color = Color.Red
                                )
                            },
                            onClick = {
                                showOptions = false
                                onResetRequested()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = null,
                            tint = LightText
                        )
                    }

                    IconButton(onClick = onPlayToggle) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Default.Stop
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = "Play/Stop",
                            tint = if (isPlaying) Color.Red else Color.Green
                        )
                    }

                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = Color.Red
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = LightText
                        )
                    }
                }

                Text(
                    text = "01:02:03",
                    color = LightText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF2D2E45),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}