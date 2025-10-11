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
        get() = preferences.getString(KEY_SELECTED_MODEL, "gemini-2.5-flash") ?: "gemini-2.5-flash"
        set(value) = preferences.edit { putString(KEY_SELECTED_MODEL, value) }

    var summaryPrompt: String
        get() = preferences.getString(KEY_SUMMARY_PROMPT, "") ?: ""
        set(value) = preferences.edit { putString(KEY_SUMMARY_PROMPT, value) }

    var bottomSheetColorOption: String
        get() =
                preferences.getString(KEY_BOTTOM_SHEET_COLOR, "system_background")
                        ?: "system_background"
        set(value) = preferences.edit { putString(KEY_BOTTOM_SHEET_COLOR, value) }

    var customBottomSheetColor: Int
        get() = preferences.getInt(KEY_CUSTOM_BOTTOM_SHEET_COLOR, 0xFF6750A4.toInt())
        set(value) = preferences.edit { putInt(KEY_CUSTOM_BOTTOM_SHEET_COLOR, value) }

    var bottomSheetTextSizeMultiplier: Float
        get() = preferences.getFloat(KEY_BOTTOM_SHEET_TEXT_SIZE_MULTIPLIER, 1.0f)
        set(value) = preferences.edit { putFloat(KEY_BOTTOM_SHEET_TEXT_SIZE_MULTIPLIER, value) }

    companion object {
        private const val PREFERENCES_NAME = "sumifai_prefs"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_SUMMARY_LENGTH = "summary_length"
        private const val KEY_SELECTED_MODEL = "selected_model"
        private const val KEY_SUMMARY_PROMPT = "summary_prompt"
        private const val KEY_BOTTOM_SHEET_COLOR = "bottom_sheet_color"
        private const val KEY_CUSTOM_BOTTOM_SHEET_COLOR = "custom_bottom_sheet_color"
        private const val KEY_BOTTOM_SHEET_TEXT_SIZE_MULTIPLIER =
                "bottom_sheet_text_size_multiplier"
    }
}
