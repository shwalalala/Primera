package com.example.primera.feature.transcription.ui

sealed class TranscriptionUiState {
    object Idle : TranscriptionUiState()
    object RequestingPermission : TranscriptionUiState()
    object Listening : TranscriptionUiState()
    object Processing : TranscriptionUiState()
    data class Success(
        val text: String,
        val confidence: Float,
        val durationMs: Long
    ) : TranscriptionUiState()
    object Uploading : TranscriptionUiState()
    object Uploaded : TranscriptionUiState()
    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : TranscriptionUiState()
}
