// Archivo: BottomNavBar.kt
// Barra de navegación inferior que sirve como control universal de la app.

package com.example.playsymphonyapp.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// Definición de los destinos de navegación
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Editor : BottomNavItem("tracks", Icons.Default.MusicNote, "Editor")
    object Library : BottomNavItem("library", Icons.Default.LibraryMusic, "Música")
    object Player : BottomNavItem("player", Icons.Default.PlayArrow, "Reproductor")
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Editor,
        BottomNavItem.Library,
        BottomNavItem.Player,
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF1A1A1A), // Fondo oscuro para coherencia
        contentColor = Color.White
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    // Función de navegación segura para evitar estancamiento
                    navController.navigate(item.route) {
                        // Vuelve al inicio de la navegación para evitar acumular pantallas
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Evita múltiples copias de la misma pantalla
                        launchSingleTop = true
                        // Restaura el estado (como el scroll) al volver
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFBB86FC),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFF3700B3).copy(alpha = 0.5f)
                )
            )
        }
    }
}
