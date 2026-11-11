package cit.edu.primera.feature.transcription.ui

sealed class TranscriptionUiState {
    object Idle : TranscriptionUiState()
    object RequestingPermission : TranscriptionUiState()
    object Listening : TranscriptionUiState()
    object Processing : TranscriptionUiState()
    data class AnalyzingWithAi(
        val rawText: String,
        val durationMs: Long
    ) : TranscriptionUiState()
    data class Success(
        val text: String,
        val confidence: Float,
        val durationMs: Long,
        val rawSpokenText: String = text,
        val isAiCorrected: Boolean = false
    ) : TranscriptionUiState()
    object Uploading : TranscriptionUiState()
    object Uploaded : TranscriptionUiState()
    data class Error(
        val message: String,
        val isRetryable: Boolean = true
    ) : TranscriptionUiState()
}
