package com.example.primera.feature.transcription.data

import com.google.firebase.auth.FirebaseAuth
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

            val transcriptionWithUser = dto.copy(
                userId = userId,
                hasWarning = warningMessage != null,
                warningMessage = warningMessage
            )

            val documentReference = firestore.collection("transcriptions")
                .add(transcriptionWithUser)
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
                document.toObject(TranscriptionDto::class.java)
            }

        val previousSymptoms = recentLogs
            .flatMap { it.detectedSymptoms }
            .toSet()

        val repeatedSymptom = currentSymptoms.firstOrNull { symptom ->
            previousSymptoms.contains(symptom)
        }

        return repeatedSymptom?.let { symptom ->
            "You have logged $symptom repeatedly within the last 3 days. Please monitor your condition and consider consulting a healthcare professional if it continues or worsens."
        }
    }
}