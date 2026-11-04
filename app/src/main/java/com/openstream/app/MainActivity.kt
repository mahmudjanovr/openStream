package com.openstream.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.openstream.app.core.designsystem.theme.OpenStreamTheme
import com.openstream.app.core.localization.AppLanguage
import com.openstream.app.core.localization.AppLocaleManager
import com.openstream.app.core.navigation.OpenStreamApp
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as OpenStreamApplication).container

        setContent {
            val selectedLanguage by appContainer.userPreferencesRepository.selectedLanguage
                .collectAsState(initial = AppLanguage.ENGLISH)

            // Dynamic localized configuration context for instant Compose string updates
            val currentContext = LocalContext.current
            val localizedContext = remember(selectedLanguage) {
                AppLocaleManager.setAppLocale(currentContext, selectedLanguage)
            }

            val localizedConfiguration = remember(selectedLanguage) {
                Configuration(localizedContext.resources.configuration).apply {
                    setLocale(Locale(selectedLanguage.code))
                }
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                OpenStreamTheme {
                    OpenStreamApp(
                        appContainer = appContainer,
                        onLanguageChange = { newLanguage ->
                            AppLocaleManager.setAppLocale(this, newLanguage)
                        }
                    )
                }
            }
        }
    }
}
