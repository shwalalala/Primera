package cit.edu.primera.feature.transcription.data

import cit.edu.primera.feature.transcription.domain.TranscriptionModel

interface TranscriptionRepository {
    suspend fun save(model: TranscriptionModel): Result<String>
}
