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
            )

            val encryptedData = hashMapOf<String, Any?>(
                "transcribedText" to CryptoManager.encrypt(dto.transcribedText),
                "detectedSymptoms" to CryptoManager.encryptStringList(dto.detectedSymptoms),
                "hasWarning" to (warningMessage != null),
                "warningMessage" to CryptoManager.encrypt(warningMessage),
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

        val recentLogs = firestore.collection("transcriptions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .get()
            .await()
            .documents
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

        return repeatedSymptom?.let { symptom ->
            "You have logged $symptom repeatedly within the last 3 days. Please monitor your condition and consider consulting a healthcare professional if it continues or worsens."
        }
    }
}