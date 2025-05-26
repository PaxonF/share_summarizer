package com.paxonf.sharesummarizer.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.luminance
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

        // State for the prompt editing dialog
        var showPromptDialog by remember { mutableStateOf(false) }
        var tempPromptInput by remember { mutableStateOf("") }

        // State for custom color picker dialog
        var showCustomColorDialog by remember { mutableStateOf(false) }
        var dialogCustomColorSelection by remember { mutableStateOf(customColor) }

        // State for model selection dialog
        var showModelSelectionDialog by remember { mutableStateOf(false) }

        // State for API key dialog
        var showApiKeyDialog by remember { mutableStateOf(false) }

        // State for preview bottom sheet
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

        // Prompt editing dialog
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

        // Custom Color Picker Dialog
        if (showCustomColorDialog) {
                CustomColorPickerDialog(
                        initialColor = dialogCustomColorSelection,
                        onColorSelected = { newColor ->
                                customColor = newColor
                                selectedColorOption =
                                        THEME_OPTION_CUSTOM // Select custom theme upon saving a
                                // color
                                showCustomColorDialog = false
                        },
                        onDismiss = { showCustomColorDialog = false }
                )
        }

        // Model Selection Dialog
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

        // API Key Edit Dialog
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
                                                "Share Summarizer",
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
                                                val saveCardEstimatedHeight =
                                                        130.dp // Content + internal padding
                                                val cardOwnBottomMargin =
                                                        16.dp // External padding on the save card
                                                // itself
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
                                        }(), // Invoke lambda to get PaddingValues
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
                                                        ElevatedCard(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .clickable {
                                                                                        showApiKeyDialog =
                                                                                                true
                                                                                },
                                                                elevation =
                                                                        CardDefaults
                                                                                .elevatedCardElevation(
                                                                                        defaultElevation =
                                                                                                2.dp
                                                                                )
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                16.dp
                                                                                        ),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        4.dp
                                                                                                )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Gemini API Key",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                if (apiKeyInput
                                                                                                                .isEmpty()
                                                                                                ) {
                                                                                                        "Not configured"
                                                                                                } else {
                                                                                                        "●●●●●●●●●●●●●●●●●●●"
                                                                                                },
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodySmall,
                                                                                        color =
                                                                                                if (apiKeyInput
                                                                                                                .isEmpty()
                                                                                                ) {
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .error
                                                                                                } else {
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .onSurfaceVariant
                                                                                                },
                                                                                        fontFamily =
                                                                                                androidx.compose
                                                                                                        .ui
                                                                                                        .text
                                                                                                        .font
                                                                                                        .FontFamily
                                                                                                        .Monospace
                                                                                )
                                                                        }

                                                                        Spacer(
                                                                                Modifier.width(8.dp)
                                                                        ) // Spacer between text and
                                                                        // icons
                                                                        val context =
                                                                                LocalContext
                                                                                        .current // Get context for intent
                                                                        IconButton(
                                                                                onClick = {
                                                                                        val intent =
                                                                                                Intent(
                                                                                                        Intent.ACTION_VIEW,
                                                                                                        Uri.parse(
                                                                                                                "https://aistudio.google.com/app/apikey"
                                                                                                        )
                                                                                                )
                                                                                        context.startActivity(
                                                                                                intent
                                                                                        )
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                24.dp
                                                                                        ) // Standardize touch target size
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Outlined
                                                                                                        .HelpOutline,
                                                                                        contentDescription =
                                                                                                "Get API Key Help",
                                                                                        tint =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant, // Softer tint
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        20.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                        IconButton(
                                                                                onClick = {
                                                                                        showApiKeyDialog =
                                                                                                true
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default
                                                                                                        .Edit,
                                                                                        contentDescription =
                                                                                                "Edit API Key",
                                                                                        tint =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        20.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        // AI Model Selection Card (Moved here)
                                                        val currentModelDisplayName =
                                                                settingsViewModel.availableModels[
                                                                        selectedModel]
                                                                        ?: selectedModel
                                                        ElevatedCard(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .clickable {
                                                                                        showModelSelectionDialog =
                                                                                                true
                                                                                },
                                                                elevation =
                                                                        CardDefaults
                                                                                .elevatedCardElevation(
                                                                                        defaultElevation =
                                                                                                2.dp
                                                                                )
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                16.dp
                                                                                        ),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "AI Model",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                currentModelDisplayName,
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodyMedium,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }
                                                                        IconButton(
                                                                                onClick = {
                                                                                        showModelSelectionDialog =
                                                                                                true
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default
                                                                                                        .Edit,
                                                                                        contentDescription =
                                                                                                "Change AI Model",
                                                                                        tint =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        20.dp
                                                                                                )
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                item {
                                        Spacer(Modifier.height(8.dp)) // Added spacer
                                        SettingsSection(title = "Summary Settings") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) { // Main column for the new section
                                                        // === New Card structure for "Summary
                                                        // Length" section ===
                                                        ElevatedCard(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation =
                                                                        CardDefaults
                                                                                .elevatedCardElevation(
                                                                                        defaultElevation =
                                                                                                2.dp
                                                                                )
                                                        ) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                ),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        ) // Spacing
                                                                        // inside
                                                                        // the
                                                                        // card
                                                                        ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        "Summary Length",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary
                                                                                )
                                                                                Text(
                                                                                        getLengthLabel(
                                                                                                summaryLengthSliderPosition
                                                                                        ),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodyMedium,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium, // Make current value stand out a bit
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }
                                                                        Slider(
                                                                                value =
                                                                                        summaryLengthSliderPosition
                                                                                                .toFloat(),
                                                                                onValueChange = {
                                                                                        summaryLengthSliderPosition =
                                                                                                it.toInt()
                                                                                },
                                                                                valueRange = 1f..5f,
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                        )
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Brief",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "Detailed",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                        // === End of new Card structure for
                                                        // "Summary Length" section ===

                                                        // === Content from old "Summary Prompt"
                                                        // section ===
                                                        Column(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(12.dp)
                                                        ) {
                                                                ElevatedCard(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .clickable {
                                                                                                tempPromptInput =
                                                                                                        summaryPromptInput
                                                                                                showPromptDialog =
                                                                                                        true
                                                                                        },
                                                                        elevation =
                                                                                CardDefaults
                                                                                        .elevatedCardElevation(
                                                                                                defaultElevation =
                                                                                                        2.dp
                                                                                        )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        16.dp
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.weight(
                                                                                                        1f
                                                                                                ),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                8.dp
                                                                                                        )
                                                                                ) {
                                                                                        Text(
                                                                                                text =
                                                                                                        "Custom Prompt",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .titleSmall,
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Medium,
                                                                                                color =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .primary
                                                                                        )
                                                                                        Text(
                                                                                                text =
                                                                                                        summaryPromptInput
                                                                                                                .ifEmpty {
                                                                                                                        "Tap to set a custom prompt"
                                                                                                                },
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .bodySmall,
                                                                                                color =
                                                                                                        if (summaryPromptInput
                                                                                                                        .isEmpty()
                                                                                                        )
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .onSurfaceVariant
                                                                                                                        .copy(
                                                                                                                                alpha =
                                                                                                                                        0.6f
                                                                                                                        )
                                                                                                        else
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .onSurfaceVariant,
                                                                                                maxLines =
                                                                                                        3,
                                                                                                overflow =
                                                                                                        TextOverflow
                                                                                                                .Ellipsis,
                                                                                                lineHeight =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .bodySmall
                                                                                                                .lineHeight
                                                                                        )
                                                                                }
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                tempPromptInput =
                                                                                                        summaryPromptInput
                                                                                                showPromptDialog =
                                                                                                        true
                                                                                        }
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector =
                                                                                                        Icons.Default
                                                                                                                .Edit,
                                                                                                contentDescription =
                                                                                                        "Edit Prompt",
                                                                                                tint =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .primary,
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                20.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                        // === End of content from old "Summary
                                                        // Prompt" section ===
                                                }
                                        }
                                }

                                item {
                                        Spacer(Modifier.height(8.dp)) // Added spacer
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

                                                        // Custom color picker (only shown when
                                                        // custom is selected)
                                                        AnimatedVisibility(
                                                                visible = false, // No longer shown
                                                                // inline
                                                                // selectedColorOption ==
                                                                // THEME_OPTION_CUSTOM,
                                                                enter =
                                                                        expandVertically() +
                                                                                fadeIn(),
                                                                exit =
                                                                        shrinkVertically() +
                                                                                fadeOut()
                                                        ) {
                                                                Card(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        colors =
                                                                                CardDefaults
                                                                                        .cardColors(
                                                                                                containerColor =
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .surfaceVariant
                                                                                        )
                                                                ) {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.padding(
                                                                                                16.dp
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        12.dp
                                                                                                )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Custom Color",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium
                                                                                )

                                                                                // Use the
                                                                                // ColorPicker
                                                                                // component
                                                                                ColorPicker(
                                                                                        selectedColor =
                                                                                                customColor,
                                                                                        onColorSelected = {
                                                                                                newColor
                                                                                                ->
                                                                                                customColor =
                                                                                                        newColor
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        // Text Size slider
                                                        ElevatedCard(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation =
                                                                        CardDefaults
                                                                                .elevatedCardElevation(
                                                                                        defaultElevation =
                                                                                                2.dp
                                                                                )
                                                        ) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                ),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                8.dp
                                                                                        ) // Spacing
                                                                        // inside
                                                                        // the
                                                                        // card
                                                                        ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Text Size",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .titleSmall,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                getTextSizeLabel(
                                                                                                        bottomSheetTextSizeMultiplierSliderPosition
                                                                                                ),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .bodyMedium,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium, // Make current value stand out a bit
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }
                                                                        Slider(
                                                                                value =
                                                                                        bottomSheetTextSizeMultiplierSliderPosition,
                                                                                onValueChange = {
                                                                                        bottomSheetTextSizeMultiplierSliderPosition =
                                                                                                it
                                                                                },
                                                                                valueRange =
                                                                                        0.5f..2.0f,
                                                                                steps = 14,
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                        )
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "50%",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                "200%",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurfaceVariant
                                                                                )
                                                                        }
                                                                }
                                                        }
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
                                Card(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(horizontal = 16.dp)
                                                        .padding(bottom = 16.dp)
                                                        .navigationBarsPadding(),
                                        elevation =
                                                CardDefaults.cardElevation(
                                                        defaultElevation = 12.dp
                                                ),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor =
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                )
                                ) {
                                        Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                                Column {
                                                        Text(
                                                                text = "Unsaved Changes",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .titleSmall,
                                                                fontWeight = FontWeight.Medium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer
                                                        )
                                                        Text(
                                                                text = "You have unsaved settings",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodySmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer
                                                                                .copy(alpha = 0.7f)
                                                        )
                                                }

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        OutlinedButton(
                                                                onClick = {
                                                                        // Reset all values to
                                                                        // initial state
                                                                        apiKeyInput = initialApiKey
                                                                        summaryLengthSliderPosition =
                                                                                initialSummaryLength
                                                                        selectedModel =
                                                                                initialSelectedModel
                                                                        summaryPromptInput =
                                                                                initialSummaryPrompt
                                                                        selectedColorOption =
                                                                                initialColorOption
                                                                        customColor =
                                                                                initialCustomColor
                                                                        bottomSheetTextSizeMultiplierSliderPosition =
                                                                                initialBottomSheetTextSizeMultiplier
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                colors =
                                                                        ButtonDefaults
                                                                                .outlinedButtonColors(
                                                                                        contentColor =
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .onSurface
                                                                                )
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Refresh,
                                                                        contentDescription = null,
                                                                        modifier =
                                                                                Modifier.size(18.dp)
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(8.dp)
                                                                )
                                                                Text("Reset")
                                                        }

                                                        Button(
                                                                onClick = {
                                                                        settingsViewModel
                                                                                .saveApiKey(
                                                                                        apiKeyInput
                                                                                )
                                                                        settingsViewModel
                                                                                .saveSummaryLength(
                                                                                        summaryLengthSliderPosition
                                                                                )
                                                                        settingsViewModel
                                                                                .saveSelectedModel(
                                                                                        selectedModel
                                                                                )
                                                                        settingsViewModel
                                                                                .saveSummaryPrompt(
                                                                                        summaryPromptInput
                                                                                )
                                                                        settingsViewModel
                                                                                .saveBottomSheetColorOption(
                                                                                        selectedColorOption
                                                                                )
                                                                        settingsViewModel
                                                                                .saveCustomBottomSheetColor(
                                                                                        customColor
                                                                                                .toArgb()
                                                                                )
                                                                        settingsViewModel
                                                                                .saveBottomSheetTextSizeMultiplier(
                                                                                        bottomSheetTextSizeMultiplierSliderPosition
                                                                                )

                                                                        // Update initial values to
                                                                        // match
                                                                        // current values after
                                                                        // saving
                                                                        initialApiKey = apiKeyInput
                                                                        initialSummaryLength =
                                                                                summaryLengthSliderPosition
                                                                        initialSelectedModel =
                                                                                selectedModel
                                                                        initialSummaryPrompt =
                                                                                summaryPromptInput
                                                                        initialColorOption =
                                                                                selectedColorOption
                                                                        initialCustomColor =
                                                                                customColor
                                                                        initialBottomSheetTextSizeMultiplier =
                                                                                bottomSheetTextSizeMultiplierSliderPosition
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                containerColor =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .primary
                                                                        )
                                                        ) { Text("Save Settings") }
                                                }
                                        }
                                }
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
private fun PromptEditDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetThemeSelection(
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
private fun StyledThemeOptionCard(
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

private fun getThemeColors(
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
