package com.paxonf.sharesummarizer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AppPreferences(private val context: Context) {

    private val preferences: SharedPreferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var apiKey: String
        get() = preferences.getString(KEY_API_KEY, "") ?: ""
        set(value) = preferences.edit { putString(KEY_API_KEY, value) }

    var summaryLength: Int
        get() = preferences.getInt(KEY_SUMMARY_LENGTH, 3)
        set(value) = preferences.edit { putInt(KEY_SUMMARY_LENGTH, value) }

    var selectedModel: String
        get() = preferences.getString(KEY_SELECTED_MODEL, "gemini-2.0-flash") ?: "gemini-2.0-flash"
        set(value) = preferences.edit { putString(KEY_SELECTED_MODEL, value) }

    companion object {
        private const val PREFERENCES_NAME = "share_summarizer_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SUMMARY_LENGTH = "summary_length"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_SELECTED_MODEL = "selected_model"
    }
}
