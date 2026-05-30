package com.example.primera.feature.transcription.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class SpeechRecognitionManager(
    private val context: Context
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var startTime: Long = 0
    private var callback: SpeechRecognitionCallback? = null

    interface SpeechRecognitionCallback {
        fun onListeningStarted()
        fun onResult(text: String, confidence: Float, durationMs: Long)
        fun onError(errorCode: Int)
    }

    fun start(callback: SpeechRecognitionCallback) {
        this.callback = callback

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback.onError(SpeechRecognizer.ERROR_CLIENT)
            return
        }

        cleanup()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@SpeechRecognitionManager)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
        startTime = System.currentTimeMillis()
    }

    fun stop() {
        speechRecognizer?.stopListening()
    }

    fun cleanup() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // RecognitionListener implementation
    override fun onReadyForSpeech(params: Bundle?) {
        callback?.onListeningStarted()
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        callback?.onError(error)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        val text = matches?.getOrNull(0) ?: ""
        val confidence = confidences?.getOrNull(0) ?: 0.0f
        val duration = System.currentTimeMillis() - startTime

        callback?.onResult(text, confidence, duration)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Partial results could be used for real-time UI updates, but for now we focus on final results
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
