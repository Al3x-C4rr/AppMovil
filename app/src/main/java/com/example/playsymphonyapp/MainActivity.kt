package com.example.playsymphonyapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.example.playsymphonyapp.components.LanguageMenu
import com.example.playsymphonyapp.Editor.ProjectViewModel
import com.example.playsymphonyapp.screens.LibraryScreen
import com.example.playsymphonyapp.screens.PlayerScreen
import com.example.playsymphonyapp.screens.TracksScreen
import com.example.playsymphonyapp.ui.theme.PlaySymphonyAppTheme
import com.example.playsymphonyapp.viewmodels.MusicViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PlaySymphonyAppTheme {
                val navController = rememberNavController()

                val musicViewModel: MusicViewModel = viewModel()
                val projectViewModel: ProjectViewModel = viewModel()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.audio_permission),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(Unit) {
                    val permission = if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    permissionLauncher.launch(permission)
                }

                Scaffold(
                    topBar = {
                        LanguageMenu()
                    },
                    bottomBar = {
                        BottomNavBar(navController)
                    }
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
                                LibraryScreen(
                                    navController = navController,
                                    viewModel = musicViewModel
                                )
                            }

                            composable("tracks") {
                                TracksScreen(
                                    navController = navController,
                                    viewModel = musicViewModel,
                                    project = projectViewModel
                                )
                            }

                            composable("player") {
                                PlayerScreen(
                                    navController = navController,
                                    viewModel = musicViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}