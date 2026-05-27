package com.example.primera.feature.transcription.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveTranscription(
        userId: String,
        text: String,
        audioDurationMs: Long,
        confidence: Float,
        languageCode: String,
        deviceInfo: String,
        version: String
    ) {
        val transcriptionData = hashMapOf(
            "userId" to userId,
            "text" to text,
            "timestamp" to Date(),
            "verboseMetadata" to hashMapOf(
                "audioDurationMs" to audioDurationMs,
                "confidenceScore" to confidence,
                "languageCode" to languageCode,
                "deviceInfo" to deviceInfo,
                "version" to version
            )
        )
        firestore.collection("transcriptions").add(transcriptionData).await()
    }
}
