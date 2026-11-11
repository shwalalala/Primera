package cit.edu.primera.feature.transcription

import cit.edu.primera.feature.transcription.data.SpeechRecognitionManager
import cit.edu.primera.feature.transcription.data.TranscriptionRepository
import cit.edu.primera.feature.transcription.domain.GeminiVoiceTextCorrector
import cit.edu.primera.feature.transcription.domain.KeywordSymptomExtractor
import cit.edu.primera.feature.transcription.domain.TranscriptionModel
import cit.edu.primera.feature.transcription.domain.VoiceCorrectionResult
import cit.edu.primera.feature.transcription.domain.VoiceLanguage
import cit.edu.primera.feature.transcription.domain.VoiceTextCorrector
import cit.edu.primera.feature.transcription.ui.TranscriptionUiState
import cit.edu.primera.feature.transcription.ui.TranscriptionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceLoggingTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun voiceLanguage_defaultIsBisaya() {
        assertEquals("ceb-PH", VoiceLanguage.BISAYA.code)
        assertEquals("Bisaya (Cebuano)", VoiceLanguage.BISAYA.displayName)
        assertEquals("ceb-PH", VoiceLanguage.BISAYA.getEffectiveLanguageTag())
    }

    @Test
    fun fallbackPunctuation_formatsBisayaStatements() {
        val input = "gikapoy ko unya labad akong ulo"
        val output = GeminiVoiceTextCorrector.fallbackPunctuate(input, isBisaya = true)
        assertEquals("Gikapoy ko unya labad akong ulo.", output)
    }

    @Test
    fun fallbackPunctuation_formatsBisayaQuestions() {
        val input = "unsaon man ni sakit akong tiyan"
        val output = GeminiVoiceTextCorrector.fallbackPunctuate(input, isBisaya = true)
        assertEquals("Unsaon man ni sakit akong tiyan?", output)
    }

    @Test
    fun fallbackPunctuation_recoversCorruptedPhoneticsWhenBisaya() {
        val input = "suck it a call tea and"
        val output = GeminiVoiceTextCorrector.fallbackPunctuate(input, isBisaya = true)
        assertEquals("Sakit akong tiyan.", output)
    }

    @Test
    fun fallbackPunctuation_formatsEnglishStatements() {
        val input = "i have a headache and i feel dizzy"
        val output = GeminiVoiceTextCorrector.fallbackPunctuate(input, isBisaya = false)
        assertEquals("I have a headache and I feel dizzy.", output)
    }

    @Test
    fun fallbackPunctuation_formatsEnglishQuestions() {
        val input = "what should i do if my feet are swollen"
        val output = GeminiVoiceTextCorrector.fallbackPunctuate(input, isBisaya = false)
        assertEquals("What should I do if my feet are swollen?", output)
    }

    @Test
    fun keywordSymptomExtractor_recognizesBisayaSymptoms() = runTest {
        val extractor = KeywordSymptomExtractor()
        val text = "Nalipong ko ganina unya labad kaayo akong ulo ug kasukaon ko"
        val symptoms = extractor.extract(text)

        assertTrue(symptoms.contains("dizziness"))
        assertTrue(symptoms.contains("headache"))
        assertTrue(symptoms.contains("nausea"))
    }

    @Test
    fun keywordSymptomExtractor_recognizesBisayaPainAndFatigue() = runTest {
        val extractor = KeywordSymptomExtractor()
        val text = "Hilab kaayo akong tiyan ug gikapoy ko unya naghubag akong tiil"
        val symptoms = extractor.extract(text)

        assertTrue(symptoms.contains("abdominal pain"))
        assertTrue(symptoms.contains("fatigue"))
        assertTrue(symptoms.contains("swelling"))
    }

    @Test
    fun transcriptionViewModel_processesVoiceResultWithAiCorrection() = runTest {
        val fakeRepo = object : TranscriptionRepository {
            override suspend fun save(model: TranscriptionModel): Result<String> = Result.success("test-id")
        }

        var passedAlternatives: List<String> = emptyList()

        val fakeCorrector = object : VoiceTextCorrector {
            override suspend fun correctAndPunctuate(
                rawText: String,
                alternatives: List<String>,
                languageCode: String
            ): VoiceCorrectionResult {
                passedAlternatives = alternatives
                return VoiceCorrectionResult(
                    text = "Sakit akong tiyan.",
                    isAiCorrected = true,
                    originalText = rawText
                )
            }
        }

        val fakeSpeechManager = object : SpeechRecognitionManager() {
            override fun start(languageCode: String, callback: SpeechRecognitionCallback) {
                // No-op for test
            }
        }

        val viewModel = TranscriptionViewModel(
            repository = fakeRepo,
            symptomExtractor = KeywordSymptomExtractor(),
            voiceTextCorrector = fakeCorrector,
            speechManager = fakeSpeechManager
        )

        assertEquals(VoiceLanguage.BISAYA, viewModel.selectedLanguage.value)

        // Simulate voice recognition result arriving with alternatives
        viewModel.onResult(
            text = "suck it a call tea and",
            confidence = 0.88f,
            durationMs = 3200L,
            alternatives = listOf("suck it a call tea and", "sakit akong tiyan", "suck it a kong tiyan")
        )
        advanceUntilIdle()

        assertEquals("Sakit akong tiyan.", viewModel.transcribedText.value)
        assertEquals("suck it a call tea and", viewModel.rawTranscribedText.value)
        assertTrue(viewModel.isAiCorrected.value)
        assertEquals(3, passedAlternatives.size)

        val uiState = viewModel.uiState.value
        assertTrue(uiState is TranscriptionUiState.Success)
        val success = uiState as TranscriptionUiState.Success
        assertEquals("Sakit akong tiyan.", success.text)
        assertTrue(success.isAiCorrected)

        // Test revert to raw text
        viewModel.revertToRawText()
        assertEquals("suck it a call tea and", viewModel.transcribedText.value)
        assertFalse(viewModel.isAiCorrected.value)
    }
}
