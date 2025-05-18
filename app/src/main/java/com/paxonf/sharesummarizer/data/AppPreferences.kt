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

    var summaryLength: Float
        get() = preferences.getFloat(KEY_SUMMARY_LENGTH, 0.5f)
        set(value) = preferences.edit { putFloat(KEY_SUMMARY_LENGTH, value) }

    var textSize: Float
        get() = preferences.getFloat(KEY_TEXT_SIZE, 1.0f)
        set(value) = preferences.edit { putFloat(KEY_TEXT_SIZE, value) }

    companion object {
        private const val PREFERENCES_NAME = "share_summarizer_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SUMMARY_LENGTH = "summary_length"
        private const val KEY_TEXT_SIZE = "text_size"
    }
}
