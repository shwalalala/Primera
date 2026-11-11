package cit.edu.primera.feature.transcription.domain

import com.google.ai.client.generativeai.GenerativeModel

interface SymptomExtractor {
    suspend fun extract(input: String): List<String>
}

class KeywordSymptomExtractor : SymptomExtractor {
    private val symptomDictionary = mapOf(
        "headache" to listOf(
            "headache", "head pain", "pain in my head", "migraine",
            "labad", "naglabad", "labad sa ulo", "sakit sa ulo", "labad ulo", "sakit ulo", "sakit akong ulo"
        ),
        "dizziness" to listOf(
            "dizzy", "dizziness", "lightheaded", "feeling faint", "room is spinning",
            "nalipong", "kalipong", "lipong", "gikalipong", "nalulula"
        ),
        "nausea" to listOf(
            "nausea", "nauseous", "vomit", "vomiting", "feel like vomiting", "morning sickness",
            "kasukaon", "kasukahon", "nagsuka", "suka", "alibadbad", "kasuka-on"
        ),
        "fatigue" to listOf(
            "tired", "fatigue", "weak", "no energy", "exhausted", "sleepy",
            "kapoy", "gikapoy", "huyang", "way kusog", "walay kusog"
        ),
        "fever" to listOf(
            "fever", "high temperature", "chills", "hot body",
            "hilanat", "gihilanat", "init ang lawas", "lagnat"
        ),
        "cough" to listOf(
            "cough", "coughing", "dry cough",
            "ubo", "giubo", "nag-ubo", "nagubo"
        ),
        "shortness of breath" to listOf(
            "shortness of breath", "difficulty breathing", "hard to breathe",
            "lisod iginhawa", "hangak", "gahangak", "kalisod sa pagginhawa", "kapos sa paghinga"
        ),
        "chest pain" to listOf(
            "chest pain", "tight chest", "pain in my chest",
            "sakit sa dughan", "huot sa dughan", "gahoot ang dughan"
        ),
        "abdominal pain" to listOf(
            "stomach pain", "abdominal pain", "belly pain", "cramps", "cramping",
            "hilab", "naghilab", "sakit sa tiyan", "hilab sa tiyan", "sakit tiyan", "cramp sa tiyan", "sakit akong tiyan"
        ),
        "back pain" to listOf(
            "back pain", "lower back pain", "pain in my back", "aching back",
            "sakit sa likod", "panuhot", "sakit sa hawak", "ngulngol sa likod", "sakit akong likod"
        ),
        "swelling" to listOf(
            "swelling", "swollen", "swollen feet", "my feet are swollen", "edema",
            "hubag", "naghubag", "manas", "nanghubag"
        ),
        "heartburn" to listOf(
            "heartburn", "acid reflux", "burning in my chest",
            "almoranas", "asim sa tutunlan", "hapdos sa dughan"
        ),
        "spotting" to listOf(
            "spotting", "bleeding", "blood",
            "nagdugo", "dugo", "patak sa dugo"
        ),
        "insomnia" to listOf(
            "insomnia", "can't sleep", "trouble sleeping",
            "dili makatug", "di makatulog", "lisod ikatug", "lisud matulog"
        ),
        "constipation" to listOf(
            "constipation", "constipated", "hard stool",
            "gahi ang kalibang", "lisod malibang", "lisud kalibang"
        )
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
            The text may be spoken in English, Bisaya (Cebuano), Tagalog/Filipino, or a mix (Bislish/Taglish).
            Common Bisaya translations to recognize:
            - "labad sa ulo", "sakit sa ulo" -> headache
            - "nalipong", "kalipong" -> dizziness
            - "kasukaon", "nagsuka" -> nausea
            - "gikapoy", "kapoy" -> fatigue
            - "hilanat", "init ang lawas" -> fever
            - "ubo", "giubo" -> cough
            - "lisod iginhawa", "hangak" -> shortness of breath
            - "sakit sa dughan", "huot ang dughan" -> chest pain
            - "sakit sa tiyan", "hilab" -> abdominal pain
            - "sakit sa likod" -> back pain
            - "hubag", "naghubag" -> swelling
            - "hapdos sa dughan", "heartburn" -> heartburn
            - "nagdugo", "patak sa dugo" -> spotting
            - "di makatulog", "lisod ikatug" -> insomnia
            - "lisod malibang" -> constipation

            Translate any detected symptoms into standard medical English terms (e.g., "headache", "dizziness", "nausea", "fatigue", "fever", "cough", "shortness of breath", "chest pain", "abdominal pain", "back pain", "swelling", "heartburn", "spotting", "insomnia", "constipation").
            Return ONLY the names of the symptoms as a comma-separated list.
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
