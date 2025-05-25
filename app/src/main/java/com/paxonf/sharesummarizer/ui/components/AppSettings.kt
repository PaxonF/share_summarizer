package com.paxonf.sharesummarizer.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.ui.theme.ShareSummarizerTheme
import com.paxonf.sharesummarizer.viewmodel.SettingsViewModel
import com.paxonf.sharesummarizer.viewmodel.SummaryUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(settingsViewModel: SettingsViewModel) {
        var apiKeyInput by remember { mutableStateOf(settingsViewModel.apiKey) }
        var summaryLengthSliderPosition by remember {
                mutableIntStateOf(settingsViewModel.summaryLength)
        }
        var selectedModel by remember { mutableStateOf(settingsViewModel.selectedModel) }
        var isModelDropdownExpanded by remember { mutableStateOf(false) }
        var summaryPromptInput by remember {
                mutableStateOf(
                        settingsViewModel.summaryPrompt.ifEmpty {
                                settingsViewModel.getDefaultPrompt()
                        }
                )
        }
        var selectedColorOption by remember {
                mutableStateOf(settingsViewModel.bottomSheetColorOption)
        }
        var customColor by remember {
                mutableStateOf(Color(settingsViewModel.customBottomSheetColor))
        }

        // State for the prompt editing dialog
        var showPromptDialog by remember { mutableStateOf(false) }
        var tempPromptInput by remember { mutableStateOf("") }

        // State for API key visibility
        var isApiKeyVisible by remember { mutableStateOf(false) }

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
                mutableStateOf(settingsViewModel.bottomSheetColorOption)
        }
        var initialCustomColor by remember {
                mutableStateOf(Color(settingsViewModel.customBottomSheetColor))
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
                        initialApiKey,
                        initialSummaryLength,
                        initialSelectedModel,
                        initialSummaryPrompt,
                        initialColorOption,
                        initialCustomColor
                ) {
                        apiKeyInput != initialApiKey ||
                                summaryLengthSliderPosition != initialSummaryLength ||
                                selectedModel != initialSelectedModel ||
                                summaryPromptInput != initialSummaryPrompt ||
                                selectedColorOption != initialColorOption ||
                                customColor != initialCustomColor
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

        // Preview bottom sheet with example content
        if (showPreviewBottomSheet) {
                val containerColor =
                        when (selectedColorOption) {
                                "primary" -> MaterialTheme.colorScheme.primaryContainer
                                "secondary" -> MaterialTheme.colorScheme.secondaryContainer
                                "tertiary" -> MaterialTheme.colorScheme.tertiaryContainer
                                "custom" -> customColor
                                else -> MaterialTheme.colorScheme.primaryContainer
                        }

                SummaryBottomSheet(
                        uiState =
                                SummaryUiState(
                                        summary = getExampleSummary(),
                                        originalText = getExampleOriginalText(),
                                        isLoading = false
                                ),
                        onDismiss = { showPreviewBottomSheet = false },
                        onRetry = { /* No-op for preview */},
                        containerColor = containerColor
                )
        }

        Scaffold(
                topBar = { TopAppBar(title = { Text("Share Summarizer") }) },
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
                                        onClick = { showPreviewBottomSheet = true },
                                        icon = {
                                                Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Preview"
                                                )
                                        },
                                        text = { Text("Preview") },
                                        containerColor =
                                                when (selectedColorOption) {
                                                        "primary" ->
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                        "secondary" ->
                                                                MaterialTheme.colorScheme
                                                                        .secondaryContainer
                                                        "tertiary" ->
                                                                MaterialTheme.colorScheme
                                                                        .tertiaryContainer
                                                        "custom" -> customColor
                                                        else ->
                                                                MaterialTheme.colorScheme
                                                                        .primaryContainer
                                                },
                                        contentColor =
                                                when (selectedColorOption) {
                                                        "primary" ->
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                        "secondary" ->
                                                                MaterialTheme.colorScheme
                                                                        .onSecondaryContainer
                                                        "tertiary" ->
                                                                MaterialTheme.colorScheme
                                                                        .onTertiaryContainer
                                                        "custom" ->
                                                                if (customColor.luminance() > 0.5f)
                                                                        Color.Black
                                                                else Color.White
                                                        else ->
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                }
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
                                                if (isApiKeyVisible) {
                                                        isApiKeyVisible = false
                                                }
                                        },
                                contentPadding =
                                        PaddingValues(
                                                start = 16.dp,
                                                end = 16.dp,
                                                top = 16.dp,
                                                bottom = 16.dp // No extra padding needed
                                        ),
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
                                                                                        isApiKeyVisible =
                                                                                                !isApiKeyVisible
                                                                                        // Click is
                                                                                        // consumed
                                                                                        // here,
                                                                                        // won't
                                                                                        // reach
                                                                                        // overlay
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
                                                                                                        "●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●●"
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

                                                                        IconButton(
                                                                                onClick = {
                                                                                        isApiKeyVisible =
                                                                                                !isApiKeyVisible
                                                                                }
                                                                        ) {
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Default
                                                                                                        .Edit,
                                                                                        contentDescription =
                                                                                                if (isApiKeyVisible
                                                                                                )
                                                                                                        "Hide API Key"
                                                                                                else
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

                                                        // Expandable API key input
                                                        AnimatedVisibility(
                                                                visible = isApiKeyVisible,
                                                                enter =
                                                                        expandVertically() +
                                                                                fadeIn(),
                                                                exit =
                                                                        shrinkVertically() +
                                                                                fadeOut()
                                                        ) {
                                                                OutlinedTextField(
                                                                        value = apiKeyInput,
                                                                        onValueChange = {
                                                                                apiKeyInput = it
                                                                        },
                                                                        label = {
                                                                                Text(
                                                                                        "Enter API Key"
                                                                                )
                                                                        },
                                                                        placeholder = {
                                                                                Text("sk-...")
                                                                        },
                                                                        singleLine = true,
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        visualTransformation =
                                                                                if (isApiKeyVisible
                                                                                ) {
                                                                                        VisualTransformation
                                                                                                .None
                                                                                } else {
                                                                                        PasswordVisualTransformation()
                                                                                },
                                                                        keyboardOptions =
                                                                                KeyboardOptions(
                                                                                        keyboardType =
                                                                                                KeyboardType
                                                                                                        .Password
                                                                                ),
                                                                        trailingIcon = {
                                                                                if (apiKeyInput
                                                                                                .isNotEmpty()
                                                                                ) {
                                                                                        IconButton(
                                                                                                onClick = {
                                                                                                        apiKeyInput =
                                                                                                                ""
                                                                                                }
                                                                                        ) {
                                                                                                Icon(
                                                                                                        imageVector =
                                                                                                                Icons.Default
                                                                                                                        .Clear,
                                                                                                        contentDescription =
                                                                                                                "Clear API Key"
                                                                                                )
                                                                                        }
                                                                                }
                                                                        },
                                                                        supportingText = {
                                                                                Text(
                                                                                        "Required for AI-powered summarization"
                                                                                )
                                                                        }
                                                                )
                                                        }
                                                }
                                        }
                                }

                                item {
                                        SettingsSection(title = "Model Selection") {
                                                ExposedDropdownMenuBox(
                                                        expanded = isModelDropdownExpanded,
                                                        onExpandedChange = {
                                                                isModelDropdownExpanded = it
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        OutlinedTextField(
                                                                value =
                                                                        settingsViewModel
                                                                                .availableModels[
                                                                                selectedModel]
                                                                                ?: selectedModel,
                                                                onValueChange = {},
                                                                readOnly = true,
                                                                label = { Text("AI Model") },
                                                                trailingIcon = {
                                                                        ExposedDropdownMenuDefaults
                                                                                .TrailingIcon(
                                                                                        expanded =
                                                                                                isModelDropdownExpanded
                                                                                )
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .menuAnchor(),
                                                                colors =
                                                                        ExposedDropdownMenuDefaults
                                                                                .outlinedTextFieldColors()
                                                        )

                                                        ExposedDropdownMenu(
                                                                expanded = isModelDropdownExpanded,
                                                                onDismissRequest = {
                                                                        isModelDropdownExpanded =
                                                                                false
                                                                }
                                                        ) {
                                                                settingsViewModel.availableModels
                                                                        .forEach {
                                                                                (
                                                                                        modelId,
                                                                                        displayName)
                                                                                ->
                                                                                DropdownMenuItem(
                                                                                        text = {
                                                                                                Text(
                                                                                                        displayName
                                                                                                )
                                                                                        },
                                                                                        onClick = {
                                                                                                selectedModel =
                                                                                                        modelId
                                                                                                isModelDropdownExpanded =
                                                                                                        false
                                                                                        },
                                                                                        contentPadding =
                                                                                                ExposedDropdownMenuDefaults
                                                                                                        .ItemContentPadding
                                                                                )
                                                                        }
                                                        }
                                                }
                                        }
                                }

                                item {
                                        SettingsSection(title = "Summary Length") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        Text(
                                                                text =
                                                                        "Length: ${getLengthLabel(summaryLengthSliderPosition)}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurface
                                                        )

                                                        Slider(
                                                                value =
                                                                        summaryLengthSliderPosition
                                                                                .toFloat(),
                                                                onValueChange = {
                                                                        summaryLengthSliderPosition =
                                                                                it.toInt()
                                                                },
                                                                valueRange = 1f..5f,
                                                                modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween
                                                        ) {
                                                                Text(
                                                                        text = "Brief",
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
                                                                        text = "Detailed",
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

                                item {
                                        SettingsSection(title = "Summary Prompt") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(12.dp)
                                                ) {
                                                        // Prompt preview card that opens the dialog
                                                        // when clicked
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
                                                                                Alignment.Top
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
                                }

                                item {
                                        SettingsSection(title = "Bottom Sheet Appearance") {
                                                Column(
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(16.dp)
                                                ) {
                                                        Text(
                                                                text =
                                                                        "Choose the color theme for the summary bottom sheet",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )

                                                        // Color options using the correct Material
                                                        // You options
                                                        Column(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(8.dp)
                                                        ) {
                                                                settingsViewModel
                                                                        .availableColorOptions
                                                                        .forEach {
                                                                                (
                                                                                        optionId,
                                                                                        displayName)
                                                                                ->
                                                                                ColorOption(
                                                                                        title =
                                                                                                displayName,
                                                                                        description =
                                                                                                when (optionId
                                                                                                ) {
                                                                                                        "primary" ->
                                                                                                                "Uses your device's primary accent color"
                                                                                                        "secondary" ->
                                                                                                                "Uses your device's secondary accent color"
                                                                                                        "tertiary" ->
                                                                                                                "Uses your device's tertiary accent color"
                                                                                                        "custom" ->
                                                                                                                "Choose your own custom color"
                                                                                                        else ->
                                                                                                                ""
                                                                                                },
                                                                                        color =
                                                                                                when (optionId
                                                                                                ) {
                                                                                                        "primary" ->
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .primaryContainer
                                                                                                        "secondary" ->
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .secondaryContainer
                                                                                                        "tertiary" ->
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .tertiaryContainer
                                                                                                        "custom" ->
                                                                                                                customColor
                                                                                                        else ->
                                                                                                                MaterialTheme
                                                                                                                        .colorScheme
                                                                                                                        .surface
                                                                                                },
                                                                                        isSelected =
                                                                                                selectedColorOption ==
                                                                                                        optionId,
                                                                                        onClick = {
                                                                                                selectedColorOption =
                                                                                                        optionId
                                                                                        }
                                                                                )
                                                                        }
                                                        }

                                                        // Custom color picker (only shown when
                                                        // custom is selected)
                                                        AnimatedVisibility(
                                                                visible =
                                                                        selectedColorOption ==
                                                                                "custom",
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
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text(
                                        text =
                                                "Customize how the AI summarizes your content. Use a detailed prompt for better results, but keep it concise for higher speed.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                        value = editedPrompt,
                                        onValueChange = { editedPrompt = it },
                                        label = { Text("Custom Prompt") },
                                        placeholder = {
                                                Text("Enter your custom summarization prompt...")
                                        },
                                        modifier = Modifier.fillMaxWidth().height(200.dp),
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
                                        modifier = Modifier.fillMaxWidth(),
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
                        ) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f)
        )
}

@Composable
private fun ColorOption(
        title: String,
        description: String,
        color: Color,
        isSelected: Boolean,
        onClick: () -> Unit
) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick() },
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                                MaterialTheme.colorScheme.surface
                                        }
                        ),
                border =
                        if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else null
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
                                        fontWeight = FontWeight.Medium,
                                        color =
                                                if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                }
                                )
                                Text(
                                        text = description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                if (isSelected) {
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                .copy(alpha = 0.7f)
                                                } else {
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                )
                        }

                        Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = color,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                }
        }
}

@Preview(showBackground = true)
@Composable
fun AppSettingsScreenPreview() {
        ShareSummarizerTheme {
                val context = LocalContext.current
                val dummyPrefs = AppPreferences(context)
                val dummyViewModel = SettingsViewModel(dummyPrefs)
                AppSettingsScreen(settingsViewModel = dummyViewModel)
        }
}

// Helper functions for example content
private fun getExampleSummary(): String {
        return """
# Article Summary: The Future of AI Technology

**Source:** TechNews Daily | **Author:** Dr. Sarah Johnson | **Date:** December 2024

## Key Points

**Artificial Intelligence Revolution**: The article discusses how AI technology is rapidly transforming various industries, from healthcare to transportation.

**Machine Learning Advances**: Recent breakthroughs in machine learning algorithms have enabled more sophisticated pattern recognition and decision-making capabilities.

**Ethical Considerations**: The piece emphasizes the importance of developing AI systems with built-in ethical guidelines and transparency measures.

## Main Takeaways

- AI adoption is accelerating across multiple sectors
- Investment in AI research has increased by 300% in the past year  
- Regulatory frameworks are being developed to ensure responsible AI deployment
- The technology promises to enhance human capabilities rather than replace them

*This summary demonstrates how your chosen color theme will appear when viewing AI-generated content summaries.*
        """.trimIndent()
}

private fun getExampleOriginalText(): String {
        return """
The Future of AI Technology: A Comprehensive Look at What's Coming Next

By Dr. Sarah Johnson, TechNews Daily, December 2024

Artificial intelligence is no longer a concept confined to science fiction. Today, AI technology is rapidly transforming industries across the globe, from healthcare and finance to transportation and entertainment. As we look toward the future, the potential applications and implications of AI continue to expand at an unprecedented pace.

Recent breakthroughs in machine learning algorithms have enabled more sophisticated pattern recognition and decision-making capabilities. These advances are making it possible for AI systems to process and analyze vast amounts of data with remarkable accuracy and speed. Companies are investing heavily in AI research and development, with funding increasing by over 300% in the past year alone.

However, with great power comes great responsibility. The article emphasizes the critical importance of developing AI systems with built-in ethical guidelines and transparency measures. As AI becomes more integrated into our daily lives, ensuring that these systems operate fairly and transparently is paramount.

The regulatory landscape is also evolving to keep pace with technological advancement. Governments worldwide are working to establish frameworks that promote innovation while protecting citizens' rights and privacy. The goal is not to stifle progress but to ensure that AI development proceeds in a responsible and beneficial manner.

Looking ahead, experts predict that AI will continue to enhance human capabilities rather than replace them entirely. The focus is shifting toward creating collaborative systems where humans and AI work together to solve complex problems and improve quality of life for everyone.
        """.trimIndent()
}
