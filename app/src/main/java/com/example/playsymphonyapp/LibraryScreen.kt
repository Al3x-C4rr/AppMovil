// Archivo: LibraryScreen.kt
// Este archivo contiene el diseño del encabezado de la biblioteca usando Jetpack Compose.

package com.example.playsymphonyapp

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

// Función Composable que define el diseño del encabezado de la biblioteca
@Composable
fun LibraryScreen() {
    // Estado para el texto de búsqueda.
    // `remember` guarda el estado entre recomposiciones (evita que se reinicie).
    // `mutableStateOf(TextFieldValue(""))` crea un estado mutable para el texto de búsqueda.
    var searchText by remember { mutableStateOf(TextFieldValue("")) }

    // Column: Contenedor vertical que apila sus hijos de arriba a abajo.
    Column(
        modifier = Modifier
            .fillMaxWidth()  // Ocupa todo el ancho disponible
            .background(DarkBackground)  // Fondo oscuro
            .padding(16.dp),  // Espacio de 16dp en todos los lados
        verticalArrangement = Arrangement.Top,  // Alinea los hijos en la parte superior
        horizontalAlignment = Alignment.Start  // Alinea los hijos a la izquierda
    ) {
        // Row: Contenedor horizontal para el título, barra de búsqueda y íconos
        Row(
            modifier = Modifier
                .fillMaxWidth()  // Ocupa todo el ancho disponible
                .padding(vertical = 8.dp),  // Espacio vertical de 8dp
            verticalAlignment = Alignment.CenterVertically,  // Centra verticalmente los hijos
            horizontalArrangement = Arrangement.SpaceBetween  // Distribuye el espacio entre los hijos
        ) {
            // Text: Título "Biblioteca"
            Text(
                text = "Biblioteca",  // Texto a mostrar
                color = LightText,  // Color del texto (blanco)
                fontSize = 20.sp,  // Tamaño de fuente de 20 sp
                modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
            )

            // BasicTextField: Campo de texto para la búsqueda
            BasicTextField(
                value = searchText,  // Valor actual del campo
                onValueChange = { searchText = it },  // Actualiza el estado cuando el texto cambia
                modifier = Modifier
                    .weight(2f)  // Asigna un peso de 2 (2/3 del espacio)
                    .height(40.dp)  // Altura fija de 40dp
                    .padding(horizontal = 12.dp),  // Espacio horizontal de 12dp
                // decorationBox: Personaliza la apariencia del campo de texto
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()  // Ocupa todo el ancho disponible
                            .background(Color(0xFF3700B3), shape = MaterialTheme.shapes.small)  // Fondo morado oscuro y bordes redondeados
                            .padding(horizontal = 12.dp, vertical = 8.dp),  // Espacio interno
                        verticalAlignment = Alignment.CenterVertically  // Centra verticalmente
                    ) {
                        // Placeholder: Muestra "Buscar canciones..." si el campo está vacío
                        if (searchText.text.isEmpty()) {
                            Text(
                                text = "Buscar canciones...",  // Texto del placeholder
                                color = Color.Gray,  // Color gris
                                fontSize = 14.sp  // Tamaño de fuente de 14 sp
                            )
                        }
                        // innerTextField(): Dibuja el campo de texto real
                        innerTextField()
                    }
                }
            )

            // Icon: Ícono de búsqueda
            Icon(
                imageVector = Icons.Default.Search,  // Ícono de búsqueda de Material Design
                contentDescription = "Ícono de búsqueda",  // Descripción para accesibilidad
                tint = LightText,  // Color del ícono (blanco)
                modifier = Modifier.padding(start = 8.dp)  // Espacio de 8dp a la izquierda
            )

            // Icon: Ícono de menú
            Icon(
                imageVector = Icons.Default.Menu,  // Ícono de menú de Material Design
                contentDescription = "Ícono de menú",  // Descripción para accesibilidad
                tint = LightText,  // Color del ícono (blanco)
                modifier = Modifier.padding(start = 8.dp)  // Espacio de 8dp a la izquierda
            )
        }

        // Spacer: Espacio vacío de 16dp de altura (para separar el encabezado del contenido)
        Spacer(modifier = Modifier.height(16.dp))

        // Text: Texto temporal para indicar dónde irán las canciones
        Text(
            text = "Aquí irán las canciones...",  // Texto temporal
            color = LightText,  // Color del texto (blanco)
            fontSize = 16.sp  // Tamaño de fuente de 16 sp
        )
    }
}

// Vista previa para ver el diseño en Android Studio
@Preview(showBackground = true)  // Muestra el fondo en la vista previa
@Composable
fun LibraryScreenPreview() {
    LibraryScreen()  // Muestra el LibraryScreen en la vista previa
}