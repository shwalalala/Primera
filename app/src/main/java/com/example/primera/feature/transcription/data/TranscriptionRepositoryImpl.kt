package com.example.primera.feature.transcription.data

import com.example.primera.feature.transcription.domain.TranscriptionModel

class TranscriptionRepositoryImpl(
    private val dataSource: TranscriptionDataSource
) : TranscriptionRepository {
    override suspend fun save(model: TranscriptionModel): Result<String> {
        return dataSource.saveTranscription(model.toDto())
    }
}
