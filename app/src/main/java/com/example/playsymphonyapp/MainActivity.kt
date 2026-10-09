// Archivo: MainActivity.kt
// Configuración central con navegación universal y gestión de permisos.

package com.example.playsymphonyapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.playsymphonyapp.components.BottomNavBar
import com.example.playsymphonyapp.screens.LibraryScreen
import com.example.playsymphonyapp.screens.PlayerScreen
import com.example.playsymphonyapp.screens.TracksScreen
import com.example.playsymphonyapp.ui.theme.PlaySymphonyAppTheme
import com.example.playsymphonyapp.viewmodels.MusicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlaySymphonyAppTheme {
                val navController = rememberNavController()
                // MusicViewModel centralizado (compartido entre pantallas)
                val musicViewModel: MusicViewModel = viewModel()
                
                // Gestión de permisos al iniciar
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(this, "Se requiere permiso para acceder a la música", Toast.LENGTH_LONG).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }

                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "library"
                        ) {
                            composable("library") { 
                                LibraryScreen(navController, musicViewModel) 
                            }
                            composable("tracks") { 
                                TracksScreen(navController, musicViewModel) 
                            }
                            composable("player") { 
                                PlayerScreen(navController, musicViewModel) 
                            }
                        }
                    }
                }
            }
        }
    }
}
