package cit.edu.primera.feature.transcription.domain

import java.util.Locale

enum class VoiceLanguage(
    val code: String,
    val displayName: String,
    val shortLabel: String
) {
    BISAYA("ceb-PH", "Bisaya (Cebuano)", "Bisaya"),
    ENGLISH("en-US", "English (US)", "English"),
    FILIPINO("fil-PH", "Filipino (Tagalog)", "Filipino"),
    AUTO("auto", "System Default", "Default");

    fun getEffectiveLanguageTag(): String {
        return if (this == AUTO) {
            Locale.getDefault().toLanguageTag()
        } else {
            code
        }
    }

    companion object {
        fun fromCode(code: String): VoiceLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BISAYA
        }
    }
}
