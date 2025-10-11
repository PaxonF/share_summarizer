package com.paxonf.sharesummarizer.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paxonf.sharesummarizer.data.AppPreferences
import com.paxonf.sharesummarizer.utils.Constants
import com.paxonf.sharesummarizer.utils.TextSummarizer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
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
            val summaryLength = appPreferences.summaryLength
            val apiKey = appPreferences.apiKey
            val selectedModel = appPreferences.selectedModel
            val summaryPrompt =
                    appPreferences.summaryPrompt.ifEmpty { Constants.DEFAULT_SUMMARY_PROMPT }

            textSummarizer
                    .summarize(text, summaryLength, apiKey, selectedModel, summaryPrompt)
                    .onCompletion {
                        uiState = uiState.copy(isLoading = false)
                    }
                    .catch { e ->
                        uiState =
                                uiState.copy(
                                        error = e.message ?: "Failed to generate summary",
                                        isLoading = false
                                )
                    }
                    .collect { chunk ->
                        if (chunk.startsWith("API Error:") || chunk.startsWith("Error from API")) {
                            uiState = uiState.copy(error = chunk, isLoading = false)
                        } else {
                            uiState = uiState.copy(summary = uiState.summary + chunk)
                        }
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
