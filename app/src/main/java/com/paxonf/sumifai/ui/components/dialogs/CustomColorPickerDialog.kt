package com.paxonf.sumifai.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.paxonf.sumifai.ui.components.ColorPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomColorPickerDialog(
        initialColor: Color,
        onColorSelected: (Color) -> Unit,
        onDismiss: () -> Unit
) {
        var pickedColor by remember { mutableStateOf(initialColor) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Choose Custom Color") },
                text = {
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                ColorPicker(
                                        selectedColor = pickedColor,
                                        onColorSelected = { pickedColor = it },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                250.dp
                                                        ) // Give picker some explicit size
                                )
                                // Optional: Display the hex code or RGB values of pickedColor here
                        }
                },
                confirmButton = {
                        Button(onClick = { onColorSelected(pickedColor) }) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.9f) // Adjust dialog width as needed
        )
}
