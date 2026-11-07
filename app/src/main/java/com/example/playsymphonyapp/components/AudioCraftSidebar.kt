package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioCraftSidebar(
    selectedSection: String,
    onSectionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(Color(0xFF121212))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SidebarItem(
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            label = "Samples",
            isSelected = selectedSection == "Samples",
            onClick = { onSectionSelected("Samples") }
        )

        SidebarItem(
            icon = Icons.Default.Timeline,
            label = "Timeline",
            isSelected = selectedSection == "Timeline",
            onClick = { onSectionSelected("Timeline") }
        )

        SidebarItem(
            icon = Icons.Default.GraphicEq,
            label = "Tracks",
            isSelected = selectedSection == "Tracks",
            onClick = { onSectionSelected("Tracks") }
        )

        SidebarItem(
            icon = Icons.Default.Tune,
            label = "Mixer",
            isSelected = selectedSection == "Mixer",
            onClick = { onSectionSelected("Mixer") }
        )

        SidebarItem(
            icon = Icons.Default.FileUpload,
            label = "Export",
            isSelected = selectedSection == "Export",
            onClick = { onSectionSelected("Export") }
        )
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val itemColor = if (isSelected) {
        Color(0xFFBB86FC)
    } else {
        Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = itemColor,
            modifier = Modifier.size(28.dp)
        )

        Text(
            text = label,
            color = itemColor,
            fontSize = 10.sp
        )
    }
}