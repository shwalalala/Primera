package com.example.primera.feature.transcription.data.repository

import com.example.primera.feature.transcription.domain.model.TranscriptionModel

interface TranscriptionRepository {
    suspend fun save(model: TranscriptionModel): Result<String>
}
