package com.example.utils

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

data class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)

object LocaleHelper {
    val supportedLanguages = listOf(
        AppLanguage("", "System Default", "Default"),
        AppLanguage("en", "English", "English"),
        AppLanguage("es", "Spanish", "Español"),
        AppLanguage("fr", "French", "Français"),
        AppLanguage("de", "German", "Deutsch"),
        AppLanguage("ja", "Japanese", "日本語"),
        AppLanguage("zh", "Chinese", "中文"),
        AppLanguage("hi", "Hindi", "हिन्दी"),
        AppLanguage("pt", "Portuguese", "Português")
    )

    fun getLanguageByCode(code: String): AppLanguage {
        return supportedLanguages.find { it.code.equals(code, ignoreCase = true) }
            ?: supportedLanguages.first()
    }

    fun applyLocale(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as? LocaleManager
                if (localeManager != null) {
                    val localeList = if (languageCode.isEmpty()) {
                        LocaleList.getEmptyLocaleList()
                    } else {
                        LocaleList.forLanguageTags(languageCode)
                    }
                    localeManager.applicationLocales = localeList
                }
            } catch (e: Exception) {
                // Fallback for custom system managers
            }
        }

        val locale = if (languageCode.isEmpty()) {
            Locale.getDefault()
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    fun wrapContext(context: Context, languageCode: String): Context {
        if (languageCode.isEmpty()) return context
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
