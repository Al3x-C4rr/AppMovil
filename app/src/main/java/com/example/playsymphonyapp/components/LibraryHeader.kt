// Archivo: LibraryHeader.kt
// Encabezado con buscador y menú desplegable sincronizado.

package com.example.playsymphonyapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

@Composable
fun LibraryHeader(navController: NavController? = null) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Biblioteca",
                color = LightText,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )

            // Buscador estilizado
            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .weight(2f)
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searchText.text.isEmpty()) {
                            Text("Buscar...", color = Color.Gray, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                }
            )

            Icon(Icons.Default.Search, "Buscar", tint = LightText, modifier = Modifier.padding(start = 8.dp))

            // Menú desplegable sincronizado con la navegación universal
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.Menu, "Menú", tint = LightText)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text("Editor", color = LightText) },
                        onClick = {
                            menuExpanded = false
                            navigateSafely(navController, "tracks")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reproductor", color = LightText) },
                        onClick = {
                            menuExpanded = false
                            navigateSafely(navController, "player")
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Función auxiliar para navegar sin "estancarse"
fun navigateSafely(navController: NavController?, route: String) {
    navController?.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
