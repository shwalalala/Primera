package cit.edu.primera.feature.transcription.domain

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import java.util.Locale

data class VoiceCorrectionResult(
    val text: String,
    val isAiCorrected: Boolean,
    val originalText: String
)

interface VoiceTextCorrector {
    suspend fun correctAndPunctuate(
        rawText: String,
        alternatives: List<String> = emptyList(),
        languageCode: String
    ): VoiceCorrectionResult

    suspend fun correctAndPunctuate(
        rawText: String,
        languageCode: String
    ): VoiceCorrectionResult = correctAndPunctuate(rawText, emptyList(), languageCode)
}

class GeminiVoiceTextCorrector(
    private val generativeModel: GenerativeModel?
) : VoiceTextCorrector {

    override suspend fun correctAndPunctuate(
        rawText: String,
        alternatives: List<String>,
        languageCode: String
    ): VoiceCorrectionResult {
        val trimmed = rawText.trim()
        if (trimmed.isBlank()) {
            return VoiceCorrectionResult(
                text = "",
                isAiCorrected = false,
                originalText = rawText
            )
        }

        val isBisaya = languageCode.startsWith("ceb", ignoreCase = true)
        val isFilipino = languageCode.startsWith("fil", ignoreCase = true)

        if (generativeModel == null) {
            return VoiceCorrectionResult(
                text = fallbackPunctuate(trimmed, isBisaya),
                isAiCorrected = false,
                originalText = rawText
            )
        }

        return try {
            val languageDescription = when {
                isBisaya -> "Bisaya (Cebuano)"
                isFilipino -> "Filipino (Tagalog)"
                languageCode.startsWith("en", ignoreCase = true) -> "English"
                else -> "Bisaya, Filipino, or English"
            }

            val alternativesSection = if (alternatives.size > 1) {
                val distinctAlternatives = alternatives
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.equals(trimmed, ignoreCase = true) }
                    .take(4)

                if (distinctAlternatives.isNotEmpty()) {
                    "\nAlternative speech recognizer hypotheses from microphone:\n" +
                            distinctAlternatives.joinToString("\n") { "- \"$it\"" }
                } else ""
            } else ""

            val prompt = """
                You are an intelligent bilingual (Bisaya/Cebuano and English) speech-to-text post-processor for a maternal health mobile app ("Primera").
                The user spoke into their phone microphone. Target language selected: $languageDescription.
                
                Raw primary speech recognition result:
                "$trimmed"$alternativesSection

                CONTEXT & CRITICAL PROBLEM:
                Many mobile phone speech engines (especially when the phone system language is set to English) force or bias acoustic inputs into English dictionary words or phonetic approximations. For example, when a user speaks Bisaya, the recognizer often outputs English sound-alike gibberish:
                - "suck it a call tea and" / "socket a kong tiyan" -> Spoken: "Sakit akong tiyan"
                - "love bad a call low" / "love bad a kong ulo" -> Spoken: "Labad akong ulo"
                - "gee car boy co" / "geeka boy" / "get car boy" -> Spoken: "Gikapoy ko"
                - "now leap on call" / "nah lipong" -> Spoken: "Nalipong ko"
                - "car sue call on" / "car so can" -> Spoken: "Kasukaon ko"
                - "on sound man knee" / "on sound money" -> Spoken: "Unsaon man ni"
                - "who bug a call teal" / "who bag akong tiil" -> Spoken: "Naghubag akong tiil"
                - "he la nut" / "he lanat" -> Spoken: "Gihilanat ko"
                - "daily market to" / "dilly make a toog" -> Spoken: "Dili makatug"
                - "suck it a kong back" -> Spoken: "Sakit akong likod"

                RULES:
                1. RECOVER BISAYA IF CORRUPTED: If target language is Bisaya, inspect whether the raw text (or alternatives) contains English phonetic sound-alikes of Bisaya words. If so, reconstruct the true intended Bisaya sentence!
                2. PRESERVE TRUE LANGUAGE:
                   - If the user genuinely spoke in English (e.g. "I have a headache and feel dizzy"), output proper English.
                   - If the user spoke in Bisaya, output proper natural Bisaya (do NOT translate it to English!).
                   - If the user spoke mixed Bislish (e.g. "Labad kaayo akong head"), preserve the natural code-switching.
                3. PUNCTUATION & CAPITALIZATION: Add natural punctuation marks (periods, commas, question marks, apostrophes like "kanus-a") and capitalize properly.
                4. PRESERVE HEALTH DETAILS: Keep all symptoms, feelings, and questions intact.
                5. OUTPUT: Output ONLY the final corrected text. Do NOT wrap in quotes. Do NOT add notes, explanations, or labels.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val resultText = response.text
                ?.trim()
                ?.removePrefix("\"")
                ?.removeSuffix("\"")
                ?.trim() ?: ""

            if (resultText.isNotBlank()) {
                VoiceCorrectionResult(
                    text = resultText,
                    isAiCorrected = true,
                    originalText = rawText
                )
            } else {
                VoiceCorrectionResult(
                    text = fallbackPunctuate(trimmed, isBisaya),
                    isAiCorrected = false,
                    originalText = rawText
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiVoiceCorrector", "Failed to correct text with AI, using fallback punctuation", e)
            VoiceCorrectionResult(
                text = fallbackPunctuate(trimmed, isBisaya),
                isAiCorrected = false,
                originalText = rawText
            )
        }
    }

    companion object {
        fun fallbackPunctuate(text: String, isBisaya: Boolean = false): String {
            if (text.isBlank()) return ""
            var formatted = text.trim()

            if (isBisaya) {
                val phoneticReplacements = listOf(
                    Regex("(?i)\\bsuck it\\b") to "sakit",
                    Regex("(?i)\\bsocket a\\b") to "sakit a",
                    Regex("(?i)\\ba call\\b") to "akong",
                    Regex("(?i)\\ba kong\\b") to "akong",
                    Regex("(?i)\\btea and\\b") to "tiyan",
                    Regex("(?i)\\blove bad\\b") to "labad",
                    Regex("(?i)\\bo low\\b") to "ulo",
                    Regex("(?i)\\bgee car boy\\b") to "gikapoy",
                    Regex("(?i)\\bgeeka boy\\b") to "gikapoy",
                    Regex("(?i)\\bget car boy\\b") to "gikapoy",
                    Regex("(?i)\\bnow leap on\\b") to "nalipong",
                    Regex("(?i)\\bnah lipong\\b") to "nalipong",
                    Regex("(?i)\\bon sound man knee\\b") to "unsaon man ni",
                    Regex("(?i)\\bon sound money\\b") to "unsaon man ni",
                    Regex("(?i)\\bman knee\\b") to "man ni"
                )
                for ((pattern, replacement) in phoneticReplacements) {
                    formatted = formatted.replace(pattern, replacement)
                }
            }

            // Capitalize first character
            formatted = formatted.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }

            // Capitalize English standalone 'i'
            formatted = formatted.replace(Regex("\\b[i]\\b"), "I")

            // Check if it already ends with punctuation
            val lastChar = formatted.lastOrNull()
            if (lastChar != null && lastChar !in listOf('.', '?', '!', ';')) {
                val lower = formatted.lowercase()
                val isQuestion = lower.startsWith("unsa") || lower.startsWith("ngano") ||
                        lower.startsWith("asa") || lower.startsWith("kanus-a") ||
                        lower.startsWith("pila") || lower.startsWith("tinuod ba") ||
                        lower.startsWith("pwede ba") || lower.startsWith("ano") ||
                        lower.startsWith("bakit") || lower.startsWith("kailan") ||
                        lower.startsWith("paano") || lower.startsWith("what") ||
                        lower.startsWith("why") || lower.startsWith("how") ||
                        lower.startsWith("where") || lower.startsWith("when") ||
                        lower.startsWith("is it") || lower.startsWith("can i") ||
                        lower.startsWith("should i") || lower.contains(" ba ") ||
                        lower.endsWith(" ba")

                formatted = if (isQuestion) {
                    "$formatted?"
                } else {
                    "$formatted."
                }
            }

            return formatted
        }
    }
}
