package com.example.primera.feature.transcription.data.repository

import com.example.primera.feature.auth.data.datasource.AuthDataSource
import com.example.primera.feature.transcription.data.datasource.TranscriptionDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val transcriptionDataSource: TranscriptionDataSource,
    private val authDataSource: AuthDataSource
) : TranscriptionRepository {

    override suspend fun saveTranscription(
        text: String,
        audioDurationMs: Long,
        confidence: Float,
        languageCode: String
    ): Result<Unit> {
        return try {
            val userId = authDataSource.getCurrentUserId() ?: throw Exception("User not authenticated")
            transcriptionDataSource.saveTranscription(
                userId = userId,
                text = text,
                audioDurationMs = audioDurationMs,
                confidence = confidence,
                languageCode = languageCode,
                deviceInfo = android.os.Build.MODEL,
                version = "1.0.0"
            )
            // Note: We might want to log activity here too, but that would require ActivityLogRepository
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
