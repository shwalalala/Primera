package com.example.primera.feature.transcription.ui

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.feature.transcription.data.SpeechRecognitionManager
import com.example.primera.feature.transcription.data.repository.TranscriptionRepository
import com.example.primera.feature.transcription.domain.model.TranscriptionModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranscriptionViewModel(
    private val repository: TranscriptionRepository,
    private val speechManager: SpeechRecognitionManager,
    private val auth: FirebaseAuth
) : ViewModel(), SpeechRecognitionManager.SpeechRecognitionCallback {

    private val _uiState = MutableStateFlow<TranscriptionUiState>(TranscriptionUiState.Idle)
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    fun startListening() {
        _uiState.value = TranscriptionUiState.Listening
        speechManager.start(this)
    }

    fun stopListening() {
        _uiState.value = TranscriptionUiState.Processing
        speechManager.stop()
    }

    fun onTextEdited(text: String) {
        _transcribedText.value = text
    }

    fun saveTranscription() {
        val currentState = _uiState.value
        if (currentState is TranscriptionUiState.Success) {
            viewModelScope.launch {
                _uiState.value = TranscriptionUiState.Uploading
                
                val model = TranscriptionModel(
                    transcribedText = _transcribedText.value,
                    audioDuration = currentState.durationMs,
                    confidenceScore = currentState.confidence,
                    languageCode = "en-US", // Default or detect
                    deviceModel = Build.MODEL,
                    appVersion = "1.0", // Hardcoded as per build.gradle for now
                    userId = auth.currentUser?.uid ?: "",
                    timestamp = System.currentTimeMillis()
                )

                repository.save(model).fold(
                    onSuccess = {
                        _uiState.value = TranscriptionUiState.Uploaded
                    },
                    onFailure = { error ->
                        _uiState.value = TranscriptionUiState.Error(
                            message = error.message ?: "Failed to upload transcription",
                            isRetryable = true
                        )
                    }
                )
            }
        }
    }

    fun dismissError() {
        _uiState.value = TranscriptionUiState.Idle
    }

    // SpeechRecognitionCallback implementation
    override fun onListeningStarted() {
        _uiState.value = TranscriptionUiState.Listening
    }

    override fun onResult(text: String, confidence: Float, durationMs: Long) {
        _transcribedText.value = text
        _uiState.value = TranscriptionUiState.Success(text, confidence, durationMs)
    }

    override fun onError(errorCode: Int) {
        val errorMessage = when (errorCode) {
            1 -> "Network timeout. Please try again."
            2 -> "Network error. Please check your internet connection."
            3 -> "Audio recording error. Please check your microphone."
            4 -> "Server error. Please try again later."
            5 -> "Client error. Please try again."
            6 -> "Speech timeout. No speech detected."
            7 -> "No recognition result found."
            8 -> "Recognition service busy. Please wait."
            9 -> "Insufficient permissions."
            else -> "Speech recognition error: $errorCode"
        }
        _uiState.value = TranscriptionUiState.Error(
            message = errorMessage,
            isRetryable = true
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.cleanup()
    }
}
