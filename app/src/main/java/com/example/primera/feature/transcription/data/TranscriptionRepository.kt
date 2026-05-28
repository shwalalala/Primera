package com.example.primera.feature.transcription.data

import com.example.primera.feature.transcription.domain.TranscriptionModel

interface TranscriptionRepository {
    suspend fun save(model: TranscriptionModel): Result<String>
}
