package com.paxonf.sharesummarizer.viewmodel

import androidx.lifecycle.ViewModel
import com.paxonf.sharesummarizer.data.AppPreferences

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {

    val apiKey: String
        get() = appPreferences.apiKey

    val summaryLength: Float
        get() = appPreferences.summaryLength

    val textSize: Float
        get() = appPreferences.textSize

    fun saveApiKey(apiKey: String) {
        appPreferences.apiKey = apiKey
    }

    fun saveSummaryLength(length: Float) {
        appPreferences.summaryLength = length
    }

    fun saveTextSize(size: Float) {
        appPreferences.textSize = size
    }
}
