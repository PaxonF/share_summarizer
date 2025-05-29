package com.paxonf.sumifai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paxonf.sumifai.data.AppPreferences
import com.paxonf.sumifai.ui.components.SummaryBottomSheet
import com.paxonf.sumifai.utils.TextSummarizer
import com.paxonf.sumifai.viewmodel.SummaryViewModel

class ShareActivity : ComponentActivity() {

    private lateinit var appPreferences: AppPreferences
    private lateinit var textSummarizer: TextSummarizer

    // ViewModel Factory
    private val summaryViewModelFactory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SummaryViewModel(appPreferences, textSummarizer) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }

    private val summaryViewModel: SummaryViewModel by viewModels { summaryViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure window to be edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        appPreferences = AppPreferences(applicationContext)
        textSummarizer = TextSummarizer(applicationContext) // Pass the context

        var sharedText: String? = null

        if (intent?.action == Intent.ACTION_SEND) {
            if ("text/plain" == intent.type || intent.type?.startsWith("text/") == true) {
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            } else if (intent.type == "*/*") { // For URLs shared from some apps
                sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                // You might want to add logic here to validate if it's a URL
                // and potentially fetch content from the URL before summarizing.
                // For now, we'll assume it's plain text or a link we want to summarize as is.
            }
        }

        setContent {
            sumifAITheme(dynamicColor = true) {
                var showBottomSheet by remember { mutableStateOf(true) }

                // Get the selected color based on user preferences
                val (containerColor, contentColor) = // Destructure to get both colors
                        getThemeColorsForShareActivity(
                                appPreferences.bottomSheetColorOption,
                                Color(appPreferences.customBottomSheetColor),
                                MaterialTheme.colorScheme
                        )

                if (showBottomSheet) {
                    LaunchedEffect(
                            sharedText
                    ) { // Re-trigger if sharedText changes (though usually not for an Activity)
                        summaryViewModel.generateSummary(sharedText)
                    }

                    SummaryBottomSheet(
                            uiState = summaryViewModel.uiState,
                            onDismiss = {
                                showBottomSheet = false
                                finishAndRemoveTask() // Or just finish() if you prefer
                            },
                            onRetry = { originalTextForRetry ->
                                summaryViewModel.generateSummary(originalTextForRetry)
                            },
                            containerColor = containerColor,
                            contentColor = contentColor // Pass contentColor
                    )
                } else {
                    // The activity will finish when showBottomSheet is false
                    // You could also directly call finish() in onDismiss.
                    // This LaunchedEffect ensures finish() is called after composition.
                    LaunchedEffect(Unit) { finishAndRemoveTask() }
                }
            }
        }

        // If no valid share intent, close immediately
        if (sharedText.isNullOrBlank() && savedInstanceState == null
        ) { // savedInstanceState check to avoid closing on config change if dialog was up
            finishAndRemoveTask()
        }
    }

    // Optional: Handle new intents if the activity is singleTask
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent
        // Re-process the intent if needed, though for a simple share target,
        // onCreate will typically handle it as it's often a new task.
        if (intent.action == Intent.ACTION_SEND) {
            val newSharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!newSharedText.isNullOrBlank()) {
                summaryViewModel.generateSummary(newSharedText)
            } else {
                finishAndRemoveTask() // Close if new intent is not valid
            }
        }
    }

    // Helper function to determine container and content colors for ShareActivity
    private fun getThemeColorsForShareActivity(
            selectedOption: String,
            customColor: Color,
            materialColorScheme: androidx.compose.material3.ColorScheme
    ): Pair<Color, Color> {
        val containerColorResult =
                when (selectedOption) {
                    "system_background" -> materialColorScheme.background
                    "system_primary", "primary" -> materialColorScheme.primaryContainer
                    "system_secondary", "secondary" -> materialColorScheme.secondaryContainer
                    "system_tertiary", "tertiary" -> materialColorScheme.tertiaryContainer
                    "light" -> Color.White
                    "sepia" -> Color(0xFFF5EEDC)
                    "dark" -> Color(0xFF121212)
                    "custom" -> customColor.copy(alpha = 0.9f) // Maintain existing alpha for custom
                    else ->
                            materialColorScheme
                                    .background // Default to background or a sensible default
                }

        val contentColorResult =
                when (selectedOption) {
                    "system_background" -> materialColorScheme.onBackground
                    "system_primary", "primary" -> materialColorScheme.onPrimaryContainer
                    "system_secondary", "secondary" -> materialColorScheme.onSecondaryContainer
                    "system_tertiary", "tertiary" -> materialColorScheme.onTertiaryContainer
                    "light", "sepia", "dark", "custom" -> {
                        if (containerColorResult.luminance() > 0.5f) Color.Black else Color.White
                    }
                    else -> materialColorScheme.onBackground
                }
        return Pair(containerColorResult, contentColorResult)
    }
}
