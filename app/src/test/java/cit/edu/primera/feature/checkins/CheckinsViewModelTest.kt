package cit.edu.primera.feature.checkins

import cit.edu.primera.feature.checkins.data.CheckinLogDto
import cit.edu.primera.feature.checkins.data.CheckinUserDto
import cit.edu.primera.feature.checkins.data.CheckinsRepository
import cit.edu.primera.feature.checkins.ui.CheckinsViewModel
import cit.edu.primera.feature.transcription.domain.KeywordSymptomExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CheckinsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeCheckinsRepository : CheckinsRepository {
        private val customOptions = mutableMapOf<String, MutableSet<String>>()
        var lastSavedLog: CheckinLogDto? = null

        override fun observeLogs(): Flow<List<CheckinLogDto>> = emptyFlow()

        override fun observeUserWeight(): Flow<CheckinUserDto?> = emptyFlow()

        override suspend fun saveLog(log: CheckinLogDto): Result<Unit> {
            lastSavedLog = log
            return Result.success(Unit)
        }

        override suspend fun updateUserWeight(weightKg: Int): Result<Unit> = Result.success(Unit)

        override fun getCustomOptions(category: String): Set<String> {
            return customOptions[category]?.toSet() ?: emptySet()
        }

        override fun addCustomOption(category: String, label: String) {
            customOptions.getOrPut(category) { mutableSetOf() }.add(label)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun applyVoiceInputToCheckin_populatesNoteAndExtractsSymptoms() = runTest {
        val repository = FakeCheckinsRepository()
        val viewModel = CheckinsViewModel(
            repository = repository,
            symptomExtractor = KeywordSymptomExtractor()
        )

        viewModel.prepareNewCheckin()
        assertEquals("", viewModel.dailyState.value.note)

        val voiceText = "Sakit akong ulo ug gikapoy ko."
        viewModel.applyVoiceInputToCheckin(voiceText)

        // Note is populated immediately
        assertEquals("Voice input: $voiceText", viewModel.dailyState.value.note)

        // Let background symptom extraction finish
        advanceUntilIdle()

        assertEquals("Voice input: $voiceText", viewModel.dailyState.value.note)
        assertTrue(viewModel.dailyState.value.selectedSymptoms.contains("Aching Head"))
        assertTrue(viewModel.dailyState.value.selectedSymptoms.contains("Fatigue"))
    }

    @Test
    fun applyVoiceInputToCheckin_appendsToExistingNote() = runTest {
        val repository = FakeCheckinsRepository()
        val viewModel = CheckinsViewModel(
            repository = repository,
            symptomExtractor = KeywordSymptomExtractor()
        )

        viewModel.onNoteChange("Drank 2 glasses of milk.")
        viewModel.applyVoiceInputToCheckin("Sakit sa likod.")

        assertEquals("Drank 2 glasses of milk.\n\nVoice input: Sakit sa likod.", viewModel.dailyState.value.note)

        advanceUntilIdle()

        assertTrue(viewModel.dailyState.value.selectedSymptoms.contains("Back Pain"))
    }

    @Test
    fun onSaveCheckin_populatesMessageFromNote() = runTest {
        val repository = FakeCheckinsRepository()
        val viewModel = CheckinsViewModel(
            repository = repository,
            symptomExtractor = KeywordSymptomExtractor()
        )

        viewModel.onNoteChange("Voice input: Sakit akong tiyan.")
        viewModel.onSymptomToggle("Nausea")
        viewModel.onSaveCheckin()

        advanceUntilIdle()

        val saved = repository.lastSavedLog
        org.junit.Assert.assertNotNull(saved)
        assertEquals("Voice input: Sakit akong tiyan.", saved?.message)
        assertTrue(saved?.description?.contains("Symptoms: Nausea") == true)
        assertTrue(saved?.description?.contains("Voice input: Sakit akong tiyan.") == true)
    }
}
