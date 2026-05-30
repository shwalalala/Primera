package com.example.primera.feature.transcription.domain

object SymptomExtractor {

    private val symptomDictionary = mapOf(
        "headache" to listOf(
            "headache", "head pain", "pain in my head", "migraine"
        ),
        "dizziness" to listOf(
            "dizzy", "dizziness", "lightheaded", "feeling faint",
            "room is spinning"
        ),
        "nausea" to listOf(
            "nausea", "nauseous", "vomit", "vomiting", "feel like vomiting"
        ),
        "fatigue" to listOf(
            "tired", "fatigue", "weak", "no energy", "exhausted"
        ),
        "fever" to listOf(
            "fever", "high temperature", "chills", "hot body"
        ),
        "cough" to listOf(
            "cough", "coughing", "dry cough"
        ),
        "shortness of breath" to listOf(
            "shortness of breath", "difficulty breathing", "hard to breathe"
        ),
        "chest pain" to listOf(
            "chest pain", "tight chest", "pain in my chest"
        ),
        "abdominal pain" to listOf(
            "stomach pain", "abdominal pain", "belly pain", "cramps"
        ),
        "back pain" to listOf(
            "back pain", "lower back pain", "pain in my back"
        ),
        "swelling" to listOf(
            "swelling", "swollen", "swollen feet", "my feet are swollen"
        )
    )

    fun extract(input: String): List<String> {
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