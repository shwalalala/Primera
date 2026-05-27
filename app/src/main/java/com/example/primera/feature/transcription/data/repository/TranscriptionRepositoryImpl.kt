package com.example.primera.feature.transcription.data.repository

import com.example.primera.feature.transcription.data.datasource.TranscriptionDataSource
import com.example.primera.feature.transcription.data.dto.toDto
import com.example.primera.feature.transcription.domain.model.TranscriptionModel

class TranscriptionRepositoryImpl(
    private val dataSource: TranscriptionDataSource
) : TranscriptionRepository {
    override suspend fun save(model: TranscriptionModel): Result<String> {
        return dataSource.saveTranscription(model.toDto())
    }
}
