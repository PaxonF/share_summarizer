package com.paxonf.sharesummarizer.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionDialog(
        initialSelectedModelId: String,
        availableModels: Map<String, String>,
        onConfirm: (String) -> Unit,
        onDismiss: () -> Unit
) {
        var tempSelectedModelId by remember { mutableStateOf(initialSelectedModelId) }

        AlertDialog(
                onDismissRequest = onDismiss,
                icon = { Icon(Icons.Filled.Tune, contentDescription = "Select Model") },
                title = { Text("Select AI Model") },
                text = {
                        LazyColumn {
                                items(availableModels.toList()) { (modelId, displayName) ->
                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .selectable(
                                                                        selected =
                                                                                (modelId ==
                                                                                        tempSelectedModelId),
                                                                        onClick = {
                                                                                tempSelectedModelId =
                                                                                        modelId
                                                                        },
                                                                        role =
                                                                                androidx.compose.ui
                                                                                        .semantics
                                                                                        .Role
                                                                                        .RadioButton
                                                                )
                                                                .padding(vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                RadioButton(
                                                        selected = (modelId == tempSelectedModelId),
                                                        onClick = null // Click is handled by Row
                                                )
                                                Spacer(Modifier.width(16.dp))
                                                Text(
                                                        text = displayName,
                                                        style = MaterialTheme.typography.bodyLarge
                                                )
                                        }
                                }
                        }
                },
                confirmButton = {
                        Button(onClick = { onConfirm(tempSelectedModelId) }) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
        )
}
