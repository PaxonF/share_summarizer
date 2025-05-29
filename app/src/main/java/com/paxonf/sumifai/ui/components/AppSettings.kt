package com.paxonf.sharesummarizer.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.ui.components.dialogs.ApiKeyEditDialog
import com.paxonf.sharesummarizer.ui.components.dialogs.CustomColorPickerDialog
import com.paxonf.sharesummarizer.ui.components.dialogs.ModelSelectionDialog
import com.paxonf.sharesummarizer.ui.components.dialogs.PromptEditDialog
import com.paxonf.sharesummarizer.ui.components.settings.SettingsSection
import com.paxonf.sharesummarizer.ui.components.settings.getLengthLabel
import com.paxonf.sharesummarizer.ui.components.settings.getTextSizeLabel
import com.paxonf.sharesummarizer.ui.components.theme.BottomSheetThemeSelection
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_CUSTOM
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_DARK
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_LIGHT
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_SEPIA
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_SYSTEM_BACKGROUND
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_SYSTEM_PRIMARY
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_SYSTEM_SECONDARY
import com.paxonf.sharesummarizer.ui.components.theme.THEME_OPTION_SYSTEM_TERTIARY
import com.paxonf.sharesummarizer.ui.components.theme.getThemeColors
import com.paxonf.sharesummarizer.ui.theme.RobotoFlex
import com.paxonf.sharesummarizer.ui.theme.ShareSummarizerTheme
import com.paxonf.sharesummarizer.viewmodel.SettingsViewModel

// Theme option identifiers
private const val THEME_OPTION_SYSTEM_BACKGROUND = "system_background"
private const val THEME_OPTION_SYSTEM_PRIMARY = "system_primary"
private const val THEME_OPTION_SYSTEM_SECONDARY = "system_secondary"
private const val THEME_OPTION_SYSTEM_TERTIARY = "system_tertiary"
private const val THEME_OPTION_LIGHT = "light"
private const val THEME_OPTION_SEPIA = "sepia"
private const val THEME_OPTION_DARK = "dark"
private const val THEME_OPTION_CUSTOM = "custom"

// Custom theme colors
private val LightThemeColor = Color.White
private val SepiaThemeColor = Color(0xFFF5EEDC)
private val DarkThemeColor = Color(0xFF121212)

private const val PREVIEW_ARTICLE_URL =
        "https://vickiboykis.com/2024/11/09/why-are-we-using-llms-as-calculators/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(settingsViewModel: SettingsViewModel) {
        var apiKeyInput by remember { mutableStateOf(settingsViewModel.apiKey) }
        var summaryLengthSliderPosition by remember {
                mutableIntStateOf(settingsViewModel.summaryLength)
        }
        var selectedModel by remember { mutableStateOf(settingsViewModel.selectedModel) }
        var summaryPromptInput by remember {
                mutableStateOf(
                        settingsViewModel.summaryPrompt.ifEmpty {
                                settingsViewModel.getDefaultPrompt()
                        }
                )
        }
        var selectedColorOption by remember {
                mutableStateOf(
                        when (settingsViewModel.bottomSheetColorOption) {
                                "primary" -> THEME_OPTION_SYSTEM_PRIMARY
                                "secondary" -> THEME_OPTION_SYSTEM_SECONDARY
                                "tertiary" -> THEME_OPTION_SYSTEM_TERTIARY
                                "custom" -> THEME_OPTION_CUSTOM
                                // Handle cases where it might be already one of the new values
                                THEME_OPTION_SYSTEM_BACKGROUND,
                                THEME_OPTION_SYSTEM_PRIMARY,
                                THEME_OPTION_SYSTEM_SECONDARY,
                                THEME_OPTION_SYSTEM_TERTIARY,
                                THEME_OPTION_LIGHT,
                                THEME_OPTION_SEPIA,
                                THEME_OPTION_DARK ->
                                        settingsViewModel.bottomSheetColorOption.takeIf {
                                                it.isNotBlank()
                                        }
                                                ?: THEME_OPTION_SYSTEM_BACKGROUND
                                else -> THEME_OPTION_SYSTEM_BACKGROUND // Default for empty or
                        // unrecognized
                        }
                )
        }
        var customColor by remember {
                mutableStateOf(Color(settingsViewModel.customBottomSheetColor))
        }
        var bottomSheetTextSizeMultiplierSliderPosition by remember {
                mutableStateOf(settingsViewModel.bottomSheetTextSizeMultiplier)
        }

        // State for dialogs
        var showPromptDialog by remember { mutableStateOf(false) }
        var showCustomColorDialog by remember { mutableStateOf(false) }
        var dialogCustomColorSelection by remember { mutableStateOf(customColor) }
        var showModelSelectionDialog by remember { mutableStateOf(false) }
        var showApiKeyDialog by remember { mutableStateOf(false) }
        var showPreviewBottomSheet by remember { mutableStateOf(false) }

        // Track the initial values with mutable state to allow updates after saving
        var initialApiKey by remember { mutableStateOf(settingsViewModel.apiKey) }
        var initialSummaryLength by remember { mutableIntStateOf(settingsViewModel.summaryLength) }
        var initialSelectedModel by remember { mutableStateOf(settingsViewModel.selectedModel) }
        var initialSummaryPrompt by remember {
                mutableStateOf(
                        settingsViewModel.summaryPrompt.ifEmpty {
                                settingsViewModel.getDefaultPrompt()
                        }
                )
        }
        var initialColorOption by remember {
                mutableStateOf(
                        when (settingsViewModel.bottomSheetColorOption) {
                                "primary" -> THEME_OPTION_SYSTEM_PRIMARY
                                "secondary" -> THEME_OPTION_SYSTEM_SECONDARY
                                "tertiary" -> THEME_OPTION_SYSTEM_TERTIARY
                                "custom" -> THEME_OPTION_CUSTOM
                                THEME_OPTION_SYSTEM_BACKGROUND,
                                THEME_OPTION_SYSTEM_PRIMARY,
                                THEME_OPTION_SYSTEM_SECONDARY,
                                THEME_OPTION_SYSTEM_TERTIARY,
                                THEME_OPTION_LIGHT,
                                THEME_OPTION_SEPIA,
                                THEME_OPTION_DARK ->
                                        settingsViewModel.bottomSheetColorOption.takeIf {
                                                it.isNotBlank()
                                        }
                                                ?: THEME_OPTION_SYSTEM_BACKGROUND
                                else -> THEME_OPTION_SYSTEM_BACKGROUND
                        }
                )
        }
        var initialCustomColor by remember {
                mutableStateOf(Color(settingsViewModel.customBottomSheetColor))
        }
        var initialBottomSheetTextSizeMultiplier by remember {
                mutableStateOf(settingsViewModel.bottomSheetTextSizeMultiplier)
        }

        // Compute whether anything has changed
        val hasChanges =
                remember(
                        apiKeyInput,
                        summaryLengthSliderPosition,
                        selectedModel,
                        summaryPromptInput,
                        selectedColorOption,
                        customColor,
                        bottomSheetTextSizeMultiplierSliderPosition,
                        initialApiKey,
                        initialSummaryLength,
                        initialSelectedModel,
                        initialSummaryPrompt,
                        initialColorOption,
                        initialCustomColor,
                        initialBottomSheetTextSizeMultiplier
                ) {
                        apiKeyInput != initialApiKey ||
                                summaryLengthSliderPosition != initialSummaryLength ||
                                selectedModel != initialSelectedModel ||
                                summaryPromptInput != initialSummaryPrompt ||
                                selectedColorOption != initialColorOption ||
                                customColor != initialCustomColor ||
                                bottomSheetTextSizeMultiplierSliderPosition !=
                                        initialBottomSheetTextSizeMultiplier
                }

        // Dialogs
        if (showPromptDialog) {
                PromptEditDialog(
                        currentPrompt = summaryPromptInput,
                        defaultPrompt = settingsViewModel.getDefaultPrompt(),
                        onDismiss = { showPromptDialog = false },
                        onSave = { newPrompt ->
                                summaryPromptInput = newPrompt
                                showPromptDialog = false
                        }
                )
        }

        if (showCustomColorDialog) {
                CustomColorPickerDialog(
                        initialColor = dialogCustomColorSelection,
                        onColorSelected = { newColor ->
                                customColor = newColor
                                selectedColorOption = THEME_OPTION_CUSTOM
                                showCustomColorDialog = false
                        },
                        onDismiss = { showCustomColorDialog = false }
                )
        }

        if (showModelSelectionDialog) {
                ModelSelectionDialog(
                        initialSelectedModelId = selectedModel,
                        availableModels = settingsViewModel.availableModels,
                        onConfirm = { newModelId ->
                                selectedModel = newModelId
                                showModelSelectionDialog = false
                        },
                        onDismiss = { showModelSelectionDialog = false }
                )
        }

        if (showApiKeyDialog) {
                ApiKeyEditDialog(
                        currentApiKey = apiKeyInput,
                        onSave = { newApiKey ->
                                apiKeyInput = newApiKey
                                showApiKeyDialog = false
                        },
                        onDismiss = { showApiKeyDialog = false }
                )
        }

        // Observe the preview summary state from ViewModel
        val previewSummaryUiState by settingsViewModel.previewSummaryUiState.collectAsState()

        // Preview bottom sheet with example content
        if (showPreviewBottomSheet) {
                val (containerColorForSheet, contentColorForSheet) =
                        getThemeColors(
                                selectedOption = selectedColorOption,
                                customColor = customColor,
                                materialColorScheme = MaterialTheme.colorScheme
                        )

                SummaryBottomSheet(
                        uiState = previewSummaryUiState,
                        onDismiss = {
                                showPreviewBottomSheet = false
                                settingsViewModel.clearPreviewSummary()
                        },
                        onRetry = { settingsViewModel.generatePreviewSummary(PREVIEW_ARTICLE_URL) },
                        containerColor = containerColorForSheet,
                        contentColor = contentColorForSheet,
                        textSizeMultiplier = settingsViewModel.bottomSheetTextSizeMultiplier
                )
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = {
                                        Text(
                                                "sumifAI",
                                                fontFamily = RobotoFlex,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 24.sp
                                        )
                                }
                        )
                },
                floatingActionButton = {
                        // Animate FAB visibility - hide when there are unsaved changes
                        AnimatedVisibility(
                                visible = !hasChanges,
                                enter =
                                        slideInVertically(
                                                initialOffsetY = { it },
                                                animationSpec = tween(300)
                                        ) + fadeIn(animationSpec = tween(300)),
                                exit =
                                        slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(300)
                                        ) + fadeOut(animationSpec = tween(300))
                        ) {
                                ExtendedFloatingActionButton(
                                        onClick = {
                                                settingsViewModel.generatePreviewSummary(
                                                        PREVIEW_ARTICLE_URL
                                                )
                                                showPreviewBottomSheet = true
                                        },
                                        icon = {
                                                Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Preview"
                                                )
                                        },
                                        text = { Text("Preview") },
                                        containerColor =
                                                getThemeColors(
                                                                selectedOption =
                                                                        selectedColorOption,
                                                                customColor = customColor,
                                                                materialColorScheme =
                                                                        MaterialTheme.colorScheme
                                                        )
                                                        .first,
                                        contentColor =
                                                getThemeColors(
                                                                selectedOption =
                                                                        selectedColorOption,
                                                                customColor = customColor,
                                                                materialColorScheme =
                                                                        MaterialTheme.colorScheme
                                                        )
                                                        .second
                                )
                        }
                }
        ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                                modifier =
                                        Modifier.fillMaxSize().padding(paddingValues).clickable(
                                                        indication = null,
                                                        interactionSource =
                                                                remember {
                                                                        MutableInteractionSource()
                                                                }
                                                ) {
                                                if (showApiKeyDialog) {
                                                        showApiKeyDialog = false
                                                }
                                        },
                                contentPadding =
                                        {
                                                val baseHorizontalPadding = 16.dp
                                                val topPadding = 16.dp
                                                val fabHeight = 56.dp
                                                val saveCardEstimatedHeight = 130.dp
                                                val cardOwnBottomMargin = 16.dp
                                                val desiredSpacingAboveBottomElement = 56.dp

                                                val bottomPaddingForFab =
                                                        fabHeight + desiredSpacingAboveBottomElement
                                                val bottomPaddingForSaveCard =
                                                        saveCardEstimatedHeight +
                                                                cardOwnBottomMargin +
                                                                desiredSpacingAboveBottomElement

                                                PaddingValues(
                                                        start = baseHorizontalPadding,
                                                        end = baseHorizontalPadding,
                                                        top = topPadding,
                                                        bottom =
                                                                if (hasChanges)
                                                                        bottomPaddingForSaveCard
                                                                else bottomPaddingForFab
                                                )
                                        }(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                                item {
                                        Text(
                                                "Configure your summarizer settings below.",
                                                style = MaterialTheme.typography.bodyLarge
                                        )
                                }

                                item {
                                        SettingsSection(title = "API Configuration") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        // API Key status card
                                                        ApiKeyCard(
                                                                apiKey = apiKeyInput,
                                                                onEditClick = {
                                                                        showApiKeyDialog = true
                                                                }
                                                        )

                                                        // AI Model Selection Card
                                                        ModelSelectionCard(
                                                                selectedModel = selectedModel,
                                                                availableModels =
                                                                        settingsViewModel
                                                                                .availableModels,
                                                                onEditClick = {
                                                                        showModelSelectionDialog =
                                                                                true
                                                                }
                                                        )
                                                }
                                        }
                                }

                                item {
                                        Spacer(Modifier.height(8.dp))
                                        SettingsSection(title = "Summary Settings") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        // Summary Length Card
                                                        SummaryLengthCard(
                                                                summaryLength =
                                                                        summaryLengthSliderPosition,
                                                                onLengthChange = {
                                                                        summaryLengthSliderPosition =
                                                                                it
                                                                }
                                                        )

                                                        // Custom Prompt Card
                                                        CustomPromptCard(
                                                                summaryPrompt = summaryPromptInput,
                                                                onEditClick = {
                                                                        showPromptDialog = true
                                                                }
                                                        )
                                                }
                                        }
                                }

                                item {
                                        Spacer(Modifier.height(8.dp))
                                        SettingsSection(title = "Appearance") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        Text(
                                                                text =
                                                                        "Choose the color theme for the summary bottom sheet.",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )

                                                        BottomSheetThemeSelection(
                                                                currentThemeOption =
                                                                        selectedColorOption,
                                                                onThemeOptionSelected = { option ->
                                                                        if (option ==
                                                                                        THEME_OPTION_CUSTOM
                                                                        ) {
                                                                                dialogCustomColorSelection =
                                                                                        customColor
                                                                                showCustomColorDialog =
                                                                                        true
                                                                        } else {
                                                                                selectedColorOption =
                                                                                        option
                                                                        }
                                                                },
                                                                customColorValue = customColor,
                                                                materialColorScheme =
                                                                        MaterialTheme.colorScheme
                                                        )

                                                        // Text Size slider
                                                        TextSizeCard(
                                                                textSizeMultiplier =
                                                                        bottomSheetTextSizeMultiplierSliderPosition,
                                                                onTextSizeChange = {
                                                                        bottomSheetTextSizeMultiplierSliderPosition =
                                                                                it
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }

                        // Floating save card that slides up from bottom
                        AnimatedVisibility(
                                visible = hasChanges,
                                enter =
                                        slideInVertically(
                                                initialOffsetY = { it },
                                                animationSpec = tween(300)
                                        ) + fadeIn(),
                                exit =
                                        slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(300)
                                        ) + fadeOut(),
                                modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                                SaveChangesCard(
                                        onReset = {
                                                // Reset all values to initial state
                                                apiKeyInput = initialApiKey
                                                summaryLengthSliderPosition = initialSummaryLength
                                                selectedModel = initialSelectedModel
                                                summaryPromptInput = initialSummaryPrompt
                                                selectedColorOption = initialColorOption
                                                customColor = initialCustomColor
                                                bottomSheetTextSizeMultiplierSliderPosition =
                                                        initialBottomSheetTextSizeMultiplier
                                        },
                                        onSave = {
                                                settingsViewModel.saveApiKey(apiKeyInput)
                                                settingsViewModel.saveSummaryLength(
                                                        summaryLengthSliderPosition
                                                )
                                                settingsViewModel.saveSelectedModel(selectedModel)
                                                settingsViewModel.saveSummaryPrompt(
                                                        summaryPromptInput
                                                )
                                                settingsViewModel.saveBottomSheetColorOption(
                                                        selectedColorOption
                                                )
                                                settingsViewModel.saveCustomBottomSheetColor(
                                                        customColor.toArgb()
                                                )
                                                settingsViewModel.saveBottomSheetTextSizeMultiplier(
                                                        bottomSheetTextSizeMultiplierSliderPosition
                                                )

                                                // Update initial values to match current values
                                                // after saving
                                                initialApiKey = apiKeyInput
                                                initialSummaryLength = summaryLengthSliderPosition
                                                initialSelectedModel = selectedModel
                                                initialSummaryPrompt = summaryPromptInput
                                                initialColorOption = selectedColorOption
                                                initialCustomColor = customColor
                                                initialBottomSheetTextSizeMultiplier =
                                                        bottomSheetTextSizeMultiplierSliderPosition
                                        }
                                )
                        }
                }
        }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                )
                content()
        }
}

private fun getLengthLabel(length: Int): String {
        return when (length) {
                1 -> "Very Short"
                2 -> "Short"
                3 -> "Medium"
                4 -> "Long"
                5 -> "Very Long"
                else -> "Medium"
        }
}

private fun getTextSizeLabel(multiplier: Float): String {
        val percentage = (multiplier * 100).toInt()
        return "$percentage%"
}

@Composable
private fun ApiKeyCard(apiKey: String, onEditClick: () -> Unit) {
        ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                                        text = "Gemini API Key",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        text =
                                                if (apiKey.isEmpty()) "Not configured"
                                                else "●●●●●●●●●●●●●●●●●●●",
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                if (apiKey.isEmpty())
                                                        MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily =
                                                androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                        }

                        Spacer(Modifier.width(8.dp))
                        val context = LocalContext.current
                        IconButton(
                                onClick = {
                                        val intent =
                                                Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(
                                                                "https://aistudio.google.com/app/apikey"
                                                        )
                                                )
                                        context.startActivity(intent)
                                },
                                modifier = Modifier.size(24.dp)
                        ) {
                                Icon(
                                        imageVector = Icons.Outlined.HelpOutline,
                                        contentDescription = "Get API Key Help",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                        IconButton(onClick = onEditClick) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit API Key",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}

@Composable
private fun ModelSelectionCard(
        selectedModel: String,
        availableModels: Map<String, String>,
        onEditClick: () -> Unit
) {
        val currentModelDisplayName = availableModels[selectedModel] ?: selectedModel

        ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = "AI Model",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        text = currentModelDisplayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        IconButton(onClick = onEditClick) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Change AI Model",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}

@Composable
private fun SummaryLengthCard(summaryLength: Int, onLengthChange: (Int) -> Unit) {
        ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        "Summary Length",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        getLengthLabel(summaryLength),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Slider(
                                value = summaryLength.toFloat(),
                                onValueChange = { onLengthChange(it.toInt()) },
                                valueRange = 1f..5f,
                                modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                Text(
                                        text = "Brief",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        text = "Detailed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}

@Composable
private fun CustomPromptCard(summaryPrompt: String, onEditClick: () -> Unit) {
        ElevatedCard(
                modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text(
                                        text = "Custom Prompt",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        text =
                                                summaryPrompt.ifEmpty {
                                                        "Tap to set a custom prompt"
                                                },
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                if (summaryPrompt.isEmpty())
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.6f)
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                                )
                        }
                        IconButton(onClick = onEditClick) {
                                Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Prompt",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                )
                        }
                }
        }
}

@Composable
private fun TextSizeCard(textSizeMultiplier: Float, onTextSizeChange: (Float) -> Unit) {
        ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Text(
                                        text = "Text Size",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                        text = getTextSizeLabel(textSizeMultiplier),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                        Slider(
                                value = textSizeMultiplier,
                                onValueChange = onTextSizeChange,
                                valueRange = 0.5f..2.0f,
                                steps = 14,
                                modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                                Text(
                                        text = "50%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        text = "200%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                        }
                }
        }
}

@Composable
private fun SaveChangesCard(onReset: () -> Unit, onSave: () -> Unit) {
        Card(
                modifier =
                        Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .navigationBarsPadding(),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
        ) {
                Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                        Column {
                                Text(
                                        text = "Unsaved Changes",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                        text = "You have unsaved settings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                        alpha = 0.7f
                                                )
                                )
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                                OutlinedButton(
                                        onClick = onReset,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        contentColor =
                                                                MaterialTheme.colorScheme.onSurface
                                                )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reset")
                                }

                                Button(
                                        onClick = onSave,
                                        modifier = Modifier.weight(1f),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme.primary
                                                )
                                ) { Text("Save Settings") }
                        }
                }
        }
}

@Preview(showBackground = true)
@Composable
fun AppSettingsScreenPreview() {
        ShareSummarizerTheme {
                val context = LocalContext.current
                val dummyPrefs = AppPreferences(context)
                val dummyViewModel = SettingsViewModel(dummyPrefs, context)
                AppSettingsScreen(settingsViewModel = dummyViewModel)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomColorPickerDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelectionDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiKeyEditDialog(
        currentApiKey: String,
        onSave: (String) -> Unit,
        onDismiss: () -> Unit
) {
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
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Password),
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
