package cit.edu.primera.feature.transcription.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

open class SpeechRecognitionManager(
    private val context: Context? = null
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var startTime: Long = 0
    private var callback: SpeechRecognitionCallback? = null

    interface SpeechRecognitionCallback {
        fun onListeningStarted()
        fun onResult(text: String, confidence: Float, durationMs: Long) {}
        fun onResult(
            text: String,
            confidence: Float,
            durationMs: Long,
            alternatives: List<String>
        ) {
            onResult(text, confidence, durationMs)
        }
        fun onError(errorCode: Int)
    }

    open fun start(
        languageCode: String = "ceb-PH",
        callback: SpeechRecognitionCallback
    ) {
        this.callback = callback

        val currentContext = context
        if (currentContext == null || !SpeechRecognizer.isRecognitionAvailable(currentContext)) {
            callback.onError(SpeechRecognizer.ERROR_CLIENT)
            return
        }

        cleanup()

        speechRecognizer = createPreferredSpeechRecognizer(currentContext).apply {
            setRecognitionListener(this@SpeechRecognitionManager)
        }

        val effectiveLanguage = if (languageCode.isBlank() || languageCode.equals("auto", ignoreCase = true)) {
            Locale.getDefault().toLanguageTag()
        } else {
            languageCode
        }

        val isBisaya = effectiveLanguage.startsWith("ceb", ignoreCase = true)
        val isFilipino = effectiveLanguage.startsWith("fil", ignoreCase = true)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, currentContext.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, effectiveLanguage)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, effectiveLanguage)

            // Prefer online recognition since cloud models natively support ceb-PH
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)

            if (isBisaya) {
                // Strictly exclude English to prevent the engine from biasing toward English acoustics
                putExtra(
                    "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                    arrayOf("ceb-PH", "ceb", "fil-PH")
                )
            } else if (isFilipino) {
                putExtra(
                    "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                    arrayOf("fil-PH", "tl-PH")
                )
            } else {
                putExtra(
                    "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                    arrayOf(effectiveLanguage, "en-US", "en-PH")
                )
            }

            putExtra("android.speech.extra.ENABLE_FORMATTING", true)
            putExtra("android.speech.extra.DICTATION_MODE", true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognizer?.startListening(intent)
            startTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e("SpeechRecognitionMgr", "Error starting speech recognition", e)
            callback.onError(SpeechRecognizer.ERROR_CLIENT)
        }
    }

    private fun createPreferredSpeechRecognizer(context: Context): SpeechRecognizer {
        val googleComponent = ComponentName(
            "com.google.android.googlequicksearchbox",
            "com.google.android.voicesearch.serviceapi.GoogleRecognitionService"
        )

        return try {
            val serviceIntent = Intent("android.speech.RecognitionService").apply {
                component = googleComponent
            }
            val resolved = context.packageManager.resolveService(serviceIntent, 0)
            if (resolved != null) {
                SpeechRecognizer.createSpeechRecognizer(context, googleComponent)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        } catch (e: Exception) {
            Log.w("SpeechRecognitionMgr", "Fallback to default SpeechRecognizer", e)
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    open fun start(callback: SpeechRecognitionCallback) {
        start(languageCode = "ceb-PH", callback = callback)
    }

    open fun stop() {
        speechRecognizer?.stopListening()
    }

    open fun cleanup() {
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
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()
        val confidences = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        val text = matches.firstOrNull() ?: ""
        val confidence = confidences?.getOrNull(0) ?: 0.0f
        val duration = System.currentTimeMillis() - startTime

        callback?.onResult(text, confidence, duration, matches)
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Partial results could be used for real-time UI updates
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
