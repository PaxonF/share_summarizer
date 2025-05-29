package com.paxonf.sharesummarizer.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun PromptEditDialog(
        currentPrompt: String,
        defaultPrompt: String,
        onDismiss: () -> Unit,
        onSave: (String) -> Unit
) {
        var editedPrompt by remember { mutableStateOf(currentPrompt) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                text = "Edit Summary Prompt",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium
                        )
                },
                text = {
                        Column(
                                modifier =
                                        Modifier.fillMaxHeight(), // Fill height to allow weights to
                                // work
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                Text(
                                        text =
                                                "Customize how the AI summarizes your content. Use a detailed prompt for better results, but keep it concise for higher speed.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                        value = editedPrompt,
                                        onValueChange = { editedPrompt = it },
                                        label = { Text("Prompt") },
                                        placeholder = {
                                                Text("Enter your summarization prompt...")
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .weight(
                                                                0.6f
                                                        ), // Make editable prompt take ~60% of
                                        // available height
                                        minLines = 8,
                                        maxLines = 12,
                                        trailingIcon = {
                                                if (editedPrompt.isNotEmpty()) {
                                                        IconButton(
                                                                onClick = { editedPrompt = "" }
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default.Clear,
                                                                        contentDescription = "Clear"
                                                                )
                                                        }
                                                }
                                        }
                                )

                                // Default prompt reference
                                ElevatedCard(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .weight(
                                                                0.4f
                                                        ), // Make default prompt take ~40% of
                                        // available height
                                        elevation =
                                                CardDefaults.elevatedCardElevation(
                                                        defaultElevation = 1.dp
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween,
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(
                                                                text = "Default Prompt",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelLarge,
                                                                fontWeight = FontWeight.Medium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        TextButton(
                                                                onClick = {
                                                                        editedPrompt = defaultPrompt
                                                                }
                                                        ) { Text("Use Default") }
                                                }
                                                Text(
                                                        text = defaultPrompt,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onSurfaceVariant,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .heightIn(max = 100.dp)
                                                                        .verticalScroll(
                                                                                rememberScrollState()
                                                                        ),
                                                        lineHeight =
                                                                MaterialTheme.typography
                                                                        .bodySmall
                                                                        .lineHeight
                                                )
                                        }
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = { onSave(editedPrompt) },
                                enabled = editedPrompt.isNotBlank()
                        ) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
        )
}
