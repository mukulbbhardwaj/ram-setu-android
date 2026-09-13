package com.vanarsena.ramsetu.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

fun Context.contextForAppLanguage(languageTag: String): Context {
    val locale = localeForAppLanguage(languageTag)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        config.setLocales(LocaleList(locale))
    }
    return createConfigurationContext(config)
}

private fun localeForAppLanguage(languageTag: String): Locale = when (languageTag) {
    "hi" -> Locale("hi")
    "en" -> Locale("en")
    else -> Locale.forLanguageTag(languageTag)
}

/**
 * Applies the in-app language to [LocalContext] and [LocalConfiguration].
 * Use inside [androidx.compose.ui.window.Dialog] — dialogs do not inherit the activity locale.
 */
@Composable
fun ProvideAppLanguage(
    language: String,
    content: @Composable () -> Unit
) {
    val appContext = LocalContext.current.applicationContext
    val localizedContext = remember(language, appContext) {
        appContext.contextForAppLanguage(language)
    }
    val localizedConfiguration = remember(localizedContext) {
        Configuration(localizedContext.resources.configuration)
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration
    ) {
        content()
    }
}
