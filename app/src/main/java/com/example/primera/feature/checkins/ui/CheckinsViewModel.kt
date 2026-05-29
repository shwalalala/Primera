package com.example.primera.feature.checkins.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.theme.LogBaby
import com.example.primera.core.theme.LogNutrition
import com.example.primera.core.theme.LogOther
import com.example.primera.core.theme.LogPain
import com.example.primera.feature.checkins.data.CheckinLogDto
import com.example.primera.feature.checkins.data.CheckinsRepository
import com.example.primera.feature.dashboard.ui.DashboardLogUiItem
import com.example.primera.feature.dashboard.ui.DashboardWeekDayItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CheckinsViewModel(
    private val repository: CheckinsRepository
) : ViewModel() {

    private val _overviewState = MutableStateFlow(CheckinsOverviewUiState())
    val overviewState: StateFlow<CheckinsOverviewUiState> = _overviewState.asStateFlow()

    private val _dailyState = MutableStateFlow(DailyCheckinUiState())
    val dailyState: StateFlow<DailyCheckinUiState> = _dailyState.asStateFlow()

    private val defaultSymptoms = listOf(
        CheckinOption("Aching Head", "🤕", "Symptom"),
        CheckinOption("Nausea", "🤢", "Symptom"),
        CheckinOption("Back Pain", "😣", "Symptom"),
        CheckinOption("Fatigue", "😴", "Symptom")
    )

    private val defaultMoods = listOf(
        CheckinOption("Normal", "😊", "Mood"),
        CheckinOption("Angry", "😡", "Mood"),
        CheckinOption("Happy", "😁", "Mood"),
        CheckinOption("Sad", "😢", "Mood")
    )

    private val defaultMedicines = listOf(
        CheckinOption("Med Pills", "💊", "Medicine"),
        CheckinOption("Gauze", "🩹", "Medicine"),
        CheckinOption("Syringe", "💉", "Medicine")
    )

    init {
        observeLogs()
        observeWeight()
        updateWeekDays()
        loadOptions()
    }

    private fun loadOptions() {
        val customSymptoms = repository.getCustomOptions("Symptom")
            .map { CheckinOption(it, "✨", "Symptom") }

        val customMoods = repository.getCustomOptions("Mood")
            .map { CheckinOption(it, "✨", "Mood") }

        val customMedicines = repository.getCustomOptions("Medicine")
            .map { CheckinOption(it, "✨", "Medicine") }

        _dailyState.update {
            it.copy(
                availableSymptoms = customSymptoms + defaultSymptoms,
                availableMoods = customMoods + defaultMoods,
                availableMedicines = customMedicines + defaultMedicines
            )
        }
    }

    private fun observeLogs() {
        repository.observeLogs()
            .onEach { logs ->
                _overviewState.update {
                    it.copy(
                        logs = mapLogs(logs),
                        isLoading = false
                    )
                }
            }
            .catch { e ->
                _overviewState.update {
                    it.copy(
                        errorMessage = e.message,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeWeight() {
        repository.observeUserWeight()
            .onEach { userDto ->
                if (userDto != null) {
                    val lastUpdate = userDto.weightUpdatedAt
                    val shouldAlert = if (lastUpdate != null) {
                        val diff = Date().time - lastUpdate.time
                        diff > 14L * 24 * 60 * 60 * 1000
                    } else {
                        true
                    }

                    _dailyState.update {
                        it.copy(
                            weightKg = userDto.weightKg?.toString() ?: "",
                            lastWeightUpdateDate = lastUpdate?.let { date ->
                                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
                            },
                            shouldShowWeightUpdateAlert = shouldAlert
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun mapLogs(logs: List<CheckinLogDto>): List<DashboardLogUiItem> {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val daySdf = SimpleDateFormat("MMM d", Locale.getDefault())

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        return logs.map { log ->
            val timestamp = log.timestamp ?: Date()

            val timeText = if (timestamp.after(today)) {
                sdf.format(timestamp)
            } else if (timestamp.time > today.time - 24 * 60 * 60 * 1000) {
                "Yesterday"
            } else {
                daySdf.format(timestamp)
            }

            DashboardLogUiItem(
                id = log.id,
                category = log.category ?: "Other",
                description = log.description ?: "",
                time = timeText,
                accentColor = getCategoryColor(log.category ?: "")
            )
        }
    }

    private fun getCategoryColor(category: String): Color {
        return when (category.lowercase()) {
            "back pain", "pain", "nausea", "symptom" -> LogPain
            "nutrition", "food" -> LogNutrition
            "fetal movement", "baby" -> LogBaby
            else -> LogOther
        }
    }

    private fun updateWeekDays() {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)

        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

        val days = mutableListOf<DashboardWeekDayItem>()
        val dayInitials = listOf("S", "M", "T", "W", "T", "F", "S")

        for (i in 0..6) {
            days.add(
                DashboardWeekDayItem(
                    initial = dayInitials[i],
                    date = calendar.get(Calendar.DAY_OF_MONTH),
                    isSelected = calendar.get(Calendar.DAY_OF_YEAR) == today
                )
            )

            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        _dailyState.update {
            it.copy(weekDays = days)
        }
    }

    fun onSearchQueryChange(query: String) {
        _overviewState.update {
            it.copy(searchQuery = query)
        }
    }

    fun onFilterChange(filter: String) {
        _overviewState.update {
            it.copy(selectedFilter = filter)
        }
    }

    fun onSymptomToggle(symptom: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedSymptoms.contains(symptom)) {
                state.selectedSymptoms - symptom
            } else {
                state.selectedSymptoms + symptom
            }

            state.copy(selectedSymptoms = newSet)
        }
    }

    fun onMoodToggle(mood: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedMoods.contains(mood)) {
                state.selectedMoods - mood
            } else {
                state.selectedMoods + mood
            }

            state.copy(selectedMoods = newSet)
        }
    }

    fun onMedicineToggle(medicine: String) {
        _dailyState.update { state ->
            val newSet = if (state.selectedMedicines.contains(medicine)) {
                state.selectedMedicines - medicine
            } else {
                state.selectedMedicines + medicine
            }

            state.copy(selectedMedicines = newSet)
        }
    }

    fun onNoteChange(note: String) {
        _dailyState.update {
            it.copy(note = note)
        }
    }

    fun onWeightChange(weight: String) {
        _dailyState.update {
            it.copy(weightKg = weight)
        }
    }

    fun onAddCustomOption(category: String, label: String) {
        if (label.isBlank()) return

        repository.addCustomOption(category, label)
        loadOptions()

        when (category.lowercase()) {
            "symptom" -> onSymptomToggle(label)
            "mood" -> onMoodToggle(label)
            "medicine" -> onMedicineToggle(label)
        }
    }

    fun applyVoiceInputToCheckin(
        transcribedText: String,
        detectedSymptoms: List<String>
    ) {
        val mappedSymptoms = detectedSymptoms
            .mapNotNull { mapDetectedSymptomToCheckinOption(it) }
            .distinct()

        mappedSymptoms.forEach { symptomLabel ->
            val alreadyAvailable = _dailyState.value.availableSymptoms.any { option ->
                option.label.equals(symptomLabel, ignoreCase = true)
            }

            if (!alreadyAvailable) {
                repository.addCustomOption("Symptom", symptomLabel)
            }
        }

        loadOptions()

        _dailyState.update { state ->
            val updatedNote = buildString {
                if (state.note.isNotBlank()) {
                    append(state.note.trim())
                    append("\n\n")
                }

                append("Voice input: ")
                append(transcribedText.trim())
            }

            state.copy(
                selectedSymptoms = state.selectedSymptoms + mappedSymptoms,
                note = updatedNote,
                success = false,
                errorMessage = null
            )
        }
    }

    private fun mapDetectedSymptomToCheckinOption(symptom: String): String? {
        return when (symptom.lowercase().trim()) {
            "headache" -> "Aching Head"
            "dizziness" -> "Dizziness"
            "nausea" -> "Nausea"
            "fatigue" -> "Fatigue"
            "back pain" -> "Back Pain"
            "abdominal pain" -> "Abdominal Pain"
            "chest pain" -> "Chest Pain"
            "shortness of breath" -> "Shortness of Breath"
            "fever" -> "Fever"
            "cough" -> "Cough"
            "swelling" -> "Swelling"
            else -> symptom.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
            }
        }
    }

    fun onSaveCheckin() {
        viewModelScope.launch {
            _dailyState.update {
                it.copy(isSaving = true)
            }

            try {
                val weight = _dailyState.value.weightKg.toIntOrNull()
                if (weight != null) {
                    repository.updateUserWeight(weight)
                }

                val state = _dailyState.value
                val descriptionParts = mutableListOf<String>()

                if (state.selectedSymptoms.isNotEmpty()) {
                    descriptionParts.add("Symptoms: ${state.selectedSymptoms.joinToString(", ")}")
                }

                if (state.selectedMoods.isNotEmpty()) {
                    descriptionParts.add("Mood: ${state.selectedMoods.joinToString(", ")}")
                }

                if (state.selectedMedicines.isNotEmpty()) {
                    descriptionParts.add("Medicine: ${state.selectedMedicines.joinToString(", ")}")
                }

                if (state.note.isNotBlank()) {
                    descriptionParts.add(state.note)
                }

                if (descriptionParts.isNotEmpty()) {
                    val category = when {
                        state.selectedSymptoms.isNotEmpty() -> "Symptom"
                        state.selectedMoods.isNotEmpty() -> "Mood"
                        state.selectedMedicines.isNotEmpty() -> "Medicine"
                        else -> "Other"
                    }

                    repository.saveLog(
                        CheckinLogDto(
                            id = state.editingId,
                            type = "Check-in",
                            category = category,
                            description = descriptionParts.joinToString("; "),
                            timestamp = Date()
                        )
                    )
                }

                _dailyState.update {
                    it.copy(
                        isSaving = false,
                        success = true
                    )
                }
            } catch (e: Exception) {
                _dailyState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun prepareNewCheckin() {
        _dailyState.update {
            DailyCheckinUiState(
                weekDays = it.weekDays,
                availableSymptoms = it.availableSymptoms,
                availableMoods = it.availableMoods,
                availableMedicines = it.availableMedicines,
                weightKg = it.weightKg,
                lastWeightUpdateDate = it.lastWeightUpdateDate,
                shouldShowWeightUpdateAlert = it.shouldShowWeightUpdateAlert
            )
        }
    }

    fun loadCheckinForEdit(log: DashboardLogUiItem) {
        val description = log.description
        val symptoms = parseSection(description, "Symptoms:")
        val moods = parseSection(description, "Mood:")
        val medicines = parseSection(description, "Medicine:")

        var note = description

        listOf("Symptoms:", "Mood:", "Medicine:").forEach { label ->
            val index = note.indexOf(label)
            if (index != -1) {
                val end = note.indexOf(";", index).let {
                    if (it == -1) note.length else it + 1
                }

                note = note.removeRange(index, end).trim()
            }
        }

        note = note.trimStart(';').trim()

        _dailyState.update {
            it.copy(
                editingId = log.id,
                selectedSymptoms = symptoms.toSet(),
                selectedMoods = moods.toSet(),
                selectedMedicines = medicines.toSet(),
                note = note,
                success = false,
                errorMessage = null
            )
        }
    }

    private fun parseSection(description: String, label: String): List<String> {
        val start = description.indexOf(label)
        if (start == -1) return emptyList()

        val contentStart = start + label.length
        val end = description.indexOf(";", contentStart).let {
            if (it == -1) description.length else it
        }

        return description.substring(contentStart, end)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    fun resetSuccess() {
        _dailyState.update {
            it.copy(success = false)
        }
    }
}