package cit.edu.primera.feature.dashboard.data

import android.util.Log
import cit.edu.primera.feature.dashboard.domain.ArticleInfo
import cit.edu.primera.feature.dashboard.domain.BabyDevelopmentData
import cit.edu.primera.feature.dashboard.domain.WeeklyInfo
import com.google.ai.client.generativeai.GenerativeModel
import org.json.JSONObject

class GeminiContentRepository(
    private val generativeModel: GenerativeModel?
) {
    suspend fun getWeeklyContent(week: Int): WeeklyInfo {
        if (generativeModel == null) {
            return BabyDevelopmentData.getInfoForWeek(week)
        }

        return try {
            val prompt = """
                You are an expert maternal health and obstetrics assistant. Provide medically accurate, actionable weekly pregnancy information and educational articles for Week $week of pregnancy.
                Format the response strictly as valid JSON with the following structure:
                {
                  "milestones": ["Key milestone 1", "Key milestone 2"],
                  "symptoms": ["Common symptom 1", "Common symptom 2"],
                  "articles": [
                    {
                      "title": "Educational Article Title 1",
                      "description": "Comprehensive explanation including what to expect during week $week, healthy habits, nutrition tips, and when to contact a healthcare provider."
                    },
                    {
                      "title": "Educational Article Title 2",
                      "description": "Practical guidance on body changes, fetal development updates, and wellness advice for week $week."
                    }
                  ]
                }
                Do not wrap the response in markdown code blocks, return ONLY raw JSON text.
            """.trimIndent()

            val response = generativeModel.generateContent(prompt)
            val rawText = response.text?.trim() ?: ""
            val jsonText = rawText
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            if (jsonText.isNotBlank()) {
                val parsed = parseWeeklyInfoFromJson(jsonText)
                if (parsed != null && parsed.milestones.isNotEmpty()) {
                    return parsed
                }
            }
            BabyDevelopmentData.getInfoForWeek(week)
        } catch (e: Exception) {
            Log.e("GeminiContentRepository", "Error fetching Gemini weekly content for week $week, using fallback", e)
            BabyDevelopmentData.getInfoForWeek(week)
        }
    }

    private fun parseWeeklyInfoFromJson(jsonString: String): WeeklyInfo? {
        return try {
            val jsonObject = JSONObject(jsonString)
            val milestonesArray = jsonObject.optJSONArray("milestones")
            val symptomsArray = jsonObject.optJSONArray("symptoms")
            val articlesArray = jsonObject.optJSONArray("articles")

            val milestones = mutableListOf<String>()
            if (milestonesArray != null) {
                for (i in 0 until milestonesArray.length()) {
                    milestones.add(milestonesArray.getString(i))
                }
            }

            val symptoms = mutableListOf<String>()
            if (symptomsArray != null) {
                for (i in 0 until symptomsArray.length()) {
                    symptoms.add(symptomsArray.getString(i))
                }
            }

            val articles = mutableListOf<ArticleInfo>()
            if (articlesArray != null) {
                for (i in 0 until articlesArray.length()) {
                    val artObj = articlesArray.getJSONObject(i)
                    articles.add(
                        ArticleInfo(
                            title = artObj.optString("title", "Weekly Guidance"),
                            description = artObj.optString("description", "")
                        )
                    )
                }
            }

            WeeklyInfo(
                milestones = milestones,
                symptoms = symptoms,
                articles = articles
            )
        } catch (e: Exception) {
            Log.e("GeminiContentRepository", "JSON parsing failed for Gemini output", e)
            null
        }
    }
}
