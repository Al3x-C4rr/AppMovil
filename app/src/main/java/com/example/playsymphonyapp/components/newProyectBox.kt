package com.example.playsymphonyapp.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun newProyectSuggestion(){

    var newProyectMessage by remember { mutableStateOf(false) }

    var newNameSong by remember { mutableStateOf("") }

    fun newProyect() {
        newProyectMessage = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Nota: Si no has grabado desde el sample. Empieza un nuevo proyecto o seleccionalo desde la biblioteca",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 10.dp)
        )
    }
    OutlinedButton(
        onClick = { newProyect() },
        modifier = Modifier.padding(horizontal =16.dp).padding(top = 10.dp),
        border = BorderStroke(1.dp, Color(0xFFBB86FC)))
    {
        Text(
            text = "Nuevo Proyecto",
            color = Color.Gray,
            modifier = Modifier.padding(5.dp)
        )
    }
    if (newProyectMessage){
        AlertDialog(
            onDismissRequest = { newProyectMessage = false },
            title = { Text(
                text = "Nuevo Proyecto",
                color = Color.Gray,
                modifier = Modifier.padding(5.dp)
            ) },
            text = {
                Column{
                    Text("Escribe el nombre de tu proyecto")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newNameSong,
                        onValueChange = { newNameSong = it },
                        label = { Text("Escribí aquí") }
                        )
                    }
                },
            confirmButton = {
                TextButton(
                    onClick = { newProyectMessage = false }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}



