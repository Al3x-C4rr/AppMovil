// Archivo: LibraryScreen.kt
// Este archivo contiene el diseño del encabezado de la biblioteca usando Jetpack Compose.

package com.example.playsymphonyapp.screens

// Importaciones necesarias para el diseño y funcionalidad de la pantalla
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*  // Para mutableStateOf, remember, etc.
import androidx.compose.ui.Alignment  // Para alineaciones (CenterVertically, Start, etc.)
import androidx.compose.ui.Modifier  // Para modificar propiedades de los componentes (padding, size, etc.)
import androidx.compose.ui.graphics.Color  // Para manejar colores
import androidx.compose.ui.text.input.TextFieldValue  // Para el valor del campo de texto
import androidx.compose.ui.tooling.preview.Preview  // Para la vista previa en Android Studio
import androidx.compose.ui.unit.dp  // Para unidades de medida (dp)
import androidx.compose.ui.unit.sp  // Para unidades de tamaño de fuente (sp)
import androidx.compose.foundation.background
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.playsymphonyapp.components.LibraryHeader
import com.example.playsymphonyapp.components.LibrarySongs
import androidx.compose.ui.text.font.FontWeight
import com.example.playsymphonyapp.ui.theme.AccentColor
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

// Función Composable que define el diseño del encabezado de la biblioteca
// Recibe navController como parámetro para navegar a otras pantallas
@Composable
fun LibraryScreen(navController: NavController) {
    // Estado dinámico para el conteo de canciones
    var songCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(DarkBackground),
    ) {
        // Encabezado principal (Búsqueda y Menú)
        LibraryHeader(navController = navController)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            // Dato dinámico que muestra la cantidad de canciones
            Text(
                text = "$songCount CANCIONES",
                color = AccentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // Lista de canciones con el nuevo diseño
            LibrarySongs { songCount = it }
        }
    }
}

// Vista previa para ver el diseño en Android Studio
@Preview(showBackground = true)  // Muestra el fondo en la vista previa
@Composable
fun LibraryScreenPreview() {
    // Para la vista previa, no necesitamos pasar navController
    // Usamos un NavController falso para evitar errores
    LibraryScreen(rememberNavController())
}