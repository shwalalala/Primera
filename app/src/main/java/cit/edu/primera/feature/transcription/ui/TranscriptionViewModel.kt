package cit.edu.primera.feature.transcription.ui

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cit.edu.primera.feature.transcription.data.SpeechRecognitionManager
import cit.edu.primera.feature.transcription.data.TranscriptionRepository
import cit.edu.primera.feature.transcription.domain.SymptomExtractor
import cit.edu.primera.feature.transcription.domain.TranscriptionModel
import cit.edu.primera.feature.transcription.domain.VoiceLanguage
import cit.edu.primera.feature.transcription.domain.VoiceTextCorrector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranscriptionViewModel(
    private val repository: TranscriptionRepository,
    private val symptomExtractor: SymptomExtractor,
    private val voiceTextCorrector: VoiceTextCorrector,
    private val speechManager: SpeechRecognitionManager
) : ViewModel(), SpeechRecognitionManager.SpeechRecognitionCallback {

    private val _uiState = MutableStateFlow<TranscriptionUiState>(TranscriptionUiState.Idle)
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private val _transcribedText = MutableStateFlow("")
    val transcribedText: StateFlow<String> = _transcribedText.asStateFlow()

    private val _rawTranscribedText = MutableStateFlow("")
    val rawTranscribedText: StateFlow<String> = _rawTranscribedText.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(VoiceLanguage.BISAYA)
    val selectedLanguage: StateFlow<VoiceLanguage> = _selectedLanguage.asStateFlow()

    private val _isAiCorrected = MutableStateFlow(false)
    val isAiCorrected: StateFlow<Boolean> = _isAiCorrected.asStateFlow()

    private var lastDurationMs: Long = 0L
    private var lastConfidence: Float = 0.0f
    private var lastAlternatives: List<String> = emptyList()

    fun selectLanguage(language: VoiceLanguage) {
        _selectedLanguage.value = language
    }

    fun startListening() {
        _uiState.value = TranscriptionUiState.Listening
        speechManager.start(
            languageCode = _selectedLanguage.value.getEffectiveLanguageTag(),
            callback = this
        )
    }

    fun stopListening() {
        _uiState.value = TranscriptionUiState.Processing
        speechManager.stop()
    }

    fun onTextEdited(text: String) {
        _transcribedText.value = text
    }

    fun revertToRawText() {
        val raw = _rawTranscribedText.value
        if (raw.isNotBlank()) {
            _transcribedText.value = raw
            _isAiCorrected.value = false
            _uiState.value = TranscriptionUiState.Success(
                text = raw,
                confidence = lastConfidence,
                durationMs = lastDurationMs,
                rawSpokenText = raw,
                isAiCorrected = false
            )
        }
    }

    fun reformatWithAi() {
        val currentText = _transcribedText.value
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.value = TranscriptionUiState.AnalyzingWithAi(
                rawText = currentText,
                durationMs = lastDurationMs
            )

            val result = voiceTextCorrector.correctAndPunctuate(
                rawText = currentText,
                alternatives = lastAlternatives,
                languageCode = _selectedLanguage.value.getEffectiveLanguageTag()
            )

            _transcribedText.value = result.text
            _isAiCorrected.value = result.isAiCorrected

            _uiState.value = TranscriptionUiState.Success(
                text = result.text,
                confidence = lastConfidence,
                durationMs = lastDurationMs,
                rawSpokenText = _rawTranscribedText.value.ifBlank { currentText },
                isAiCorrected = result.isAiCorrected
            )
        }
    }

    fun saveTranscription() {
        val currentState = _uiState.value

        if (currentState is TranscriptionUiState.Success) {
            viewModelScope.launch {
                _uiState.value = TranscriptionUiState.Uploading

                val detectedSymptoms = symptomExtractor.extract(_transcribedText.value)

                val model = TranscriptionModel(
                    transcribedText = _transcribedText.value,
                    detectedSymptoms = detectedSymptoms,
                    hasWarning = false,
                    warningMessage = null,
                    audioDuration = currentState.durationMs,
                    confidenceScore = currentState.confidence,
                    languageCode = _selectedLanguage.value.getEffectiveLanguageTag(),
                    deviceModel = Build.MODEL,
                    appVersion = "1.0",
                    userId = "",
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

    override fun onListeningStarted() {
        _uiState.value = TranscriptionUiState.Listening
    }

    override fun onResult(text: String, confidence: Float, durationMs: Long) {
        onResult(text, confidence, durationMs, emptyList())
    }

    override fun onResult(
        text: String,
        confidence: Float,
        durationMs: Long,
        alternatives: List<String>
    ) {
        lastConfidence = confidence
        lastDurationMs = durationMs
        lastAlternatives = alternatives
        _rawTranscribedText.value = text

        // Send to AI for punctuation, phonetic recovery, and correction before displaying
        viewModelScope.launch {
            _uiState.value = TranscriptionUiState.AnalyzingWithAi(
                rawText = text,
                durationMs = durationMs
            )

            val correction = voiceTextCorrector.correctAndPunctuate(
                rawText = text,
                alternatives = alternatives,
                languageCode = _selectedLanguage.value.getEffectiveLanguageTag()
            )

            _transcribedText.value = correction.text
            _isAiCorrected.value = correction.isAiCorrected

            _uiState.value = TranscriptionUiState.Success(
                text = correction.text,
                confidence = confidence,
                durationMs = durationMs,
                rawSpokenText = text,
                isAiCorrected = correction.isAiCorrected
            )
        }
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
        speechManager.cleanup()
    }
}
