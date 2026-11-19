package com.example.playsymphonyapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.playsymphonyapp.ui.theme.LightText

@Composable
fun FadeButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            border = BorderStroke(1.dp, Color(0xFFBB86FC))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(" Fade In", color = LightText)
        }

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .weight(1f)
                .padding(4.dp),
            border = BorderStroke(1.dp, Color(0xFFBB86FC))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(" Fade Out", color = LightText)
        }
    }
}