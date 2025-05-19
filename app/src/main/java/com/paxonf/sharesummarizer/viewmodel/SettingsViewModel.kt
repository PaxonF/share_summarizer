package com.paxonf.sharesummarizer.viewmodel

import androidx.lifecycle.ViewModel
import com.paxonf.sharesummarizer.data.AppPreferences

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {

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

    fun saveApiKey(apiKey: String) {
        appPreferences.apiKey = apiKey
    }

    fun saveSummaryLength(length: Int) {
        appPreferences.summaryLength = length
    }

    fun saveSelectedModel(modelId: String) {
        appPreferences.selectedModel = modelId
    }
}
