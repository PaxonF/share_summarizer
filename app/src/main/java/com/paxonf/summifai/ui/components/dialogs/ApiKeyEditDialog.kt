package com.paxonf.sharesummarizer.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyEditDialog(currentApiKey: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var tempApiKey by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Filled.Key, contentDescription = "Edit API Key") },
            title = { Text("Edit Gemini API Key") },
            text = {
                OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API Key") },
                        placeholder = { Text("Enter your Gemini API key") },
                        singleLine = true,
                        // visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            if (tempApiKey.isNotEmpty()) {
                                IconButton(onClick = { tempApiKey = "" }) {
                                    Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear API Key"
                                    )
                                }
                            }
                        }
                )
            },
            confirmButton = { Button(onClick = { onSave(tempApiKey) }) { Text("Set") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
