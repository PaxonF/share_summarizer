package com.paxonf.sharesummarizer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.ui.theme.ShareSummarizerTheme
import com.paxonf.sharesummarizer.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(settingsViewModel: SettingsViewModel) {
    var apiKeyInput by remember { mutableStateOf(settingsViewModel.apiKey) }
    var summaryLengthSliderPosition by remember {
        mutableIntStateOf(settingsViewModel.summaryLength)
    }

    // Track the initial values with mutable state to allow updates after saving
    var initialApiKey by remember { mutableStateOf(settingsViewModel.apiKey) }
    var initialSummaryLength by remember { mutableIntStateOf(settingsViewModel.summaryLength) }

    // Compute whether anything has changed
    val hasChanges =
            remember(
                    apiKeyInput,
                    summaryLengthSliderPosition,
                    initialApiKey,
                    initialSummaryLength
            ) {
                apiKeyInput != initialApiKey || summaryLengthSliderPosition != initialSummaryLength
            }

    Scaffold(topBar = { TopAppBar(title = { Text("App Settings") }) }) { paddingValues ->
        Column(
                modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                    "Configure your summarizer settings below.",
                    style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        if (apiKeyInput.isNotEmpty()) {
                            IconButton(onClick = { apiKeyInput = "" }) {
                                Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear API Key"
                                )
                            }
                        }
                    }
            )

            Text("Summary Length: $summaryLengthSliderPosition")
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Short", style = MaterialTheme.typography.bodyMedium)
                Text("Long", style = MaterialTheme.typography.bodyMedium)
            }
            Slider(
                    value = summaryLengthSliderPosition.toFloat(),
                    onValueChange = { summaryLengthSliderPosition = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 4, // 5 positions (1-5) means 4 steps between them
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp)
            )

            // Add other settings UI components here (e.g., Switches, RadioButtons)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                    onClick = {
                        settingsViewModel.saveApiKey(apiKeyInput)
                        settingsViewModel.saveSummaryLength(summaryLengthSliderPosition)

                        // Update initial values to match current values after saving
                        initialApiKey = apiKeyInput
                        initialSummaryLength = summaryLengthSliderPosition
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasChanges
            ) { Text("Save Settings") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppSettingsScreenPreview() {
    ShareSummarizerTheme {
        // For preview, create a dummy AppPreferences and ViewModel
        val context = LocalContext.current
        val dummyPrefs = AppPreferences(context)
        val dummyViewModel = SettingsViewModel(dummyPrefs)
        AppSettingsScreen(settingsViewModel = dummyViewModel)
    }
}
