package com.paxonf.sumifai.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paxonf.sumifai.data.AppPreferences
import com.paxonf.sumifai.utils.Constants
import com.paxonf.sumifai.utils.TextSummarizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                    "gemini-1.5-flash" to "Gemini 1.5 Flash",
                    "gemini-2.0-flash" to "Gemini 2.0 Flash",
                    "gemini-2.5-flash-preview-04-17" to "Gemini 2.5 Flash (Preview)",
                    "gemini-2.5-pro-preview-05-06" to "Gemini 2.5 Pro (Preview)"
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
            _previewSummaryUiState.value =
                    SummaryUiState(isLoading = true, originalText = articleUrl)
            val summary =
                    textSummarizer.summarize(
                            text = articleUrl,
                            summaryLength = summaryLength,
                            apiKey = apiKey,
                            modelId = selectedModel,
                            summaryPrompt = summaryPrompt.ifEmpty { getDefaultPrompt() }
                    )
            if (summary.startsWith("Error") ||
                            summary.startsWith("API Error") ||
                            summary.startsWith("Network error")
            ) {
                _previewSummaryUiState.value =
                        SummaryUiState(error = summary, originalText = articleUrl)
            } else {
                _previewSummaryUiState.value =
                        SummaryUiState(summary = summary, originalText = articleUrl)
            }
        }
    }

    fun clearPreviewSummary() {
        _previewSummaryUiState.value = SummaryUiState()
    }
}
