package com.example.primera.feature.transcription.domain

import com.google.ai.client.generativeai.GenerativeModel

interface SymptomExtractor {
    suspend fun extract(input: String): List<String>
}

class KeywordSymptomExtractor : SymptomExtractor {
    private val symptomDictionary = mapOf(
        "headache" to listOf("headache", "head pain", "pain in my head", "migraine"),
        "dizziness" to listOf("dizzy", "dizziness", "lightheaded", "feeling faint", "room is spinning"),
        "nausea" to listOf("nausea", "nauseous", "vomit", "vomiting", "feel like vomiting", "morning sickness"),
        "fatigue" to listOf("tired", "fatigue", "weak", "no energy", "exhausted", "sleepy"),
        "fever" to listOf("fever", "high temperature", "chills", "hot body"),
        "cough" to listOf("cough", "coughing", "dry cough"),
        "shortness of breath" to listOf("shortness of breath", "difficulty breathing", "hard to breathe"),
        "chest pain" to listOf("chest pain", "tight chest", "pain in my chest"),
        "abdominal pain" to listOf("stomach pain", "abdominal pain", "belly pain", "cramps", "cramping"),
        "back pain" to listOf("back pain", "lower back pain", "pain in my back", "aching back"),
        "swelling" to listOf("swelling", "swollen", "swollen feet", "my feet are swollen", "edema"),
        "heartburn" to listOf("heartburn", "acid reflux", "burning in my chest"),
        "spotting" to listOf("spotting", "bleeding", "blood"),
        "insomnia" to listOf("insomnia", "can't sleep", "trouble sleeping"),
        "constipation" to listOf("constipation", "constipated", "hard stool")
    )

    override suspend fun extract(input: String): List<String> {
        val normalizedInput = input
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return symptomDictionary
            .filter { (_, keywords) ->
                keywords.any { keyword ->
                    Regex("\\b${Regex.escape(keyword)}\\b")
                        .containsMatchIn(normalizedInput)
                }
            }
            .map { (symptom, _) -> symptom }
            .distinct()
    }
}

class GeminiSymptomExtractor(
    private val generativeModel: GenerativeModel
) : SymptomExtractor {
    override suspend fun extract(input: String): List<String> {
        val prompt = """
            Extract a list of medical symptoms from the following text. 
            Return only the names of the symptoms as a comma-separated list.
            If no symptoms are found, return "None".
            
            Text: "$input"
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text ?: ""
            if (text.trim().equals("None", ignoreCase = true)) {
                emptyList()
            } else {
                text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeminiSymptomExtractor", "Error extracting symptoms", e)
            emptyList()
        }
    }
}
