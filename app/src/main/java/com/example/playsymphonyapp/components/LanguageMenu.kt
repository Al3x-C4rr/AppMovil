package com.example.playsymphonyapp.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.playsymphonyapp.R

@Composable
fun LanguageMenu() {
    var expanded by remember { mutableStateOf(false) }

    Surface {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                TextButton(
                    onClick = { expanded = true }
                ) {
                    Text(stringResource(R.string.language))
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(
                        "" to stringResource(R.string.language_system),
                        "es" to "Español",
                        "en" to "English"
                    ).forEach { (tag, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                expanded = false

                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(tag)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}