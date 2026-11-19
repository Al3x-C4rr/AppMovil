package com.example.playsymphonyapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playsymphonyapp.ui.theme.LightText

@Composable
fun PitchControlSection(
    pitch: Float,
    onPitchChange: (Float) -> Unit
) {
    val trackColor = when {
        pitch < -2f -> Color(0xFF03DAC6)
        pitch > 2f -> Color(0xFFFF9800)
        else -> Color(0xFFBB86FC)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = " Tono: ${pitch.toInt()} semitonos",
                color = LightText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = when {
                    pitch > 0 -> "AGUDO"
                    pitch < 0 -> "GRAVE"
                    else -> "ORIGINAL"
                },
                color = trackColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }

        Slider(
            value = pitch,
            onValueChange = onPitchChange,
            valueRange = -12f..12f,
            steps = 24,
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("-12 (Grave)", color = Color.Gray, fontSize = 9.sp)
            Text("0", color = Color.Gray, fontSize = 9.sp)
            Text("+12 (Agudo)", color = Color.Gray, fontSize = 9.sp)
        }
    }
}