package app.slovami.ai

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Применяет сохранённую локаль до создания Activity. При первом запуске
 * выбирает RU для русской системной локали, иначе EN.
 */
class SlovamiAiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        applyLanguagePreference()
    }

    private fun applyLanguagePreference() {
        val prefs = getSharedPreferences("interview_prefs", MODE_PRIVATE)
        var langName = prefs.getString("asr_language", null)

        // Свежая установка → определяем по системному языку.
        if (langName == null) {
            langName = detectDefaultAsrLanguage()
            prefs.edit().putString("asr_language", langName).apply()
            Log.i(TAG, "First-run: set asr_language=$langName based on system locale")
        }

        // Если AppCompat уже знает локаль (Android 13+ запомнил/пользователь поменял через системные Settings)
        // — не перезаписываем, его выбор приоритетнее.
        val current = AppCompatDelegate.getApplicationLocales()
        if (!current.isEmpty) return

        val localeTag = when (langName) {
            "EN" -> "en"
            "RU" -> "ru"
            else -> return
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    /** Начальное значение для системной локали: ru → RU, остальные → EN. */
    private fun detectDefaultAsrLanguage(): String {
        val systemLocale = resources.configuration.locales.get(0) ?: Locale.getDefault()
        val lang = systemLocale.language.lowercase()
        return if (lang == "ru") "RU" else "EN"
    }

    companion object {
        private const val TAG = "SlovamiAiApp"
    }
}
