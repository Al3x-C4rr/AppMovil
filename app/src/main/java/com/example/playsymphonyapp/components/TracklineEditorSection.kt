package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playsymphonyapp.viewmodels.MusicViewModel
import com.example.playsymphonyapp.ui.theme.LightText


@Composable
fun TimelineEditorSection(
    inPos: Float,
    outPos: Float,
    playPos: Float,
    zoom: Float,
    onInChange: (Float) -> Unit,
    onOutChange: (Float) -> Unit,
    onPlayChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    viewModel: MusicViewModel

) {
    val currentPos by viewModel.currentPosition
    val currentTime by viewModel.currentTime
    val totalDur by viewModel.totalDuration

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1E1E1E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        // Controles de zoom
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ZoomIn,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )

            Spacer(Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .background(
                        color = Color(0xFF2D2E45),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(2.dp)
            ) {
                listOf(0.5f, 1f, 2f).forEach { z ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (zoom == z) {
                                    Color(0xFF6200EE)
                                } else {
                                    Color.Transparent
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { onZoomChange(z) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${if (z < 1) "0.5" else z.toInt()}x",
                            color = LightText,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = " Snap",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        WaveformEditorPro(
            inPos = inPos,
            outPos = outPos,
            playPos = playPos,
            onInChange = onInChange,
            onOutChange = onOutChange,
            onPlayChange = onPlayChange
        )

        // --- CONEXIÓN CON EL PROGRESO REAL ---

        Spacer(Modifier.height(12.dp))

        // Información de selección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "IN " + currentTime,
                color = Color.Green,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "2:22 seleccionado",
                color = Color.Gray,
                fontSize = 11.sp
            )

            Text(
                text = totalDur + " OUT",
                color = Color(0xFFFF9800),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        // Acciones de selección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onInChange(0f)
                    onOutChange(1f)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2E45)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(" Todo", fontSize = 10.sp)
            }

            Button(
                onClick = { onPlayChange(inPos) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2E45)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(" IN", fontSize = 10.sp)
            }

            Button(
                onClick = { onPlayChange(outPos) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2E45)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(" OUT", fontSize = 10.sp)
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFBB86FC)
            )
        ) {
            Icon(
                imageVector = Icons.Default.ContentCut,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Black
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = "CORTAR REGIÓN",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}