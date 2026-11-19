// Archivo: LibraryHeader.kt
// Encabezado con buscador funcional, menú de idioma en rueda de ajustes y título centrado.

package com.example.playsymphonyapp.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.playsymphonyapp.R
import com.example.playsymphonyapp.ui.theme.DarkBackground
import com.example.playsymphonyapp.ui.theme.LightText

@Composable
fun LibraryHeader(
    navController: NavController? = null,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {}
) {
    var activeSearchText by remember(searchQuery) { mutableStateOf(searchQuery) }
    var menuExpanded by remember { mutableStateOf(false) }
    var langMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Fila superior con buscador, lupa, rueda de configuración e ícono de menú
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Buscador estilizado funcional
            BasicTextField(
                value = activeSearchText,
                onValueChange = {
                    activeSearchText = it
                    onSearchQueryChange(it)
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchQueryChange(activeSearchText)
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(Color(0xFF1E1E1E), shape = MaterialTheme.shapes.small)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeSearchText.isEmpty()) {
                            Text(
                                text = stringResource(R.string.search_hint),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Botón de Lupa para ejecutar/confirmar búsqueda
            IconButton(
                onClick = { onSearchQueryChange(activeSearchText) }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search),
                    tint = LightText
                )
            }

            // Ruedita de Configuración para Menú de Idioma
            Box {
                IconButton(onClick = { langMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.language),
                        tint = LightText
                    )
                }

                DropdownMenu(
                    expanded = langMenuExpanded,
                    onDismissRequest = { langMenuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    listOf(
                        "" to stringResource(R.string.language_system),
                        "es" to "Español",
                        "en" to "English"
                    ).forEach { (tag, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = LightText) },
                            onClick = {
                                langMenuExpanded = false
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(tag)
                                )
                            }
                        )
                    }
                }
            }

            // Menú desplegable de 3 líneas para Navegación
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(R.string.menu),
                        tint = LightText
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E1E1E))
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.editor), color = LightText) },
                        onClick = {
                            menuExpanded = false
                            navigateSafely(navController, "tracks")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.player), color = LightText) },
                        onClick = {
                            menuExpanded = false
                            navigateSafely(navController, "player")
                        }
                    )
                }
            }
        }

        // Título "Biblioteca" centrado abajo y más grande
        Text(
            text = stringResource(R.string.library),
            color = LightText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp)
        )
    }
}

fun navigateSafely(navController: NavController?, route: String) {
    navController?.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
