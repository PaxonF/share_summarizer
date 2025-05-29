package com.paxonf.sharesummarizer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.*

@Composable
fun ColorPicker(
        selectedColor: Color,
        onColorSelected: (Color) -> Unit,
        modifier: Modifier = Modifier
) {
        var showColorPickerDialog by remember { mutableStateOf(false) }

        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Choose a color:", style = MaterialTheme.typography.bodyMedium)

                // Quick preset colors
                LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                        items(getPredefinedColors()) { color ->
                                ColorCircle(
                                        color = color,
                                        isSelected = color == selectedColor,
                                        onClick = { onColorSelected(color) }
                                )
                        }
                }

                // Custom color picker button
                OutlinedButton(
                        onClick = { showColorPickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
                ) {
                        Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Custom Color Picker")
                }

                // Show selected color info
                SelectedColorInfo(selectedColor = selectedColor)
        }

        // Color picker dialog
        if (showColorPickerDialog) {
                ColorPickerDialog(
                        initialColor = selectedColor,
                        onColorSelected = { color ->
                                onColorSelected(color)
                                showColorPickerDialog = false
                        },
                        onDismiss = { showColorPickerDialog = false }
                )
        }
}

@Composable
private fun ColorPickerDialog(
        initialColor: Color,
        onColorSelected: (Color) -> Unit,
        onDismiss: () -> Unit
) {
        var currentColor by remember { mutableStateOf(initialColor) }
        var hue by remember { mutableFloatStateOf(rgbToHsv(initialColor)[0]) }
        var saturation by remember { mutableFloatStateOf(rgbToHsv(initialColor)[1]) }
        var value by remember { mutableFloatStateOf(rgbToHsv(initialColor)[2]) }

        // Update color when HSV values change
        LaunchedEffect(hue, saturation, value) { currentColor = hsvToColor(hue, saturation, value) }

        Dialog(onDismissRequest = onDismiss) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                        Column(
                                modifier =
                                        Modifier.padding(24.dp)
                                                .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                                // Header
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = "Color Picker",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Medium
                                        )
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close"
                                                )
                                        }
                                }

                                // Color preview
                                Card(
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor = currentColor
                                                ),
                                        elevation =
                                                CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                        Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Text(
                                                        text =
                                                                "#${Integer.toHexString(currentColor.toArgb()).uppercase().substring(2)}",
                                                        color =
                                                                if (currentColor.luminance() > 0.5f)
                                                                        Color.Black
                                                                else Color.White,
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Medium
                                                )
                                        }
                                }

                                // Preset colors section
                                Text(
                                        text = "Preset Colors",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                )

                                LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                        items(getPredefinedColors()) { color ->
                                                ColorCircle(
                                                        color = color,
                                                        isSelected = color == currentColor,
                                                        onClick = {
                                                                currentColor = color
                                                                val hsv = rgbToHsv(color)
                                                                hue = hsv[0]
                                                                saturation = hsv[1]
                                                                value = hsv[2]
                                                        },
                                                        size = 40.dp
                                                )
                                        }
                                }

                                // Custom color sliders
                                Text(
                                        text = "Custom Color",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                )

                                // Hue slider
                                ColorSliderSection(
                                        label = "Hue",
                                        value = hue,
                                        valueRange = 0f..360f,
                                        onValueChange = { hue = it },
                                        color = hsvToColor(hue, 1f, 1f)
                                )

                                // Saturation slider
                                ColorSliderSection(
                                        label = "Saturation",
                                        value = saturation,
                                        valueRange = 0f..1f,
                                        onValueChange = { saturation = it },
                                        color = hsvToColor(hue, saturation, 1f)
                                )

                                // Value/Brightness slider
                                ColorSliderSection(
                                        label = "Brightness",
                                        value = value,
                                        valueRange = 0f..1f,
                                        onValueChange = { value = it },
                                        color = hsvToColor(hue, 1f, value)
                                )

                                // Action buttons
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                        OutlinedButton(
                                                onClick = onDismiss,
                                                modifier = Modifier.weight(1f)
                                        ) { Text("Cancel") }

                                        Button(
                                                onClick = { onColorSelected(currentColor) },
                                                modifier = Modifier.weight(1f)
                                        ) { Text("Select") }
                                }
                        }
                }
        }
}

@Composable
private fun ColorSliderSection(
        label: String,
        value: Float,
        valueRange: ClosedFloatingPointRange<Float>,
        onValueChange: (Float) -> Unit,
        color: Color
) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                        )

                        // Color indicator
                        Box(
                                modifier =
                                        Modifier.size(24.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                        1.dp,
                                                        MaterialTheme.colorScheme.outline,
                                                        CircleShape
                                                )
                        )
                }

                Slider(
                        value = value,
                        onValueChange = onValueChange,
                        valueRange = valueRange,
                        modifier = Modifier.fillMaxWidth()
                )

                Text(
                        text =
                                when (label) {
                                        "Hue" -> "${value.toInt()}°"
                                        else -> "${(value * 100).toInt()}%"
                                },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
}

@Composable
private fun SelectedColorInfo(selectedColor: Color) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
                Box(
                        modifier =
                                Modifier.size(32.dp)
                                        .clip(CircleShape)
                                        .background(selectedColor)
                                        .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.outline,
                                                CircleShape
                                        )
                )

                Column {
                        Text(
                                text = "Selected Color",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                                text =
                                        "#${Integer.toHexString(selectedColor.toArgb()).uppercase().substring(2)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                        )
                }
        }
}

@Composable
private fun ColorCircle(
        color: Color,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        size: Dp = 48.dp
) {
        Box(
                modifier =
                        modifier.size(size)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color =
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                )
                                .clickable { onClick() },
                contentAlignment = Alignment.Center
        ) {
                if (isSelected) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                                drawCircle(
                                        color =
                                                if (color.luminance() > 0.5f)
                                                        androidx.compose.ui.graphics.Color.Black
                                                else androidx.compose.ui.graphics.Color.White,
                                        radius = 8.dp.toPx()
                                )
                        }
                }
        }
}

private fun getPredefinedColors(): List<Color> =
        listOf(
                Color(0xFF6750A4), // Material Purple
                Color(0xFF1976D2), // Blue
                Color(0xFF388E3C), // Green
                Color(0xFFE64A19), // Deep Orange
                Color(0xFFD32F2F), // Red
                Color(0xFF7B1FA2), // Purple
                Color(0xFF303F9F), // Indigo
                Color(0xFF0097A7), // Cyan
                Color(0xFF689F38), // Light Green
                Color(0xFFFF5722), // Orange
                Color(0xFFE91E63), // Pink
                Color(0xFF795548), // Brown
                Color(0xFF607D8B), // Blue Grey
                Color(0xFF424242), // Grey
                Color(0xFF000000), // Black
        )

// HSV conversion functions
private fun rgbToHsv(color: Color): FloatArray {
        val r = color.red
        val g = color.green
        val b = color.blue

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val hue =
                when {
                        delta == 0f -> 0f
                        max == r -> 60f * (((g - b) / delta) % 6f)
                        max == g -> 60f * (((b - r) / delta) + 2f)
                        else -> 60f * (((r - g) / delta) + 4f)
                }.let { if (it < 0) it + 360f else it }

        val saturation = if (max == 0f) 0f else delta / max
        val value = max

        return floatArrayOf(hue, saturation, value)
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float): Color {
        val c = value * saturation
        val x = c * (1 - abs(((hue / 60f) % 2) - 1))
        val m = value - c

        val (r, g, b) =
                when ((hue / 60f).toInt()) {
                        0 -> Triple(c, x, 0f)
                        1 -> Triple(x, c, 0f)
                        2 -> Triple(0f, c, x)
                        3 -> Triple(0f, x, c)
                        4 -> Triple(x, 0f, c)
                        else -> Triple(c, 0f, x)
                }

        return Color(r + m, g + m, b + m, 1f)
}
