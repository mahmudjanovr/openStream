package com.openstream.app.core.localization

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

enum class AppLanguage(val code: String, val displayNameResId: Int) {
    ENGLISH("en", com.openstream.app.R.string.settings_lang_en),
    UZBEK("uz", com.openstream.app.R.string.settings_lang_uz),
    RUSSIAN("ru", com.openstream.app.R.string.settings_lang_ru);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

object AppLocaleManager {

    fun setAppLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val systemLocaleManager = context.getSystemService(LocaleManager::class.java)
            systemLocaleManager?.applicationLocales = LocaleList(locale)
        }

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocaleToConfiguration(config: Configuration, language: AppLanguage) {
        val locale = Locale(language.code)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
    }
}
