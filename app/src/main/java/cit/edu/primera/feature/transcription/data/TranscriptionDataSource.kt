package cit.edu.primera.feature.transcription.data

import cit.edu.primera.core.util.CryptoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class TranscriptionDataSource(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun saveTranscription(dto: TranscriptionDto): Result<String> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val warningMessage = detectThreeDaySymptomPattern(
                userId = userId,
                currentSymptoms = dto.detectedSymptoms
            ) ?: detectSymptomWarning(dto.detectedSymptoms)
              ?: dto.warningMessage

            val encryptedWarning = CryptoManager.encrypt(warningMessage)

            val encryptedData = hashMapOf<String, Any?>(
                "transcribedText" to CryptoManager.encrypt(dto.transcribedText),
                "message" to CryptoManager.encrypt(dto.transcribedText),
                "detectedSymptoms" to CryptoManager.encryptStringList(dto.detectedSymptoms),
                "hasWarning" to (warningMessage != null || dto.hasWarning),
                "warningMessage" to encryptedWarning,
                "warning" to encryptedWarning,
                "audioDuration" to dto.audioDuration,
                "confidenceScore" to dto.confidenceScore,
                "languageCode" to dto.languageCode,
                "deviceModel" to dto.deviceModel,
                "appVersion" to dto.appVersion,
                "userId" to userId,
                "timestamp" to FieldValue.serverTimestamp()
            )

            val documentReference = firestore.collection("transcriptions")
                .add(encryptedData)
                .await()

            Result.success(documentReference.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun detectThreeDaySymptomPattern(
        userId: String,
        currentSymptoms: List<String>
    ): String? {
        if (currentSymptoms.isEmpty()) return null

        val startDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -2)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return try {
            val recentLogs = firestore.collection("transcriptions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .filter { document ->
                    val ts = document.getTimestamp("timestamp")?.toDate()
                    ts != null && !ts.before(startDate)
                }
                .mapNotNull { document ->
                    val rawSymptoms = document.get("detectedSymptoms")
                    CryptoManager.decryptStringList(rawSymptoms)
                }

            val previousSymptoms = recentLogs
                .flatMap { it }
                .toSet()

            val repeatedSymptom = currentSymptoms.firstOrNull { symptom ->
                previousSymptoms.contains(symptom)
            }

            repeatedSymptom?.let { symptom ->
                "You have logged $symptom repeatedly within the last 3 days. Please monitor your condition and consider consulting a healthcare professional if it continues or worsens."
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun detectSymptomWarning(symptoms: List<String>): String? {
        if (symptoms.isEmpty()) return null

        val critical = symptoms.firstOrNull { s ->
            val lower = s.lowercase().trim()
            lower.contains("bleeding") || lower.contains("spotting") ||
                    lower.contains("chest pain") || lower.contains("shortness of breath") ||
                    lower.contains("fever") || lower.contains("headache") ||
                    lower.contains("aching head") || lower.contains("dizziness") ||
                    lower.contains("abdominal pain") || lower.contains("swelling")
        }

        return if (critical != null) {
            "Attention: You reported $critical. Monitor this symptom closely, rest, stay hydrated, and consult your healthcare provider if it persists or worsens."
        } else {
            val symptomText = symptoms.joinToString(", ")
            "Monitor your reported symptoms ($symptomText) closely. Prioritize rest and hydration, and consult your doctor if they continue or worsen."
        }
    }
}