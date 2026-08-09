// Archivo: LibraryScreen.kt
// Este archivo contiene el diseño del encabezado de la biblioteca usando Jetpack Compose.

package com.example.playsymphonyapp.screens


// Importaciones necesarias para el diseño y funcionalidad de la pantalla
import androidx.compose.foundation.layout.*  // Para Column, Row, Spacer, padding, etc.
import androidx.compose.foundation.text.BasicTextField  // Para el campo de texto básico
import androidx.compose.material.icons.Icons  // Para acceder a los íconos de Material Design
import androidx.compose.material.icons.filled.Menu  // Ícono de menú
import androidx.compose.material.icons.filled.Search  // Ícono de búsqueda
import androidx.compose.material3.*  // Para Text, Icon, MaterialTheme, etc.
import androidx.compose.runtime.*  // Para mutableStateOf, remember, etc.
import androidx.compose.ui.Alignment  // Para alineaciones (CenterVertically, Start, etc.)
import androidx.compose.ui.Modifier  // Para modificar propiedades de los componentes (padding, size, etc.)
import androidx.compose.ui.graphics.Color  // Para manejar colores
import androidx.compose.ui.text.input.TextFieldValue  // Para el valor del campo de texto
import androidx.compose.ui.tooling.preview.Preview  // Para la vista previa en Android Studio
import androidx.compose.ui.unit.dp  // Para unidades de medida (dp)
import androidx.compose.ui.unit.sp  // Para unidades de tamaño de fuente (sp)
import androidx.compose.foundation.background
import com.example.playsymphonyapp.ui.theme.DarkBackground  // Color de fondo oscuro (definido en Color.kt)
import com.example.playsymphonyapp.ui.theme.LightText  // Color de texto claro (definido en Color.kt)
import com.example.playsymphonyapp.components.LibrarySongs

// Función Composable que define el diseño del encabezado de la biblioteca
@Composable
fun LibraryScreen() {
    // Estado para el texto de búsqueda.
    // `remember` guarda el estado entre recomposiciones (evita que se reinicie).
    // `mutableStateOf(TextFieldValue(""))` crea un estado mutable para el texto de búsqueda.
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Column: Contenedor vertical que apila sus hijos de arriba a abajo.
    Column(
        modifier = Modifier.fillMaxSize()
            .fillMaxWidth()  // Ocupa todo el ancho disponible
            .background(DarkBackground)  // Fondo oscuro
            .padding(16.dp),  // Espacio de 16dp en todos los lados
        verticalArrangement = Arrangement.Top,  // Alinea los hijos en la parte superior
        horizontalAlignment = Alignment.Start  // Alinea los hijos a la izquierda
    )
    {
        LibraryHeader()
        LibrarySongs()
    }


}


// Vista previa para ver el diseño en Android Studio
@Preview(showBackground = true)  // Muestra el fondo en la vista previa
@Composable
fun LibraryScreenPreview() {
    LibraryScreen()  // Muestra el LibraryScreen en la vista previa
}