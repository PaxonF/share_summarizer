package com.paxonf.sumifai.ui.components.theme

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Theme option identifiers
const val THEME_OPTION_SYSTEM_BACKGROUND = "system_background"
const val THEME_OPTION_SYSTEM_PRIMARY = "system_primary"
const val THEME_OPTION_SYSTEM_SECONDARY = "system_secondary"
const val THEME_OPTION_SYSTEM_TERTIARY = "system_tertiary"
const val THEME_OPTION_LIGHT = "light"
const val THEME_OPTION_SEPIA = "sepia"
const val THEME_OPTION_DARK = "dark"
const val THEME_OPTION_CUSTOM = "custom"

// Custom theme colors
val LightThemeColor = Color.White
val SepiaThemeColor = Color(0xFFF5EEDC)
val DarkThemeColor = Color(0xFF121212)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetThemeSelection(
        currentThemeOption: String,
        onThemeOptionSelected: (String) -> Unit,
        customColorValue: Color,
        materialColorScheme: ColorScheme
) {
        var isSystemThemeDropdownExpanded by remember { mutableStateOf(false) }

        val systemThemeSubOptions =
                mapOf(
                        THEME_OPTION_SYSTEM_BACKGROUND to "Background",
                        THEME_OPTION_SYSTEM_PRIMARY to "Primary",
                        THEME_OPTION_SYSTEM_SECONDARY to "Secondary",
                        THEME_OPTION_SYSTEM_TERTIARY to "Tertiary"
                )

        data class DirectThemeInfo(
                val id: String,
                val title: String,
                val description: String,
                val color: Color
        )

        val directThemeOptions =
                listOf(
                        DirectThemeInfo(
                                THEME_OPTION_LIGHT,
                                "Light Theme",
                                "A clean, bright interface.",
                                LightThemeColor
                        ),
                        DirectThemeInfo(
                                THEME_OPTION_SEPIA,
                                "Sepia Theme",
                                "A warm, paper-like feel for comfortable reading.",
                                SepiaThemeColor
                        ),
                        DirectThemeInfo(
                                THEME_OPTION_DARK,
                                "Dark Theme",
                                "Easy on the eyes in low light conditions.",
                                DarkThemeColor
                        ),
                        DirectThemeInfo(
                                THEME_OPTION_CUSTOM,
                                "Custom Color",
                                "Choose your own unique color.",
                                customColorValue
                        )
                )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { // Increased spacing for cards
                // System Theme Dropdown
                val systemThemeIsActive = currentThemeOption.startsWith("system_")
                val currentSystemSubOption =
                        if (systemThemeIsActive) currentThemeOption
                        else THEME_OPTION_SYSTEM_BACKGROUND
                val systemThemeDisplayTitle =
                        if (systemThemeIsActive) {
                                "System Theme: ${systemThemeSubOptions[currentSystemSubOption] ?: "Background"}"
                        } else {
                                "System Theme"
                        }
                val systemThemeDisplayColor =
                        getThemeColors(
                                        currentSystemSubOption,
                                        customColorValue,
                                        materialColorScheme
                                )
                                .first

                ExposedDropdownMenuBox(
                        expanded = isSystemThemeDropdownExpanded,
                        onExpandedChange = {
                                isSystemThemeDropdownExpanded = !isSystemThemeDropdownExpanded
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                        StyledThemeOptionCard(
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                title = systemThemeDisplayTitle,
                                description = "Adapts to your system's color settings.",
                                themeColorCircle = systemThemeDisplayColor,
                                isSelected = systemThemeIsActive,
                                onClick = {},
                                isDropdownTrigger = true,
                                isDropdownExpanded = isSystemThemeDropdownExpanded
                        )

                        ExposedDropdownMenu(
                                expanded = isSystemThemeDropdownExpanded,
                                onDismissRequest = { isSystemThemeDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                systemThemeSubOptions.forEach { (optionId, displayName) ->
                                        val (optionContainerColor, _) =
                                                getThemeColors(
                                                        optionId,
                                                        customColorValue,
                                                        materialColorScheme
                                                )
                                        DropdownMenuItem(
                                                text = { Text(displayName) },
                                                onClick = {
                                                        onThemeOptionSelected(optionId)
                                                        isSystemThemeDropdownExpanded = false
                                                },
                                                leadingIcon = { // Add color preview to dropdown
                                                        // item
                                                        Surface(
                                                                modifier = Modifier.size(24.dp),
                                                                shape = CircleShape,
                                                                color = optionContainerColor,
                                                                border =
                                                                        BorderStroke(
                                                                                1.dp,
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .outline
                                                                        )
                                                        ) {}
                                                },
                                                trailingIcon =
                                                        if (currentThemeOption == optionId) {
                                                                {
                                                                        Icon(
                                                                                Icons.Filled.Check,
                                                                                contentDescription =
                                                                                        "Selected"
                                                                        )
                                                                }
                                                        } else null,
                                                colors =
                                                        MenuDefaults.itemColors(
                                                                // Optional: customize item colors
                                                                // if needed
                                                                )
                                        )
                                }
                        }
                }

                // Direct Theme Buttons
                directThemeOptions.forEach { themeInfo ->
                        StyledThemeOptionCard(
                                title = themeInfo.title,
                                description = themeInfo.description,
                                themeColorCircle =
                                        if (themeInfo.id == THEME_OPTION_CUSTOM) customColorValue
                                        else themeInfo.color,
                                isSelected = currentThemeOption == themeInfo.id,
                                onClick = { onThemeOptionSelected(themeInfo.id) }
                        )
                }
        }
}

@Composable
fun StyledThemeOptionCard(
        title: String,
        description: String?,
        themeColorCircle: Color, // Color for the small preview circle
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier, // Allow passing a modifier, e.g. for menuAnchor
        isDropdownTrigger: Boolean = false, // To show dropdown arrow
        isDropdownExpanded: Boolean = false // State of dropdown for arrow direction
) {
        Card(
                modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor =
                                        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                border =
                        if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        else CardDefaults.outlinedCardBorder(enabled = true)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                                Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium
                                )
                                if (description != null) {
                                        Text(
                                                text = description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color =
                                                        (if (isSelected)
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer
                                                                else
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant)
                                                                .copy(alpha = 0.7f)
                                        )
                                }
                        }
                        Spacer(
                                Modifier.width(16.dp)
                        ) // Increased spacer for better separation before right-aligned elements

                        if (isDropdownTrigger) {
                                Icon(
                                        imageVector =
                                                if (isDropdownExpanded) Icons.Filled.ArrowDropUp
                                                else Icons.Filled.ArrowDropDown,
                                        contentDescription = "Toggle system theme options"
                                        // Tint will be inherited from Card's contentColor via
                                        // LocalContentColor
                                        )
                                Spacer(Modifier.width(8.dp)) // Spacer between arrow and circle
                        }

                        Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = themeColorCircle,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                }
        }
}

fun getThemeColors(
        selectedOption: String,
        customColor: Color,
        materialColorScheme: ColorScheme
): Pair<Color, Color> { // Returns (containerColor, contentColor)
        val containerColor =
                when (selectedOption) {
                        THEME_OPTION_SYSTEM_BACKGROUND -> materialColorScheme.background
                        THEME_OPTION_SYSTEM_PRIMARY -> materialColorScheme.primaryContainer
                        THEME_OPTION_SYSTEM_SECONDARY -> materialColorScheme.secondaryContainer
                        THEME_OPTION_SYSTEM_TERTIARY -> materialColorScheme.tertiaryContainer
                        THEME_OPTION_LIGHT -> LightThemeColor
                        THEME_OPTION_SEPIA -> SepiaThemeColor
                        THEME_OPTION_DARK -> DarkThemeColor
                        THEME_OPTION_CUSTOM -> customColor
                        else -> materialColorScheme.background // Default
                }

        val contentColor =
                when (selectedOption) {
                        THEME_OPTION_SYSTEM_BACKGROUND -> materialColorScheme.onBackground
                        THEME_OPTION_SYSTEM_PRIMARY -> materialColorScheme.onPrimaryContainer
                        THEME_OPTION_SYSTEM_SECONDARY -> materialColorScheme.onSecondaryContainer
                        THEME_OPTION_SYSTEM_TERTIARY -> materialColorScheme.onTertiaryContainer
                        THEME_OPTION_LIGHT,
                        THEME_OPTION_SEPIA,
                        THEME_OPTION_DARK,
                        THEME_OPTION_CUSTOM -> {
                                if (containerColor.luminance() > 0.5f) Color.Black else Color.White
                        }
                        else -> materialColorScheme.onBackground // Default
                }
        return Pair(containerColor, contentColor)
}
