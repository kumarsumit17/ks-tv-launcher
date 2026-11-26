package com.ks.tvlauncher.data.language

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * The suite's language rule (ks-utility `specs/languages.md`): follow the system, allow a choice,
 * remember it. KS speaks English and Hindi.
 *
 * - Android 13+ keeps the choice itself (the per-app language, `locales_config.xml`), so it also
 *   shows in the TV's own app-language settings.
 * - Android 8 to 12 has no per-app language: KS saves the choice in its own preferences and wraps
 *   the activity's and the application's context with it ([wrap]).
 *
 * No AndroidX AppCompat (specs/dependencies.md).
 */
object AppLanguage {
    /** "Same as the system". */
    const val SYSTEM = ""

    /** The app's languages, in the order Settings shows them. */
    val SUPPORTED = listOf("en", "hi")

    private const val PREFS = "ks_language"
    private const val KEY = "choice"

    /** A saved value as a choice: one of [SUPPORTED], else [SYSTEM] (anything unknown). */
    fun parse(saved: String?): String {
        val tag = saved?.trim().orEmpty()
        if (tag.isEmpty()) return SYSTEM
        val language = tag.substringBefore('-').substringBefore('_').lowercase(Locale.ROOT)
        return language.takeIf { it in SUPPORTED } ?: SYSTEM
    }

    /** The current choice: [SYSTEM], "en" or "hi". */
    fun choice(context: Context): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            parse(if (locales.isEmpty) null else locales[0].toLanguageTag())
        } else {
            parse(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null))
        }

    /** Saves [choice] and shows the activity in it. */
    fun set(activity: Activity, choice: String) {
        val value = parse(choice)
        if (value == choice(activity)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android saves it and recreates the activity.
            activity.getSystemService(LocaleManager::class.java).applicationLocales =
                if (value == SYSTEM) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(value)
        } else {
            activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, value).commit()
            activity.recreate()
        }
    }

    /** Android 8 to 12: [base] with the chosen language, or [base] itself when following the system. */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
        val value = parse(base.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null))
        if (value == SYSTEM) return base
        val locale = Locale.forLanguageTag(value)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }
}
