package com.paxonf.sharesummarizer.viewmodel

import androidx.lifecycle.ViewModel
import com.paxonf.sharesummarizer.data.AppPreferences

class SettingsViewModel(private val appPreferences: AppPreferences) : ViewModel() {

    val apiKey: String
        get() = appPreferences.apiKey

    val summaryLength: Int
        get() = appPreferences.summaryLength

    fun saveApiKey(apiKey: String) {
        appPreferences.apiKey = apiKey
    }

    fun saveSummaryLength(length: Int) {
        appPreferences.summaryLength = length
    }
}
