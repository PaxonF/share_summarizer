package com.paxonf.sumifai.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paxonf.sumifai.data.AppPreferences
import com.paxonf.sumifai.utils.Constants
import com.paxonf.sumifai.utils.TextSummarizer
import kotlinx.coroutines.launch

class SummaryViewModel(
        private val appPreferences: AppPreferences,
        private val textSummarizer: TextSummarizer
) : ViewModel() {

    var uiState by mutableStateOf(SummaryUiState())
        private set

    fun generateSummary(text: String?) {
        if (text.isNullOrBlank()) {
            uiState = SummaryUiState(error = "No text to summarize")
            return
        }

        uiState = SummaryUiState(isLoading = true, originalText = text)

        viewModelScope.launch {
            try {
                val summaryLength = appPreferences.summaryLength
                val apiKey = appPreferences.apiKey
                val selectedModel = appPreferences.selectedModel
                val summaryPrompt =
                        appPreferences.summaryPrompt.ifEmpty { Constants.DEFAULT_SUMMARY_PROMPT }

                val summary =
                        textSummarizer.summarize(
                                text,
                                summaryLength,
                                apiKey,
                                selectedModel,
                                summaryPrompt
                        )
                uiState = SummaryUiState(summary = summary, originalText = text, isLoading = false)
            } catch (e: Exception) {
                uiState =
                        SummaryUiState(
                                error = e.message ?: "Failed to generate summary",
                                originalText = text,
                                isLoading = false
                        )
            }
        }
    }
}

data class SummaryUiState(
        val isLoading: Boolean = false,
        val summary: String = "",
        val originalText: String = "",
        val error: String? = null
)
