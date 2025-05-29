package com.paxonf.sumifai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.paxonf.sumifai.data.AppPreferences
import com.paxonf.sumifai.ui.components.AppSettingsScreen
import com.paxonf.sumifai.ui.theme.sumifAITheme
import com.paxonf.sumifai.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appPreferences: AppPreferences

    // ViewModel Factory to pass AppPreferences
    private val settingsViewModelFactory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(appPreferences, applicationContext) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }

    private val settingsViewModel: SettingsViewModel by viewModels { settingsViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install the splash screen
        installSplashScreen()

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        appPreferences = AppPreferences(applicationContext)

        setContent {
            sumifAITheme(dynamicColor = true) {
                AppSettingsScreen(settingsViewModel = settingsViewModel)
            }
        }
    }
}
