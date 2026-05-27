package com.example.primera.feature.transcription.data.repository

interface TranscriptionRepository {
    suspend fun saveTranscription(
        text: String,
        audioDurationMs: Long,
        confidence: Float,
        languageCode: String = "en-US"
    ): Result<Unit>
}
