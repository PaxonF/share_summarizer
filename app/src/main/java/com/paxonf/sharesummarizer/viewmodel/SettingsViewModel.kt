package com.paxonf.sharesummarizer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.utils.Constants
import com.paxonf.sharesummarizer.utils.TextSummarizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

class SettingsViewModel(private val appPreferences: AppPreferences, private val context: Context) :
        ViewModel() {

    private val textSummarizer = TextSummarizer(context)

    // StateFlow for preview summary
    private val _previewSummaryUiState = MutableStateFlow(SummaryUiState())
    val previewSummaryUiState: StateFlow<SummaryUiState> = _previewSummaryUiState.asStateFlow()

    val apiKey: String
        get() = appPreferences.apiKey

    val summaryLength: Int
        get() = appPreferences.summaryLength

    val selectedModel: String
        get() = appPreferences.selectedModel

    // Map of model IDs to display names
    val availableModels =
            mapOf(
                    "gemini-2.5-flash" to "Gemini 2.5 Flash",
                    "gemini-2.5-pro" to "Gemini 2.5 Pro"
            )

    val summaryPrompt: String
        get() = appPreferences.summaryPrompt.ifEmpty { "" }

    val bottomSheetColorOption: String
        get() = appPreferences.bottomSheetColorOption

    val customBottomSheetColor: Int
        get() = appPreferences.customBottomSheetColor

    val bottomSheetTextSizeMultiplier: Float
        get() = appPreferences.bottomSheetTextSizeMultiplier

    fun getDefaultPrompt(): String {
        return Constants.DEFAULT_SUMMARY_PROMPT
    }

    fun saveApiKey(apiKey: String) {
        appPreferences.apiKey = apiKey
    }

    fun saveSummaryLength(length: Int) {
        appPreferences.summaryLength = length
    }

    fun saveSelectedModel(modelId: String) {
        appPreferences.selectedModel = modelId
    }

    fun saveSummaryPrompt(prompt: String) {
        appPreferences.summaryPrompt = prompt
    }

    fun saveBottomSheetColorOption(option: String) {
        appPreferences.bottomSheetColorOption = option
    }

    fun saveCustomBottomSheetColor(color: Int) {
        appPreferences.customBottomSheetColor = color
    }

    fun saveBottomSheetTextSizeMultiplier(multiplier: Float) {
        appPreferences.bottomSheetTextSizeMultiplier = multiplier
    }

    fun generatePreviewSummary(articleUrl: String) {
        viewModelScope.launch {
            // Reset state for a new summary generation
            _previewSummaryUiState.value = SummaryUiState(isLoading = true, originalText = articleUrl)

            textSummarizer.summarize(
                text = articleUrl,
                summaryLength = summaryLength,
                apiKey = apiKey,
                modelId = selectedModel,
                summaryPrompt = summaryPrompt.ifEmpty { getDefaultPrompt() }
            )
            .onCompletion {
                // Flow is complete, set loading to false
                _previewSummaryUiState.value = _previewSummaryUiState.value.copy(isLoading = false)
            }
            .catch { e ->
                // An exception occurred in the flow, update UI with error
                _previewSummaryUiState.value = SummaryUiState(error = "Error: ${e.message}", originalText = articleUrl)
            }
            .collect { chunk ->
                // Check if the chunk itself is an error message from the API
                if (chunk.startsWith("API Error:") || chunk.startsWith("Error from API")) {
                     _previewSummaryUiState.value = _previewSummaryUiState.value.copy(error = chunk, isLoading = false)
                } else {
                    // Append the new chunk to the summary
                    _previewSummaryUiState.value = _previewSummaryUiState.value.copy(
                        summary = _previewSummaryUiState.value.summary + chunk
                    )
                }
            }
        }
    }

    fun clearPreviewSummary() {
        _previewSummaryUiState.value = SummaryUiState()
    }
}
