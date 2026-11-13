package com.example.playsymphonyapp.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaveformEditorPro(
    inPos: Float,
    outPos: Float,
    playPos: Float,
    onInChange: (Float) -> Unit,
    onOutChange: (Float) -> Unit,
    onPlayChange: (Float) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(
                color = Color.Black.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(
                "0:00", "0:28", "0:56", "1:23",
                "1:51", "2:19", "2:47", "3:14"
            ).forEach {
                Text(
                    text = it,
                    color = Color.Gray,
                    fontSize = 8.sp
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onPlayChange(change.position.x / widthPx)
                    }
                }
        ) {
            val barWidth = 3f
            val spacing = 3f
            val centerY = size.height / 2

            for (i in 0 until (size.width / (barWidth + spacing)).toInt()) {
                val x = i * (barWidth + spacing)
                val h = (size.height * 0.2f) + (i % 5) * 15f
                val isSelected = (x / size.width) in inPos..outPos

                drawLine(
                    color = if (isSelected) {
                        Color(0xFFFF9800)
                    } else {
                        Color.DarkGray
                    },
                    start = Offset(x, centerY - h / 2),
                    end = Offset(x, centerY + h / 2),
                    strokeWidth = barWidth
                )
            }

            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = Size(inPos * size.width, size.height)
            )

            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(outPos * size.width, 0f),
                size = Size((1f - outPos) * size.width, size.height)
            )
        }

        // Marcador IN
        Box(
            modifier = Modifier
                .offset(x = (maxWidth * inPos) - 15.dp)
                .width(30.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onInChange((change.position.x / widthPx) + inPos)
                    }
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color.Green,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "IN",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(Color.Green)
                )
            }
        }

        // Marcador OUT
        Box(
            modifier = Modifier
                .offset(x = (maxWidth * outPos) - 15.dp)
                .width(30.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onOutChange((change.position.x / widthPx) + outPos)
                    }
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color(0xFFFF9800),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "OUT",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFFF9800))
                )
            }
        }

        // Cabezal de reproducción
        Box(
            modifier = Modifier
                .offset(x = (maxWidth * playPos) - 1.5.dp)
                .width(3.dp)
                .fillMaxHeight()
                .background(Color.Red)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Red, RoundedCornerShape(2.dp))
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
            )
        }
    }
}