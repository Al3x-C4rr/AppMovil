package com.example.playsymphonyapp.respaldo_codigo // Este archivo contiene el diseño del reproductor de media de la biblioteca usando Jetpack Compose.

// Importaciones necesarias para el diseño y funcionalidad de la pantalla
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets//
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.Purple40
import com.example.playsymphonyapp.ui.theme.LightText
import androidx.compose.ui.text.style.TextAlign

// Función Composable que define el diseño del encabezado de la biblioteca
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationBar(){
    val tecladoVisible = WindowInsets.isImeVisible //deteccion de teclado

    var nameSong by remember {
        mutableStateOf("Cancion temporal")
    }

    var nameArtist by remember {
        mutableStateOf("Julio Ortiz")
    }

    var progresoCancion by remember {
        mutableStateOf("1:23")
    }

    var durationCancion by remember {
        mutableStateOf("5:55")
    }

    Box(
        modifier = Modifier.fillMaxSize()
    )
    {
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()  // Ocupa todo el ancho disponible
                .background(Purple40)  // Fondo oscuro
                .padding(16.dp),  // Espacio de 16dp en todos los lados
            verticalArrangement = Arrangement.Bottom,  // Alinea los hijos en la parte inferior
            horizontalAlignment = Alignment.Start  // Alinea los hijos a la izquierda
        )
        {
            // Row: Contenedor horizontal para el título, barra de búsqueda y íconos
            Row() {
                // Text: Canciones que esten pasando
                Text(
                    text = "$nameSong - $nameArtist",  // Texto a mostrar
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 15.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()  // Ocupa todo el ancho disponible
                    .padding(vertical = 8.dp),  // Espacio vertical de 8dp
                verticalAlignment = Alignment.CenterVertically,  // Centra verticalmente los hijos
            ) {
                Text(
                    text = progresoCancion,
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 10.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f)  // Asigna un peso de 1 (1/3 del espacio)
                )
                Text(
                    text = durationCancion,
                    color = LightText,  // Color del texto (blanco)
                    fontSize = 10.sp,  // Tamaño de fuente de 20 sp
                    modifier = Modifier.weight(1f),  // Asigna un peso de 1 (1/3 del espacio)
                    textAlign = TextAlign.Right
                )

            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()  // Ocupa todo el ancho disponible
                    .padding(vertical = 8.dp),  // Espacio vertical de 8dp
                verticalAlignment = Alignment.CenterVertically,  // Centra verticalmente los hijos
                horizontalArrangement = Arrangement.SpaceBetween  // Distribuye el espacio entre los hijos
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowLeft,  // Ícono de búsqueda de Material Design
                    contentDescription = "Ícono de flecha hacia la izquierda",  // Descripción para accesibilidad
                    tint = LightText,  // Color del ícono (blanco)
                    modifier = Modifier.padding(start = 8.dp)  // Espacio de 8dp a la izquierda
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow,  // Ícono de búsqueda de Material Design
                    contentDescription = "Ícono de flecha hacia la derecha",  // Descripción para accesibilidad
                    tint = LightText,  // Color del ícono (blanco)
                    modifier = Modifier.padding(start = 8.dp)  // Espacio de 8dp a la izquierda
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,  // Ícono de búsqueda de Material Design
                    contentDescription = "Ícono de búsqueda",  // Descripción para accesibilidad
                    tint = LightText,  // Color del ícono (blanco)
                    modifier = Modifier.padding(start = 8.dp)  // Espacio de 8dp a la izquierda
                )
            }



            // Spacer: Espacio vacío de 16dp de altura (para separar el encabezado del contenido)
            Spacer(modifier = Modifier.height(16.dp))

        }
    }


}

// Vista previa para ver el diseño en Android Studio
@Preview(showBackground = true)  // Muestra el fondo en la vista previa
@Composable
fun NavigationBarPreview() {
    NavigationBar()  // Muestra el MediaControl en la vista previa
}