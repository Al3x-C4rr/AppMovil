package com.example.playsymphonyapp

// 1. Importaciones de Android
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// 2. Importaciones de Jetpack Compose
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.playsymphonyapp.components.MediaControl
import com.example.playsymphonyapp.navigation.NavigationBar
import com.example.playsymphonyapp.screens.LibraryScreen

// 3. Importaciones de tu proyecto
import com.example.playsymphonyapp.ui.theme.PlaySymphonyAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaySymphonyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraryScreen()
                    MediaControl()
                    NavigationBar()
                }
            }
        }
    }
}