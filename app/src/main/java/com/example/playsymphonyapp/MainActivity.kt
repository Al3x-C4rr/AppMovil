// Archivo: MainActivity.kt
// Configuración central de la aplicación con navegación universal.

package com.example.playsymphonyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.playsymphonyapp.components.BottomNavBar
import com.example.playsymphonyapp.screens.LibraryScreen
import com.example.playsymphonyapp.screens.PlayerScreen
import com.example.playsymphonyapp.screens.TracksScreen
import com.example.playsymphonyapp.ui.theme.PlaySymphonyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlaySymphonyAppTheme {
                val navController = rememberNavController()

                // Scaffold es el contenedor principal que sostiene la barra de navegación inferior
                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    // El NavHost gestiona el cambio de pantallas basándose en la ruta actual
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "library"
                        ) {
                            // Definición de las 3 rutas principales (Editor, Música, Reproductor)
                            composable("library") { LibraryScreen(navController) }
                            composable("tracks") { TracksScreen(navController) }
                            composable("player") { PlayerScreen(navController) }
                        }
                    }
                }
            }
        }
    }
}
