// Archivo: BottomNavBar.kt
// Barra de navegación ultra-resistente y simple.

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
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Editor : BottomNavItem("tracks", Icons.Default.MusicNote, "Editor")
    object Library : BottomNavItem("library", Icons.Default.LibraryMusic, "Música")
    object Player : BottomNavItem("player", Icons.Default.PlayArrow, "Reproductor")
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(BottomNavItem.Editor, BottomNavItem.Library, BottomNavItem.Player)
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(containerColor = Color(0xFF1A1A1A), contentColor = Color.White) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, null) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    // NAVEGACIÓN TOTALMENTE LIBRE
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFBB86FC),
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color(0xFF3700B3).copy(alpha = 0.3f)
                )
            )
        }
    }
}
